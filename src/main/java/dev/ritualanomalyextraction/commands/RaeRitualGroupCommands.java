package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.systems.RitualAnimationManager;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.TimeUnit;

public class RaeRitualGroupCommands extends AbstractCommandCollection {
    public RaeRitualGroupCommands(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
        this.addSubCommand(new PlayAnimationCommand("play","Start the ritual animation"));
        this.addSubCommand(new PlayAndResetAnimationCommand("playandreset", "Resets the ritual animation"));
        this.addSubCommand(new ResetAnimationCommand("reset", "Resets the ritual animation"));
    }


    public static class ResetAnimationCommand extends AbstractWorldCommand {

        public ResetAnimationCommand(String name, String description) {
            super(name, description);
        }

        @Override
        protected void execute(@NonNullDecl CommandContext context, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
            RitualAnimationManager.reset(world);
        }
    }

    public static class PlayAnimationCommand extends AbstractWorldCommand {

        public PlayAnimationCommand(String name, String description) {
            super(name, description);
        }

        @Override
        protected void execute(@NonNullDecl CommandContext context, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
            RitualAnimationManager.play(world);
        }
    }

    public static class PlayAndResetAnimationCommand extends AbstractWorldCommand {

        public PlayAndResetAnimationCommand(String name, String description) {
            super(name, description);
        }

        @Override
        protected void execute(@NonNullDecl CommandContext context, @NonNullDecl World world, @NonNullDecl Store<EntityStore> store) {
            RitualAnimationManager.play(world);
            HytaleServer.SCHEDULED_EXECUTOR.schedule(()->RitualAnimationManager.reset(world),16, TimeUnit.SECONDS);
        }
    }
}
