package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class RaeConfig {

    public static final BuilderCodec<RaeConfig> CODEC = BuilderCodec.builder(RaeConfig.class,RaeConfig::new)
            .append(new KeyedCodec<>("SetLobbyAsDefaultWorld",Codec.BOOLEAN),
                    (config, setLobbyAsDefaultWorld) -> config.setLobbyAsDefaultWorld = setLobbyAsDefaultWorld,
                    config -> config.setLobbyAsDefaultWorld ).add()
            .append(new KeyedCodec<>("CreateLobbyUponServerStart",Codec.BOOLEAN),
                    (config, createLobbyUponServerStart) -> config.createLobbyUponServerStart = createLobbyUponServerStart,
                    config -> config.createLobbyUponServerStart ).add()
            .append(new KeyedCodec<>("LobbyWorldName",Codec.STRING),
                    (config, lobbyWorldName) -> config.lobbyWorldName = lobbyWorldName,
                    config -> config.lobbyWorldName ).add()
            .build();


    private boolean setLobbyAsDefaultWorld = true;
    private boolean createLobbyUponServerStart = true;
    private String lobbyWorldName = InstanceUtil.DEFAULT_LOBBY_NAME;





    public boolean shouldCreateLobbyUponServerStart() {
        return createLobbyUponServerStart;
    }

    public void setCreateLobbyUponServerStart(boolean createLobbyUponServerStart) {
        this.createLobbyUponServerStart = createLobbyUponServerStart;
    }

    public boolean shouldSetLobbyAsDefaultWorld() {
        return setLobbyAsDefaultWorld;
    }

    public void setSetLobbyAsDefaultWorld(boolean setLobbyAsDefaultWorld) {
        this.setLobbyAsDefaultWorld = setLobbyAsDefaultWorld;
    }

    public String getLobbyWorldName() {
        return lobbyWorldName;
    }

    public void setLobbyWorldName(String lobbyWorldName) {
        this.lobbyWorldName = lobbyWorldName;
    }
}
