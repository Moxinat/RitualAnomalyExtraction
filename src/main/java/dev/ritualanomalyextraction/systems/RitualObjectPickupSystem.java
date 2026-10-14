package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DropItemEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.utils.WorldUtil;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RitualObjectPickupSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Post> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Map<PlayerRef, String> carriedRitualObjects = new HashMap<>();

    public RitualObjectPickupSystem() {
        super(UseBlockEvent.Post.class);

        LOGGER.at(Level.INFO).log("RitualObjectPickupSystem registered.");
    }

    public static boolean isCarryingRitualObject(PlayerRef player) {
        return carriedRitualObjects.containsKey(player);
    }

    public static String getCarriedRitualObject(PlayerRef player) {
        return carriedRitualObjects.get(player);
    }

    public static void clearCarriedRitualObject(PlayerRef player) {
        String removedObjectId = carriedRitualObjects.remove(player);

        LOGGER.at(Level.INFO).log("Cleared carried ritual object for player '%s'. Removed object: '%s'.", player, removedObjectId);
    }

    private static void setCarriedRitualObject(PlayerRef player, String itemId) {
        carriedRitualObjects.put(player, itemId);

        LOGGER.at(Level.INFO).log("Set carried ritual object for player '%s' to '%s'.", player, itemId);
    }

    private static BlockPosition toBlockPosition(org.joml.Vector3i pos) {
        return new BlockPosition(pos.x, pos.y, pos.z);
    }

    private static void sendAlreadyCarryingMessage(PlayerRef player) {
        player.sendMessage(Message.raw("You are already carrying a ritual object."));
    }

    private static boolean removeWorldRitualObjectBlock(CommandBuffer<EntityStore> commandBuffer,
                                                        Vector3i pos) {
        World world = commandBuffer.getExternalData().getWorld();
        return WorldUtil.setBlock(world,pos, BlockType.EMPTY);
    }

    public static boolean isRitualObjectItemId(String itemId) {
        if (itemId == null) {
            return false;
        }

        return Constants.ritualObjectsWorldToItem.contains(itemId);
    }

    public static void clearCarriedRitualObjectIfMatches(PlayerRef player, String itemId) {
        if (player == null || itemId == null) {
            return;
        }

        String carried = carriedRitualObjects.get(player);

        if (carried != null && carried.equalsIgnoreCase(itemId)) {
            clearCarriedRitualObject(player);
        }
    }

    public static void setCarriedRitualObjectIfEmpty(PlayerRef player, String itemId) {
        if (player == null || itemId == null) {
            return;
        }

        if (!isRitualObjectItemId(itemId)) {
            return;
        }

        if (isCarryingRitualObject(player)) {
            sendAlreadyCarryingMessage(player);
            return;
        }

        setCarriedRitualObject(player, itemId);
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

        String rawWorldBlockId = event.getBlockType().getId();

        if (rawWorldBlockId == null) {
            return;
        }

        if (!Constants.ritualObjectsWorldToItem.contains(rawWorldBlockId)) {
            return;
        }

        Ref<EntityStore> entity = event.getContext().getEntity();

        PlayerRef player = store.getComponent(entity, PlayerRef.getComponentType());

        if (player == null) {
            LOGGER.at(Level.WARNING).log(
                    "Interaction entity '%s' has no PlayerRef component.",
                    entity
            );
            return;
        }

        if (isCarryingRitualObject(player)) {
            sendAlreadyCarryingMessage(player);
            return;
        }

        ItemStackTransaction giveTransaction;

        try {
            giveTransaction = Player.giveItem(
                    new ItemStack(rawWorldBlockId, 1),
                    entity,
                    store
            );
        } catch (Exception exception) {
            LOGGER.at(Level.SEVERE).withCause(exception).log(
                    "Exception while trying to give ritual object item '%s' to player '%s'. This likely means the item asset/json is missing or invalid.",
                    rawWorldBlockId,
                    player
            );
            return;
        }

        if (!giveTransaction.succeeded()) {
            LOGGER.at(Level.WARNING).log(
                    "Failed to give ritual object '%s' to player '%s'. Inventory may be full or item asset may be invalid.",
                    rawWorldBlockId,
                    player
            );
            return;
        }

        Vector3i targetBlock = event.getTargetBlock();

        boolean removedBlock = removeWorldRitualObjectBlock(commandBuffer, targetBlock);

        if (!removedBlock) {
            LOGGER.at(Level.WARNING).log(
                    "Player received ritual object '%s', but world block at %s could not be removed.",
                    rawWorldBlockId,
                    targetBlock
            );
        }

        setCarriedRitualObject(player, rawWorldBlockId);

        BlockPosition pickedUpPosition = toBlockPosition(event.getTargetBlock());

        LOGGER.at(Level.INFO).log(
                "Picked up ritual object '%s' from block '%s' at %s. Current carry map size: %s",
                rawWorldBlockId,
                rawWorldBlockId,
                pickedUpPosition,
                carriedRitualObjects.size()
        );

    }

    public static class DropRequestListener extends EntityEventSystem<EntityStore, DropItemEvent.PlayerRequest> {

        public DropRequestListener() {
            super(DropItemEvent.PlayerRequest.class);
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
                           @Nonnull DropItemEvent.PlayerRequest event) {

            Ref<EntityStore> entity = chunk.getReferenceTo(index);
            PlayerRef player = store.getComponent(entity, PlayerRef.getComponentType());

            if (player == null) {
                return;
            }

            // First simple version:
            // If the player currently carries a ritual object, block all item dropping.
            // This avoids slot lookup/API uncertainty and prevents carry-state bugs.
            if (!RitualObjectPickupSystem.isCarryingRitualObject(player)) {
                return;
            }

            LOGGER.at(Level.INFO).log(
                    "Blocked drop request for player '%s' because they are carrying ritual object '%s'.",
                    player,
                    RitualObjectPickupSystem.getCarriedRitualObject(player)
            );

            event.setCancelled(true);
        }
    }

}
