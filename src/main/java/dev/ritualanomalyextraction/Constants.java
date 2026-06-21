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
            new Vector3i(46, 134, -14), // church back
            new Vector3i(-24, 107, -213), // windmill
            new Vector3i(35, 105, -159), // ruined house
            new Vector3i(-149, 118, -158), // school under the roof
            new Vector3i(-256, 103, -56), // boat house
            new Vector3i(-198, 108, -151), // tavern
            new Vector3i(-77, 153, 131) // top of light house
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
