package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
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

import javax.annotation.Nonnull;


public class InstanceUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.get("RAE|InstanceUtil");

    public static final String DEFAULT_LOBBY_NAME = "RAE-Lobby";
    public static final String DEFAULT_MINIGAME_NAME = "RAE-Minigame";
    private static boolean alreadySetUpLobby = false;

    public static boolean generateInstance(World fromWorld,String instanceCreationName,String instanceFolderName, Transform returnPoint, Ref<EntityStore> playerRef){
        LOGGER.atInfo().log("Trying to generate " + instanceCreationName);
        if(fromWorld == null){
            LOGGER.atSevere().log("Source World is null!");
            return false;
        }

        java.util.concurrent.CompletableFuture<World> completableFuture= InstancesPlugin.get().spawnInstance(instanceFolderName, instanceCreationName,fromWorld,returnPoint);

        completableFuture.thenRun(()-> {
            try {
                teleportPlayerToInstance(playerRef,completableFuture.get());
            } catch (Exception e) {
                LOGGER.atSevere().log("Failed to teleport player to %s! (Cause: Exception)",instanceCreationName);
            }
        });
        LOGGER.atInfo().log("Created %s for Ritual Anomaly Extraction!",instanceCreationName);
        return true;
    }

    //currently only one Lobby World can be present at the same time
    //teleports the player to the Lobby World if enabled!
    public static void trySetUpLobby(World fromWorld, Ref<EntityStore> playerRef){
        if(alreadySetUpLobby) return;
        Config<RaeConfig> config = RitualAnomalyPlugin.getInstance().getConfig();
        RaeConfig raeConfig = config.get();
        //Should it create the lobby, if it doesn't already exist
        if(raeConfig.shouldCreateLobbyUponServerStart() && Universe.get().getWorld(DEFAULT_LOBBY_NAME) == null){
            if(generateInstance(fromWorld,
                    DEFAULT_LOBBY_NAME,
                    DEFAULT_LOBBY_NAME,
                    fromWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(
                            playerRef,
                            playerRef.getStore())
                    ,playerRef)){
                if(raeConfig.shouldSetLobbyAsDefaultWorld()) HytaleServer.get().getConfig().getDefaults().setWorld(DEFAULT_LOBBY_NAME);
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
            if (generateInstance(
                    fromWorld,
                    DEFAULT_MINIGAME_NAME,
                    DEFAULT_MINIGAME_NAME,
                    fromWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(
                            playerRef,
                            playerRef.getStore()),
                    playerRef)) {
                LOGGER.atInfo().log("Created the Minigame for Ritual Anomaly Extraction!");
            }else{
                LOGGER.atSevere().log("Failed creating the Minigame for Ritual Anomaly Extraction!");
            }
        }else{
            LOGGER.atInfo().log("Minigame world already exists!");
        }
    }

    public static void teleportPlayerToInstance(@Nonnull Ref<EntityStore> playerRef, @Nonnull World toWorld){
        teleportPlayerToX(playerRef,toWorld,"Instance");
    }
    public static void teleportPlayerToWorld(@Nonnull Ref<EntityStore> playerRef, @Nonnull World toWorld){
        teleportPlayerToX(playerRef,toWorld,"World");
    }

    private static void teleportPlayerToX(@Nonnull Ref<EntityStore> playerRef, @Nonnull World toWorld,@Nonnull String type){
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
                                type+" '" + toWorld.getName() + "'");
                    }

                    Teleport teleportComponent = Teleport.createForPlayer(toWorld, spawnPoint);
                    store.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
                    LOGGER.atInfo().log("Teleporting player to %s!",type);
                }
            });
        }else{
            LOGGER.atSevere().log("Failed to teleport player to %s! (Cause: Invalid Ref)",type);
        }
    }
}
