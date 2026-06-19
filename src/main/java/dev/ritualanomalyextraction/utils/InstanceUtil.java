package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.ritualanomalyextraction.RitualAnomalyPlugin;
import org.joml.Vector3d;


public class InstanceUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.get("RAE|InstanceUtil");

    public static final String DEFAULT_LOBBY_NAME = "RAE-Lobby";
    public static final String DEFAULT_MINIGAME_NAME = "RAE-Minigame";
    private static boolean alreadySetUpLobby = false;

    public static boolean generateInstance(World fromWorld,String instanceName, Transform fromWorldSpawnPoint, Ref<EntityStore> playerRef){
        LOGGER.atInfo().log("Trying to generate " + instanceName);
        if(fromWorld == null){
            LOGGER.atSevere().log("Source World is null!");
            return false;
        }
        java.util.concurrent.CompletableFuture<World> completableFuture= InstancesPlugin.get().spawnInstance(instanceName, instanceName,fromWorld,fromWorldSpawnPoint);

        completableFuture.thenRun(()-> {
            try {
                teleportPlayerToWorld(playerRef,completableFuture.get());
            } catch (Exception e) {
                LOGGER.atSevere().log("Failed to teleport player to %s! (Cause: Exception)",instanceName);
            }
        });
        LOGGER.atInfo().log("Created %s for Ritual Anomaly Extraction!",instanceName);
        return true;
    }

    //currently only one Lobby World can be present at the same time
    //teleports the player to the Lobby World if enabled!
    public static void trySetUpLobby(World fromWorld, Ref<EntityStore> playerRef){
        if(alreadySetUpLobby) return;
        Config<RaeConfig> config = RitualAnomalyPlugin.getInstance().getConfig();
        RaeConfig raeConfig = config.get();
        String currentLobbyName = raeConfig.getLobbyWorldName();
        //Should it create the lobby, if it doesn't already exist
        if(raeConfig.shouldCreateLobbyUponServerStart() && Universe.get().getWorld(currentLobbyName) == null){
            //TransformComponent transformComponent = playerRef.getStore().getComponent(playerRef,TransformComponent.getComponentType()); doesn't seam to work like I want, thus creating a placeholder
            TransformComponent transformComponent = new TransformComponent(
                    new Vector3d(0,200,0),
                    new Rotation3f(0,180,0)
            );
            if(transformComponent != null && generateInstance(fromWorld, currentLobbyName,transformComponent.getTransform(),playerRef)){
                if(raeConfig.shouldSetLobbyAsDefaultWorld()) HytaleServer.get().getConfig().getDefaults().setWorld(currentLobbyName);
                alreadySetUpLobby = true;
            }else{
                LOGGER.atSevere().log("Failed creating the lobby for Ritual Anomaly Extraction!");
            }
        }else{
            LOGGER.atInfo().log("Lobby either exists or shouldn't be created.");
            alreadySetUpLobby = raeConfig.shouldCreateLobbyUponServerStart();
        }

        config.save();
    }

    //currently only one Minigame World can be present at the same time
    //does not teleport the player to the Minigame world!
    public static void tryGenerateMinigameWorld(World fromWorld, Ref<EntityStore> playerRef){
        if(Universe.get().getWorld(DEFAULT_MINIGAME_NAME) == null){
            //TransformComponent transformComponent = playerRef.getStore().getComponent(playerRef,TransformComponent.getComponentType()); doesn't seam to work like I want, thus creating a placeholder
            TransformComponent transformComponent = new TransformComponent(
                    new Vector3d(0,200,0),
                    new Rotation3f(0,180,0)
            );
            if(transformComponent == null) return;
            if (!generateInstance(fromWorld, DEFAULT_MINIGAME_NAME, transformComponent.getTransform(), playerRef)) {
                LOGGER.atSevere().log("Failed creating the Minigame for Ritual Anomaly Extraction!");
            }else{
                LOGGER.atInfo().log("Created the Minigame for Ritual Anomaly Extraction!");
            }
        }else{
            LOGGER.atInfo().log("Minigame world already exists!");
        }
    }

    public static void teleportPlayerToWorld(Ref<EntityStore> playerRef, World toWorld){
        if(playerRef.isValid()){

            Store<EntityStore> store = playerRef.getStore();
            World worldFrom = store.getExternalData().getWorld();
            worldFrom.execute(() -> {
                Transform spawnPoint = toWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(playerRef, store);
                if (spawnPoint != null) {
                    TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());
                    HeadRotation headRotationComponent = store.getComponent(playerRef, HeadRotation.getComponentType());
                    if (transformComponent != null && headRotationComponent != null) {
                        TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(playerRef, TeleportHistory.getComponentType());
                        teleportHistoryComponent.append(
                                worldFrom,
                                new Vector3d(transformComponent.getPosition()),
                                headRotationComponent.getRotation().clone(),
                                "Instance '" + toWorld.getName() + "'");
                    }

                    Teleport teleportComponent = Teleport.createForPlayer(toWorld, spawnPoint);
                    store.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
                    LOGGER.atSevere().log("Teleport player to world!");
                }
            });
        }else{
            LOGGER.atSevere().log("Failed to teleport player to world! (Cause: Invalid Ref)");
        }
    }
}
