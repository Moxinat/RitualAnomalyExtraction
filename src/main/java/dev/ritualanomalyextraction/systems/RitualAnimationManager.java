package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import dev.ritualanomalyextraction.Constants;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RitualAnimationManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Vector3i controllerBlockPos = new Vector3i(32,114,-8);
    private static final String controllerBlockID = "Ritual_Animation";

    private static final String bloodyGoldID = "Rock_Gold_Brick_Ornate_Glowing_Bloody";
    private static final String glowGoldID = "Rock_Gold_Brick_Ornate_Glowing";
    private final static List<Vector3i> goldPos = List.of(
            //SouthEast
            new Vector3i(39,114,-4),
            new Vector3i(39,114,-3),
            new Vector3i(38,114,-2),
            new Vector3i(37,114,-1),
            new Vector3i(36,114,-1),

            new Vector3i(39,109,-4),
            new Vector3i(39,109,-3),
            new Vector3i(38,109,-2),
            new Vector3i(37,109,-1),
            new Vector3i(36,109,-1),

            new Vector3i(39,113,-4),
            new Vector3i(39,112,-4),
            new Vector3i(39,111,-4),
            new Vector3i(39,110,-4),

            new Vector3i(36,113,-1),
            new Vector3i(36,112,-1),
            new Vector3i(36,111,-1),
            new Vector3i(36,110,-1),

            //NorthEast
            new Vector3i(39,114,-12),
            new Vector3i(39,114,-13),
            new Vector3i(38,114,-14),
            new Vector3i(37,114,-15),
            new Vector3i(36,114,-15),

            new Vector3i(39,109,-12),
            new Vector3i(39,109,-13),
            new Vector3i(38,109,-14),
            new Vector3i(37,109,-15),
            new Vector3i(36,109,-15),

            new Vector3i(39,113,-12),
            new Vector3i(39,112,-12),
            new Vector3i(39,111,-12),
            new Vector3i(39,110,-12),

            new Vector3i(36,113,-15),
            new Vector3i(36,112,-15),
            new Vector3i(36,111,-15),
            new Vector3i(36,110,-15),

            //SouthWest
            new Vector3i(25,114,-4),
            new Vector3i(25,114,-3),
            new Vector3i(26,114,-2),
            new Vector3i(27,114,-1),
            new Vector3i(28,114,-1),

            new Vector3i(25,109,-4),
            new Vector3i(25,109,-3),
            new Vector3i(26,109,-2),
            new Vector3i(27,109,-1),
            new Vector3i(28,109,-1),

            new Vector3i(25,113,-4),
            new Vector3i(25,112,-4),
            new Vector3i(25,111,-4),
            new Vector3i(25,110,-4),

            new Vector3i(28,113,-1),
            new Vector3i(28,112,-1),
            new Vector3i(28,111,-1),
            new Vector3i(28,110,-1),

            //NorthWest
            new Vector3i(25,114,-12),
            new Vector3i(25,114,-13),
            new Vector3i(26,114,-14),
            new Vector3i(27,114,-15),
            new Vector3i(28,114,-15),

            new Vector3i(25,109,-12),
            new Vector3i(25,109,-13),
            new Vector3i(26,109,-14),
            new Vector3i(27,109,-15),
            new Vector3i(28,109,-15),

            new Vector3i(25,113,-12),
            new Vector3i(25,112,-12),
            new Vector3i(25,111,-12),
            new Vector3i(25,110,-12),

            new Vector3i(28,113,-15),
            new Vector3i(28,112,-15),
            new Vector3i(28,111,-15),
            new Vector3i(28,110,-15),

            //Ground
            new Vector3i(36,108,-8),
            new Vector3i(28,108,-8),
            new Vector3i(32,108,-4),
            new Vector3i(32,108,-12)
    );

    private static final Vector3d min = new Vector3d(28,108,-12);
    private static final Vector3d max = new Vector3d(36,111,-4);


    private RitualAnimationManager() {
    }


    public static void reset(World world) {
        placeBlock(world,controllerBlockPos,"empty");
        replaceGoldPos(world,glowGoldID);
    }

    public static void reset() {
        reset(Universe.get().getWorld(Constants.miniGameWorldName));
    }

    public static void play(World world) {
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                ()-> {
                    placeBlock(world,controllerBlockPos,controllerBlockID);
                    HytaleServer.SCHEDULED_EXECUTOR.schedule(
                            ()->{
                                explosion(world);
                                replaceGoldPos(world,bloodyGoldID);
                            },
                            8,
                            TimeUnit.SECONDS
                    );
                    applyEntityEffects(world);
                },
                1,
                TimeUnit.SECONDS
        );
    }

    public static void play() {
        play(Universe.get().getWorld(Constants.miniGameWorldName));
    }

    private static void applyEntityEffects(World world){
        world.execute(()->{
            EntityEffect effect = EntityEffect.getAssetMap().getAsset("RAE_Dissolve");
            if(effect == null){
                LOGGER.at(Level.WARNING).log("Failed to find entity effect! Skipping...");
                return;
            }
            Store<EntityStore> store = world.getEntityStore().getStore();
            List<Ref<EntityStore>> refs = TargetUtil.getAllEntitiesInBox(
                    min,
                    max,
                    store
                    );

            LOGGER.at(Level.INFO).log("Found %s entity candidates for applying effects",String.valueOf(refs.size()));
            for(Ref<EntityStore> entityRef : refs) {
                if(!entityRef.isValid()){
                    LOGGER.at(Level.WARNING).log("Invalid ref! Skipping...");
                    return;
                }
                ModelComponent model = store.getComponent(entityRef, ModelComponent.getComponentType());
                if(model == null){
                    LOGGER.at(Level.WARNING).log("Entity doesn't have a model! Skipping...");
                    return;
                }
                if(!model.getModel().getModelAssetId().endsWith("Display")){
                    LOGGER.at(Level.INFO).log("Entity is not a display entity (pedestal)! Skipping...");
                    return;
                }

                EffectControllerComponent effectControllerComponent = store.ensureAndGetComponent(entityRef, EffectControllerComponent.getComponentType());
                effectControllerComponent.addEffect(entityRef, effect, 16f, OverlapBehavior.OVERWRITE, store); // disappear in 2 seconds, but stays invisible for another 14 seconds
                LOGGER.at(Level.INFO).log("Applied Effect to pedestal entity display...");
            }
        });

    }
    private static void placeBlock(World world,Vector3i pos, String blockID){
        if(world== null){
            LOGGER.atSevere().log("No World! Aborting...");
            return;
        }
        if(blockID == null||blockID.isBlank()|| blockID.equalsIgnoreCase("empty")){
            world.execute(()-> world.setBlock(pos.x, pos.y, pos.z, BlockType.EMPTY_KEY));
        }else {
            world.execute(()-> world.setBlock(pos.x, pos.y, pos.z, blockID));
        }
        LOGGER.atInfo().log("Replaced a block!");

    }

    private static void tremor(World world){
        CameraEffect cameraEffect = CameraEffect.getAssetMap().getAsset("Tremor");
        if(cameraEffect == null){
            LOGGER.at(Level.WARNING).log("Failed finding tremor camshake effect! Skipping...");
            return;
        }

        for(PlayerRef ref : world.getPlayerRefs()){
            ref.getPacketHandler().writeNoCache(cameraEffect.createCameraShakePacket(0.1f));
        }
    }

    private static void explosionSound(World world){
        int index = SoundEvent.getAssetMap().getIndex("RAE_Ritual_Boom");
        SoundUtil.playSoundEvent2d( index, SoundCategory.SFX,world.getEntityStore().getStore());
    }

    private static void explosion(World world){
        tremor(world);
        explosionSound(world);
    }

    private static void replaceGoldPos(World world,String blockID){
        if(world== null){
            LOGGER.atSevere().log("No World! Aborting...");
            return;
        }
        world.execute(()->{
            for(Vector3i pos :goldPos){
                world.setBlock(pos.x, pos.y, pos.z, blockID);
            }
        } );

    }

}
