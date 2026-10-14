package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.utils.WorldUtil;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class RitualObjectSpawnManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final List<Vector3i> spawnedPositions = new ArrayList<>();

    private RitualObjectSpawnManager() {
    }

    public static void spawnRandomRitualObjects(World world) {
        if (world == null) {
            LOGGER.at(Level.WARNING).log("Cannot spawn ritual objects: world is null.");
            return;
        }

        if (Constants.ritualObjectSpawnSpots.size() < Constants.ritualObjectsWorldID.size()) {
            LOGGER.at(Level.WARNING).log("Not enough ritual object spawn spots configured.");
            return;
        }

        clearSpawnedObjects(world);

        List<Vector3i> shuffledSpots = new ArrayList<>(Constants.ritualObjectSpawnSpots);
        Collections.shuffle(shuffledSpots);

        for (int i = 0; i < Constants.ritualObjectsWorldID.size(); i++) {
            String blockId = Constants.ritualObjectsWorldID.get(i);
            Vector3i pos = shuffledSpots.get(i);

            boolean success = WorldUtil.setBlock(world, pos, blockId);

            if (success) {
                spawnedPositions.add(new Vector3i(pos));

                LOGGER.at(Level.INFO).log("Spawned ritual object '%s' at %s.", blockId, pos);
            } else {
                LOGGER.at(Level.WARNING).log("Failed to spawn ritual object '%s' at %s.", blockId, pos);
            }
        }
    }

    public static void clearSpawnedObjects(World world) {
        if (world == null) {
            return;
        }
        for (Vector3i pos : spawnedPositions) {
            WorldUtil.setBlock(world, pos,BlockType.EMPTY);
        }


        spawnedPositions.clear();

        LOGGER.at(Level.INFO).log("Cleared spawned ritual objects.");
    }

    public static List<Vector3i> getSpawnedPositions() {
        return List.copyOf(spawnedPositions);
    }
}
