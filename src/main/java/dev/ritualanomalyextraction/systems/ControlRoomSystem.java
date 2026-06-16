package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.RitualGameManager.GameState;
import dev.ritualanomalyextraction.components.FarmSessionComponent;
import dev.ritualanomalyextraction.systems.ScoreTracker;

/**
 * Manages the control room hub phase.
 * Activates the entry portal when the player is in CONTROL_ROOM state,
 * shows progress on return, and initiates the next round on portal entry.
 *
 * HOW TO WIRE:
 *   TODO: Register Hytale trigger volume enter event on "entry_portal" tag
 *         → call onPlayerEnterEntryPortal(player)
 *   TODO: Teleport player to {variant}_player_spawn in startFarmRound()
 *   TODO: Teleport player to control_room_spawn in onPlayerReturnedFromFarm()
 */
public class ControlRoomSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final RitualGameManager  gameManager;
    private final FarmSessionManager farmSessionManager;
    private final MonsterSystem      monsterSystem;
    private final ScoreTracker       scoreTracker;

    public ControlRoomSystem(RitualGameManager gameManager,
                             FarmSessionManager farmSessionManager,
                             MonsterSystem monsterSystem,
                             ScoreTracker scoreTracker) {
        this.gameManager        = gameManager;
        this.farmSessionManager = farmSessionManager;
        this.monsterSystem      = monsterSystem;
        this.scoreTracker       = scoreTracker;
    }

    public void register(JavaPlugin plugin) {
        // TODO: register trigger volume enter event for "entry_portal" tag
        LOGGER.atInfo().log("ControlRoomSystem registered (entry portal event pending real class name).");
    }

    // ── Called when player first joins ─────────────────────────────────────

    public void onPlayerFirstReady(PlayerRef player) {
        // TODO: teleport player to control_room_spawn
        showProgress(player);
        player.sendMessage(Message.raw("Step through the portal when you're ready."));
    }

    // ── Called when player steps into the entry portal ─────────────────────

    public void onPlayerEnterEntryPortal(PlayerRef player) {
        if (gameManager.getState() != GameState.CONTROL_ROOM) return;
        startFarmRound(player);
    }

    private void startFarmRound(PlayerRef player) {
        FarmSessionComponent session = farmSessionManager.beginRound(gameManager.getRoundNumber());

        LOGGER.atInfo().log("Round " + (gameManager.getRoundNumber() + 1)
            + " starting: variant=" + session.getVariantId()
            + " isReal=" + session.isReal());

        gameManager.onRoundStart();
        monsterSystem.activate(session.getVariantId());

        // TODO: teleport player to: {variant}_player_spawn
        // E.g. world.teleportPlayer(player, getSpawnFor(session.getVariantId()))
        player.sendMessage(Message.raw("Entering the farm..."));
        player.sendMessage(Message.raw(
            "[EXTRACT] = take the object  |  [ANOMALY] = this farm is fake"
        ));
    }

    // ── Called by DecisionSystem after a round resolves ────────────────────

    public void onPlayerReturnedFromFarm(PlayerRef player) {
        // TODO: teleport player to control_room_spawn
        showProgress(player);
        if (gameManager.getState() == GameState.CONTROL_ROOM) {
            player.sendMessage(Message.raw("Step through the portal for the next farm."));
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void showProgress(PlayerRef player) {
        int extracted = farmSessionManager.getCurrent() != null
            ? 0  // mid-round shouldn't show
            : countFromScoreTracker();
        // TODO: use a real scoreboard API for persistent HUD; sendMessage is fallback
        player.sendMessage(Message.raw(
            "Objects extracted: " + countFromScoreTracker() + " / " + ScoreTracker.REQUIRED_OBJECTS
        ));
    }

    private int countFromScoreTracker() { return scoreTracker.getObjectsExtracted(); }
}
