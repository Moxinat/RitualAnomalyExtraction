package dev.ritualanomalyextraction.events;
import com.hypixel.hytale.server.core.universe.PlayerRef;
public class GameWonEvent {
    private final PlayerRef player;
    private final String    scoreSummary;
    public GameWonEvent(PlayerRef player, String scoreSummary) {
        this.player = player; this.scoreSummary = scoreSummary;
    }
    public PlayerRef getPlayer()       { return player; }
    public String    getScoreSummary() { return scoreSummary; }
}
