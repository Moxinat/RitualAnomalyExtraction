package dev.ritualanomalyextraction.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.ritualanomalyextraction.Constants;

import javax.annotation.Nonnull;

//Rework when we have Noesis
@Deprecated
public class DeprecatedStatusHud extends CustomUIHud {

    private int gameTime;

    public DeprecatedStatusHud(@Nonnull PlayerRef playerRef, @Nonnull String key, int gameTime) {
        super(playerRef, key);
        this.gameTime = gameTime;
    }

    //TODO rework this
    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {

        if (getKey().equals(Constants.UI_END_SUCCESS)) {
            uiCommandBuilder.append("Hud/endScreenUI.ui");
            uiCommandBuilder.set("#Title.Text", "RITUAL COMPLETE");
            uiCommandBuilder.set("#Title.Style.TextColor", "#33ff66");
            uiCommandBuilder.set("#Subtitle.Text", "The last offering has been accepted.");
            uiCommandBuilder.set("#Hint.Text", "You survived the ritual.");
            return;
        }

        if (getKey().equals(Constants.UI_END_FAIL)) {
            uiCommandBuilder.append("Hud/endScreenUI.ui");
            uiCommandBuilder.set("#Title.Text", "RITUAL FAILED");
            uiCommandBuilder.set("#Title.Style.TextColor", "#ff3333");
            uiCommandBuilder.set("#Subtitle.Text", "The deep has claimed the offering.");
            uiCommandBuilder.set("#Hint.Text", "No light remains beyond the mist.");
            return;
        }

        uiCommandBuilder.append("Hud/statusUI.ui");

        if (getKey().equals(Constants.UI_IDLE)) {
            uiCommandBuilder.set("#TextField.Text", "To start the game type /start or press the button");
            uiCommandBuilder.set("#TextField.Style.TextColor", "#ffffff");
        }
        if (getKey().equals(Constants.UI_TIMER)) {
            int seconds = gameTime % 60;
            int minutes = gameTime / 60;
            uiCommandBuilder.set("#TextField.Text", String.format("%d:%02d", minutes, seconds));
            if (minutes <= 0) {
                uiCommandBuilder.set("#TextField.Style.TextColor", "#ff3333");
            } else {
                uiCommandBuilder.set("#TextField.Style.TextColor", "#ffffff");
            }
        }
        //isn't this unreachable?
        if (getKey().equals(Constants.UI_END_FAIL)) {
            uiCommandBuilder.set("#TextField.Text", "The ritual is broken... now are you.");
            uiCommandBuilder.set("#TextField.Style.TextColor", "#ff3333");
        }
        //isn't this unreachable?
        if (getKey().equals(Constants.UI_END_SUCCESS)) {
            uiCommandBuilder.set("#TextField.Text", "The ritual has been complete!");
            uiCommandBuilder.set("#TextField.Style.TextColor", "#33ff66");
        }

        if (getKey().equals(Constants.UI_RESET)) {
            uiCommandBuilder.set("#TextField.Text", "Resetting the game... Stand by");
            uiCommandBuilder.set("#TextField.Style.TextColor", "#ff3333");
        }
        if (getKey().equals(Constants.UI_READY)) {
            uiCommandBuilder.set("#TextField.Text", "Game is ready: /start");
            uiCommandBuilder.set("#TextField.Style.TextColor", "#ffffff");
        }
    }

}
