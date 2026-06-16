package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.components.MonsterComponent;
import dev.ritualanomalyextraction.components.MonsterComponent.MonsterState;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Controls the Monster's patrol/alert/chase/return behaviour per farm variant.
 * Adapted from HunterSystem — same AI logic, per-variant patrol paths, no altar-draw.
 *
 * HOW TO WIRE (same TODOs as HunterSystem):
 *   TODO: Replace moveToward() with real NPC instruction call
 *   TODO: Replace canSeePlayer()/isWithinRange() with real LoS/distance API
 *   TODO: Replace setSpeed() with real NPC speed modifier
 *   TODO: activate() should teleport the Monster NPC to {variant}_monster_spawn
 *   TODO: deactivate() should freeze/remove the Monster NPC
 */
public class MonsterSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Per-variant patrol paths [x, y, z] — update Y after map is built.
    // Farms are ~30×30 blocks; 3–4 waypoints each.
    private static final Map<String, float[][]> PATROL_PATHS = Map.of(
        "farm_a", new float[][]{
            { 5, 1,  5}, {20, 1,  0}, {15, 1, -15}
        },
        "farm_b", new float[][]{
            {-5, 1,  0}, { 5, 1, -10}, {15, 1,  5}
        },
        "farm_c", new float[][]{
            { 0, 1,  0}, {10, 1, -5}, {20, 1, -10}, {10, 1,  5}
        }
    );

    private static final float LOS_RANGE       = 12.0f;
    private static final float LOS_HALF_ANGLE  = 35.0f;
    private static final float PROXIMITY_RANGE =  4.0f;
    private static final float CATCH_RANGE     =  1.2f;
    private static final long  WAYPOINT_IDLE_MS= 2_000;
    private static final long  CATCH_LINGER_MS = 3_000;
    private static final long  TICK_INTERVAL_MS=   200;

    private final RitualGameManager gameManager;
    private final ScoreTracker      scoreTracker;
    private final MonsterComponent  monsterState = new MonsterComponent();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?>             tickFuture;

    private float[][] currentPatrolPath;
    private long      waypointIdleEndMs  = 0;
    private boolean   idlingAtWaypoint   = false;
    private long      catchLingerEndMs   = 0;

    private Consumer<PlayerRef> onCaughtCallback;

    public MonsterSystem(RitualGameManager gameManager, ScoreTracker scoreTracker) {
        this.gameManager  = gameManager;
        this.scoreTracker = scoreTracker;
    }

    public void register(JavaPlugin plugin) {
        tickFuture = scheduler.scheduleAtFixedRate(
            this::tick, 1_000, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS
        );
        LOGGER.atInfo().log("MonsterSystem registered.");
    }

    public void activate(String variantId) {
        currentPatrolPath = PATROL_PATHS.getOrDefault(variantId, PATROL_PATHS.get("farm_a"));
        monsterState.resetPatrolIndex();
        monsterState.setState(MonsterState.PATROL);
        idlingAtWaypoint = false;
        LOGGER.atInfo().log("Monster activated for " + variantId);
        // TODO: teleport Monster NPC to {variantId}_monster_spawn
    }

    public void deactivate() {
        monsterState.setState(MonsterState.INACTIVE);
        LOGGER.atInfo().log("Monster deactivated.");
        // TODO: freeze or despawn Monster NPC
    }

    public void setOnCaughtCallback(Consumer<PlayerRef> cb) { onCaughtCallback = cb; }

    // ── Tick ────────────────────────────────────────────────────────────────

    private void tick() {
        if (!gameManager.isInFarm()) return;
        if (monsterState.getState() == MonsterState.INACTIVE) return;
        if (currentPatrolPath == null) return;

        long nowMs = System.currentTimeMillis();
        switch (monsterState.getState()) {
            case PATROL -> tickPatrol(nowMs);
            case ALERT  -> tickAlert(nowMs);
            case CHASE  -> tickChase(nowMs);
            case RETURN -> tickReturn(nowMs);
            default     -> {}
        }
    }

    private void tickPatrol(long nowMs) {
        if (idlingAtWaypoint) {
            if (nowMs >= waypointIdleEndMs) {
                idlingAtWaypoint = false;
                monsterState.advancePatrolIndex(currentPatrolPath.length);
            }
            // TODO: checkDetection()
            return;
        }
        setSpeed(MonsterState.PATROL);
        moveToward(currentPatrolPath[monsterState.getCurrentPatrolIndex()]);
        if (hasReachedTarget(currentPatrolPath[monsterState.getCurrentPatrolIndex()])) {
            idlingAtWaypoint  = true;
            waypointIdleEndMs = nowMs + WAYPOINT_IDLE_MS;
        }
        // TODO: checkDetection()
    }

    private void tickAlert(long nowMs) {
        setSpeed(MonsterState.ALERT);
        // TODO: if (canSeePlayer() || isWithinRange(PROXIMITY_RANGE)) → CHASE else → PATROL
    }

    private void tickChase(long nowMs) {
        setSpeed(MonsterState.CHASE);
        if (monsterState.shouldSamplePlayerPos(nowMs)) {
            // TODO: monsterState.recordPlayerPos(playerX, playerY, playerZ, nowMs)
        }
        float[] target = monsterState.getLastKnownPlayerPos();
        if (target == null) { transitionTo(MonsterState.RETURN); return; }
        moveToward(target);
        // TODO: if (isWithinRange(CATCH_RANGE)) → catchPlayer()
        // TODO: if (hasReachedTarget && !isWithinRange(LOS_RANGE)) → RETURN
    }

    private void tickReturn(long nowMs) {
        if (nowMs < catchLingerEndMs) return;
        setSpeed(MonsterState.PATROL);
        moveToward(currentPatrolPath[monsterState.getCurrentPatrolIndex()]);
        if (hasReachedTarget(currentPatrolPath[monsterState.getCurrentPatrolIndex()])) {
            transitionTo(MonsterState.PATROL);
        }
    }

    // ── Stubs ───────────────────────────────────────────────────────────────

    private void moveToward(float[] target) {
        // TODO: monsterNpc.addInstruction(new MoveToInstruction(target[0], target[1], target[2]))
    }

    private boolean hasReachedTarget(float[] target) {
        // TODO: return dist(getMonsterPos(), target) < 1.5f
        return false;
    }

    private void setSpeed(MonsterState state) {
        float mult = switch (state) {
            case PATROL -> MonsterComponent.PATROL_SPEED;
            case ALERT  -> MonsterComponent.ALERT_SPEED;
            case CHASE  -> MonsterComponent.CHASE_SPEED;
            default     -> MonsterComponent.PATROL_SPEED;
        };
        // TODO: apply mult to Monster NPC's movement speed component
    }

    private void transitionTo(MonsterState next) {
        if (monsterState.getState() == next) return;
        MonsterState prev = monsterState.getState();
        monsterState.setState(next);
        if (next == MonsterState.CHASE && prev != MonsterState.CHASE) {
            LOGGER.atInfo().log("Monster entering CHASE.");
            // TODO: SoundPlayer.playAt("monster_growl", monsterPos, 1.0f, 1.0f)
        }
    }

    private void catchPlayer(PlayerRef player) {
        catchLingerEndMs = System.currentTimeMillis() + CATCH_LINGER_MS;
        transitionTo(MonsterState.RETURN);
        // TODO: SoundPlayer.playAt("player_caught", playerPos, 1.0f, 1.0f)
        // TODO: teleport player back to {variant}_player_spawn (not control room — costs time)
        player.sendMessage(Message.raw("It found you."));
        if (onCaughtCallback != null) onCaughtCallback.accept(player);
    }

    public MonsterComponent getState() { return monsterState; }

    public void shutdown() {
        if (tickFuture != null) tickFuture.cancel(false);
        scheduler.shutdown();
    }
}
