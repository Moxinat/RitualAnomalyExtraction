package main.java.dev.ritualanomalyextraction;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import main.java.dev.ritualanomalyextraction.commands.SpawnObjectsCommand;
import main.java.dev.ritualanomalyextraction.commands.StartCommand;
import main.java.dev.ritualanomalyextraction.listeners.PlayerReadyListener;
import main.java.dev.ritualanomalyextraction.systems.*;

import javax.annotation.Nonnull;

public class RitualAnomalyPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private RitualGameManager gameManager;

    public RitualAnomalyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("RitualAnomalyExtraction constructed.");
    }

    @Override
    protected void setup() {
        gameManager = new RitualGameManager();

        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,event -> PlayerReadyListener.onPlayerReady(event, gameManager));

        this.getCommandRegistry().registerCommand(new SpawnObjectsCommand("rae", "Ritual Anomaly Extraction debug commands"));
        this.getCommandRegistry().registerCommand(new StartCommand("start", "Start the game", gameManager));
        this.getCommandRegistry().registerCommand(new StartCommand("reset", "Reset the game", gameManager));

        this.getEntityStoreRegistry().registerWorldEventType(UseBlockEvent.Post.class);
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem());
        this.getEntityStoreRegistry().registerSystem(new RitualObjectPickupSystem.DropRequestListener());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem.BreakListener());

        LOGGER.atInfo().log("RitualAnomalyExtraction setup complete.");
    }
}
