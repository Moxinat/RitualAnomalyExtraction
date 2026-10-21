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
import java.util.concurrent.atomic.AtomicLong;

/**
 * First draft: three red dynamic-light entities per monster.
 *
 * Call registerMonster on the monster's world thread after NPC spawning.
 * Call registerMonster from the NPC spawn hook on the world thread.
 * The Ticker updates all registered lights once per world tick.
 * The lights are owned by their monster and are removed with it.
 */
public final class MonsterRedLightSystem {

    private static final HytaleLogger LOGGER =
            HytaleLogger.forEnclosingClass();

    // TEMPORARY DEBUG: set to false after diagnosing the red-light issue.
    private static final boolean DEBUG_RED_LIGHT = true;
    private static final AtomicLong DEBUG_TICKS = new AtomicLong();

    private static void debug(String message, Object... args) {
        if (DEBUG_RED_LIGHT) {
            LOGGER.atInfo().log(
                    "%s",
                    "[RedLight] " + String.format(message, args)
            );
        }
    }

    private static final int LIGHT_COUNT = 3;
    private static final byte LIGHT_RADIUS = 8;
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
            debug("registerMonster skipped: null monster reference");
            return;
        }

        Store<EntityStore> store = monster.getStore();
        store.assertThread();

        debug("registerMonster called: valid=%s, alreadyRegistered=%s",
                monster.isValid(), LIGHTS.containsKey(monster));
        if (!monster.isValid()) {
            debug("registerMonster skipped: invalid monster reference");
            return;
        }
        if (LIGHTS.containsKey(monster)) {
            debug("registerMonster skipped: monster already registered");
            return;
        }

        TransformComponent monsterTransform =
                store.getComponent(monster, TransformComponent.getComponentType());
        if (monsterTransform == null) {
            debug("registerMonster skipped: missing monster TransformComponent");
            return;
        }
        debug("Monster transform: position=%s, yaw=%s",
                monsterTransform.getPosition(), monsterTransform.getRotation().yaw());

        ModelAsset carrierAsset =
                ModelAsset.getAssetMap().getAsset("LightCarrier");

        if (carrierAsset == null) {
            debug("ERROR: LightCarrier ModelAsset not found!");
            return;
        }

        debug("LightCarrier ModelAsset loaded successfully");

        Vector3d[] positions = calculatePositions(monsterTransform);
        List<Ref<EntityStore>> lights = new ArrayList<>(LIGHT_COUNT);

        try {
            for (int i = 0; i < positions.length; i++) {
                Vector3d position = positions[i];
                debug("Creating light %d/%d at %s", i + 1, LIGHT_COUNT, position);
                Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();


                Model carrierModel = Model.createUnitScaleModel(carrierAsset);

                holder.addComponent(
                        ModelComponent.getComponentType(),
                        new ModelComponent(carrierModel)
                );

                holder.addComponent(
                        PersistentModel.getComponentType(),
                        new PersistentModel(carrierModel.toReference())
                );

                holder.addComponent(
                        BoundingBox.getComponentType(),
                        new BoundingBox(carrierModel.getBoundingBox())
                );

                holder.addComponent(
                        PropComponent.getComponentType(),
                        PropComponent.get()
                );


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
                debug("Light %d spawn result: nonNull=%s, valid=%s",
                        i + 1, light != null, light != null && light.isValid());
                if (light == null) {
                    throw new IllegalStateException("Failed to spawn a Sailor red light.");
                }
                lights.add(light);
            }
        } catch (RuntimeException exception) {
            debug("Light creation FAILED: %s: %s",
                    exception.getClass().getSimpleName(), exception.getMessage());
            removeLights(store, lights);
            throw exception;
        }

        LIGHTS.put(monster, List.copyOf(lights));
        debug("Monster registered with %d light references", lights.size());
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
            debug("Update stopped: monster no longer valid");
            LIGHTS.remove(monster); // Children were removed along with the monster.
            return;
        }

        TransformComponent monsterTransform =
                store.getComponent(monster, TransformComponent.getComponentType());
        if (monsterTransform == null) {
            debug("Update stopped: monster TransformComponent missing");
            unregisterMonster(monster);
            return;
        }

        Vector3d[] positions = calculatePositions(monsterTransform);
        boolean logPositions = DEBUG_RED_LIGHT && DEBUG_TICKS.get() % 100 == 0;
        if (logPositions) {
            debug("Updating monster at %s; active lights=%d",
                    monsterTransform.getPosition(), lights.size());
        }
        for (int i = 0; i < LIGHT_COUNT; i++) {
            Ref<EntityStore> light = lights.get(i);
            if (!light.isValid()) {
                debug("Update stopped: light %d reference invalid", i + 1);
                unregisterMonster(monster);
                return;
            }

            TransformComponent lightTransform =
                    store.getComponent(light, TransformComponent.getComponentType());
            if (lightTransform == null) {
                debug("Update stopped: light %d missing TransformComponent", i + 1);
                unregisterMonster(monster);
                return;
            }

            lightTransform.setPosition(positions[i]);
            if (logPositions) {
                debug("Light %d position updated to %s", i + 1, positions[i]);
            }
        }
    }

    /** To be called once per update on the correct world thread. */
    public static void updateAll(Store<EntityStore> store) {
        store.assertThread();

        if (DEBUG_RED_LIGHT && DEBUG_TICKS.incrementAndGet() % 100 == 0) {
            long worldMonsters = LIGHTS.keySet().stream()
                    .filter(monster -> monster.getStore() == store).count();
            debug("Ticker running: registered monsters=%d, this world=%d",
                    LIGHTS.size(), worldMonsters);
        }

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
            debug("Unregistering monster; removing %d lights", lights.size());
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

            debug("Sailor added to ECS: valid=%s", ref.isValid());

            commandBuffer.run(s -> {
                debug("Deferred spawn callback running: valid=%s", ref.isValid());
                if (!ref.isValid()) {
                    debug("ERROR: Ref invalid after ECS add");
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
                debug(
                        "Tracked monster removed: reason=%s, valid=%s",
                        reason,
                        ref.isValid()
                );
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
