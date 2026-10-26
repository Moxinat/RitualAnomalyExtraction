package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class RitualObjectsCommands extends AbstractWorldCommand {
    public RitualObjectsCommands(@NonNullDecl String name,@NonNullDecl String description) {
        super(name, description);
        this.addSubCommand(new ClearObjectsCommand("clear","clears ritual object from the game world, if they are present"));
        this.addSubCommand(new SpawnObjectsCommand("spawn","spawns ritual object for the game world, if none are present"));
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
        var game = RitualGameManager.gameFromWorldName(world.getName());
        if(game == null){
            commandContext.sendMessage(Constants.NOT_A_GAME_WORLD);
            return;
        }
        var positions = game.getRitualItemPositions();
        for(var position : positions){
            commandContext.sendMessage(Message.raw(position.left().getId()+": "+position.right().toString()));
        }
    }

    public static class ClearObjectsCommand extends AbstractWorldCommand {

        public ClearObjectsCommand(String name, String description) {
            super(name, description);
        }

        @Override
        protected void execute(@NonNullDecl CommandContext context, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
            world.execute(() -> {
                RitualObjectSpawnManager.clearSpawnedObjects(world);
                context.sendMessage(Message.raw("Tried clearing spawned ritual objects."));

            });
        }
    }
    public static class SpawnObjectsCommand extends AbstractWorldCommand {

        public SpawnObjectsCommand(String name, String description) {
            super(name, description);
            setAllowsExtraArguments(true);
        }

        @Override
        protected void execute(@NonNullDecl CommandContext context, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
            world.execute(() -> {
                RitualObjectSpawnManager.spawnRandomRitualObjects(world);
                context.sendMessage(Message.raw("Tried spawning ritual objects."));
            });
        }

    }


}
