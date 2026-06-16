package dev.ritualanomalyextraction;

import dev.ritualanomalyextraction.systems.DecisionSystem.DecisionOutcome;
import dev.ritualanomalyextraction.systems.ScoreTracker;

public class RitualGameManager {

    public enum GameState {
        IDLE,           // server started, no player yet
        CONTROL_ROOM,   // player in hub, can enter portal
        IN_FARM,        // player inside a farm — monster active
        ROUND_RESULT,   // decision processed, brief pause before return
        WIN             // 3 objects extracted
    }

    private GameState   state       = GameState.IDLE;
    private int         roundNumber = 0;
    private final ScoreTracker scoreTracker;

    public RitualGameManager(ScoreTracker scoreTracker) {
        this.scoreTracker = scoreTracker;
    }

    public void onPlayerReady() {
        state = GameState.CONTROL_ROOM;
        scoreTracker.startGame();
    }

    public void onRoundStart() {
        state = GameState.IN_FARM;
        scoreTracker.startRound();
    }

    public void onRoundResult(DecisionOutcome outcome) {
        scoreTracker.recordOutcome(outcome);
        roundNumber++;
        state = scoreTracker.isWinConditionMet() ? GameState.WIN : GameState.ROUND_RESULT;
    }

    public void onReturnToControlRoom() {
        if (state != GameState.WIN) state = GameState.CONTROL_ROOM;
    }

    public GameState getState()      { return state; }
    public int       getRoundNumber(){ return roundNumber; }
    public boolean   isInFarm()      { return state == GameState.IN_FARM; }
    public long      getElapsedRoundMs() { return scoreTracker.getElapsedRoundMs(); }
}
