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
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import dev.ritualanomalyextraction.asset.GameConfig;
import dev.ritualanomalyextraction.components.RitualPedestalComponent;
import dev.ritualanomalyextraction.systems.NewRitualProgressTracker;
import dev.ritualanomalyextraction.systems.RitualAnimationManager;
import dev.ritualanomalyextraction.systems.RitualObjectPickupSystem;
import dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;
import dev.ritualanomalyextraction.ui.DeprecatedStatusHud;
import dev.ritualanomalyextraction.utils.WorldUtil;
import it.unimi.dsi.fastutil.Pair;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RitualGame {
    public final static HytaleLogger MINIGAME_LOGGER = HytaleLogger.get("RAE|Minigame");


    public HytaleLogger LOGGER;
    private final int ID;
    private final GameConfig CONFIG;
    private ScheduledFuture<?> TICKING;
    private RitualGameManager.GameState state;
    private int timeRemaining;
    private final int warnTime;

    private World gameWorld;

    private final Map<BlockPosition, RitualPedestalComponent> pedestalStates = new HashMap<>(4);
    private final NewRitualProgressTracker ritualProgressTracker;

    private final List<Pair<BlockType, Vector3i>> formerRitualBlocks = new ArrayList<>();
    private final List<Pair<BlockType, Vector3i>> ritualItems = new ArrayList<>();

    public RitualGame(){
        this(new GameConfig(GameConfig.getAssetMap().getAsset("Default")));
    }

    //TODO make the config a copy so id doesn't get accidentally deleted
    public RitualGame(@NonNullDecl GameConfig config){
        state = RitualGameManager.GameState.BOOTING;
        CONFIG = new GameConfig(config);
        ID = RitualGameManager.takeNextID();
        ritualProgressTracker = new NewRitualProgressTracker(this);
        LOGGER = MINIGAME_LOGGER.getSubLogger(String.valueOf(ID));
        timeRemaining = CONFIG.getGameTime();
        warnTime = CONFIG.getWarningTime();
        Objects.requireNonNull(WorldUtil.tryGenerateMinigameWorld(CONFIG, CONFIG.getMiniGameInstanceName() + ID)).thenAccept((world -> {
            gameWorld = world;
            RitualGameManager.registerGame(this);
            spawnSailors();

            gameWorld.execute(()-> RitualObjectSpawnManager.spawnRandomRitualObjects(gameWorld,this));

            //I don't know why, but it needs some delay else I'm crashing with division by 0 (client only)
            HytaleServer.SCHEDULED_EXECUTOR.schedule(this::start,1,TimeUnit.SECONDS);
        }));

    }

    private void start(){
        //TODO later only take players that are part of the "party", not sure how to do it without noesis yet
        WorldUtil.teleportPlayersFromSameWorldToWorld(RitualGameManager.getLobby(),gameWorld,((playerRef, ref, store) -> {
            Player player = store.getComponent(ref, Player.getComponentType());
            assert player != null;
            player.getHudManager().resetHud(playerRef);
            playerRef.sendMessage(Constants.MESSAGE_START1);
            playerRef.sendMessage(Constants.MESSAGE_START2);
        })).thenRun(()->{
            startTimeTicking();
            state = RitualGameManager.GameState.RUNNING;
        });
    }


    public void end(){
        LOGGER.atInfo().log("Game ended.");
        TICKING.cancel(false);
        state = RitualGameManager.GameState.DELETING;
        clearPedestals();

        WorldUtil.teleportPlayersFromSameWorldToWorld(gameWorld,RitualGameManager.getLobby(),(playerRef, ref, store)->{
            Player player = store.getComponent(ref, Player.getComponentType());
            assert player != null;
            // since the game isn't limited to one we don't need to wait for the old one to reset
            RitualObjectPickupSystem.clearCarriedRitualObject(playerRef);
            player.getHudManager().resetHud(playerRef);
            player.getHudManager().addCustomHud(playerRef, new DeprecatedStatusHud(playerRef, Constants.UI_IDLE, 0));
        }).thenRun(() -> {
            InstancesPlugin.safeRemoveInstance(gameWorld);
            RitualGameManager.unregisterGame(this);
        });
    }

    public void startRitual(){
        state = RitualGameManager.GameState.RITUAL;
        LOGGER.at(Level.INFO).log("Ritual started. Game state changed to RITUAL.");
        TICKING.cancel(false);

        RitualAnimationManager.play(gameWorld);

        //TODO get time from asset
        HytaleServer.SCHEDULED_EXECUTOR.schedule(this::onRitualComplete, Constants.celebrationTime, TimeUnit.SECONDS);
    }

    public void onRitualComplete(){
        state = RitualGameManager.GameState.ENDED;

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

    public void addFormerRitualBlock(Integer formerBlockID, Vector3i position){
        formerRitualBlocks.add(Pair.of(BlockType.getAssetMap().getAsset(formerBlockID),position));
    }

    public List<Pair<BlockType, Vector3i>> consumeFormerRitualBlocks() {
        var save = new ArrayList<>(formerRitualBlocks);
        formerRitualBlocks.clear();
        return save;
    }

    public void addRitualItemPosition(BlockType blockID,Vector3i position){
        ritualItems.add(Pair.of(blockID,position));
    }
    public List<Pair<BlockType, Vector3i>> consumeRitualItemPositions() {
        var save = new ArrayList<>(ritualItems);
        ritualItems.clear();
        return save;
    }
    public List<Pair<BlockType, Vector3i>> getRitualItemPositions() {
        return ritualItems;
    }

    private void clearPedestals(){
        if(gameWorld.isAlive()){
            gameWorld.execute(()->{
                pedestalStates.values().forEach(pedestal ->{
                    Ref<EntityStore> visualEntity = pedestal.getVisualItemEntity();

                    if (visualEntity != null && visualEntity.isValid()) {
                        visualEntity.getStore().removeEntity(visualEntity, RemoveReason.REMOVE);
                    }
                });
                pedestalStates.clear();
            });
        }else{
            LOGGER.atInfo().log("World is already in shutdown!");
            pedestalStates.clear();
        }

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
            playEndSoundEvent();
            state = RitualGameManager.GameState.ENDED;
            HytaleServer.SCHEDULED_EXECUTOR.schedule(this::end, CONFIG.getEndWaitingTime(), TimeUnit.SECONDS);
        }
    }



    private void playWarningSoundEvent(){
        int index = SoundEvent.getAssetMap().getIndex(CONFIG.getWarningSoundEvent());
        SoundUtil.playSoundEvent2d( index, SoundCategory.SFX, gameWorld.getEntityStore().getStore());
        LOGGER.atInfo().log("Played warning sfx!");
    }


    private void playEndSoundEvent(){
        int index = SoundEvent.getAssetMap().getIndex(CONFIG.getGameOverSoundEvent());
        SoundUtil.playSoundEvent2d( index, SoundCategory.SFX, gameWorld.getEntityStore().getStore());
        LOGGER.atInfo().log("Played warning sfx!");
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
