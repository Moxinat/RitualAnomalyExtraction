package dev.ritualanomalyextraction;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import dev.ritualanomalyextraction.systems.RitualAnimationManager;
import dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;
import dev.ritualanomalyextraction.systems.RitualProgressTracker;
import dev.ritualanomalyextraction.ui.StatusHud;
import dev.ritualanomalyextraction.utils.InstanceUtil;
import it.unimi.dsi.fastutil.Pair;

import java.util.concurrent.*;
import java.util.logging.Level;

public class RitualGameManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static CompletableFuture<World> lobbyWorld;
    private static CompletableFuture<World> miniGameWorld;

    public enum GameState {
        IDLE,
        CONTROL_ROOM,
        RITUAL_ACTIVE,
        RITUAL_COMPLETE,
        FAIL
    }

    private GameState state = GameState.IDLE;
    private long gameStartMs = 0;
    private int gameTimeLimit;

    public void onPlayerReady() {
        if (state != GameState.IDLE) {
            return;
        }

        state = GameState.CONTROL_ROOM;
        gameStartMs = System.currentTimeMillis();
        gameTimeLimit = Constants.gameTimeLimit;

        LOGGER.at(Level.INFO).log("Players ready. Game state changed to CONTROL_ROOM.");

        if (Constants.controlRoomWorld == null) {
            LOGGER.at(Level.WARNING).log("Control room world is null");
            return;
        }

        spawnSailors();

        Constants.controlRoomWorld.execute( () -> {
            for (PlayerRef playerRef : Universe.get().getPlayers()) {
                InstanceUtil.teleportPlayerToWorld(playerRef.getReference(), Constants.controlRoomWorld);
                playerRef.sendMessage(Message.raw("Ritual Anomaly Extraction started."));
                playerRef.sendMessage(Message.raw("Find the ritual objects and place them on the pedestals."));
            }
        });

        if (Constants.ritualWorld == null) {
            LOGGER.at(Level.WARNING).log("Ritual world is null");
            return;
        }

        Constants.ritualWorld.execute(() -> {
            RitualObjectSpawnManager.spawnRandomRitualObjects(Constants.ritualWorld);
        });

        updateGameTimer();
    }

    public void onRitualCelebrationStart() {
        if (state == GameState.RITUAL_COMPLETE) {
            return;
        }

        state = GameState.RITUAL_ACTIVE;
        LOGGER.at(Level.INFO).log("Ritual started. Game state changed to RITUAL_ACTIVE.");

        // TODO: Trigger ritual Celebration effects.
        RitualAnimationManager.play();
        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            assert Constants.lobbyWorld != null;
            Constants.lobbyWorld.execute(this::onRitualCelebrationComplete);
        }, Constants.celebrationTime, TimeUnit.SECONDS);

    }

    public void onRitualCelebrationComplete() {
        if (state == GameState.RITUAL_COMPLETE) {
            return;
        }

        state = GameState.RITUAL_COMPLETE;

        LOGGER.at(Level.INFO).log("Ritual complete. Game state changed to RITUAL_COMPLETE.");

        Constants.controlRoomWorld.execute(() -> {
            for (PlayerRef playerRef : Universe.get().getPlayers()) {
                Player player = playerRef.getReference()
                        .getStore()
                        .getComponent(playerRef.getReference(), Player.getComponentType());

                if (player == null) {
                    continue;
                }

                player.getHudManager().addCustomHud(
                        playerRef,
                        new StatusHud(playerRef, "endSuccess", getGameManager())
                );
            }
        });

        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            reset();
        }, 8, TimeUnit.SECONDS);
        // TODO: Show final win message / end screen.
    }

    public void reset() {
        state = GameState.IDLE;
        gameStartMs = 0;
        gameTimeLimit = Constants.gameTimeLimit;

        LOGGER.at(Level.INFO).log("Game manager reset.");

        RitualProgressTracker.reset();
        RitualAnimationManager.reset();
        RitualObjectSpawnManager.clearSpawnedObjects(Constants.ritualWorld);
        // TODO: reset Pedestal
        Constants.controlRoomWorld.execute( () -> {
            for (PlayerRef playerRef : Universe.get().getPlayers()) {
                Player player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                assert player != null;
                player.getHudManager().resetHud(playerRef);
                player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef,"spawn", getGameManager()));
            }
        });
    }

    public void spawnSailors() {
        Constants.controlRoomWorld.execute( () -> {
            for (Transform transform : Constants.gameSailorSpawn) {
                Pair<Ref<EntityStore>, INonPlayerCharacter> monster = NPCPlugin.get().spawnNPC(Constants.controlRoomWorld.getEntityStore().getStore(), "RAE_Monster_NPC_Role", null, transform.getPosition(), Constants.ritualMonsterSpawnRot);
                LOGGER.atInfo().log("Spawned Sailor at %s", transform.getPosition());
            }
        });
    }

    public static void setLobbyWorld(CompletableFuture<World> world) {
        lobbyWorld = world;
    }

    public static CompletableFuture<World> getLobbyWorld() {
        return lobbyWorld;
    }

    public static void setMiniGameWorld(CompletableFuture<World> world) {
        miniGameWorld = world;
    }

    public static CompletableFuture<World> getMiniGameWorld() {
        return miniGameWorld;
    }

    public GameState getState() {
        return state;
    }

    public void setGameState(GameState gameState) {
        this.state = gameState;
    }

    public boolean isInControlRoom() {
        return state == GameState.CONTROL_ROOM;
    }

    public boolean isRitualActive() {
        return state == GameState.RITUAL_ACTIVE;
    }

    public boolean isRitualComplete() {
        return state == GameState.RITUAL_COMPLETE;
    }

    public long getElapsedGameMs() {
        if (gameStartMs == 0) {
            return 0;
        }

        return System.currentTimeMillis() - gameStartMs;
    }

    public int getGameTimeLimit() {
        return gameTimeLimit;
    }

    public RitualGameManager getGameManager() {
        return this;
    }

    private void updateGameTimer() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable countdownTask = new Runnable() {
            @Override
            public void run() {

                if (state != GameState.CONTROL_ROOM) {
                    scheduler.shutdown();
                    return;
                }
                if (gameTimeLimit >= 0) {
                    Constants.controlRoomWorld.execute( () -> {
                        for (PlayerRef playerRef : Constants.controlRoomWorld.getPlayerRefs()) {
                            Player player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                            assert player != null;
                            player.getHudManager().resetHud(playerRef);
                            player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef,"gameTimer", getGameManager()));
                        }
                    });
                    if(gameTimeLimit == 60){
                        int index = SoundEvent.getAssetMap().getIndex("RAE_Time");
                        SoundUtil.playSoundEvent2d( index, SoundCategory.SFX,Constants.controlRoomWorld.getEntityStore().getStore());
                    }

                    gameTimeLimit--;
                } else {
                    Constants.controlRoomWorld.execute( () -> {
                        for (PlayerRef playerRef : Constants.controlRoomWorld.getPlayerRefs()) {
                            Player player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                            assert player != null;
                            player.getHudManager().resetHud(playerRef);
                            player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef,"endFail", getGameManager()));
                        }
                    });
                    scheduler.shutdown();
                    state = GameState.FAIL;
                }
            }
        }; scheduler.scheduleAtFixedRate(countdownTask, 0, 1, TimeUnit.SECONDS);
    }
}