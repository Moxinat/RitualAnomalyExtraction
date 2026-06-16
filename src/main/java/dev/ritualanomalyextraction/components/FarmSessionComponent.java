package dev.ritualanomalyextraction.components;

/**
 * Holds the state for a single farm visit round.
 * Created by FarmSessionManager at the start of each round; discarded after ROUND_RESULT.
 */
public class FarmSessionComponent {

    private final String  variantId;
    private final boolean isReal;
    private final String  objectId;   // null when isReal == false
    private boolean objectGrabbed = false;
    private boolean decisionMade  = false;

    public FarmSessionComponent(String variantId, boolean isReal, String objectId) {
        this.variantId = variantId;
        this.isReal    = isReal;
        this.objectId  = objectId;
    }

    public String  getVariantId()           { return variantId; }
    public boolean isReal()                 { return isReal; }
    public String  getObjectId()            { return objectId; }
    public boolean isObjectGrabbed()        { return objectGrabbed; }
    public void    setObjectGrabbed(boolean v){ objectGrabbed = v; }
    public boolean isDecisionMade()         { return decisionMade; }
    public void    setDecisionMade(boolean v){ decisionMade = v; }
}
