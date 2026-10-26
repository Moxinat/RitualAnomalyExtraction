package dev.ritualanomalyextraction;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import dev.ritualanomalyextraction.systems.PedestalSystem;
import dev.ritualanomalyextraction.systems.MonsterRedLightSystem;
import dev.ritualanomalyextraction.systems.RitualAnimationManager;
import dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;
import dev.ritualanomalyextraction.systems.RitualProgressTracker;
import dev.ritualanomalyextraction.ui.StatusHud;
import dev.ritualanomalyextraction.utils.WorldUtil;

import java.util.HashMap;
import java.util.Map;

public class RitualGameManager {
    public final static HytaleLogger LOGGER = RitualAnomalyPlugin.getInstance().getLogger().getSubLogger("GameManager");
    private static int lastID = 0;
    private static World lobby;

    private RitualGameManager(){}
    private static final Map<Integer, RitualGame> GAME_REGISTRY = new HashMap<>(2);

    public static void createLobby(){
        var lobby0 = Universe.get().getWorld(Constants.DEFAULT_LOBBY_NAME);
        if(lobby0 != null){
            lobby = lobby0;
            return;
        }
        var worldFuture = WorldUtil.trySetUpLobby();
        assert worldFuture != null;
        worldFuture.thenAccept((world)->lobby = world);
    }

    public static int takeNextID(){
        return lastID++;
    }

    public static void registerGame(RitualGame game){
        if(GAME_REGISTRY.containsKey(game.getID())) return;

        GAME_REGISTRY.put(game.getID(), game);
        LOGGER.atInfo().log("Registered game %s",game.getID());
    }
    public static void unregisterGame(RitualGame game){
        GAME_REGISTRY.remove(game.getID());
        LOGGER.atInfo().log("Unregistered game %s",game.getID());

    }

    public static RitualGame gameFromID(int ID){
        return GAME_REGISTRY.get(ID);
    }
    public static RitualGame gameFromWorldName(String worldName){
        for(var game : GAME_REGISTRY.values()){
            if(game.getGameWorld().getName().equals(worldName)) return game;
        }
        return null;
    }

    public static World getLobby() {
        if(lobby == null) createLobby();
        return lobby;
    }


    public static void shutdown() {
        for (var game :GAME_REGISTRY.values()){
            game.end();
        }
    }

    public enum GameState{
        BOOTING,
        RUNNING,
        RITUAL,
        ENDED,
        DELETING
    }
}
