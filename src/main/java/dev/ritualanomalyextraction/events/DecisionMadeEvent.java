package dev.ritualanomalyextraction.events;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.systems.DecisionSystem.DecisionOutcome;
import dev.ritualanomalyextraction.systems.DecisionSystem.DecisionType;
public class DecisionMadeEvent {
    private final PlayerRef      player;
    private final DecisionType   decision;
    private final boolean        wasCorrect;
    private final DecisionOutcome outcome;
    public DecisionMadeEvent(PlayerRef player, DecisionType decision,
                             boolean wasCorrect, DecisionOutcome outcome) {
        this.player = player; this.decision = decision;
        this.wasCorrect = wasCorrect; this.outcome = outcome;
    }
    public PlayerRef      getPlayer()    { return player; }
    public DecisionType   getDecision()  { return decision; }
    public boolean        wasCorrect()   { return wasCorrect; }
    public DecisionOutcome getOutcome()  { return outcome; }
}
