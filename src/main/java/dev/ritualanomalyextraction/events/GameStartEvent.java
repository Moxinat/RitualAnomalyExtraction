package dev.ritualanomalyextraction.events;
import com.hypixel.hytale.server.core.universe.PlayerRef;
public class GameStartEvent {
    private final PlayerRef player;
    public GameStartEvent(PlayerRef player) { this.player = player; }
    public PlayerRef getPlayer() { return player; }
}
