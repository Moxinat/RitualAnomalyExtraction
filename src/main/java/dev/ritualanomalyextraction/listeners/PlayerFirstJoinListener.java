package dev.ritualanomalyextraction.listeners;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.ui.DeprecatedStatusHud;
import dev.ritualanomalyextraction.utils.WorldUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PlayerFirstJoinListener {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void onPlayerReady(PlayerReadyEvent event) {

        Ref<EntityStore> playerEntityRef = event.getPlayerRef();

        if (!playerEntityRef.isValid()) {
            return;
        }


        PlayerRef playerRef = playerEntityRef.getStore().getComponent(playerEntityRef, PlayerRef.getComponentType());
        Player player = playerEntityRef.getStore().getComponent(playerEntityRef, Player.getComponentType());



        if (player == null) {
            return;
        }

        if (Universe.get().getWorld("RAE-Lobby")== null) {
            LOGGER.at(Level.WARNING).log("Spawn world is null");
            return;
        }

        if (playerRef == null) {
            LOGGER.at(Level.SEVERE).log("playerRef is null playerReadyListener");
        }

        if(!WorldUtil.shouldForceTp()){
            return;
        }

        //only teleport the player to the current game world, if they newly joined the server
        if(NewPlayersList.checkAndConsumeNewPlayer(playerRef)){
            player.getHudManager().addCustomHud(playerRef, new DeprecatedStatusHud(playerRef, Constants.UI_IDLE,-1));
            WorldUtil.teleportPlayerToWorld(RitualGameManager.getLobby(),playerEntityRef);
        }

    }



    public static void onNewConnection(PlayerConnectEvent event){
        NewPlayersList.addNewPlayer(event.getPlayerRef());
    }
    public static void onConnectionRemoved(PlayerDisconnectEvent event){
        NewPlayersList.checkAndConsumeNewPlayer(event.getPlayerRef());
    }

    public static class NewPlayersList {
        private static final List<PlayerRef> LIST = new ArrayList<>(4);

        public static boolean checkAndConsumeNewPlayer(PlayerRef playerRef){
            return LIST.remove(playerRef);
        }
        public static boolean addNewPlayer(PlayerRef playerRef){
            if(LIST.contains(playerRef)) return false;
            return LIST.add(playerRef);
        }

    }
}