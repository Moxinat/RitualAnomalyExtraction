package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.components.FarmSessionComponent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles EXTRACT and ANOMALY button presses. Evaluates the player's decision
 * against the current FarmSessionComponent and dispatches the result.
 *
 * HOW TO WIRE:
 *   TODO: Register Hytale block-interact event on blocks tagged {variant}_extract_button
 *         → call onExtractPressed(player)
 *   TODO: Register Hytale block-interact event on blocks tagged {variant}_anomaly_button
 *         → call onAnomalyPressed(player)
 *   TODO: Teleport player back to control_room_spawn on round end
 */
public class DecisionSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final long RETURN_DELAY_MS = 2_000;

    public enum DecisionType   { EXTRACT, ANOMALY }
    public enum DecisionOutcome {
        OBJECT_EXTRACTED,     // EXTRACT on real farm with object in hand ✓
        WRONG_EXTRACT_FAKE,   // EXTRACT on fake farm ✗
        ANOMALY_CORRECT,      // ANOMALY on fake farm ✓
        ANOMALY_WRONG         // ANOMALY on real farm ✗
    }

    private final RitualGameManager  gameManager;
    private final FarmSessionManager farmSessionManager;
    private final MonsterSystem      monsterSystem;
    private final ScoreTracker       scoreTracker;
    private final ControlRoomSystem  controlRoomSystem;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public DecisionSystem(RitualGameManager gameManager,
                          FarmSessionManager farmSessionManager,
                          MonsterSystem monsterSystem,
                          ScoreTracker scoreTracker,
                          ControlRoomSystem controlRoomSystem) {
        this.gameManager        = gameManager;
        this.farmSessionManager = farmSessionManager;
        this.monsterSystem      = monsterSystem;
        this.scoreTracker       = scoreTracker;
        this.controlRoomSystem  = controlRoomSystem;
    }

    public void register(JavaPlugin plugin) {
        // TODO: register block-interact event for {variant}_extract_button blocks
        // TODO: register block-interact event for {variant}_anomaly_button blocks
        LOGGER.atInfo().log("DecisionSystem registered (button interact events pending real class names).");
    }

    // ── Called by block interact events ────────────────────────────────────

    public void onExtractPressed(PlayerRef player) {
        if (!gameManager.isInFarm()) return;
        FarmSessionComponent session = farmSessionManager.getCurrent();
        if (session == null || session.isDecisionMade()) return;

        if (!session.isReal()) {
            resolve(player, session, DecisionType.EXTRACT, DecisionOutcome.WRONG_EXTRACT_FAKE);
            return;
        }

        if (!session.isObjectGrabbed()) {
            player.sendMessage(Message.raw(
                "You haven't picked up the object. Find it first — or call ANOMALY."));
            return; // no penalty, player can still act
        }

        resolve(player, session, DecisionType.EXTRACT, DecisionOutcome.OBJECT_EXTRACTED);
    }

    public void onAnomalyPressed(PlayerRef player) {
        if (!gameManager.isInFarm()) return;
        FarmSessionComponent session = farmSessionManager.getCurrent();
        if (session == null || session.isDecisionMade()) return;

        DecisionOutcome outcome = session.isReal()
            ? DecisionOutcome.ANOMALY_WRONG
            : DecisionOutcome.ANOMALY_CORRECT;
        resolve(player, session, DecisionType.ANOMALY, outcome);
    }

    // ── Called by MonsterSystem when the monster catches the player ─────────

    public void onPlayerCaught(PlayerRef player) {
        if (!gameManager.isInFarm()) return;
        FarmSessionComponent session = farmSessionManager.getCurrent();
        if (session == null || session.isDecisionMade()) return;

        // Caught = forced failure outcome; use ANOMALY_WRONG as the penalty bucket
        player.sendMessage(Message.raw("It found you. Ejecting... [-50 pts]"));
        resolve(player, session, DecisionType.EXTRACT, DecisionOutcome.ANOMALY_WRONG);
    }

    // ── Core resolve ────────────────────────────────────────────────────────

    private void resolve(PlayerRef player, FarmSessionComponent session,
                         DecisionType decision, DecisionOutcome outcome) {
        session.setDecisionMade(true);
        monsterSystem.deactivate();

        gameManager.onRoundResult(outcome);
        farmSessionManager.endRound();

        showFeedback(player, outcome);

        if (gameManager.getState() == RitualGameManager.GameState.WIN) {
            player.sendMessage(Message.raw("ALL THREE OBJECTS EXTRACTED."));
            player.sendMessage(Message.raw(scoreTracker.getSummary()));
            // TODO: show full win screen / EventTitleUtil
        } else {
            scheduler.schedule(
                () -> returnToControlRoom(player),
                RETURN_DELAY_MS, TimeUnit.MILLISECONDS
            );
        }
    }

    private void returnToControlRoom(PlayerRef player) {
        gameManager.onReturnToControlRoom();
        controlRoomSystem.onPlayerReturnedFromFarm(player);
        // TODO: teleport player to control_room_spawn position
    }

    private void showFeedback(PlayerRef player, DecisionOutcome outcome) {
        String msg = switch (outcome) {
            case OBJECT_EXTRACTED   -> "CORRECT — Object secured. +150 pts";
            case ANOMALY_CORRECT    -> "CORRECT — Anomaly identified. +75 pts";
            case WRONG_EXTRACT_FAKE -> "WRONG — That farm was fake. -50 pts";
            case ANOMALY_WRONG      -> "WRONG — The object was there. -50 pts";
        };
        player.sendMessage(Message.raw(msg));
    }

    public void shutdown() { scheduler.shutdown(); }
}
