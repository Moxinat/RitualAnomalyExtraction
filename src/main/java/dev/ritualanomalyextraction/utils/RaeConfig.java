package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.util.Config;

public class RaeConfig {

    public static final BuilderCodec<RaeConfig> CODEC = BuilderCodec.builder(RaeConfig.class,RaeConfig::new)
            .append(new KeyedCodec<>("SetLobbyAsDefaultWorld",Codec.BOOLEAN),
                    (config, setLobbyAsDefaultWorld) -> config.setLobbyAsDefaultWorld = setLobbyAsDefaultWorld,
                    config -> config.setLobbyAsDefaultWorld ).add()
            .append(new KeyedCodec<>("CreateLobbyUponServerStart",Codec.BOOLEAN),
                    (config, createLobbyUponServerStart) -> config.createLobbyUponServerStart = createLobbyUponServerStart,
                    config -> config.createLobbyUponServerStart ).add()
            .append(new KeyedCodec<>("ForceTP",Codec.BOOLEAN),
                    (config, forceTP) -> config.forceTP = forceTP,
                    config -> config.forceTP ).add()
            .build();


    private boolean setLobbyAsDefaultWorld = true;
    private boolean createLobbyUponServerStart = true;
    private boolean forceTP = true;

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

    public boolean shouldForceTP() {
        return forceTP;
    }

    public void setForceTP(boolean forceTP) {
        this.forceTP = forceTP;
    }

    public static RaeConfigManager getManager(){
        return RaeConfigManager.INSTANCE;
    }

    public static class RaeConfigManager{
        public static RaeConfigManager INSTANCE;

        private final Config<RaeConfig> CONFIG_FILE_MANAGER;
        private final RaeConfig CONFIG;

        public RaeConfigManager(Config<RaeConfig> config){
            INSTANCE = this;
            CONFIG_FILE_MANAGER = config;
            CONFIG = config.get();
        }

        public boolean shouldCreateLobbyUponServerStart() {
            return CONFIG.createLobbyUponServerStart;
        }

        public void setCreateLobbyUponServerStart(boolean createLobbyUponServerStart) {
            CONFIG.createLobbyUponServerStart = createLobbyUponServerStart;
            CONFIG_FILE_MANAGER.save();
        }

        public boolean shouldSetLobbyAsDefaultWorld() {
            return CONFIG.setLobbyAsDefaultWorld;
        }

        public void setSetLobbyAsDefaultWorld(boolean setLobbyAsDefaultWorld) {
            CONFIG.setLobbyAsDefaultWorld = setLobbyAsDefaultWorld;
        }

        public boolean shouldForceTP() {
            return CONFIG.forceTP;
        }

        public void setForceTP(boolean forceTP) {
            CONFIG.forceTP = forceTP;
        }

    }
}
