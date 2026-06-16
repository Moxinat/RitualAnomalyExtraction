package dev.ritualanomalyextraction.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.systems.ControlRoomSystem;

public class PlayerReadyListener {

    public static void onPlayerReady(PlayerReadyEvent event,
                                     RitualGameManager gameManager,
                                     ControlRoomSystem controlRoomSystem) {
        Ref<EntityStore> playerEntityRef = event.getPlayerRef();
        PlayerRef player = playerEntityRef.getStore().getComponent(playerEntityRef, PlayerRef.getComponentType());

        if (gameManager.getState() == RitualGameManager.GameState.IDLE) {
            gameManager.onPlayerReady();
            controlRoomSystem.onPlayerFirstReady(player);
        }
    }
}
