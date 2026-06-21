package dev.ritualanomalyextraction.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.RitualGameManager;

import javax.annotation.Nonnull;
import java.awt.*;

public class StatusHud extends CustomUIHud {

    private int gameTime;

    public StatusHud(@Nonnull PlayerRef playerRef, @Nonnull String key, RitualGameManager gameTime) {
        super(playerRef, key);
        this.gameTime = gameTime.getGameTimeLimit();
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Hud/statusUI.ui");
        if (getKey().equals("spawn")) {
            uiCommandBuilder.set("#TextField.Text", "To start the game type /start");
            uiCommandBuilder.set("#TextField.Style.TextColor", "#ffffff");
        }
        if (getKey().equals("gameTimer")) {
            int seconds = gameTime % 60;
            int minutes = gameTime / 60;
            uiCommandBuilder.set("#TextField.Text", String.format("%d:%02d", minutes, seconds));
            uiCommandBuilder.set("#TextField.Style.TextColor", "#ffffff");
        }
        if (getKey().equals("endFail")) {
            uiCommandBuilder.set("#TextField.Text", "You failed!");
            uiCommandBuilder.set("#TextField.Style.TextColor", "#ff3333");
        }
        if (getKey().equals("endSuccess")) {
            uiCommandBuilder.set("#TextField.Text", "You won!");
            uiCommandBuilder.set("#TextField.Style.TextColor", "##33ff66");
        }
    }

}
