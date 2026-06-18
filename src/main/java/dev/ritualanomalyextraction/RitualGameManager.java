package dev.ritualanomalyextraction;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

public class RitualGameManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public enum GameState {
        IDLE,
        CONTROL_ROOM,
        RITUAL_ACTIVE,
        RITUAL_COMPLETE
    }

    private GameState state = GameState.IDLE;
    private long gameStartMs = 0;

    public void onPlayerReady() {
        if (state != GameState.IDLE) {
            return;
        }

        state = GameState.CONTROL_ROOM;
        gameStartMs = System.currentTimeMillis();

        LOGGER.at(Level.INFO).log("Player ready. Game state changed to CONTROL_ROOM.");

        // TODO: Spawn player in control room when teleport API is wired.
        // TODO: Call RitualObjectSpawnManager from the real game start flow.
    }

    public void onRitualStarted() {
        if (state == GameState.RITUAL_COMPLETE) {
            return;
        }

        state = GameState.RITUAL_ACTIVE;

        LOGGER.at(Level.INFO).log("Ritual started. Game state changed to RITUAL_ACTIVE.");

        // TODO: Trigger ritual effects.
        // TODO: Activate final portal / win condition / end sequence.
    }

    public void onRitualComplete() {
        if (state == GameState.RITUAL_COMPLETE) {
            return;
        }

        state = GameState.RITUAL_COMPLETE;

        LOGGER.at(Level.INFO).log("Ritual complete. Game state changed to RITUAL_COMPLETE.");

        // TODO: Show final win message / end screen.
    }

    public void reset() {
        state = GameState.IDLE;
        gameStartMs = 0;

        LOGGER.at(Level.INFO).log("Game manager reset.");

        // TODO: Reset RitualProgressTracker.
        // TODO: Clear spawned ritual objects.
        // TODO: Clear pedestal states if needed.
    }

    public GameState getState() {
        return state;
    }

    public boolean isInControlRoom() {
        return state == GameState.CONTROL_ROOM;
    }

    public boolean isRitualActive() {
        return state == GameState.RITUAL_ACTIVE;
    }

    public boolean isRitualComplete() {
        return state == GameState.RITUAL_COMPLETE;
    }

    public long getElapsedGameMs() {
        if (gameStartMs == 0) {
            return 0;
        }

        return System.currentTimeMillis() - gameStartMs;
    }
}