package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.components.RitualPedestalComponent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.joml.Vector3i;
import org.joml.Vector3d;

public class PedestalSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Post> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String RITUAL_PEDESTAL_ID = "Ritual_Pedestal";

    public PedestalSystem() {
        super(UseBlockEvent.Post.class);
    }

    private static final Map<BlockPosition, RitualPedestalComponent> pedestalStates = new HashMap<>();

    public static RitualPedestalComponent getOrCreatePedestal(BlockPosition pos) {
        BlockPosition key = new BlockPosition(pos);
        return pedestalStates.computeIfAbsent(key, ignored -> new RitualPedestalComponent());
    }

    public static RitualPedestalComponent getPedestal(BlockPosition pos) {
        return pedestalStates.get(new BlockPosition(pos));
    }

    public static void removePedestal(BlockPosition pos) {
        pedestalStates.remove(new BlockPosition(pos));
    }

    private static BlockPosition toBlockPosition(Vector3i pos) {
        return new BlockPosition(pos.x, pos.y, pos.z);
    }

    private static Vector3d toVisualItemPosition(Vector3i pos) {
        return new Vector3d(pos.x + 0.5, pos.y + 1.2, pos.z + 0.5);
    }

    private static String resolveDisplayModelId(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return "RitualItemTest";
        }

        return switch (itemId) {
            // TODO: Replace these example ritual object IDs with the final item IDs.
            case "ritual_candle" -> "RitualCandleDisplay";
            case "ritual_bone" -> "RitualBoneDisplay";
            case "ritual_orb" -> "RitualOrbDisplay";
            default -> "RitualItemTest";
        };
    }

    private static Ref<EntityStore> spawnVisualItemEntity(CommandBuffer<EntityStore> commandBuffer,
                                                          String itemId,
                                                          Vector3i blockPosition) {

        String modelId = resolveDisplayModelId(itemId);

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(modelId);
        if (modelAsset == null) {
            LOGGER.at(Level.WARNING).log("ModelAsset not found for item '" + itemId + "': " + modelId);
            return null;
        }

        Model model = Model.createScaledModel(modelAsset, 0.45F);

        holder.addComponent(
                TransformComponent.getComponentType(),
                new TransformComponent(toVisualItemPosition(blockPosition), Rotation3f.IDENTITY)
        );

        holder.addComponent(
                PersistentModel.getComponentType(),
                new PersistentModel(model.toReference())
        );

        holder.addComponent(
                ModelComponent.getComponentType(),
                new ModelComponent(model)
        );

        holder.addComponent(
                EntityScaleComponent.getComponentType(),
                new EntityScaleComponent(0.45F)
        );

        Box boundingBox = model.getBoundingBox();
        if (boundingBox != null) {
            holder.addComponent(
                    BoundingBox.getComponentType(),
                    new BoundingBox(boundingBox)
            );
        }

        holder.addComponent(
                NetworkId.getComponentType(),
                new NetworkId(commandBuffer.getExternalData().takeNextNetworkId())
        );

        holder.ensureComponent(UUIDComponent.getComponentType());

        return commandBuffer.addEntity(holder, AddReason.SPAWN);
    }

    private static void removeVisualItemEntity(CommandBuffer<EntityStore> commandBuffer,
                                               RitualPedestalComponent pedestal) {
        Ref<EntityStore> visualEntity = pedestal.getVisualItemEntity();

        if (visualEntity == null) {
            return;
        }

        if (visualEntity.isValid()) {
            commandBuffer.removeEntity(visualEntity, RemoveReason.REMOVE);
        }

        pedestal.setVisualItemEntity(null);
    }

    @Override
    public @Nonnull Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull UseBlockEvent.Post event) {

        String blockId = event.getBlockType().getId();

        if (event.getInteractionType() != InteractionType.Use) {
            return;
        }

        if (!RITUAL_PEDESTAL_ID.equalsIgnoreCase(blockId)){
            return;
        }

        Ref<EntityStore> entity = event.getContext().getEntity();
        PlayerRef player = store.getComponent(entity, PlayerRef.getComponentType());

        if (player == null) {
            return;
        }

        RitualPedestalComponent pedestal = getOrCreatePedestal(toBlockPosition(event.getTargetBlock()));

        if (pedestal.hasItem()) {
            ItemStackTransaction giveTransaction = Player.giveItem(
                new ItemStack(pedestal.getPlacedItemId(), 1),
                entity,
                store
            );
            if (giveTransaction.succeeded()) {
                removeVisualItemEntity(commandBuffer, pedestal);
                pedestal.clear();
            }
            return;
        }

        ItemStack itemInHand = InventoryComponent.getItemInHand(store, entity);
        if (itemInHand == null) {
            return;
        }

        if (itemInHand.isEmpty()) {
            return;
        }

        byte activeSlot = InventoryUtils.getActiveSlot(entity, InventoryComponent.HOTBAR_SECTION_ID, store);
        if (activeSlot < 0) {
            return;
        }

        InventoryComponent.Hotbar hotbar = store.getComponent(entity, InventoryComponent.Hotbar.getComponentType());
        if (hotbar == null) {
            return;
        }

        ItemStackSlotTransaction transaction = hotbar.getInventory()
            .removeItemStackFromSlot(activeSlot, itemInHand, 1);
        if (!transaction.succeeded()) {
            return;
        }

        String itemId = itemInHand.getItemId();
        Ref<EntityStore> visualItemEntity = spawnVisualItemEntity(commandBuffer, itemId, event.getTargetBlock());
        if (visualItemEntity == null) {
            Player.giveItem(new ItemStack(itemId, 1), entity, store);
            return;
        }

        pedestal.setPlacedItemId(itemId);
        pedestal.setVisualItemEntity(visualItemEntity);

        if (RitualObjectPickupSystem.isRitualObjectItemId(itemId)) {
            RitualObjectPickupSystem.clearCarriedRitualObjectIfMatches(player, itemId);
        }

    }

    public static class BreakListener extends EntityEventSystem<EntityStore, BreakBlockEvent> {

        public BreakListener() {
            super(BreakBlockEvent.class);
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
                           @Nonnull BreakBlockEvent event) {
            String blockId = event.getBlockType().getId();
            if (!RITUAL_PEDESTAL_ID.equalsIgnoreCase(blockId)) {
                return;
            }

            RitualPedestalComponent pedestal = PedestalSystem.getPedestal(toBlockPosition(event.getTargetBlock()));
            if (pedestal != null) {
                PedestalSystem.removeVisualItemEntity(commandBuffer, pedestal);
            }
            PedestalSystem.removePedestal(toBlockPosition(event.getTargetBlock()));
        }
    }

}
