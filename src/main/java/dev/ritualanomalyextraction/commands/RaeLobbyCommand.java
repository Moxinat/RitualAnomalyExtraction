package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.utils.WorldUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class RaeLobbyCommand extends AbstractPlayerCommand {
    @Nonnull
    private final FlagArg generateLobby = this.withFlagArg("generate", "Generates the lobby, if not present");

    public RaeLobbyCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        World lobbyWorld = RitualGameManager.getLobby();
        boolean lobbyExists = lobbyWorld != null;


        if (commandContext.get(this.generateLobby)) {
            if (lobbyExists) {
                commandContext.sendMessage(Message.raw(
                        "RAE Lobby already exists! Skipping creation..."
                ));
                WorldUtil.teleportPlayerToWorld(lobbyWorld,ref);
                return;
            }
        }

        if (lobbyExists) {
            WorldUtil.teleportPlayerToWorld(lobbyWorld,ref);
            return;
        }

        commandContext.sendMessage(Message.raw(
                "Could not find RAE Lobby world!"
        ));

    }

}
