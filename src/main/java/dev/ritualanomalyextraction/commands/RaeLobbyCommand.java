package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.utils.WorldUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class RaeLobbyCommand extends AbstractPlayerCommand {
    @Nonnull
    private final FlagArg generateLobby = this.withFlagArg("generate", "Generates the lobby, if not present");
    @Nonnull
    private final OptionalArg<World> deleteLobby = this.withOptionalArg("deleteAndResetTo", "Deletes the lobby, if present and resets the default world", ArgTypes.WORLD);

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
        World newDefaultWorld = commandContext.get(this.deleteLobby);
        if (newDefaultWorld != null && lobbyExists){
            if (Universe.get().getWorlds().size() == 1) {
                commandContext.sendMessage(Message.raw(
                        "Only one world exists! Aborting..."
                ));
            } else if (lobbyWorld == newDefaultWorld) {
                commandContext.sendMessage(Message.raw(
                        "The new default world is the same as the lobby! Aborting..."
                ));
                return;
            } else {
                HytaleServer.get().getConfig().getDefaults().setWorld(newDefaultWorld.getName());

                String lobbyName = lobbyWorld.getName();
                lobbyWorld.getWorldConfig().setDeleteOnRemove(true);
                lobbyWorld.getWorldConfig().setDeleteOnUniverseStart(true);
                Universe.get().removeWorld(lobbyName);
                commandContext.sendMessage(Message.translation("server.universe.removeworld.success").param("worldName", lobbyName));
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
