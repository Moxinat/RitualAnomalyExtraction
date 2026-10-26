package dev.ritualanomalyextraction.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class RitualDisplayEntity implements Component<EntityStore> {
    @NullableDecl
    @Override
    public Component<EntityStore> clone() {
        return new RitualDisplayEntity();
    }
}
