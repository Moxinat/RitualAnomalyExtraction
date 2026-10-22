package dev.ritualanomalyextraction.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.instances.InstanceValidator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.*;

public class GameConfig implements JsonAssetWithMap<String, DefaultAssetMap<String, GameConfig>> {

    public static AssetBuilderCodec<String,GameConfig> CODEC = AssetBuilderCodec.builder(
            GameConfig.class,
                    GameConfig::new,
                    Codec.STRING,
                    (c,v)->c.id = v,
                    c -> c.id,
                    (c,v) -> c.data = v,
            c ->c.data)

            .appendInherited(new KeyedCodec<>("GameTime", Codec.INTEGER),
                    (c,v)->c.gameTime = v,
                    c-> c.gameTime,
                    (c1,c2)->c1.gameTime = c2.gameTime
            ).addValidator(Validators.min(120)).add()
            .appendInherited(new KeyedCodec<>("PossibleItems", new ArrayCodec<>(ItemModelPair.CODEC,ItemModelPair[]::new)),
                    (c,v)->c.possibleItems = v,
                    c-> c.possibleItems,
                    (c1,c2)->c1.possibleItems = c2.possibleItems
            ).addValidator(Validators.nonEmptyArray()).addValidator(Validators.uniqueInArray()).add()
            .appendInherited(new KeyedCodec<>("PossibleItemLocations", new ArrayCodec<>(CommentedLocation.CODEC,CommentedLocation[]::new)),
                    (c,v)->c.possibleItemLocations = v,
                    c-> c.possibleItemLocations,
                    (c1,c2)->c1.possibleItemLocations = c2.possibleItemLocations
            ).addValidator(Validators.nonEmptyArray()).addValidator(Validators.uniqueInArray()).add()
            .appendInherited(new KeyedCodec<>("SailorSpawnLocations", new ArrayCodec<>(CommentedLocation.CODEC,CommentedLocation[]::new)),
                    (c,v)->c.sailorSpawnLocations = v,
                    c-> c.sailorSpawnLocations,
                    (c1,c2)->c1.sailorSpawnLocations = c2.sailorSpawnLocations
            ).addValidator(Validators.nonEmptyArray()).addValidator(Validators.uniqueInArray()).add()
            //WARN animation is defined for the specific instance so others will fail
            //TODO resolve this by adding another asset for tha animation
            .appendInherited(new KeyedCodec<>("MiniGameInstanceName", Codec.STRING),
                    (c,v)-> c.miniGameInstanceName = v,
                    c -> c.miniGameInstanceName,
                    (c1,c2)->c1.miniGameInstanceName = c2.miniGameInstanceName
            ).addValidator(InstanceValidator.INSTANCE).addValidator(Validators.nonNull()).add()
            .appendInherited(new KeyedCodec<>("WarningTime", Codec.INTEGER),
                    (c,v)->c.warningTime = v,
                    c-> c.warningTime,
                    (c1,c2)->c1.warningTime = c2.warningTime
            ).addValidator(Validators.min(5)).add()
            .appendInherited(new KeyedCodec<>("WarningSoundEvent", Codec.STRING),
                    (c,v)->c.warningSoundEvent = v,
                    c-> c.warningSoundEvent,
                    (c1,c2)->c1.warningSoundEvent = c2.warningSoundEvent
            ).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(Validators.nonNull()).add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;
    private int gameTime = 900;
    private ItemModelPair[] possibleItems;
    private CommentedLocation[] possibleItemLocations;
    private CommentedLocation[] sailorSpawnLocations;
    private String miniGameInstanceName;
    private int warningTime = 60;
    private String warningSoundEvent;


    private Map<String,String> itemIdModelIdMap;
    private List<Vector3i> itemLocations;
    private List<Transform> spawnLocations;
    private static AssetStore<String, GameConfig, DefaultAssetMap<String, GameConfig>> ASSET_STORE;
    private static DefaultAssetMap<String,GameConfig> ASSET_MAP;
    @Nonnull
    public static AssetStore<String, GameConfig, DefaultAssetMap<String, GameConfig>> getAssetStore() {
        if (ASSET_STORE == null) ASSET_STORE = AssetRegistry.getAssetStore(GameConfig.class);
        return ASSET_STORE;
    }

    @Nonnull
    public static DefaultAssetMap<String, GameConfig> getAssetMap() {
        if (ASSET_MAP == null) ASSET_MAP = getAssetStore().getAssetMap();
        return ASSET_MAP;
    }

    public Map<String,String> getItemIdModelIdMap(){
        if(itemIdModelIdMap == null){
            itemIdModelIdMap = new HashMap<>(possibleItems.length);
            for(var p : possibleItems){
                itemIdModelIdMap.put(p.item,p.model);
            }
        }
        return itemIdModelIdMap;
    }

    public String getMiniGameInstanceName() {
        return miniGameInstanceName;
    }

    public int getWarningTime() {
        return warningTime;
    }

    public String getWarningSoundEvent() {
        return warningSoundEvent;
    }

    public int getGameTime() {
        return gameTime;
    }
    public List<Vector3i> getPossibleItemLocations(){
        if(itemLocations == null){
            itemLocations = new ArrayList<>(possibleItemLocations.length);
            for(var location : possibleItemLocations){
                itemLocations.add(location.location);
            }
        }
        return itemLocations;
    }
    public List<Transform> getSailorSpawnLocations(){
        if(spawnLocations == null){
            spawnLocations = new ArrayList<>(sailorSpawnLocations.length);
            for(var location : sailorSpawnLocations){
                spawnLocations.add(new Transform(location.location));
            }
        }
        return spawnLocations;
    }

    @Override
    public String getId() {
        return id;
    }

    private static class ItemModelPair{
        public static BuilderCodec<ItemModelPair> CODEC = BuilderCodec.builder(ItemModelPair.class,ItemModelPair::new)
                .append(new KeyedCodec<>("Item", Codec.STRING),
                    (c,v)->c.item = v,
                        c-> c.item
                ).addValidator(Item.VALIDATOR_CACHE.getValidator()).add()
                .append(new KeyedCodec<>("Model", Codec.STRING),
                        (c,v)->c.model = v,
                        c-> c.model
                ).addValidator(ModelAsset.VALIDATOR_CACHE.getValidator()).add()
                .build();

        private String item;
        private String model;

    }
    private static class CommentedLocation{
        public static BuilderCodec<CommentedLocation> CODEC = BuilderCodec.builder(CommentedLocation.class,CommentedLocation::new)
                .append(new KeyedCodec<>("Comment", Codec.STRING),
                        (c,v)->c.comment = v,
                        c-> c.comment
                ).metadata(new UIPropertyTitle("Comment (purely organizational)")).add()
                .append(new KeyedCodec<>("Location", Vector3iUtil.CODEC),
                        (c,v)->c.location = v,
                        c-> c.location
                ).addValidator(Validators.nonNull()).add()
                .build();

        private String comment;
        private Vector3i location;
    }

    public static final SingleArgumentType<GameConfig> ARGUMENT_TYPE = new SingleArgumentType<>("gameplayConfig","") {
        @Override
        public GameConfig parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
            return getAssetStore().getAssetMap().getAsset(input);
        }

        @Override
        public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
            final String name = textAlreadyEntered.toLowerCase(Locale.ROOT);
            for (final var gameConfig : getAssetStore().getAssetMap().getAssetMap().keySet()) {
                if (gameConfig.toLowerCase(Locale.ROOT).startsWith(name)) {
                    result.suggest(gameConfig);
                }
            }
        }

    };
}
