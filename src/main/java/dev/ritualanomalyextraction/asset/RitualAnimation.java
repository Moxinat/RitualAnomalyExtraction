package dev.ritualanomalyextraction.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.Float2ObjectMapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.MapKeyValidator;
import com.hypixel.hytale.codec.validation.validator.MapValueValidator;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.nimbusds.jose.util.Pair;
import dev.ritualanomalyextraction.systems.RitualAnimationManager;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RitualAnimation implements JsonAssetWithMap<String, DefaultAssetMap<String,RitualAnimation>> {

    public static AssetBuilderCodec<String,RitualAnimation> CODEC = AssetBuilderCodec.builder(
                    RitualAnimation.class,
                    RitualAnimation::new,
                    Codec.STRING,
                    (c,v)->c.id = v,
                    c -> c.id,
                    (c,v) -> c.data = v,
                    c ->c.data)
            .appendInherited(new KeyedCodec<>("Origin",Vector3iUtil.CODEC),
                    (c,v)->c.origin = v,
                    c-> c.origin,
                    (c1,c2)->c1.origin = c2.origin
            ).addValidator(Validators.nonNull()).documentation("The Centre the replaced blocks are relative to.").add()
            .appendInherited(new KeyedCodec<>("RitualEntitySearchBox",Box.CODEC),
                    (c,v)->c.ritualEntitySearchBox = v,
                    c-> c.ritualEntitySearchBox,
                    (c1,c2)->c1.ritualEntitySearchBox = c2.ritualEntitySearchBox
            ).addValidator(Validators.nonNull()).documentation("In which area the ritualDisplayEntities should be searched for. \n Relativie to the origin").add()
            .appendInherited(new KeyedCodec<>("TimedTasks", new Float2ObjectMapCodec<>(Tasks.CODEC, Float2ObjectOpenHashMap::new)),
                    (c,v)->c.tasks = v,
                    c-> c.tasks,
                    (c1,c2)->c1.tasks = c2.tasks
            ).addValidator(new MapKeyValidator<>(Validators.min(0f))).addValidator(new MapValueValidator<>(Validators.nonNull())).documentation("The task/animation frame to run when the this much seconds after start have passed.").add()

            .afterDecode((RitualAnimation::resolveTasks))
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private Vector3i origin;
    private Box ritualEntitySearchBox;
    private Float2ObjectMap<Tasks> tasks = new Float2ObjectOpenHashMap<>();

    private Float2ObjectOpenHashMap<RitualAnimationManager.AnimationSheet> resolvedAnimation;

    private void resolveTasks(){
        resolvedAnimation = new Float2ObjectOpenHashMap<>(tasks.size());
        tasks.forEach((time,tasks)-> resolvedAnimation.put((float)time,tasks.toResolvedAnimationSheet(origin)));
    }

    public Pair<Vector3d,Vector3d> getRitualEntitySearchArea() {
        var origin3d = new Vector3d(origin);
        var min = new Vector3d(ritualEntitySearchBox.min).add(origin3d);
        var max = new Vector3d(ritualEntitySearchBox.max).add(origin3d);
        return Pair.of(min,max);
    }

    public Vector3i getOrigin() {
        return origin;
    }

    public Float2ObjectOpenHashMap<RitualAnimationManager.AnimationSheet> getResolvedTasks() {
        if(resolvedAnimation == null) resolveTasks();
        return resolvedAnimation;
    }

    private static AssetStore<String, RitualAnimation, DefaultAssetMap<String, RitualAnimation>> ASSET_STORE;
    private static DefaultAssetMap<String,RitualAnimation> ASSET_MAP;
    public static ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(RitualAnimation::getAssetStore));
    @Nonnull
    public static AssetStore<String, RitualAnimation, DefaultAssetMap<String, RitualAnimation>> getAssetStore() {
        if (ASSET_STORE == null) ASSET_STORE = AssetRegistry.getAssetStore(RitualAnimation.class);
        return ASSET_STORE;
    }
    @Nonnull
    public static DefaultAssetMap<String, RitualAnimation> getAssetMap() {
        if (ASSET_MAP == null) ASSET_MAP = getAssetStore().getAssetMap();
        return ASSET_MAP;
    }
    @Override
    public String getId() {
        return id;
    }

    private RitualAnimation(){}

    private static class Tasks{
        public static BuilderCodec<Tasks> CODEC = BuilderCodec.builder(
                        Tasks.class,
                        Tasks::new)
                .appendInherited(new KeyedCodec<>("PlacementTasks",new ArrayCodec<>(BlockReplace.CODEC,BlockReplace[]::new)),
                        (c,v)->c.placementTasks = v,
                        c-> c.placementTasks,
                        (c1,c2)->c1.placementTasks = c2.placementTasks
                ).documentation("Which blocks should be placed where.").add()
                .appendInherited(new KeyedCodec<>("SoundEvent",Codec.STRING),
                        (c,v)->c.soundEvent = v,
                        c-> c.soundEvent,
                        (c1,c2)->c1.soundEvent = c2.soundEvent
                ).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).documentation("Which soundEvent should be played.").add()
                .appendInherited(new KeyedCodec<>("CameraEffect",Codec.STRING),
                        (c,v)->c.cameraEffect = v,
                        c-> c.cameraEffect,
                        (c1,c2)->c1.cameraEffect = c2.cameraEffect
                ).addValidator(CameraEffect.VALIDATOR_CACHE.getValidator()).documentation("Which cameraEffect should be applied to ALL players in the world").add()
                .appendInherited(new KeyedCodec<>("DisplayEntityRootInteraction",Codec.STRING),
                        (c,v)->c.rootInteraction = v,
                        c-> c.rootInteraction,
                        (c1,c2)->c1.rootInteraction = c2.rootInteraction
                ).addValidator(RootInteraction.VALIDATOR_CACHE.getValidator()).documentation("The interaction to run on the pedestal displayEntities inside the search area").add()
                .build();


        private BlockReplace[] placementTasks;
        private String soundEvent;
        private String cameraEffect;
        private String rootInteraction;

        public RitualAnimationManager.AnimationSheet toResolvedAnimationSheet(Vector3i origin){
            List<Pair<BlockType,Vector3i[]>> resolvedTask;
            if(placementTasks != null){
                resolvedTask= new ArrayList<>(placementTasks.length);
                for(final var replaceTask : placementTasks){
                    final var resolvedPositions =  Arrays.stream(replaceTask.locations).map((p)-> new Vector3i(origin).add(p)).toList().toArray(new Vector3i[0]);
                    resolvedTask.add(Pair.of(replaceTask.getToBlock(),resolvedPositions));
                }
            }else {
                resolvedTask = null;
            }
            return new RitualAnimationManager.AnimationSheet(
                    resolvedTask,
                    SoundEvent.getAssetMap().getIndex(soundEvent),
                    CameraEffect.getAssetMap().getAsset(cameraEffect),
                    RootInteraction.getAssetMap().getAsset(rootInteraction)
            );
        }

    }

    private static class BlockReplace{
        public static BuilderCodec<BlockReplace> CODEC = BuilderCodec.builder(BlockReplace.class, BlockReplace::new)
                .documentation("Contains a reference to a block and an array of positions. \n That block will be placed there.")
                .append(new KeyedCodec<>("ToBlock", Codec.STRING),
                        (c,v)->c.toBlock = v,
                        c-> c.toBlock
                ).addValidator(BlockType.VALIDATOR_CACHE.getValidator()).addValidator(Validators.nonNull()).documentation("The block that should be placed.").add() //late validator?
                .append(new KeyedCodec<>("Locations", new ArrayCodec<>(Vector3iUtil.CODEC,Vector3i[]::new)),
                        (c,v)->c.locations = v,
                        c-> c.locations
                ).addValidator(Validators.nonNull()).addValidator(Validators.nonNullArrayElements()).addValidator(Validators.nonEmptyArray()).documentation("The position the blocks should be placed. Relative to the defined origin").add()
                .build();

        private String toBlock;
        private Vector3i[] locations;

        private BlockType getToBlock() {
            return BlockType.fromString(toBlock);
        }
    }

}
