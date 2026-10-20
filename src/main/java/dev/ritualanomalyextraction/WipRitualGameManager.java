package dev.ritualanomalyextraction;

import com.hypixel.hytale.logger.HytaleLogger;

import java.util.HashMap;
import java.util.Map;

public class WipRitualGameManager {
    public final static HytaleLogger LOGGER = RitualAnomalyPlugin.getInstance().getLogger().getSubLogger("GameManager");
    private static int lastID = 0;

    private static final Map<Integer,WipRitualGame> GAME_REGISTRY = new HashMap<>(2);


    public static int takeNextID(){
        return lastID++;
    }

    public static void registerGame(WipRitualGame game){
        if(GAME_REGISTRY.containsKey(game.getID())) return;

        GAME_REGISTRY.put(game.getID(), game);
        LOGGER.atInfo().log("Registered game %s",game.getID());
    }
    public static void unregisterGame(WipRitualGame game){
        GAME_REGISTRY.remove(game.getID());
        LOGGER.atInfo().log("Unregistered game %s",game.getID());

    }

    public static WipRitualGame gameFromID(int ID){
        return GAME_REGISTRY.get(ID);
    }
    public static WipRitualGame gameFromWorldName(String worldName){
        for(var game : GAME_REGISTRY.values()){
            if(game.getGameWorld().getName().equals(worldName)) return game;
        }
        return null;
    }
}
