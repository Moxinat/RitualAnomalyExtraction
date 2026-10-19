package dev.ritualanomalyextraction;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.ritualanomalyextraction.commands.ResetCommand;
import com.hypixel.hytale.server.core.util.Config;
import dev.ritualanomalyextraction.commands.RaeGroupCommands;
import dev.ritualanomalyextraction.commands.StartCommand;
import dev.ritualanomalyextraction.listeners.PlayerFirstJoinListener;
import dev.ritualanomalyextraction.systems.*;
import dev.ritualanomalyextraction.utils.WorldUtil;
import dev.ritualanomalyextraction.utils.RaeConfig;

import javax.annotation.Nonnull;

public class RitualAnomalyPlugin extends JavaPlugin {
    private static RitualAnomalyPlugin instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Config<RaeConfig> CONFIG = this.withConfig("RitualAnomalyExtraction-Config",RaeConfig.CODEC);
    private RaeConfig.RaeConfigManager CONFIG_MANAGER;

    private final RitualGameManager gameManager;

    public RitualAnomalyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        gameManager = new RitualGameManager();
        LOGGER.atInfo().log("RitualAnomalyExtraction constructed.");
    }

    @Override
    protected void setup() {
        instance = this;
        CONFIG.save().thenRun(()->CONFIG_MANAGER = new RaeConfig.RaeConfigManager(CONFIG));

        RitualProgressTracker.setGameManager(gameManager);

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,event -> PlayerFirstJoinListener.onPlayerReady(event, gameManager));
        this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, PlayerFirstJoinListener::onNewConnection);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerFirstJoinListener::onConnectionRemoved);

        this.getCommandRegistry().registerCommand(new RaeGroupCommands("rae", "Ritual Anomaly Extraction commands"));
        this.getCommandRegistry().registerCommand(new StartCommand("start", "Start the game", gameManager));
        this.getCommandRegistry().registerCommand(new ResetCommand("reset", "Reset the game", gameManager));

        this.getEntityStoreRegistry().registerWorldEventType(UseBlockEvent.Post.class);
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem());
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem.DropRequestListener());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem.BreakListener());
        this.getEntityStoreRegistry().registerSystem(new StartButtonSystem(gameManager));
        this.getEntityStoreRegistry().registerSystem(new MonsterRedLightSystem.Ticker());

        LOGGER.atInfo().log("RitualAnomalyExtraction setup complete.");
    }

    @Override
    protected void start() {
        WorldUtil.trySetUpLobby();
        WorldUtil.tryGenerateMinigameWorld();
    }

    @Override
    protected void shutdown(){
        CONFIG.save();
    }

    public static RitualAnomalyPlugin getInstance(){
        return instance;

    }

    public RaeConfig.RaeConfigManager getConfig(){
        return CONFIG_MANAGER;
    }
}
