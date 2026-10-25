package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.RitualGame;
import dev.ritualanomalyextraction.asset.GameConfig;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;


public class StartCommand extends AbstractPlayerCommand {

    private final OptionalArg<GameConfig> GAME_CONFIG = this.withOptionalArg(
            "gameConfig", "", GameConfig.ARGUMENT_TYPE
    );

    public StartCommand(String name, String description) {
        super(name, description);
        this.setPermissionGroups("hytale:Adventurer");
    }



    @Override
    protected void execute(@NonNullDecl CommandContext context, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {
        var gameConfig = context.get(GAME_CONFIG);

        if(gameConfig == null){
            new RitualGame();
        }else{
            new RitualGame(gameConfig);

        }
    }
}
