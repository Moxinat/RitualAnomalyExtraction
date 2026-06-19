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
            .build();


    private boolean setLobbyAsDefaultWorld = true;
    private boolean createLobbyUponServerStart = true;





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
}
