package dev.ritualanomalyextraction;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
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
import dev.ritualanomalyextraction.asset.GameConfig;
import dev.ritualanomalyextraction.systems.PedestalSystem;
import dev.ritualanomalyextraction.systems.RitualAnimationManager;
import dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;
import dev.ritualanomalyextraction.systems.RitualProgressTracker;
import dev.ritualanomalyextraction.ui.StatusHud;
import dev.ritualanomalyextraction.utils.WorldUtil;
import it.unimi.dsi.fastutil.Pair;

import java.util.concurrent.*;
import java.util.logging.Level;

public class RitualGameManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static CompletableFuture<World> lobbyWorldFuture;
    private static CompletableFuture<World> miniGameWorldFuture;
    private World miniGameWorld;
    private World lobbyWorld;

    private GameConfig gameConfig;
    private ScheduledFuture<?> TICKING;

    private static RitualGameManager INSTANCE;

    public RitualGameManager(){
        INSTANCE = this;
    }

    public enum GameState {
        IDLE,
        CONTROL_ROOM,
        RITUAL_ACTIVE,
        RITUAL_COMPLETE,
        FAIL,
        RESETTING
    }

    private GameState state = GameState.IDLE;
    private long gameStartMs = 0;
    private int gameTimeLimit;

    public void onPlayerReady(){
        onPlayerReady(GameConfig.getAssetMap().getAsset("Default"));
    }
    public void onPlayerReady(GameConfig gameConfig) {
        if (state != GameState.IDLE) {
            return;
        }

        this.gameConfig = gameConfig;
        if(this.gameConfig == null) return;


        state = GameState.CONTROL_ROOM;
        gameStartMs = System.currentTimeMillis();
        gameTimeLimit = gameConfig.getGameTime();

        LOGGER.at(Level.INFO).log("Players ready. Game state changed to CONTROL_ROOM.");

        miniGameWorld = Universe.get().getWorld(Constants.miniGameWorldName);

        if (miniGameWorld == null) {
            LOGGER.at(Level.WARNING).log("Ritual world is null");
            return;
        }
        spawnSailors();

        //I think it would be better to have an ui for these messages, instead if sending it into chat. Maybe with icons what ritual items are left to search for
        WorldUtil.teleportPlayersFromLobbyToMinigame().thenRun(()->{
            miniGameWorld.sendMessage(Message.raw("Ritual Anomaly Extraction started."));
            miniGameWorld.sendMessage(Message.raw("Find the ritual objects and place them on the pedestals."));
        });

        miniGameWorld.execute(() -> RitualObjectSpawnManager.spawnRandomRitualObjects(miniGameWorld,gameConfig));

        updateGameTimer();
    }

    public void onRitualCelebrationStart() {
        if (state == GameState.RITUAL_COMPLETE) {
            return;
        }

        state = GameState.RITUAL_ACTIVE;
        LOGGER.at(Level.INFO).log("Ritual started. Game state changed to RITUAL_ACTIVE.");

        RitualAnimationManager.play();

        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            assert miniGameWorld != null;
            onRitualCelebrationComplete();
        }, Constants.celebrationTime, TimeUnit.SECONDS);
    }

    public void onRitualCelebrationComplete() {
        if (state == GameState.RITUAL_COMPLETE) {
            return;
        }

        state = GameState.RITUAL_COMPLETE;

        LOGGER.at(Level.INFO).log("Ritual complete. Game state changed to RITUAL_COMPLETE.");

        miniGameWorld.execute(() -> {
            for (PlayerRef playerRef : miniGameWorld.getPlayerRefs()) {
                Player player = playerRef.getReference()
                        .getStore()
                        .getComponent(playerRef.getReference(), Player.getComponentType());

                if (player == null) {
                    continue;
                }

                player.getHudManager().addCustomHud(
                        playerRef,
                        new StatusHud(playerRef, Constants.UI_END_SUCCESS, getGameManager())
                );
            }
        });

        HytaleServer.SCHEDULED_EXECUTOR.schedule(this::reset, 5, TimeUnit.SECONDS);
    }

    public void reset() {
        state = GameState.RESETTING;
        gameStartMs = 0;
        gameTimeLimit = 60;
        gameConfig = null;
        if(TICKING != null)TICKING.cancel(true);

        LOGGER.at(Level.INFO).log("Game manager reset.");

        RitualProgressTracker.reset();
        RitualAnimationManager.reset();
        RitualObjectSpawnManager.clearSpawnedObjects(Universe.get().getWorld(WorldUtil.DEFAULT_MINIGAME_NAME));
        PedestalSystem.reset();

        WorldUtil.teleportPlayersFromMinigameToLobby((playerRef,ref, store)->{
                    Player player = store.getComponent(ref, Player.getComponentType());
                    assert player != null;
                    player.getHudManager().resetHud(playerRef);
                    player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef, Constants.UI_RESET, getGameManager()));
        });


        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> InstancesPlugin.safeRemoveInstance(Universe.get().getWorld(Constants.miniGameWorldName)), 5, TimeUnit.SECONDS);

        HytaleServer.SCHEDULED_EXECUTOR.schedule(WorldUtil::tryGenerateMinigameWorld, 10, TimeUnit.SECONDS);

        HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
            Universe.get().getWorld(WorldUtil.DEFAULT_LOBBY_NAME).execute( () -> {
                for (PlayerRef playerRef : Universe.get().getWorld(WorldUtil.DEFAULT_LOBBY_NAME).getPlayerRefs()) {
                    Player player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                    player.getHudManager().resetHud(playerRef);
                    player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef,Constants.UI_READY, getGameManager()));
                }
                setGameState(GameState.IDLE);
            });
        }, 13, TimeUnit.SECONDS);
    }

    public void spawnSailors() {
        Universe.get().getWorld(Constants.miniGameWorldName).execute( () -> {
            for (Transform transform : gameConfig.getSailorSpawnLocations()) {
                Pair<Ref<EntityStore>, INonPlayerCharacter> monster = NPCPlugin.get().spawnNPC(Universe.get().getWorld(Constants.miniGameWorldName).getEntityStore().getStore(), "RAE_Monster_NPC_Role", null, transform.getPosition(), Constants.ritualMonsterSpawnRot);
                LOGGER.atInfo().log("Spawned Sailor at %s", transform.getPosition());
            }
        });
    }

    public static void setLobbyWorldFuture(CompletableFuture<World> world) {
        lobbyWorldFuture = world;
    }

    public static CompletableFuture<World> getLobbyWorldFuture() {
        return lobbyWorldFuture;
    }

    public static void setMiniGameWorld(CompletableFuture<World> world) {
        miniGameWorldFuture = world;
    }

    public static CompletableFuture<World> getMiniGameWorld() {
        return miniGameWorldFuture;
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

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public RitualGameManager getGameManager() {
        return this;
    }

    public static RitualGameManager getInstance(){
        return INSTANCE;
    }

    private void updateGameTimer() {
        TICKING = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
                ()->{
                    if (state != GameState.CONTROL_ROOM) {
                        return;
                    }
                    if (gameTimeLimit >= 1) {
                        Universe.get().getWorld(Constants.miniGameWorldName).execute( () -> {
                            for (PlayerRef playerRef : Universe.get().getWorld(Constants.miniGameWorldName).getPlayerRefs()) {
                                Player player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                                assert player != null;
                                player.getHudManager().resetHud(playerRef);
                                player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef, Constants.UI_TIMER, getGameManager()));
                            }
                        });
                        if (gameTimeLimit == 60) {
                            int index = SoundEvent.getAssetMap().getIndex("RAE_Time");
                            SoundUtil.playSoundEvent2d( index, SoundCategory.SFX, Universe.get().getWorld(Constants.miniGameWorldName).getEntityStore().getStore());
                        }

                        gameTimeLimit--;
                    } else {
                        Universe.get().getWorld(Constants.miniGameWorldName).execute( () -> {
                            for (PlayerRef playerRef : Universe.get().getWorld(Constants.miniGameWorldName).getPlayerRefs()) {
                                Player player = playerRef.getReference().getStore().getComponent(playerRef.getReference(), Player.getComponentType());
                                assert player != null;
                                player.getHudManager().resetHud(playerRef);
                                player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef,Constants.UI_END_FAIL, getGameManager()));
                            }
                        });
                        state = GameState.FAIL;
                        HytaleServer.SCHEDULED_EXECUTOR.schedule(this::reset, 2, TimeUnit.SECONDS);
                    }
                }
                , 0, 1, TimeUnit.SECONDS
        );
    }
}