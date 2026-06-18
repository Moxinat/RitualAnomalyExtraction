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
    private static String currentLobbyName;
    private static boolean alreadySetUpLobby = false;

    public static boolean generateLobby(World fromWorld, Transform fromWorldSpawnPoint,Ref<EntityStore> playerRef){
        LOGGER.atInfo().log("Trying to generate Lobby");
        if(fromWorld == null){
            LOGGER.atSevere().log("Source World is null!");
            return false;
        }
        java.util.concurrent.CompletableFuture<World> completableFuture= InstancesPlugin.get().spawnInstance(currentLobbyName, currentLobbyName,fromWorld,fromWorldSpawnPoint);

        completableFuture.thenRun(()-> {
            try {
                teleportPlayerToLobby(playerRef,completableFuture.get());
            } catch (Exception e) {
                LOGGER.atSevere().log("Failed to teleport player to lobby! (Cause: Exception)");
            }
        });
        LOGGER.atInfo().log("Created the lobby for Ritual Anomaly Extraction");
        return true;
    }

    public static void trySetUpLobby(World fromWorld, Ref<EntityStore> playerRef, Store<EntityStore> store){
        if(alreadySetUpLobby) return;
        Config<RaeConfig> config = RitualAnomalyPlugin.getInstance().getConfig();
        RaeConfig raeConfig = config.get();
        currentLobbyName = raeConfig.getLobbyWorldName();
        //Should it create the lobby, if it doesn't already exist
        if(raeConfig.shouldCreateLobbyUponServerStart() && Universe.get().getWorld(currentLobbyName) == null){
            //TransformComponent transformComponent = store.getComponent(playerRef,TransformComponent.getComponentType()); doesn't seam to work like I want, thus creating a placeholder
            TransformComponent transformComponent = new TransformComponent(
                    new Vector3d(0,200,0),
                    new Rotation3f(0,180,0)
            );
            if(transformComponent != null && generateLobby(fromWorld,transformComponent.getTransform(),playerRef)){
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
    private static void teleportPlayerToLobby(Ref<EntityStore> playerRef,World lobbyWorld){
        if(playerRef.isValid()){

            Store<EntityStore> store = playerRef.getStore();
            World worldFrom = store.getExternalData().getWorld();
            worldFrom.execute(() -> {
                Transform spawnPoint = lobbyWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(playerRef, store);
                if (spawnPoint != null) {
                    TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());
                    HeadRotation headRotationComponent = store.getComponent(playerRef, HeadRotation.getComponentType());
                    if (transformComponent != null && headRotationComponent != null) {
                        TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(playerRef, TeleportHistory.getComponentType());
                        teleportHistoryComponent.append(
                                worldFrom,
                                new Vector3d(transformComponent.getPosition()),
                                headRotationComponent.getRotation().clone(),
                                "Instance '" + lobbyWorld.getName() + "'");
                    }

                    Teleport teleportComponent = Teleport.createForPlayer(lobbyWorld, spawnPoint);
                    store.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
                }
            });
        }else{
            LOGGER.atSevere().log("Failed to teleport player to lobby! (Cause: Invalid Ref)");
        }
    }
}
