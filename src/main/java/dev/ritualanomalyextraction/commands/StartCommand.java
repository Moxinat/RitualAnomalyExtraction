package main.java.dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import main.java.dev.ritualanomalyextraction.systems.RitualObjectSpawnManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class SpawnObjectsCommand extends AbstractCommand{

    public SpawnObjectsCommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        String[] args = tokenize(context.getInputString());
        int startIndex = commandArgStartIndex(args);

        if (args.length - startIndex >= 1 && "spawnobjects".equalsIgnoreCase(args[startIndex])) {
            return handleSpawnObjects(context);
        }

        if (args.length - startIndex >= 1 && "clearobjects".equalsIgnoreCase(args[startIndex])) {
            return handleClearObjects(context);
        }

        context.sendMessage(Message.raw("Usage: /rae spawnobjects | /rae clearobjects"));
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handleSpawnObjects(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("This command currently requires a player sender."));
            return CompletableFuture.completedFuture(null);
        }

        World world = playerWorld(context);
        if (world == null) {
            context.sendMessage(Message.raw("Could not resolve player world."));
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        world.execute(() -> {
            try {
                RitualObjectSpawnManager.spawnRandomRitualObjects(world);
                context.sendMessage(Message.raw("Spawned ritual objects: " + RitualObjectSpawnManager.getSpawnedPositions().size()));
                future.complete(null);
            } catch (Exception exception) {
                context.sendMessage(Message.raw("Failed to spawn ritual objects: " + exception.getClass().getSimpleName() + ": " + exception.getMessage()));
                future.complete(null);
            }
        });

        return future;
    }

    private CompletableFuture<Void> handleClearObjects(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("This command currently requires a player sender."));
            return CompletableFuture.completedFuture(null);
        }

        World world = playerWorld(context);
        if (world == null) {
            context.sendMessage(Message.raw("Could not resolve player world."));
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        world.execute(() -> {
            try {
                RitualObjectSpawnManager.clearSpawnedObjects(world);

                context.sendMessage(Message.raw("Cleared spawned ritual objects."));

                future.complete(null);
            } catch (Exception exception) {
                context.sendMessage(Message.raw("Failed to clear ritual objects: " + exception.getClass().getSimpleName() + ": " + exception.getMessage()));
                future.complete(null);
            }
        });
        return future;
    }

    private static @Nullable World playerWorld(CommandContext context) {
        Ref<EntityStore> playerRef = context.senderAsPlayerRef();

        if (playerRef == null || !playerRef.isValid()) {
            return null;
        }

        return playerRef.getStore().getExternalData().getWorld();
    }

    private static String[] tokenize(String input) {
        if (input == null || input.isBlank()) {
            return new String[0];
        }

        return input.trim().split("\\s+");
    }

    private int commandArgStartIndex(String[] args) {
        if (args.length == 0) {
            return 0;
        }

        String commandName = getName();

        if (commandName != null && commandName.equalsIgnoreCase(args[0])) {
            return 1;
        }

        return 0;
    }

}
