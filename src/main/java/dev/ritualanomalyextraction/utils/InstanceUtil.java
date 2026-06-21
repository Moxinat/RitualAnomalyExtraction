package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.RitualAnomalyPlugin;
import dev.ritualanomalyextraction.RitualGameManager;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class InstanceUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.get("RAE|InstanceUtil");

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
        if (instanceCreationName.equals("RAE-Lobby")) {
            RitualGameManager.setLobbyWorld(worldFuture);
        }
        if (instanceCreationName.equals("RAE-Minigame")) {
            RitualGameManager.setMiniGameWorld(worldFuture);
        }

        LOGGER.atInfo().log("Created %s for Ritual Anomaly Extraction!", instanceCreationName);
        return true;
    }

    public static void trySetUpLobby() {
        if (alreadySetUpLobby) {
            return;
        }
        Config<RaeConfig> config = RitualAnomalyPlugin.getInstance().getConfig();
        RaeConfig raeConfig = config.get();
        World fromWorld = Universe.get().getDefaultWorld();

        if (raeConfig.shouldCreateLobbyUponServerStart() && Universe.get().getWorld(DEFAULT_LOBBY_NAME) == null) {
            TransformComponent transformComponent = new TransformComponent(new Vector3d(0,200,0), new Rotation3f(0,180,0));
            if (generateInstance(fromWorld, DEFAULT_LOBBY_NAME, DEFAULT_LOBBY_NAME, transformComponent.getTransform())) {
                alreadySetUpLobby = true;
            } else {
                LOGGER.atSevere().log("Failed creating the lobby for Ritual Anomaly Extraction!");
            }
        } else {
            LOGGER.atInfo().log("Lobby either exists or shouldn't be created.");
            alreadySetUpLobby = true;
        }

        config.save();
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

    public static void teleportPlayerToWorld(Ref<EntityStore> playerRef, World toWorld) {

        if (!playerRef.isValid()) {
            LOGGER.atSevere().log("Failed to teleport player to world! (Cause: Invalid Ref)");
        }

        //for correct teleport history
        String worldType;
        String[] files = toWorld.getSavePath().toFile().list();
        if(files != null && Arrays.stream(files).toList().contains("instance.bson")){
            worldType = "Instance";
        }else{
            worldType = "World";
        }

            Store<EntityStore> store = playerRef.getStore();
            World worldFrom = store.getExternalData().getWorld();

            worldFrom.execute(() -> {
                Transform spawnPoint = toWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(playerRef, store);
                if (spawnPoint != null) {

                    TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());
                    HeadRotation headRotationComponent = store.getComponent(playerRef, HeadRotation.getComponentType());
                    if (toWorld.getName().equals("RAE-Minigame")) {
                        transformComponent = new TransformComponent(Constants.controlRoomPos.getPosition(), Constants.controlRoomRot);
                        headRotationComponent = new HeadRotation(Constants.controlRoomRot);
                        spawnPoint = new Transform(transformComponent.getPosition(), headRotationComponent.getRotation());
                    }

                    if (transformComponent != null && headRotationComponent != null) {
                        TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(playerRef, TeleportHistory.getComponentType());
                        teleportHistoryComponent.append(worldFrom, new Vector3d(transformComponent.getPosition()), headRotationComponent.getRotation().clone(),worldType +" '" + toWorld.getName() + "'");
                    }

                    Teleport teleportComponent = Teleport.createForPlayer(toWorld, spawnPoint);
                    store.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
                    LOGGER.atInfo().log("Teleport player to %s", toWorld.getName());
                }
            });
    }

    public static boolean shouldForceTp(){
        return RitualAnomalyPlugin.getInstance().getConfig().get().shouldForceTP();
    }
}

