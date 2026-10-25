package dev.ritualanomalyextraction;

import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import dev.ritualanomalyextraction.asset.GameConfig;
import dev.ritualanomalyextraction.asset.RitualAnimation;
import dev.ritualanomalyextraction.commands.RaeGroupCommands;
import dev.ritualanomalyextraction.commands.StartCommand;
import dev.ritualanomalyextraction.components.RitualDisplayEntity;
import dev.ritualanomalyextraction.listeners.PlayerFirstJoinListener;
import dev.ritualanomalyextraction.systems.PedestalSystem;
import dev.ritualanomalyextraction.systems.RitualObjectPickupSystem;
import dev.ritualanomalyextraction.systems.StartButtonSystem;
import dev.ritualanomalyextraction.utils.RaeConfig;

import javax.annotation.Nonnull;

public class RitualAnomalyPlugin extends JavaPlugin {
    private static RitualAnomalyPlugin instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Config<RaeConfig> CONFIG = this.withConfig("RitualAnomalyExtraction-Config",RaeConfig.CODEC);
    private RaeConfig.RaeConfigManager CONFIG_MANAGER;

    private ComponentType<EntityStore,RitualDisplayEntity> RITUAL_DISPLAY_ENTITY_COMPONENTTYPE;

    public RitualAnomalyPlugin(@Nonnull JavaPluginInit init) {
        super(init);

        LOGGER.atInfo().log("RitualAnomalyExtraction constructed.");
    }

    @Override
    protected void setup() {
        instance = this;
        CONFIG.save().thenRun(()->CONFIG_MANAGER = new RaeConfig.RaeConfigManager(CONFIG));

        this.getAssetRegistry().register(HytaleAssetStore.builder(RitualAnimation.class, new DefaultAssetMap<>())
                .loadsAfter(Item.class)
                .loadsAfter(BlockType.class)
                .loadsAfter(RootInteraction.class)
                        .loadsAfter(SoundEvent.class)
                        .loadsAfter(CameraEffect.class)
                .setCodec(RitualAnimation.CODEC)
                .setPath("Game/Animations")
                .setKeyFunction(RitualAnimation::getId)
                .build());
        this.getAssetRegistry().register(HytaleAssetStore.builder(GameConfig.class, new DefaultAssetMap<>())
                .loadsAfter(Item.class)
                .loadsAfter(ModelAsset.class)
                .loadsAfter(RitualAnimation.class)
                .setCodec(GameConfig.CODEC)
                .setPath("Game/Config")
                .setKeyFunction(GameConfig::getId)
                .build());



        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerFirstJoinListener::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, PlayerFirstJoinListener::onNewConnection);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerFirstJoinListener::onConnectionRemoved);

        this.getCommandRegistry().registerCommand(new RaeGroupCommands("rae", "Ritual Anomaly Extraction commands"));
        this.getCommandRegistry().registerCommand(new StartCommand("start", "Start the game"));

        this.getEntityStoreRegistry().registerWorldEventType(UseBlockEvent.Post.class);
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem());
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem.DropRequestListener());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem.BreakListener());
        this.getEntityStoreRegistry().registerSystem(new StartButtonSystem());

        this .RITUAL_DISPLAY_ENTITY_COMPONENTTYPE = this.getEntityStoreRegistry().registerComponent(RitualDisplayEntity.class,RitualDisplayEntity::new);

        LOGGER.atInfo().log("RitualAnomalyExtraction setup complete.");
    }

    @Override
    protected void start() {
        RitualGameManager.createLobby();
    }

    @Override
    protected void shutdown(){
        CONFIG.save();
        RitualGameManager.shutdown();
    }

    public static RitualAnomalyPlugin getInstance(){
        return instance;

    }

    public RaeConfig.RaeConfigManager getConfig(){
        return CONFIG_MANAGER;
    }

    public ComponentType<EntityStore, RitualDisplayEntity> getRitualDisplayEntityComponenttype() {
        return RITUAL_DISPLAY_ENTITY_COMPONENTTYPE;
    }
}
