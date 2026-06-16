package dev.ritualanomalyextraction.commands;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.components.FarmSessionComponent;
import dev.ritualanomalyextraction.components.MonsterComponent;
import dev.ritualanomalyextraction.systems.FarmSessionManager;
import dev.ritualanomalyextraction.systems.MonsterSystem;
import dev.ritualanomalyextraction.systems.ScoreTracker;

import javax.annotation.Nonnull;

/**
 * /rae_debug — prints full game state for in-dev testing.
 */
public class DebugCommand extends CommandBase {

    private final RitualGameManager  gameManager;
    private final ScoreTracker       scoreTracker;
    private final FarmSessionManager farmSessionManager;
    private final MonsterSystem      monsterSystem;

    public DebugCommand(RitualGameManager gameManager, ScoreTracker scoreTracker,
                        FarmSessionManager farmSessionManager, MonsterSystem monsterSystem) {
        super("rae_debug", "Print Ritual Anomaly Extraction game state.");
        this.setPermissionGroups("hytale:Adventurer");
        this.gameManager        = gameManager;
        this.scoreTracker       = scoreTracker;
        this.farmSessionManager = farmSessionManager;
        this.monsterSystem      = monsterSystem;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        FarmSessionComponent session = farmSessionManager.getCurrent();
        MonsterComponent monster     = monsterSystem.getState();

        String sessionInfo = session == null ? "none"
            : session.getVariantId() + " real=" + session.isReal()
              + " grabbed=" + session.isObjectGrabbed();

        String status = String.format(
            "[RAE] State=%s | Round=%d | Objects=%d/3 | Score=%d | Mistakes=%d | Session=[%s] | Monster=%s",
            gameManager.getState(),
            gameManager.getRoundNumber(),
            scoreTracker.getObjectsExtracted(),
            scoreTracker.getTotalScore(),
            scoreTracker.getMistakes(),
            sessionInfo,
            monster.getState()
        );
        ctx.sendMessage(Message.raw(status));
    }
}
