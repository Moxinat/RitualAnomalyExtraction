package dev.ritualanomalyextraction.systems;

import dev.ritualanomalyextraction.components.FarmSessionComponent;

/**
 * Decides real vs fake each round, creates FarmSessionComponent,
 * and cleans up after a round ends.
 * No Hytale events wired — called directly by ControlRoomSystem and DecisionSystem.
 */
public class FarmSessionManager {

    public static final String[] VARIANTS = {"farm_a", "farm_b", "farm_c"};
    private static final String[] OBJECTS  = {"ritual_skull", "ritual_lantern", "ritual_effigy"};

    private final ScoreTracker    scoreTracker;
    private FarmSessionComponent  current;

    public FarmSessionManager(ScoreTracker scoreTracker) {
        this.scoreTracker = scoreTracker;
    }

    public FarmSessionComponent beginRound(int roundNumber) {
        String variantId = VARIANTS[roundNumber % VARIANTS.length];
        String objectId  = OBJECTS [roundNumber % OBJECTS.length];

        // Final winning round is always real so the player can't be cheated of victory
        boolean forceReal = !scoreTracker.isWinConditionMet()
            && (scoreTracker.getObjectsExtracted() == ScoreTracker.REQUIRED_OBJECTS - 1);
        boolean isReal = forceReal || Math.random() < 0.5;

        current = new FarmSessionComponent(variantId, isReal, isReal ? objectId : null);
        return current;
    }

    public void onObjectGrabbed() {
        if (current != null) current.setObjectGrabbed(true);
    }

    public FarmSessionComponent getCurrent() { return current; }

    public void endRound() { current = null; }
}
