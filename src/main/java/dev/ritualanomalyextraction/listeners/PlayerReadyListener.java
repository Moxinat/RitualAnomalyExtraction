package dev.ritualanomalyextraction.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.utils.InstanceUtil;

public class PlayerReadyListener {

    public static void onPlayerReady(PlayerReadyEvent event,
                                     RitualGameManager gameManager) {

        Ref<EntityStore> playerEntityRef = event.getPlayerRef();

        if (!playerEntityRef.isValid()) {
            return;
        }

        Store<EntityStore> store = playerEntityRef.getStore();

        PlayerRef player = store.getComponent(playerEntityRef, PlayerRef.getComponentType());

        if (player == null) {
            return;
        }

        if (gameManager.getState() == RitualGameManager.GameState.IDLE) {
            gameManager.onPlayerReady();

            player.sendMessage(Message.raw("Ritual Anomaly Extraction started."));
            player.sendMessage(Message.raw("Find the ritual objects and place them on the pedestals."));
        }

        InstanceUtil.trySetUpLobby(event.getPlayer().getWorld(), playerEntityRef, store);
    }
}