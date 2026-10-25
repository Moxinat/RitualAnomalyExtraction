package dev.ritualanomalyextraction.commands;


import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
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
            RaeConfig.RaeConfigManager config = RitualAnomalyPlugin.getInstance().getConfig();
            boolean current = config.shouldForceTP();
            boolean now = !current;
            commandContext.sendMessage(Message.raw(
                    "Changed ForceTp from "+ current +" to " + now
            ));
            config.setForceTP(now);
        }
    }
}
