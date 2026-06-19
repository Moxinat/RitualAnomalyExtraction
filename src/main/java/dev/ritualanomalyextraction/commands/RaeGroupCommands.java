package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RaeGroupCommands extends AbstractCommandCollection {
    public RaeGroupCommands(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
        this.addSubCommand(new SpawnObjectsCommand("spawnobjects","Command to spawn RAE spiritual Objects"));
        this.addSubCommand(new ClearObjectsCommand("clearobjects","Command to despawn RAE spiritual Objects"));
        this.addSubCommand(new RaeLobbyCommand("lobby","Command for the RAE Lobby"));
    }
}
