package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.function.consumer.TriConsumer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SetBlockSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import dev.ritualanomalyextraction.RitualAnomalyPlugin;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.asset.GameConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static dev.ritualanomalyextraction.Constants.DEFAULT_LOBBY_NAME;

public class WorldUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.get("RAE|WorldUtil");


    public static CompletableFuture<World> generateInstance(@NonNullDecl World fromWorld, String instanceCreationName, String instanceFolderName, Transform returnPoint){
        LOGGER.atInfo().log("Trying to generate " + instanceCreationName);
        CompletableFuture<World> worldFuture =  InstancesPlugin.get().spawnInstance(instanceFolderName, instanceCreationName, fromWorld, returnPoint);
        LOGGER.atInfo().log("Created %s for Ritual Anomaly Extraction!", instanceCreationName);
        return worldFuture;
    }

    public static CompletableFuture<World> trySetUpLobby() {
        World fromWorld = Universe.get().getDefaultWorld();
        if (Universe.get().getWorld(DEFAULT_LOBBY_NAME) == null) {
            LOGGER.atInfo().log("Setting up Lobby.");
            // maybe make the transform a bit different?
            TransformComponent transformComponent = new TransformComponent(new Vector3d(0, 80, 0), new Rotation3f(0, 180, 0));
            return generateInstance(fromWorld, DEFAULT_LOBBY_NAME, DEFAULT_LOBBY_NAME, transformComponent.getTransform());
        }
        return null;
    }

    public static CompletableFuture<World> tryGenerateMinigameWorld(GameConfig config,String name) {
        if (Universe.get().getWorld(name) == null) {
            Transform transform = new Transform(new Vector3d(0,200,0), new Rotation3f(0,180,0)); // Constant?
            return generateInstance(RitualGameManager.getLobby(),name, config.getMiniGameInstanceName(),transform);
        } else {
            LOGGER.atInfo().log("Minigame world already exists!");
            return null;
        }
    }

    private static void addTeleportComponent(@Nonnull World worldFrom,@Nonnull  World worldTo,@Nonnull Store<EntityStore> store,@Nonnull  Ref<EntityStore> playerRef,@Nonnull String worldType){
        worldTo.getWorldConfig().getSpawnProvider().getSpawnPointAsync(playerRef, store).thenAccept(
                spawnPoint ->{
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
        );
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


    public static boolean shouldForceTp(){
        return RitualAnomalyPlugin.getInstance().getConfig().shouldForceTP();
    }

    public static void setBlock(World world, Vector3i position, String block,@Nullable Runnable onFinnish,@Nullable Runnable onFail){
        setBlock(world,position.x,position.y,position.z,block,onFinnish,onFail);
    }
    public static void setBlock(World world, Vector3i position, BlockType blockType,@Nullable Runnable onFinnish,@Nullable Runnable onFail){
        setBlock(world,position.x,position.y,position.z,blockType,onFinnish,onFail);
    }
    public static void setBlock(World world, int x, int y, int z, String block,@Nullable Runnable onFinnish,@Nullable Runnable onFail){
        final var blockType = BlockType.fromString(block);
        if(blockType == null) return;
        setBlock(world,x,y,z,blockType,onFinnish,onFail);
    }
    public static void setBlock(World world, int x, int y, int z,@NonNullDecl BlockType blockType,@Nullable Runnable onFinnish,@Nullable Runnable onFail){
        final var chunkStore = world.getChunkStore();
        chunkStore.getChunkSectionReferenceAtBlockAsync(x, y, z, GetChunkFlags.SET_TICKING).thenAccept(sectionRef -> {
            if (sectionRef == null || !sectionRef.isValid()) {
                LOGGER.atWarning().log("Failed setting block at %s %s %s to %s",x,y,z,blockType.getId());
                if(onFail != null) onFail.run();
                return;
            }
            BlockOperations.setBlock(
                    chunkStore,
                    sectionRef,
                    x,y,z,
                    BlockType.getAssetMap().getIndex(blockType.getId()),
                    blockType,
                    RotationTuple.NONE_INDEX,
                    FillerBlockUtil.NO_FILLER,
                    SetBlockSettings.NONE);
            if(onFinnish != null) onFinnish.run();
        }).exceptionally(t -> {
            LOGGER.at(Level.WARNING).withCause(t).log("Error while getting chunk section.");
            if(onFail != null) onFail.run();
            return null;
        });
    }

    public static BlockType getBlockType(World world, int x, int y, int z){
        return BlockType.getAssetMap().getAsset(getBlockID(world, x, y, z));
    }
    public static int getBlockID(World world, int x, int y, int z){
        final var chunkStore = world.getChunkStore();
        final var b = new Object(){int blockID;};
        chunkStore.getChunkSectionReferenceAtBlockAsync(x, y, z, GetChunkFlags.SET_TICKING).thenAcceptAsync(sectionRef -> {
            if (sectionRef == null || !sectionRef.isValid()) {
                LOGGER.atWarning().log("Failed getting block at %s %s %s",x,y,z);
                return;
            }

            var blockSection = chunkStore.getStore().getComponent(sectionRef, BlockSection.getComponentType());
            if(blockSection == null){
                LOGGER.atWarning().log("Failed getting BlockSection for %s %s %s",x,y,z);
                return;
            }

            b.blockID  = blockSection.get(x, y, z);
        }, world).exceptionally(t -> {
            LOGGER.at(Level.WARNING).withCause(t).log("Error while getting chunk section.");
            return null;
        });
        return b.blockID;
    }
}

