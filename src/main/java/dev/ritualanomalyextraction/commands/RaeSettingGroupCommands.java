package dev.ritualanomalyextraction.commands;


import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.Config;
import dev.ritualanomalyextraction.RitualAnomalyPlugin;
import dev.ritualanomalyextraction.utils.RaeConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;


public class RaeSettingGroupCommands extends AbstractCommandCollection {
    public RaeSettingGroupCommands(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
        this.addSubCommand(new ToggleForceTpCommand("toggleforcetp", "Toggles forced tp"));
    }


    public static class ToggleForceTpCommand extends CommandBase {

        public ToggleForceTpCommand(String name, String description) {
            super(name, description);
        }

        @Override
        protected void executeSync(@NonNullDecl CommandContext commandContext) {
            Config<RaeConfig> config = RitualAnomalyPlugin.getInstance().getConfig();
            RaeConfig raeConfig = config.get();
            boolean current = raeConfig.shouldForceTP();
            boolean now = !current;
            raeConfig.setForceTP(now);
            commandContext.sendMessage(Message.raw(
                    "Changed ForceTp from "+ current +" to " + now
            ));
            config.save();
        }
    }
}
