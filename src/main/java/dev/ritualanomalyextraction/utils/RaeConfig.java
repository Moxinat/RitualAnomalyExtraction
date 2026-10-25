package dev.ritualanomalyextraction.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.util.Config;

//is this still needed?
public class RaeConfig {

    public static final BuilderCodec<RaeConfig> CODEC = BuilderCodec.builder(RaeConfig.class,RaeConfig::new)
            .append(new KeyedCodec<>("SetLobbyAsDefaultWorld",Codec.BOOLEAN),
                    (config, setLobbyAsDefaultWorld) -> config.setLobbyAsDefaultWorld = setLobbyAsDefaultWorld,
                    config -> config.setLobbyAsDefaultWorld ).add()
            .append(new KeyedCodec<>("ForceTP",Codec.BOOLEAN),
                    (config, forceTP) -> config.forceTP = forceTP,
                    config -> config.forceTP ).add()
            .build();


    private boolean setLobbyAsDefaultWorld = true;
    private boolean forceTP = true;

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

        public boolean shouldSetLobbyAsDefaultWorld() {
            return CONFIG.setLobbyAsDefaultWorld;
        }

        public void setSetLobbyAsDefaultWorld(boolean setLobbyAsDefaultWorld) {
            CONFIG.setLobbyAsDefaultWorld = setLobbyAsDefaultWorld;
            CONFIG_FILE_MANAGER.save();
        }

        public boolean shouldForceTP() {
            return CONFIG.forceTP;
        }

        public void setForceTP(boolean forceTP) {
            CONFIG.forceTP = forceTP;
            CONFIG_FILE_MANAGER.save();
        }

    }
}
