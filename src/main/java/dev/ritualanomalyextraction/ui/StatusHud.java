package main.java.dev.ritualanomalyextraction.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import main.java.dev.ritualanomalyextraction.Constants;

import javax.annotation.Nonnull;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerHud extends CustomUIHud {

    public TimerHud(@Nonnull PlayerRef playerRef, @Nonnull String key) {
        super(playerRef, key);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/gameTimer.ui");
        if (getKey().equals("spawn")) {
            uiCommandBuilder.set("#TextField.", Message.raw("To start the game type /start"));
        }
    }

}
