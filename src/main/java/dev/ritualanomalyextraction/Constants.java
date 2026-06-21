package dev.ritualanomalyextraction;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3i;

import java.util.List;

public class Constants {

    //Initial spawn constants
    public static final World lobbyWorld = Universe.get().getWorld("RAE-Lobby");

    //not needed, since i've set the world spawn point - noc
    /*public static final Transform lobbyPos = new Transform(0.0, 80, 0.0);
    public static final Rotation3f lobbyRot = new Rotation3f(0, 0, 0);*/

    //Control room constants
    public static final World controlRoomWorld = Universe.get().getWorld("RAE-Minigame");

    //not needed, since i've set the world spawn point - noc
    /*public static final Transform controlRoomPos = new Transform(46.5, 134.5, 06.5);
    public static final Rotation3f controlRoomRot = new Rotation3f(0, 80, 0);*/

    //Ritual object spawn constants
    public static final World ritualWorld = Universe.get().getWorld("RAE-Minigame");
    public static final List<String> ritualObjectsWorldID = List.of(
            "Ritual_Active_Pentacle",
            "Ritual_Blood_Effigy",
            "Ritual_Ghost_Candle",
            "Ritual_Soul_Lantern"
    );
    public static final List<Vector3i> ritualObjectSpawnSpots = List.of(
            // TODO add actual coordinates
            new Vector3i(-36, 130, 189),
            new Vector3i(-38, 130, 195),
            new Vector3i(-43, 130, 199),
            new Vector3i(-49, 130, 199),
            new Vector3i(-54, 130, 195),
            new Vector3i(-56, 130, 189),
            new Vector3i(-54, 130, 183),
            new Vector3i(-49, 130, 179),
            new Vector3i(-43, 130, 179),
            new Vector3i(-38, 130, 183)
    );

    //Pickup system constants
    public static final List<String> ritualObjectsWorldToItem = List.of(
            "Ritual_Active_Pentacle",
            "Ritual_Blood_Effigy",
            "Ritual_Ghost_Candle",
            "Ritual_Soul_Lantern"
    );
    //It works without map since the items and the blocks are the same; players can't place it, but it is one
    /*public static final Map<String, String> ritualObjectsWorldToItem = Map.of(
            "ritual_skull_world", "ritual_skull",
            "ritual_lantern_world", "ritual_lantern",
            "ritual_effigy_world", "ritual_effigy"
    );*/

    //Pedestal system constants
    public static final String ritualPedestalID = "Ritual_Pedestal";

    //Game time limit in seconds
    public static final Integer gameTimeLimit = 600;

    //Celebration time in seconds
    public static final Integer celebrationTime = 20;
}
