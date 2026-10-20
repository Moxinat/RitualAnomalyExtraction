package dev.ritualanomalyextraction;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.BlockPosition;
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
import dev.ritualanomalyextraction.components.RitualPedestalComponent;
import dev.ritualanomalyextraction.systems.*;
import dev.ritualanomalyextraction.ui.DeprecatedStatusHud;
import dev.ritualanomalyextraction.ui.StatusHud;
import dev.ritualanomalyextraction.utils.WorldUtil;
import it.unimi.dsi.fastutil.Pair;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WipRitualGame {
    public final static HytaleLogger LOGGER = RitualAnomalyPlugin.getInstance().getLogger().getSubLogger("Minigame");


    public final HytaleLogger GAME_LOGGER;
    private final int ID;
    private final GameConfig CONFIG;
    private ScheduledFuture<?> TICKING;
    private RitualGameManager.GameState state;
    private int timeRemaining;
    private final int warnTime;

    private World gameWorld;

    private final Map<BlockPosition, RitualPedestalComponent> pedestalStates = new HashMap<>(4);
    private final NewRitualProgressTracker ritualProgressTracker;

    //TODO make the config a copy so id doesn't get accidentally deleted
    public WipRitualGame(@NonNullDecl GameConfig config){
        state = RitualGameManager.GameState.CONTROL_ROOM;
        CONFIG = config;
        ID = WipRitualGameManager.takeNextID();
        ritualProgressTracker = new NewRitualProgressTracker(this);
        GAME_LOGGER = LOGGER.getSubLogger(String.valueOf(ID));
        timeRemaining = config.getGameTime();
        warnTime = config.getWarningTime();
        Objects.requireNonNull(WorldUtil.tryGenerateMinigameWorld(config, config.getMiniGameInstanceName() + ID)).thenAccept((world -> {
            gameWorld = world;
            WipRitualGameManager.registerGame(this);
            spawnSailors();
            //TODO update manager for different games
            RitualObjectSpawnManager.spawnRandomRitualObjects(gameWorld,config);

            WorldUtil.teleportPlayersFromLobbyToMinigame().thenRun(()->{
                //I think it would be better to have an ui for these messages, instead if sending it into chat. Maybe with icons what ritual items are left to search for
                gameWorld.sendMessage(Message.raw("Ritual Anomaly Extraction started."));
                gameWorld.sendMessage(Message.raw("Find the ritual objects and place them on the pedestals."));
                startTimeTicking();
                state = RitualGameManager.GameState.RITUAL_ACTIVE;
            });
        }));

    }


    public void end(){
        TICKING.cancel(true);
        clearPedestals();
        //TODO get the lobby world from manager
        WorldUtil.teleportPlayersFromSameWorldToWorld(gameWorld,Universe.get().getWorld(WorldUtil.DEFAULT_LOBBY_NAME),(playerRef,ref, store)->{
            Player player = store.getComponent(ref, Player.getComponentType());
            assert player != null;
            // since the game isn't limited to one we don't need to wait for the old one to reset
            player.getHudManager().addCustomHud(playerRef, new DeprecatedStatusHud(playerRef, Constants.UI_IDLE, 0));
        }).thenRun(() -> {
            InstancesPlugin.safeRemoveInstance(gameWorld);
            WipRitualGameManager.unregisterGame(this);
        });
    }

    public void startRitual(){
        state = RitualGameManager.GameState.RITUAL_ACTIVE;
        LOGGER.at(Level.INFO).log("Ritual started. Game state changed to RITUAL_ACTIVE.");

        RitualAnimationManager.play(gameWorld);

        //TODO get time from asset
        HytaleServer.SCHEDULED_EXECUTOR.schedule(this::onRitualComplete, Constants.celebrationTime, TimeUnit.SECONDS);
    }

    public void onRitualComplete(){
        state = RitualGameManager.GameState.RITUAL_COMPLETE;

        LOGGER.at(Level.INFO).log("Ritual complete. Game state changed to RITUAL_COMPLETE.");

        var store =gameWorld.getEntityStore().getStore();
        gameWorld.execute(() -> {
            for (PlayerRef playerRef : gameWorld.getPlayerRefs()) {
                Player player = store.getComponent(playerRef.getReference(), Player.getComponentType());

                if (player == null) {
                    continue;
                }

                player.getHudManager().addCustomHud(
                        playerRef,
                        new DeprecatedStatusHud(playerRef, Constants.UI_END_SUCCESS, 0)
                );
            }
        });

        HytaleServer.SCHEDULED_EXECUTOR.schedule(this::end, 5, TimeUnit.SECONDS);
    }

    private void clearPedestals(){
        gameWorld.execute(()->{
            pedestalStates.values().forEach(pedestal ->{
                Ref<EntityStore> visualEntity = pedestal.getVisualItemEntity();

                if (visualEntity != null && visualEntity.isValid()) {
                    visualEntity.getStore().removeEntity(visualEntity, RemoveReason.REMOVE);
                }
            });
            pedestalStates.clear();
        });
    }

    private void spawnSailors() {
        gameWorld.execute( () -> {
            for (Transform transform : CONFIG.getSailorSpawnLocations()) {
                Pair<Ref<EntityStore>, INonPlayerCharacter> monster = NPCPlugin.get().spawnNPC(gameWorld.getEntityStore().getStore(), "RAE_Monster_NPC_Role", null, transform.getPosition(), Constants.ritualMonsterSpawnRot);
                LOGGER.atInfo().log("Spawned Sailor at %s", transform.getPosition());
            }
        });
    }

    public void startTimeTicking(){
        TICKING = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::tickTime, 0, 1, TimeUnit.SECONDS);
    }

    private void tickTime(){
        if (timeRemaining >= 1) {
            updateUI(Constants.UI_TIMER);
            if (timeRemaining == warnTime) {
                playWarningSoundEvent();
            }
            timeRemaining--;
        } else {
            updateUI(Constants.UI_END_FAIL);
            state = RitualGameManager.GameState.FAIL;
            HytaleServer.SCHEDULED_EXECUTOR.schedule(this::end, 2, TimeUnit.SECONDS);
        }
    }



    private void playWarningSoundEvent(){
        int index = SoundEvent.getAssetMap().getIndex(CONFIG.getWarningSoundEvent());
        SoundUtil.playSoundEvent2d( index, SoundCategory.SFX, gameWorld.getEntityStore().getStore());
    }

    private void updateUI(String id){
        final Store<EntityStore> worldStore = gameWorld.getEntityStore().getStore();
        gameWorld.execute( () -> {
            for (PlayerRef playerRef : gameWorld.getPlayerRefs()) {
                Player player = worldStore.getComponent(playerRef.getReference(), Player.getComponentType());
                assert player != null;
                player.getHudManager().addCustomHud(playerRef, new DeprecatedStatusHud(playerRef, id, timeRemaining));
            }
        });
    }

    public int getID(){
        return ID;
    }

    public World getGameWorld(){
        return gameWorld;
    }

    public Map<BlockPosition, RitualPedestalComponent> getPedestalStatesMap() {
        return pedestalStates;
    }

    public GameConfig getConfig() {
        return CONFIG;
    }

    public NewRitualProgressTracker getRitualProgressTracker() {
        return ritualProgressTracker;
    }
}
