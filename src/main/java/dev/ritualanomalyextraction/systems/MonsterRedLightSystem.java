package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * First draft: three red dynamic-light entities per monster.
 *
 * Call registerMonster on the monster's world thread after NPC spawning.
 * Call updateMonster (or updateAll for the world) from a future tick hook.
 * The lights are owned by the monster, so they are removed with it.
 *
 * No spawn/tick/reset hooks are wired up in this draft.
 */
public final class MonsterRedLightSystem {

    private static final int LIGHT_COUNT = 3;
    private static final byte LIGHT_RADIUS = 3;
    private static final byte LIGHT_RED = 15;

    private static final double HEIGHT_OFFSET = 0.25;
    private static final double NEAR_FORWARD = 1.5;
    private static final double FAR_FORWARD = 4.0;
    private static final double FAR_SIDE = 2.0;

    private static final Map<Ref<EntityStore>, List<Ref<EntityStore>>> LIGHTS =
            new ConcurrentHashMap<>();

    private MonsterRedLightSystem() {
    }

    public static void registerMonster(Ref<EntityStore> monster) {
        if (monster == null) {
            return;
        }

        Store<EntityStore> store = monster.getStore();
        store.assertThread();

        if (!monster.isValid() || LIGHTS.containsKey(monster)) {
            return;
        }

        TransformComponent monsterTransform =
                store.getComponent(monster, TransformComponent.getComponentType());
        if (monsterTransform == null) {
            return;
        }

        Vector3d[] positions = calculatePositions(monsterTransform);
        List<Ref<EntityStore>> lights = new ArrayList<>(LIGHT_COUNT);

        try {
            for (Vector3d position : positions) {
                Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
                holder.addComponent(
                        TransformComponent.getComponentType(),
                        new TransformComponent(position, new Rotation3f())
                );
                holder.addComponent(
                        DynamicLight.getComponentType(),
                        new DynamicLight(new ColorLight(
                                LIGHT_RADIUS, LIGHT_RED, (byte) 0, (byte) 0
                        ))
                );
                holder.addComponent(
                        NetworkId.getComponentType(),
                        new NetworkId(store.getExternalData().takeNextNetworkId())
                );
                holder.addComponent(
                        UUIDComponent.getComponentType(),
                        UUIDComponent.randomUUID()
                );

                // Parent ownership handles cleanup when the NPC is removed.
                Ref<EntityStore> light = store.addEntity(holder, AddReason.SPAWN, monster);
                if (light == null) {
                    throw new IllegalStateException("Failed to spawn a Sailor red light.");
                }
                lights.add(light);
            }
        } catch (RuntimeException exception) {
            removeLights(store, lights);
            throw exception;
        }

        LIGHTS.put(monster, List.copyOf(lights));
    }

    public static void updateMonster(Ref<EntityStore> monster) {
        if (monster == null) {
            return;
        }

        Store<EntityStore> store = monster.getStore();
        store.assertThread();

        List<Ref<EntityStore>> lights = LIGHTS.get(monster);
        if (lights == null) {
            return;
        }

        if (!monster.isValid()) {
            LIGHTS.remove(monster); // Children were removed along with the monster.
            return;
        }

        TransformComponent monsterTransform =
                store.getComponent(monster, TransformComponent.getComponentType());
        if (monsterTransform == null) {
            unregisterMonster(monster);
            return;
        }

        Vector3d[] positions = calculatePositions(monsterTransform);
        for (int i = 0; i < LIGHT_COUNT; i++) {
            Ref<EntityStore> light = lights.get(i);
            if (!light.isValid()) {
                unregisterMonster(monster);
                return;
            }

            TransformComponent lightTransform =
                    store.getComponent(light, TransformComponent.getComponentType());
            if (lightTransform == null) {
                unregisterMonster(monster);
                return;
            }

            lightTransform.setPosition(positions[i]);
        }
    }

    /** To be called once per update on the correct world thread. */
    public static void updateAll(Store<EntityStore> store) {
        store.assertThread();

        for (Ref<EntityStore> monster : List.copyOf(LIGHTS.keySet())) {
            if (monster.getStore() == store) {
                updateMonster(monster);
            }
        }
    }

    public static void unregisterMonster(Ref<EntityStore> monster) {
        if (monster == null) {
            return;
        }

        Store<EntityStore> store = monster.getStore();
        store.assertThread();

        List<Ref<EntityStore>> lights = LIGHTS.remove(monster);
        if (lights != null) {
            removeLights(store, lights);
        }
    }

    private static void removeLights(
            Store<EntityStore> store, List<Ref<EntityStore>> lights) {
        for (Ref<EntityStore> light : lights) {
            if (light != null && light.isValid()) {
                store.removeEntity(light, RemoveReason.REMOVE);
            }
        }
    }

    private static Vector3d[] calculatePositions(TransformComponent monsterTransform) {
        Vector3d base = new Vector3d(monsterTransform.getPosition())
                .add(0.0, HEIGHT_OFFSET, 0.0);

        // Keep lights parallel to the ground; ignore the monster's pitch.
        Vector3d forward = Transform.getDirection(
                0.0f, monsterTransform.getRotation().yaw()
        );
        forward.y = 0.0;
        forward.normalize();

        Vector3d right = new Vector3d(-forward.z, 0.0, forward.x);

        Vector3d near = new Vector3d(base)
                .add(new Vector3d(forward).mul(NEAR_FORWARD));
        Vector3d farLeft = new Vector3d(base)
                .add(new Vector3d(forward).mul(FAR_FORWARD))
                .sub(new Vector3d(right).mul(FAR_SIDE));
        Vector3d farRight = new Vector3d(base)
                .add(new Vector3d(forward).mul(FAR_FORWARD))
                .add(new Vector3d(right).mul(FAR_SIDE));

        return new Vector3d[]{near, farLeft, farRight};
    }
}
