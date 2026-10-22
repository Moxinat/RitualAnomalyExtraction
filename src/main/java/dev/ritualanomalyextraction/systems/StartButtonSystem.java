package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.RitualGame;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.logging.Level;

public class StartButtonSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Post> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String START_BUTTON_BLOCK_ID = "button_block";
    private static final String START_BUTTON_PRESSED_ID = "*button_block_state_definitions_pressed";
    private static final String START_BUTTON_PRESSED_ALT_ID = "*button_block_state_definitions_pressedalt";

    public StartButtonSystem() {
        super(UseBlockEvent.Post.class);


        LOGGER.at(Level.INFO).log("StartButtonSystem registered.");
    }

    private static String normalizeId(String id) {
        if (id == null) {
            return "";
        }

        return id.toLowerCase(Locale.ROOT);
    }

    @Override
    public @Nonnull Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void handle(int index,
                       @Nonnull ArchetypeChunk<EntityStore> chunk,
                       @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull UseBlockEvent.Post event) {

        if (event.getInteractionType() != InteractionType.Use) {
            return;
        }

        String blockId = normalizeId(event.getBlockType().getId());

        if (!(START_BUTTON_BLOCK_ID.equals(blockId) || START_BUTTON_PRESSED_ID.equals(blockId) || START_BUTTON_PRESSED_ALT_ID.equals(blockId))) {
            return;
        }

        Ref<EntityStore> entity = event.getContext().getEntity();
        PlayerRef playerRef = store.getComponent(entity, PlayerRef.getComponentType());

        LOGGER.at(Level.INFO).log("Start button used by player '%s'.", playerRef);

        new RitualGame();
    }
}