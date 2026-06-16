package dev.ritualanomalyextraction.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.systems.ControlRoomSystem;

public class PlayerReadyListener {

    public static void onPlayerReady(PlayerReadyEvent event,
                                     RitualGameManager gameManager,
                                     ControlRoomSystem controlRoomSystem) {
        PlayerRef player = event.getPlayer().getPlayerRef();

        if (gameManager.getState() == RitualGameManager.GameState.IDLE) {
            gameManager.onPlayerReady();
            controlRoomSystem.onPlayerFirstReady(player);
        }
    }
}
