package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.RitualAnomalyPlugin;
import dev.ritualanomalyextraction.RitualGameManager;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class WorldUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.get("RAE|WorldUtil");

    public static final String DEFAULT_LOBBY_NAME = "RAE-Lobby";
    public static final String DEFAULT_MINIGAME_NAME = "RAE-Minigame";
    private static boolean alreadySetUpLobby = false;

    public static boolean generateInstance(World fromWorld, String instanceCreationName, String instanceFolderName, Transform returnPoint) {
        LOGGER.atInfo().log("Trying to generate " + instanceCreationName);
        if (fromWorld == null) {
            LOGGER.atSevere().log("Source World is null!");
            return false;
        }

        CompletableFuture<World> worldFuture =  InstancesPlugin.get().spawnInstance(instanceFolderName, instanceCreationName, fromWorld, returnPoint);
        if (instanceCreationName.equals(DEFAULT_LOBBY_NAME)) {
            RitualGameManager.setLobbyWorldFuture(worldFuture);
        }
        if (instanceCreationName.equals(DEFAULT_MINIGAME_NAME)) {
            RitualGameManager.setMiniGameWorld(worldFuture);
        }

        LOGGER.atInfo().log("Created %s for Ritual Anomaly Extraction!", instanceCreationName);
        return true;
    }

    public static void trySetUpLobby() {
        if (alreadySetUpLobby) {
            return;
        }
        RaeConfig.RaeConfigManager raeConfig = RitualAnomalyPlugin.getInstance().getConfig();
        World fromWorld = Universe.get().getDefaultWorld();

        if (raeConfig.shouldCreateLobbyUponServerStart() && Universe.get().getWorld(DEFAULT_LOBBY_NAME) == null) {
            TransformComponent transformComponent = new TransformComponent(new Vector3d(0,80,0), new Rotation3f(0,180,0));
            if (generateInstance(fromWorld, DEFAULT_LOBBY_NAME, DEFAULT_LOBBY_NAME, transformComponent.getTransform())) {
                alreadySetUpLobby = true;
            } else {
                LOGGER.atSevere().log("Failed creating the lobby for Ritual Anomaly Extraction!");
            }
        } else {
            LOGGER.atInfo().log("Lobby either exists or shouldn't be created.");
            alreadySetUpLobby = true;
        }
    }

    public static void tryGenerateMinigameWorld() {
        if (Universe.get().getWorld(DEFAULT_MINIGAME_NAME) == null) {
            World fromWorld = Universe.get().getDefaultWorld();
            TransformComponent transformComponent = new TransformComponent(new Vector3d(0,200,0), new Rotation3f(0,180,0));
            if (!generateInstance(fromWorld,DEFAULT_MINIGAME_NAME, DEFAULT_MINIGAME_NAME, transformComponent.getTransform())) {
                LOGGER.atSevere().log("Failed creating the Minigame for Ritual Anomaly Extraction!");
            } else {
                LOGGER.atInfo().log("Created the Minigame for Ritual Anomaly Extraction!");
            }
        } else {
            LOGGER.atInfo().log("Minigame world already exists!");
        }
    }

    private static void addTeleportComponent(@Nonnull World worldFrom,@Nonnull  World worldTo,@Nonnull Store<EntityStore> store,@Nonnull  Ref<EntityStore> playerRef,@Nonnull String worldType){
        Transform spawnPoint = worldTo.getWorldConfig().getSpawnProvider().getSpawnPoint(playerRef, store);
        if (spawnPoint != null) {

            TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());
            HeadRotation headRotationComponent = store.getComponent(playerRef, HeadRotation.getComponentType());

            if (transformComponent != null && headRotationComponent != null) {
                TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(playerRef, TeleportHistory.getComponentType());
                teleportHistoryComponent.append(worldFrom, new Vector3d(transformComponent.getPosition()), headRotationComponent.getRotation().clone(),worldType +" '" + worldTo.getName() + "'");
            }

            Teleport teleportComponent = Teleport.createForPlayer(worldTo, spawnPoint);
            store.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
            LOGGER.atInfo().log("Teleport player to %s", worldTo.getName());
        }
    }

    private static String resolveWorldType(@Nonnull World world){
        String[] files = world.getSavePath().toFile().list();
        if(files != null && Arrays.stream(files).toList().contains("instance.bson")){
            return "Instance";
        }else{
            return "World";
        }
    }
    public static void teleportPlayerToWorld(@Nonnull World worldTo,@Nonnull  Ref<EntityStore> playerRef) {

        if (!playerRef.isValid()) {
            LOGGER.atSevere().log("Failed to teleport player to world! (Cause: Invalid Ref)");
            return;
        }

        Store<EntityStore> store = playerRef.getStore();
        World worldFrom = store.getExternalData().getWorld();

        worldFrom.execute(() -> {
                addTeleportComponent(worldFrom,worldTo,store,playerRef,resolveWorldType(worldTo));
            });
    }

    //use this if the given players are in different world
    @SafeVarargs
    public static void teleportPlayersToWorld(@Nonnull World worldTo, @Nonnull Ref<EntityStore>... playerRefs) {
        for(Ref<EntityStore> ref : playerRefs){
            if (!ref.isValid()) {
                LOGGER.atSevere().log("Failed to teleport player to world! (Cause: Invalid Ref)");
                continue;
            }
            Store<EntityStore> store = ref.getStore();
            World worldFrom = store.getExternalData().getWorld();

            worldFrom.execute(() -> {
                addTeleportComponent(worldFrom, worldTo,store,ref,resolveWorldType(worldTo));
            });
        }
    }

    //use this, if all given players are in the same world
    public static CompletableFuture<Void> teleportPlayersFromSameWorldToWorld(@Nonnull World worldFrom, @Nonnull World worldTo,@Nullable TriConsumer<PlayerRef,Ref<EntityStore>,Store<EntityStore>> additionalTask) {
        return CompletableFuture.runAsync(()->{
            worldFrom.execute(() -> {
                Store<EntityStore> store = worldFrom.getEntityStore().getStore();
                for(PlayerRef playerRef : worldFrom.getPlayerRefs()){

                    Ref<EntityStore> ref = playerRef.getReference();
                    if (ref == null || !ref.isValid()) {
                        LOGGER.atSevere().log("Failed to teleport player to world! (Cause: Invalid Ref)");
                        continue;
                    }
                    if(additionalTask != null){
                        additionalTask.accept(playerRef,ref,store);
                    }


                    addTeleportComponent(worldFrom,worldTo,store,ref,resolveWorldType(worldTo));
                }
            });
        });
    }


    public static CompletableFuture<Void> teleportPlayersFromMinigameToLobby(TriConsumer<PlayerRef,Ref<EntityStore>,Store<EntityStore>> additionalTask){
        return teleportPlayersFromSameWorldToWorld(Universe.get().getWorld(DEFAULT_MINIGAME_NAME),Universe.get().getWorld(DEFAULT_LOBBY_NAME),additionalTask);
    }

    public static CompletableFuture<Void> teleportPlayersFromLobbyToMinigame(){
        return teleportPlayersFromSameWorldToWorld(Universe.get().getWorld(DEFAULT_LOBBY_NAME),Universe.get().getWorld(DEFAULT_MINIGAME_NAME),null);
    }

    public static boolean shouldForceTp(){
        return RitualAnomalyPlugin.getInstance().getConfig().shouldForceTP();
    }
}

