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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.Constants;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RitualAnimationManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Vector3i controllerBlockPos = new Vector3i(32,114,-8);
    private static final String controllerBlockID = "Ritual_Animation";

    private static final List<Ref<EntityStore>> pedestalModels = new ArrayList<>(4);


    private RitualAnimationManager() {
    }


    public static void reset(World world) {
        placeBlock(world,controllerBlockPos,"empty");
        pedestalModels.clear();
    }

    public static void reset() {
        reset(Universe.get().getWorld(Constants.miniGameWorldName));
    }

    public static void play(World world) {
        placeBlock(world,controllerBlockPos,controllerBlockID);
        HytaleServer.SCHEDULED_EXECUTOR.schedule(
                ()->explosion(world),
                8,
                TimeUnit.SECONDS
        );
        applyEntityEffects(world);
    }

    public static void play() {
        play(Universe.get().getWorld(Constants.miniGameWorldName));
    }

    //TODO fix: currently all refs seem to be invalid or something else, so the effect does not get applied to them, which
    private static void applyEntityEffects(World world){
        world.execute(()->{
            EntityEffect effect = EntityEffect.getAssetMap().getAsset("RAE_Dissolve");
            if(effect == null){
                LOGGER.at(Level.WARNING).log("Failed to find entity effect! Skipping...");
                return;
            }
            for(Ref<EntityStore> entityRef : pedestalModels) {
                if(!entityRef.isValid()){
                    LOGGER.at(Level.WARNING).log("Invalid ref! Skipping...");
                    return;
                }
                Store<EntityStore> store = entityRef.getStore();
                EffectControllerComponent effectControllerComponent = store.ensureAndGetComponent(entityRef, EffectControllerComponent.getComponentType());
                effectControllerComponent.addEffect(entityRef, effect, 2f, OverlapBehavior.OVERWRITE, store);
            }
        });

    }
    public static void registerPedestalModel(Ref<EntityStore> ref){
        pedestalModels.add(ref);
    }
    public static void unregisterPedestalModel(Ref<EntityStore> ref){
        pedestalModels.remove(ref);
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

}
