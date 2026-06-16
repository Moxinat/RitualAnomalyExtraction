package dev.ritualanomalyextraction.systems;

import dev.ritualanomalyextraction.systems.DecisionSystem.DecisionOutcome;

public class ScoreTracker {

    public static final int REQUIRED_OBJECTS = 3;

    private static final int SCORE_OBJECT_EXTRACTED = 100;
    private static final int SCORE_ANOMALY_CORRECT  =  75;
    private static final int SCORE_PENALTY          =  50;
    private static final long TIME_BONUS_THRESHOLD_MS = 20_000; // under 20 s = bonus

    private int  objectsExtracted    = 0;
    private int  correctAnomalyCalls = 0;
    private int  mistakes            = 0;
    private int  totalScore          = 0;
    private long gameStartMs         = 0;
    private long roundStartMs        = 0;

    public void startGame()  { gameStartMs = System.currentTimeMillis(); }
    public void startRound() { roundStartMs = System.currentTimeMillis(); }

    public long getElapsedRoundMs() {
        return System.currentTimeMillis() - roundStartMs;
    }

    public void recordOutcome(DecisionOutcome outcome) {
        int delta = switch (outcome) {
            case OBJECT_EXTRACTED -> {
                objectsExtracted++;
                int base = SCORE_OBJECT_EXTRACTED;
                int bonus = getElapsedRoundMs() < TIME_BONUS_THRESHOLD_MS ? 50 : 0;
                yield base + bonus;
            }
            case ANOMALY_CORRECT -> {
                correctAnomalyCalls++;
                yield SCORE_ANOMALY_CORRECT;
            }
            case WRONG_EXTRACT_FAKE, ANOMALY_WRONG -> {
                mistakes++;
                yield -SCORE_PENALTY;
            }
        };
        totalScore += delta;
    }

    public int     getObjectsExtracted()  { return objectsExtracted; }
    public int     getTotalScore()        { return totalScore; }
    public int     getMistakes()          { return mistakes; }
    public boolean isWinConditionMet()   { return objectsExtracted >= REQUIRED_OBJECTS; }

    public String getSummary() {
        long totalSec = (System.currentTimeMillis() - gameStartMs) / 1000;
        return String.format("Objects: %d/3 | Score: %d | Mistakes: %d | Time: %d:%02d",
            objectsExtracted, totalScore, mistakes, totalSec / 60, totalSec % 60);
    }
}
