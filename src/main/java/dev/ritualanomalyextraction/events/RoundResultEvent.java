package dev.ritualanomalyextraction.events;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.systems.DecisionSystem.DecisionOutcome;
public class RoundResultEvent {
    private final PlayerRef       player;
    private final DecisionOutcome outcome;
    private final int             scoreChange;
    private final int             objectsExtracted;
    public RoundResultEvent(PlayerRef player, DecisionOutcome outcome,
                            int scoreChange, int objectsExtracted) {
        this.player = player; this.outcome = outcome;
        this.scoreChange = scoreChange; this.objectsExtracted = objectsExtracted;
    }
    public PlayerRef       getPlayer()          { return player; }
    public DecisionOutcome getOutcome()         { return outcome; }
    public int             getScoreChange()     { return scoreChange; }
    public int             getObjectsExtracted(){ return objectsExtracted; }
}
