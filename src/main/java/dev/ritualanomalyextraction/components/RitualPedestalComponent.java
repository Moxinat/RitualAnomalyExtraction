package main.java.dev.ritualanomalyextraction.components;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class RitualPedestalComponent {
    private String placedItemId;
    private Ref<EntityStore> visualItemEntity;

    public boolean hasItem(){
        return placedItemId != null;
    }

    public String getPlacedItemId() {
        return placedItemId;
    }

    public void setPlacedItemId(String itemId) {
        this.placedItemId = itemId;
    }

    public Ref<EntityStore> getVisualItemEntity() {
        return visualItemEntity;
    }

    public void setVisualItemEntity(Ref<EntityStore> visualItemEntity) {
        this.visualItemEntity = visualItemEntity;
    }

    public void clear() {
        this.placedItemId = null;
        this.visualItemEntity = null;
    }
}
