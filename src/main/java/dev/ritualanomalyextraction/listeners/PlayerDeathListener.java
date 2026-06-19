package main.java.dev.ritualanomalyextraction.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import main.java.dev.ritualanomalyextraction.Constants;
import main.java.dev.ritualanomalyextraction.RitualGameManager;
import main.java.dev.ritualanomalyextraction.ui.StatusHud;

import java.util.logging.Level;

public class PlayerReadyListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void onPlayerReady(PlayerReadyEvent event, RitualGameManager gameManager) {

        Ref<EntityStore> playerEntityRef = event.getPlayerRef();
        PlayerRef playerRef = playerEntityRef.getStore().getComponent(playerEntityRef, PlayerRef.getComponentType());
        Player player = playerEntityRef.getStore().getComponent(playerEntityRef, Player.getComponentType());

        if (!playerEntityRef.isValid()) {
            return;
        }

        if (playerRef == null) {
            return;
        }

        if (Constants.spawnWorld == null) {
            LOGGER.at(Level.WARNING).log("Spawn world is null");
            return;
        }

        assert player != null;
        if (gameManager.getState() == RitualGameManager.GameState.IDLE || gameManager.getState() == RitualGameManager.GameState.RITUAL_COMPLETE || gameManager.getState() == RitualGameManager.GameState.FAIL) {
            player.getHudManager().resetUserInterface(playerRef);
            player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef, "spawn", gameManager));
            playerRef.updatePosition(Constants.spawnWorld, Constants.spawnPos, Constants.spawnRot);
        }
        if (gameManager.getState() == RitualGameManager.GameState.CONTROL_ROOM) {
            player.getHudManager().resetUserInterface(playerRef);
            player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef, "gameTimer", gameManager));
            assert Constants.controlRoomWorld != null;
            playerRef.updatePosition(Constants.controlRoomWorld, Constants.controlRoomPos, Constants.controlRoomRot);
        }
    }
}