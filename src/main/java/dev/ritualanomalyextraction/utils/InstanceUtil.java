package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.instances.config.InstanceEntityConfig;
import com.hypixel.hytale.builtin.instances.config.WorldReturnPoint;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.ritualanomalyextraction.RitualAnomalyPlugin;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class InstanceUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.get("RAE|InstanceUtil");

    public static final String LOBBY_NAME = "RAE-Lobby";

    public static boolean generateLobby(World fromWorld, Transform fromWorldSpawnPoint,Ref<EntityStore> playerRef){
        LOGGER.atInfo().log("Trying to generate Lobby");
        if(fromWorld == null){
            LOGGER.atSevere().log("Source World is null!");
            return false;
        }
        java.util.concurrent.CompletableFuture<World> completableFuture= InstancesPlugin.get().spawnInstance(LOBBY_NAME,LOBBY_NAME,fromWorld,fromWorldSpawnPoint);

        completableFuture.thenRun(()-> {
            try {
                teleportPlayerToLobby(completableFuture.get());
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        });
        LOGGER.atInfo().log("Created the lobby for Ritual Anomaly Extraction");
        return true;
    }

    public static void trySetUpLobby(World fromWorld, Ref<EntityStore> playerRef, Store<EntityStore> store){
        Config<RaeConfig> config = RitualAnomalyPlugin.getInstance().getConfig();
        RaeConfig raeConfig = config.get();
        //Should it create the lobby, if it doesn't already exist
        if(raeConfig.shouldCreateLobbyUponServerStart() && Universe.get().getWorld(LOBBY_NAME) == null){
            TransformComponent transformComponent = store.getComponent(playerRef,TransformComponent.getComponentType());

            if(transformComponent != null && generateLobby(fromWorld,transformComponent.getTransform(),playerRef)){
                if(raeConfig.shouldSetLobbyAsDefaultWorld()){
                    HytaleServer.get().getConfig().getDefaults().setWorld(LOBBY_NAME);
                }
                raeConfig.setLobbyWorldName(LOBBY_NAME);
            }else{
                LOGGER.atSevere().log("Failed creating the lobby for Ritual Anomaly Extraction!");
            }
        }else{
            LOGGER.atInfo().log("Lobby either exists or shouldn't be created.");
        }

        config.save();
    }
    private static void teleportPlayerToLobby(Ref<EntityStore> playerRef,World lobbyWorld){
        if(playerRef().isValid()){
            worldFrom.execute(() -> {
                Store<Entitystore> store = playerRef.getStore();
                World worldFrom = store.getExternalData().getWorld();

                Transform spawnPoint = lobbyWorld.getWorldConfig().getSpawnProvider().getSpawnPoint(playerRef, store);
                if (spawnPoint != null) {
                    TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());
                    HeadRotation headRotationComponent = store.getComponent(playerRef, HeadRotation.getComponentType());
                    if (transformComponent != null && headRotationComponent != null) {
                        TeleportHistory teleportHistoryComponent = store.ensureAndGetComponent(playerRef, TeleportHistory.getComponentType());
                        teleportHistoryComponent.append(
                                worldFrom,
                                new Vector3d(transformComponent.getPosition()),
                                headRotationComponent.getRotation().clone(), "Instance '" + lobbyWorld.getName() + "'");
                    }

                    Teleport teleportComponent = Teleport.createForPlayer(lobbyWorld, spawnPoint);
                    store.addComponent(playerRef, Teleport.getComponentType(), teleportComponent);
                }
            });
        }else{
            LOGGER.atSevere().log("Failed to teleport player to lobby!");
        }
    }
}
