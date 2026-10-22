package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Three red dynamic-light entities per monster.
 *
 * Register monsters on their world thread after NPC spawning.
 * The Ticker updates registered lights once per world tick.
 * The lights are explicitly removed when their monster is unregistered.
 */
public final class MonsterRedLightSystem {

    private static final HytaleLogger LOGGER =
            HytaleLogger.forEnclosingClass();

    private static final int LIGHT_COUNT = 3;
    private static final byte LIGHT_RADIUS = 4;
    private static final byte LIGHT_RED = 15;

    private static final double HEIGHT_OFFSET = 1;
    private static final double NEAR_FORWARD = 2;
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

        if (!monster.isValid()) {
            return;
        }
        if (LIGHTS.containsKey(monster)) {
            return;
        }

        TransformComponent monsterTransform =
                store.getComponent(monster, TransformComponent.getComponentType());
        if (monsterTransform == null) {
            LOGGER.atWarning().log("Cannot spawn Sailor red lights: monster has no TransformComponent.");
            return;
        }

        ModelAsset carrierAsset =
                ModelAsset.getAssetMap().getAsset("LightCarrier");

        if (carrierAsset == null) {
            LOGGER.atWarning().log("Cannot spawn Sailor red lights: LightCarrier ModelAsset not found.");
            return;
        }

        Vector3d[] positions = calculatePositions(monsterTransform);
        List<Ref<EntityStore>> lights = new ArrayList<>(LIGHT_COUNT);

        try {
            for (int i = 0; i < positions.length; i++) {
                Vector3d position = positions[i];
                Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

                Model carrierModel = Model.createScaledModel(carrierAsset, 1F);

                holder.addComponent(
                        ModelComponent.getComponentType(),
                        new ModelComponent(carrierModel)
                );

                holder.addComponent(
                        EntityScaleComponent.getComponentType(),
                        new EntityScaleComponent(1F)
                );

                if (carrierModel.getBoundingBox() != null) {
                    holder.addComponent(
                            BoundingBox.getComponentType(),
                            new BoundingBox(carrierModel.getBoundingBox())
                    );
                }

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

                Ref<EntityStore> light = store.addEntity(holder, AddReason.SPAWN);
                if (light == null) {
                    throw new IllegalStateException("Failed to spawn a Sailor red light.");
                }
                lights.add(light);
            }
        } catch (RuntimeException exception) {
            LOGGER.atSevere().withCause(exception).log("Failed to create Sailor red lights.");
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
            LIGHTS.remove(monster);
            return;
        }

        TransformComponent monsterTransform =
                store.getComponent(monster, TransformComponent.getComponentType());
        if (monsterTransform == null) {
            LOGGER.atWarning().log("Stopping Sailor red lights: monster has no TransformComponent.");
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
                LOGGER.atWarning().log("Stopping Sailor red lights: light entity " + (i + 1) + " has no TransformComponent.");
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


    public static final class SpawnListener extends RefSystem<EntityStore> {

        @Override
        public Query<EntityStore> getQuery() {
            return NPCEntity.getComponentType();
        }

        @Override
        public void onEntityAdded(
                Ref<EntityStore> ref,
                AddReason reason,
                Store<EntityStore> store,
                CommandBuffer<EntityStore> commandBuffer) {

            NPCEntity npc = commandBuffer.getComponent(
                    ref, NPCEntity.getComponentType()
            );

            if (npc == null ||
                    !"RAE_Monster_NPC_Role".equals(npc.getRoleName())) {
                return;
            }

            commandBuffer.run(s -> {
                if (!ref.isValid()) {
                    return;
                }

                MonsterRedLightSystem.registerMonster(ref);
            });
        }

        @Override
        public void onEntityRemove(
                Ref<EntityStore> ref,
                RemoveReason reason,
                Store<EntityStore> store,
                CommandBuffer<EntityStore> commandBuffer) {

            if (LIGHTS.containsKey(ref)) {
                commandBuffer.run(s ->
                        MonsterRedLightSystem.unregisterMonster(ref)
                );
            }
        }
    }


    /** Updates registered monster lights once per world tick. */
    public static final class Ticker extends TickingSystem<EntityStore> {

        @Override
        public void tick(float dt, int systemIndex, Store<EntityStore> store) {
            MonsterRedLightSystem.updateAll(store);
        }
    }
}
