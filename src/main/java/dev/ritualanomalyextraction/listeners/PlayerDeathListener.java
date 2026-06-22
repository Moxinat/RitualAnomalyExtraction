package dev.ritualanomalyextraction.listeners;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.ui.StatusHud;
import dev.ritualanomalyextraction.utils.InstanceUtil;

import javax.annotation.Nonnull;


public class PlayerDeathListener extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent deathComponent, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        if (store.getExternalData().getWorld().getName().equals(Constants.ritualWorld.getName())) {
            InstanceUtil.teleportPlayerToWorld(playerRef.getReference(), Constants.ritualWorld);
        }
    }
}