package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.nimbusds.jose.util.Pair;
import dev.ritualanomalyextraction.RitualAnomalyPlugin;
import dev.ritualanomalyextraction.RitualGame;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.asset.RitualAnimation;
import dev.ritualanomalyextraction.utils.WorldUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RitualAnimationManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private RitualAnimationManager() {
    }


    public static void reset(World world) {
        var game = RitualGameManager.gameFromWorldName(world.getName());
        world.execute(()->{
            assert game != null;
            for(var task : game.consumeFormerRitualBlocks()){
                WorldUtil.setBlock(world,task.right(),task.left(),null,null);
            }
        });
    }


    public static void play(World world){
        var game = RitualGameManager.gameFromWorldName(world.getName());
        if(game == null){
            LOGGER.atWarning().log("No game found! Aborting play...");
            return;
        }
        play(world,game);
    }
    public static void play(World world,final @Nonnull RitualGame game) {
        System.out.println(world.isInThread());
        RitualAnimation animation = game.getConfig().getRitualAnimation();
        var store =  world.getEntityStore().getStore();
        var searchArea = animation.getRitualEntitySearchArea();
        animation.getResolvedTasks().forEach((delay, sheet)-> HytaleServer.SCHEDULED_EXECUTOR.schedule(()->{
            var toPlace = sheet.placementTasks;
            var soundEvent = sheet.soundEventIndex;
            var cameraEffect = sheet.cameraEffect;
            var rootInteraction = sheet.rootInteraction;

            if(toPlace != null) world.execute(()->placeBlocks(toPlace,world,game));
            if(soundEvent != null)playSFX(soundEvent,store);
            if(cameraEffect != null)playCameraEffect(cameraEffect,world.getPlayerRefs());
            if(rootInteraction != null)world.execute(()-> runRootInteractions(world.getEntityStore().getStore(),rootInteraction,searchArea));
            }, (long)(float) delay,TimeUnit.SECONDS // wtf is this casting
        ));
    }

    private static void placeBlocks(List<Pair<BlockType,Vector3i[]>> toPlace,World world, RitualGame game){
        for(var place:toPlace){
            for(var pos : place.getRight()){
                WorldUtil.replaceBlock(world,pos.x,pos.y,pos.z,place.getLeft(),(id)-> game.addFormerRitualBlock(id,pos),null);
            }
        }
    }

    private static void playSFX(int soundEventIndex, Store<EntityStore> store){
        SoundUtil.playSoundEvent2d(soundEventIndex, SoundCategory.SFX,store);
    }

    private static void playCameraEffect(CameraEffect cameraEffect,Collection<PlayerRef> playerRefs){
        for(PlayerRef ref : playerRefs){
            //TODO make strength into an asset
            ref.getPacketHandler().writeNoCache(cameraEffect.createCameraShakePacket(0.1f));
        }
    }

    private static void runRootInteractions(Store<EntityStore> store,RootInteraction rootInteraction, Pair<Vector3d, Vector3d> searchArea){
        final var entityRefs = List.copyOf(TargetUtil.getAllEntitiesInBox(
                searchArea.getLeft(),
                searchArea.getRight(),
                store
        ));
        //need to do it this way bc I need the commandBuffer and the correct store
        //is parallel better or not?
        store.forEachEntityParallel(RitualAnomalyPlugin.getInstance().getRitualDisplayEntityComponenttype(),(index,chunk,buffer)-> buffer.run((store0)->{
            var ref = chunk.getReferenceTo(index);
            if(!entityRefs.contains(ref)){
                LOGGER.atInfo().log("Skipping entity because it was not the area");
                return;
            }
            final var interactionManager = store0.getComponent(ref, InteractionModule.get().getInteractionManagerComponent());
            if (interactionManager == null){
                LOGGER.atInfo().log("Ritual entity had no InteractionManager");
                return;
            }
            var context = InteractionContext.forInteraction(interactionManager,ref,InteractionType.Use,store0);
            interactionManager.tryStartChain(ref,buffer,InteractionType.ProjectileMiss, context, rootInteraction);
        }));
    }


    public record AnimationSheet(List<Pair<BlockType,Vector3i[]>> placementTasks, Integer soundEventIndex, CameraEffect cameraEffect,RootInteraction rootInteraction){}

}
