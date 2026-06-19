package main.java.dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import main.java.dev.ritualanomalyextraction.RitualGameManager;

import java.util.logging.Level;

public final class RitualProgressTracker {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final int REQUIRED_OBJECTS = 3;
    private static int placedObjectCount = 0;
    private static boolean ritualStarted = false;
    private static RitualGameManager gameManager;

    private RitualProgressTracker(RitualGameManager gameManager) {
        this.gameManager = gameManager;
    }

    public static void onObjectPlaced(PlayerRef player, String objectId) {
        if (ritualStarted) {
            return;
        }

        placedObjectCount = Math.min(REQUIRED_OBJECTS, placedObjectCount + 1);

        LOGGER.at(Level.INFO).log("Ritual object placed: '%s'. Progress: %s/%s", objectId, placedObjectCount, REQUIRED_OBJECTS);

        sendProgressMessage(player);

        if (placedObjectCount >= REQUIRED_OBJECTS) {
            ritualStarted = true;
            LOGGER.at(Level.INFO).log("Ritual started.");
            gameManager.onRitualCelebrationStart();
        }
    }

    public static void onObjectRemoved(PlayerRef player, String objectId) {
        if (ritualStarted) {
            return;
        }

        placedObjectCount = Math.max(0, placedObjectCount - 1);

        LOGGER.at(Level.INFO).log("Ritual object removed: '%s'. Progress: %s/%s", objectId, placedObjectCount, REQUIRED_OBJECTS);

        sendProgressMessage(player);
    }

    private static void sendProgressMessage(PlayerRef player) {
        sendMessage(player, "Ritual progress: " + placedObjectCount + "/" + REQUIRED_OBJECTS);
    }

    private static void sendMessage(PlayerRef player, String text) {
        if (player == null) {
            return;
        }
        player.sendMessage(Message.raw(text));
    }

    public static int getPlacedObjectCount() {
        return placedObjectCount;
    }

    public static boolean hasRitualStarted() {
        return ritualStarted;
    }

    public static void reset() {
        placedObjectCount = 0;
        ritualStarted = false;
        LOGGER.at(Level.INFO).log("Ritual progress reset.");
    }
}