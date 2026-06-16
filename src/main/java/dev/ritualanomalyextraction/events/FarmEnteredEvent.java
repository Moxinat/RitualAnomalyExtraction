package dev.ritualanomalyextraction.events;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.components.FarmSessionComponent;
public class FarmEnteredEvent {
    private final PlayerRef           player;
    private final FarmSessionComponent session;
    public FarmEnteredEvent(PlayerRef player, FarmSessionComponent session) {
        this.player = player; this.session = session;
    }
    public PlayerRef            getPlayer()  { return player; }
    public FarmSessionComponent getSession() { return session; }
}
