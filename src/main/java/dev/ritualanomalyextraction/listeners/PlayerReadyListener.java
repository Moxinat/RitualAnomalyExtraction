package dev.ritualanomalyextraction.listeners;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.ui.StatusHud;
import dev.ritualanomalyextraction.utils.InstanceUtil;

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

        if (player == null) {
            return;
        }

        if (Constants.lobbyWorld == null) {
            LOGGER.at(Level.WARNING).log("Spawn world is null");
            return;
        }

        if(!InstanceUtil.shouldForceTp()){
            return;
        }
        if (gameManager.getState() == RitualGameManager.GameState.IDLE || gameManager.getState() == RitualGameManager.GameState.RITUAL_COMPLETE || gameManager.getState() == RitualGameManager.GameState.FAIL) {
            player.getHudManager().resetHud(playerRef);
            player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef, "spawn", gameManager));
            InstanceUtil.teleportPlayerToWorld(playerRef.getReference(), Constants.lobbyWorld);
        }
        if (gameManager.getState() == RitualGameManager.GameState.CONTROL_ROOM) {
            player.getHudManager().resetHud(playerRef);
            player.getHudManager().addCustomHud(playerRef, new StatusHud(playerRef, "gameTimer", gameManager));
            assert Constants.controlRoomWorld != null;
            InstanceUtil.teleportPlayerToWorld(playerRef.getReference(), Constants.ritualWorld);
        }
    }
}