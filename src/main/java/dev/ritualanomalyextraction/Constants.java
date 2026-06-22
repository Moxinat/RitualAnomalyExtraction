package dev.ritualanomalyextraction;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;

public class Constants {

    //Initial spawn constants
    public static final World lobbyWorld = Universe.get().getWorld("RAE-Lobby");

    public static final Transform lobbyPos = new Transform(0.0, 80, 0.0);
    public static final Rotation3f lobbyRot = new Rotation3f(0, 0, 0);

    //Control room constants
    public static String miniGameWorldName = "RAE-Minigame";
    public static final World controlRoomWorld = Universe.get().getWorld("RAE-Minigame");
    public static final Transform controlRoomPos = new Transform(32.5, 108.5, -7.5);
    public static final Rotation3f controlRoomRot = new Rotation3f(0, -90, 0);


    //Ritual object spawn constants
    public static final World ritualWorld = Universe.get().getWorld("RAE-Minigame");
    public static final List<String> ritualObjectsWorldID = List.of(
            "Ritual_Active_Pentacle",
            "Ritual_Blood_Effigy",
            "Ritual_Ghost_Candle",
            "Ritual_Soul_Lantern"
    );
    public static final List<Vector3i> ritualObjectSpawnSpots = List.of(
            new Vector3i(46, 134, -14), // church back
            new Vector3i(-24, 107, -213), // windmill
            new Vector3i(35, 105, -159), // ruined house
            new Vector3i(-149, 118, -158), // school under the roof
            new Vector3i(-259, 103, -56), // boat house
            new Vector3i(-198, 108, -151), // tavern
            new Vector3i(-77, 153, 131), // top of lighthouse
            new Vector3i( -106, 113, -90), // outhouse
            new Vector3i(-114, 102, -214) // well
    );

    //Pickup system constants
    public static final List<String> ritualObjectsWorldToItem = List.of(
            "Ritual_Active_Pentacle",
            "Ritual_Blood_Effigy",
            "Ritual_Ghost_Candle",
            "Ritual_Soul_Lantern"
    );

    public static final List<Vector3i> ritualCircleSpawnSpots = List.of(
            new Vector3i(36, 109, -7),
            new Vector3i(32, 109, -11),
            new Vector3i(28, 109, -7),
            new Vector3i(32, 109, -3)
    );

    public static final List<Vector3i> ritualCircleSpawnSpotsRot = List.of(
            new Vector3i(0, 90, 0),
            new Vector3i(0, 180, 0),
            new Vector3i(0, -90, 0),
            new Vector3i(0, 0, 0)
    );

    public static final List<Transform> gameSailorSpawn = List.of(
            new Transform(46, 134, 18), //church cliff
            new Transform(-110, 120, 111), //lighthouse path
            new Transform(-124, 119, 75), //lighthouse end island
            new Transform(-296, 102, -254), //lone island
            new Transform(-37, 105, -207), //windmill path north
            new Transform(129, 104, -184), //windmill island east
            new Transform(-68, 126, -118), //center windmill hill
            new Transform(-248, 103, -66), //center middle dock
            new Transform(-97, 104, -217) //library entrance
    );

    public static final Transform ritualMonsterSpawn = new Transform(-36, 130, 189);
    public static final Rotation3f ritualMonsterSpawnRot = new Rotation3f(0, 90, 0);

    //Pedestal system constants
    public static final String ritualPedestalID = "Ritual_Pedestal";

    //Game time limit in seconds
    public static final Integer gameTimeLimit = 900;

    //Celebration time in seconds
    public static final Integer celebrationTime = 20;
}
