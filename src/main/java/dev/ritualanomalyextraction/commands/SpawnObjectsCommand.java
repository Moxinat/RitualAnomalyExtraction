package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.asset.GameConfig;
import dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Objects;


public class SpawnObjectsCommand extends AbstractWorldCommand {

    public SpawnObjectsCommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext context, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
        world.execute(() -> {
            try {
                RitualObjectSpawnManager.spawnRandomRitualObjects(world, Objects.requireNonNull(GameConfig.getAssetMap().getAsset("Default")));
                context.sendMessage(Message.raw("Spawned ritual objects: " + RitualObjectSpawnManager.getSpawnedPositions().size()));
            } catch (Exception exception) {
                context.sendMessage(Message.raw("Failed to spawn ritual objects: " + exception.getClass().getSimpleName() + ": " + exception.getMessage()));
            }
        });
    }

}
