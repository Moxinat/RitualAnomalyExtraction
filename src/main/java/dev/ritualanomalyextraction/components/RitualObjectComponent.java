package dev.ritualanomalyextraction.components;

/** Plugin-managed state for a ritual object the player can carry. */
public class RitualObjectComponent {
    private final String  objectId;
    private boolean isCarried   = false;
    private boolean isExtracted = false;

    public RitualObjectComponent(String objectId) { this.objectId = objectId; }

    public String  getObjectId()              { return objectId; }
    public boolean isCarried()                { return isCarried; }
    public void    setCarried(boolean v)      { isCarried = v; }
    public boolean isExtracted()              { return isExtracted; }
    public void    setExtracted(boolean v)    { isExtracted = v; }
}
