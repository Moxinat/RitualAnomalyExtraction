package dev.ritualanomalyextraction;

import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;


public class Constants {

    public static final String DEFAULT_LOBBY_NAME = "RAE-Lobby";

    public static final Transform ritualMonsterSpawn = new Transform(-36, 130, 189);
    public static final Rotation3f ritualMonsterSpawnRot = new Rotation3f(0, 90, 0);

    //Pedestal system constants
    public static final String ritualPedestalID = "Ritual_Pedestal";

    //Celebration time in seconds
    public static final Integer celebrationTime = 12;

    //UI Keys
    public static final String KEY_LOBBY = "LOBBY";

    public static final String UI_END_SUCCESS = "RitualState-EndSuccess";
    public static final String UI_END_FAIL = "RitualState-EndFail";
    public static final String UI_IDLE = "RitualState-Idle";
    public static final String UI_READY = "RitualState-Ready";
    public static final String UI_TIMER = "RitualState-Timer";
    public static final String UI_RESET = "RitualState-Reset";

    public static final Message MESSAGE_START1 = Message.raw("Ritual Anomaly Extraction started.");
    public static final Message MESSAGE_START2 = Message.raw("Find the ritual objects and place them on the pedestals.");

    public static final Message NOT_A_GAME_WORLD = Message.raw("World is not associated with a game!");
}
