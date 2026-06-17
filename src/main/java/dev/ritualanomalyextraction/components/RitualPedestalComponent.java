package dev.ritualanomalyextraction.components;

public class RitualPedestalComponent {
    private String placedItemId;

    public boolean hasItem(){
        return placedItemId != null;
    }

    public String getPlacedItemId() {
        return placedItemId;
    }

    public void setPlacedItemId(String itemId) {
        this.placedItemId = itemId;
    }

    public void clear() {
        this.placedItemId = null;
    }
}
