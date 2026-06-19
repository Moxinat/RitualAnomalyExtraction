package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ClearObjectsCommand extends AbstractWorldCommand {

    public ClearObjectsCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext context, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
        world.execute(() -> {
            try {
                RitualObjectSpawnManager.clearSpawnedObjects(world);
                context.sendMessage(Message.raw("Cleared spawned ritual objects."));
            } catch (Exception exception) {
                context.sendMessage(Message.raw(
                        "Failed to clear ritual objects: "
                                + exception.getClass().getSimpleName()
                                + ": "
                                + exception.getMessage()
                ));
            }
        });
    }

}
