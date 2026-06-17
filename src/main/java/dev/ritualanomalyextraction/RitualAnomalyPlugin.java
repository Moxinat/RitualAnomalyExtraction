package dev.ritualanomalyextraction;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.ritualanomalyextraction.commands.DebugCommand;
import dev.ritualanomalyextraction.listeners.PlayerReadyListener;
import dev.ritualanomalyextraction.systems.*;

import javax.annotation.Nonnull;

public class RitualAnomalyPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final ScoreTracker       scoreTracker;
    private final RitualGameManager  gameManager;
    private final FarmSessionManager farmSessionManager;
    private final ControlRoomSystem  controlRoomSystem;
    private final DecisionSystem     decisionSystem;
    private final MonsterSystem      monsterSystem;
    private final AtmosphereSystem   atmosphereSystem;

    public RitualAnomalyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        scoreTracker       = new ScoreTracker();
        gameManager        = new RitualGameManager(scoreTracker);
        farmSessionManager = new FarmSessionManager(scoreTracker);
        monsterSystem      = new MonsterSystem(gameManager, scoreTracker);
        atmosphereSystem   = new AtmosphereSystem(gameManager, monsterSystem);
        controlRoomSystem  = new ControlRoomSystem(gameManager, farmSessionManager, monsterSystem, scoreTracker);
        decisionSystem     = new DecisionSystem(gameManager, farmSessionManager,
                                                monsterSystem, scoreTracker, controlRoomSystem);
        LOGGER.atInfo().log("RitualAnomalyExtraction constructed.");
    }

    @Override
    protected void setup() {
        this.getEventRegistry().registerGlobal(
            PlayerReadyEvent.class,
            event -> PlayerReadyListener.onPlayerReady(event, gameManager, controlRoomSystem)
        );

        this.getCommandRegistry().registerCommand(
            new DebugCommand(gameManager, scoreTracker, farmSessionManager, monsterSystem)
        );

        monsterSystem.setOnCaughtCallback(decisionSystem::onPlayerCaught);

        controlRoomSystem.register(this);
        decisionSystem.register(this);
        monsterSystem.register(this);
        atmosphereSystem.register(this);

        this.getEntityStoreRegistry().registerWorldEventType(UseBlockEvent.Post.class);
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem.PlaceListener());
        this.getEntityStoreRegistry().registerSystem(new PedestalSystem.BreakListener());

        LOGGER.atInfo().log("RitualAnomalyExtraction setup complete.");
    }
}
