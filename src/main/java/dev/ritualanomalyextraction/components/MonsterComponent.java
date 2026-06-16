package dev.ritualanomalyextraction.components;

/** Plugin-managed state for the Monster's patrol/chase behaviour. */
public class MonsterComponent {

    public enum MonsterState { PATROL, ALERT, CHASE, RETURN, INACTIVE }

    private MonsterState state             = MonsterState.INACTIVE;
    private int          currentPatrolIndex = 0;
    private float[]      lastKnownPlayerPos = null;
    private long         lastSampleTimeMs  = 0;
    private static final long SAMPLE_INTERVAL_MS = 2_000;

    public static final float PATROL_SPEED = 1.0f;
    public static final float ALERT_SPEED  = 1.2f;
    public static final float CHASE_SPEED  = 1.5f;

    public MonsterState getState()              { return state; }
    public void         setState(MonsterState s){ state = s; }
    public int          getCurrentPatrolIndex() { return currentPatrolIndex; }
    public void         advancePatrolIndex(int total) {
        currentPatrolIndex = (currentPatrolIndex + 1) % total;
    }
    public void resetPatrolIndex() { currentPatrolIndex = 0; }

    public float[] getLastKnownPlayerPos()     { return lastKnownPlayerPos; }
    public boolean shouldSamplePlayerPos(long nowMs) {
        return nowMs - lastSampleTimeMs >= SAMPLE_INTERVAL_MS;
    }
    public void recordPlayerPos(float x, float y, float z, long nowMs) {
        lastKnownPlayerPos = new float[]{x, y, z};
        lastSampleTimeMs   = nowMs;
    }
}
