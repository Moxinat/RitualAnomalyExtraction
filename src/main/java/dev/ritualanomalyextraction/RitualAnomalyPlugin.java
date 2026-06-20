package dev.ritualanomalyextraction;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.ritualanomalyextraction.commands.ResetCommand;
import com.hypixel.hytale.server.core.util.Config;
import dev.ritualanomalyextraction.commands.SpawnObjectsCommand;
import dev.ritualanomalyextraction.commands.StartCommand;
import dev.ritualanomalyextraction.listeners.PlayerReadyListener;
import dev.ritualanomalyextraction.systems.*;
import dev.ritualanomalyextraction.utils.InstanceUtil;
import dev.ritualanomalyextraction.utils.RaeConfig;

import javax.annotation.Nonnull;

public class RitualAnomalyPlugin extends JavaPlugin {
    private static RitualAnomalyPlugin instance;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Config<RaeConfig> CONFIG = this.withConfig("RitualAnomalyExtraction-Config",RaeConfig.CODEC);

    private final RitualGameManager gameManager = new RitualGameManager();

    public RitualAnomalyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("RitualAnomalyExtraction constructed.");
    }

    @Override
    protected void setup() {
        instance = this;
        CONFIG.save();
        RitualProgressTracker.setGameManager(gameManager);

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,event -> PlayerReadyListener.onPlayerReady(event, gameManager));

        this.getCommandRegistry().registerCommand(new SpawnObjectsCommand("rae", "Ritual Anomaly Extraction debug commands"));
        this.getCommandRegistry().registerCommand(new StartCommand("start", "Start the game", gameManager));
        this.getCommandRegistry().registerCommand(new ResetCommand("reset", "Reset the game", gameManager));

        this.getEntityStoreRegistry().registerWorldEventType(UseBlockEvent.Post.class);
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem());
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem.DropRequestListener());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem.BreakListener());
        this.getEntityStoreRegistry().registerSystem(new StartButtonSystem(gameManager));

        LOGGER.atInfo().log("RitualAnomalyExtraction setup complete.");
    }

    @Override
    protected void shutdown(){
        CONFIG.save();
    }

    public static RitualAnomalyPlugin getInstance(){
        return instance;

    }
    public Config<RaeConfig> getConfig(){
        return CONFIG;
    }
}
