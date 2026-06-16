package dev.ritualanomalyextraction.events;
import com.hypixel.hytale.server.core.universe.PlayerRef;
public class ObjectPickedUpEvent {
    private final PlayerRef player;
    private final String    objectId;
    public ObjectPickedUpEvent(PlayerRef player, String objectId) {
        this.player = player; this.objectId = objectId;
    }
    public PlayerRef getPlayer()   { return player; }
    public String    getObjectId() { return objectId; }
}
