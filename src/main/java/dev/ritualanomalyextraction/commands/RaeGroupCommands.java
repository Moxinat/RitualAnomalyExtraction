package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RaeGroupCommands extends AbstractCommandCollection {
    public RaeGroupCommands(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
        this.addSubCommand(new SpawnObjectsCommand("spawnobjects","Command to spawn RAE spiritual Objects"));
        this.addSubCommand(new ClearObjectsCommand("clearobjects","Command to despawn RAE spiritual Objects"));
        this.addSubCommand(new RaeLobbyCommand("lobby","Command for the RAE Lobby"));
        this.addSubCommand(new RaeRitualGroupCommands("ritual","Commands for triggering the ritual animation"));
        this.addSubCommand(new RaeSettingGroupCommands("setting","Commands for changing settings"));
        this.addSubCommand(new clearhud("clearhud","clearhud"));
    }

    private static class clearhud extends AbstractPlayerCommand {

        public clearhud(@NonNullDecl String name, @NonNullDecl String description) {
            super(name, description);
        }

        @Override
        protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
            Player player = store.getComponent(ref, Player.getComponentType());
            assert player != null;
            player.getHudManager().resetHud(playerRef);
        }
    }
}
