package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import dev.ritualanomalyextraction.RitualGameManager;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class StartCommand extends AbstractCommand{

    RitualGameManager gameManager;

    public StartCommand(String name, String description, RitualGameManager gameManager) {
        super(name, description);
        setAllowsExtraArguments(false);
        this.gameManager = gameManager;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {

        if (!context.isPlayer()) {
            context.sender().sendMessage(Message.raw("Must be a player to execute this command!"));
            return CompletableFuture.completedFuture(null);
        }

        if (gameManager.getState() == RitualGameManager.GameState.RESETTING) {
            context.sendMessage(Message.raw("You cannot do that right now. The map is resetting!"));
            return CompletableFuture.completedFuture(null);
        }

        if (gameManager.getState() == RitualGameManager.GameState.CONTROL_ROOM) {
            return CompletableFuture.completedFuture(null);
        }

        gameManager.onPlayerReady();

        return CompletableFuture.completedFuture(null);
    }

}
