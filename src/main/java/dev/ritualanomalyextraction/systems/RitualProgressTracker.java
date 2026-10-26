package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.RitualGame;

import java.util.logging.Level;

public final class RitualProgressTracker {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final int REQUIRED_OBJECTS = 4;
    private int placedObjectCount = 0;
    private boolean ritualStarted = false;


    private final RitualGame game;

    public RitualProgressTracker(RitualGame game) {
        this.game = game;
    }


    public void onObjectPlaced(PlayerRef player, String objectId) {
        if (ritualStarted) {
            return;
        }

        placedObjectCount = Math.min(REQUIRED_OBJECTS, placedObjectCount + 1);

        LOGGER.at(Level.INFO).log("Ritual object placed: '%s'. Progress: %s/%s", objectId, placedObjectCount, REQUIRED_OBJECTS);

        sendProgressMessage(player);

        if (placedObjectCount >= REQUIRED_OBJECTS) {
            ritualStarted = true;
            LOGGER.at(Level.INFO).log("Ritual started.");
            game.startRitual();
        }
    }

    public void onObjectRemoved(PlayerRef player, String objectId) {
        if (ritualStarted) {
            return;
        }

        placedObjectCount = Math.max(0, placedObjectCount - 1);

        LOGGER.at(Level.INFO).log("Ritual object removed: '%s'. Progress: %s/%s", objectId, placedObjectCount, REQUIRED_OBJECTS);

        sendProgressMessage(player);
    }

    private void sendProgressMessage(PlayerRef player) {
        sendMessage(player, "Ritual progress: " + placedObjectCount + "/" + REQUIRED_OBJECTS);
    }

    private void sendMessage(PlayerRef player, String text) {
        if (player == null) {
            return;
        }
        player.sendMessage(Message.raw(text));
    }

    public int getPlacedObjectCount() {
        return placedObjectCount;
    }

    public boolean hasRitualStarted() {
        return ritualStarted;
    }
}