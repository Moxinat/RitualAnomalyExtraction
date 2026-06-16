package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.components.MonsterComponent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Ambient wind loop + heartbeat-by-proximity. Silenced during control room phase.
 *
 * HOW TO WIRE:
 *   TODO: All SoundPlayer calls — replace with real Hytale audio API
 *   TODO: getMonsterDistanceToPlayer() — replace with real position API
 */
public class AtmosphereSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final float HEARTBEAT_START = 20.0f;
    private static final float HEARTBEAT_FULL  =  6.0f;
    private static final long  TICK_MS         =  500;

    private final RitualGameManager gameManager;
    private final MonsterSystem     monsterSystem;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> audioTick;

    private boolean windPlaying      = false;
    private float   lastHeartbeatVol = 0.0f;

    public AtmosphereSystem(RitualGameManager gameManager, MonsterSystem monsterSystem) {
        this.gameManager   = gameManager;
        this.monsterSystem = monsterSystem;
    }

    public void register(JavaPlugin plugin) {
        audioTick = scheduler.scheduleAtFixedRate(
            this::tick, 2_000, TICK_MS, TimeUnit.MILLISECONDS
        );
        LOGGER.atInfo().log("AtmosphereSystem registered.");
    }

    private void tick() {
        boolean inFarm = gameManager.isInFarm();

        // Wind: start when in farm, stop on return to control room
        if (inFarm && !windPlaying) {
            windPlaying = true;
            LOGGER.atInfo().log("Ambient wind started.");
            // TODO: windChannel = SoundPlayer.playLooping("ambient_wind_loop", Vec3.ZERO, 0.35f)
        } else if (!inFarm && windPlaying) {
            windPlaying = false;
            // TODO: windChannel.stop()
        }

        if (!inFarm) return;

        // Heartbeat: scales with monster proximity
        // TODO: float dist = getMonsterDistanceToPlayer(); — real position API
        float dist = HEARTBEAT_START; // placeholder

        boolean monsterActive = monsterSystem.getState().getState() != MonsterComponent.MonsterState.INACTIVE;
        float targetVol = monsterActive ? computeHeartbeatVol(dist) : 0.0f;

        if (Math.abs(targetVol - lastHeartbeatVol) > 0.02f) {
            lastHeartbeatVol = targetVol;
            if (targetVol <= 0.0f) {
                // TODO: heartbeatChannel.stop()
            } else {
                // TODO: heartbeatChannel.setVolume(targetVol) or start if not playing
            }
        }
    }

    private float computeHeartbeatVol(float dist) {
        if (dist >= HEARTBEAT_START) return 0.0f;
        float t = 1.0f - ((dist - HEARTBEAT_FULL) / (HEARTBEAT_START - HEARTBEAT_FULL));
        return Math.max(0.0f, Math.min(1.0f, t)) * 0.8f;
    }

    public void shutdown() {
        if (audioTick != null) audioTick.cancel(false);
        scheduler.shutdown();
        // TODO: stop windChannel and heartbeatChannel
    }
}
