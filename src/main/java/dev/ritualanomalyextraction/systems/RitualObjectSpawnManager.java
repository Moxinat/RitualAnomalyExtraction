package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class RitualObjectSpawnManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final List<String> RITUAL_WORLD_OBJECTS = List.of(
            "ritual_skull_world",
            "ritual_lantern_world",
            "ritual_effigy_world"
    );

    private static final List<Vector3i> POSSIBLE_SPAWN_SPOTS = List.of(
            // TODO add actual coordinates
            new Vector3i(-36, 130, 189),
            new Vector3i(-38, 130, 195),
            new Vector3i(-43, 130, 199),
            new Vector3i(-49, 130, 199),
            new Vector3i(-54, 130, 195),
            new Vector3i(-56, 130, 189),
            new Vector3i(-54, 130, 183),
            new Vector3i(-49, 130, 179),
            new Vector3i(-43, 130, 179),
            new Vector3i(-38, 130, 183)
    );

    private static final List<Vector3i> spawnedPositions = new ArrayList<>();

    private RitualObjectSpawnManager() {
    }

    public static void spawnRandomRitualObjects(World world) {
        if (world == null) {
            LOGGER.at(Level.WARNING).log("Cannot spawn ritual objects: world is null.");
            return;
        }

        if (POSSIBLE_SPAWN_SPOTS.size() < RITUAL_WORLD_OBJECTS.size()) {
            LOGGER.at(Level.WARNING).log("Not enough ritual object spawn spots configured.");
            return;
        }

        clearSpawnedObjects(world);

        List<Vector3i> shuffledSpots = new ArrayList<>(POSSIBLE_SPAWN_SPOTS);
        Collections.shuffle(shuffledSpots);

        for (int i = 0; i < RITUAL_WORLD_OBJECTS.size(); i++) {
            String blockId = RITUAL_WORLD_OBJECTS.get(i);
            Vector3i pos = shuffledSpots.get(i);

            boolean success = setBlock(world, pos, blockId);

            if (success) {
                spawnedPositions.add(new Vector3i(pos));

                LOGGER.at(Level.INFO).log(
                        "Spawned ritual object '%s' at %s.",
                        blockId,
                        pos
                );
            } else {
                LOGGER.at(Level.WARNING).log(
                        "Failed to spawn ritual object '%s' at %s.",
                        blockId,
                        pos
                );
            }
        }
    }

    public static void clearSpawnedObjects(World world) {
        if (world == null) {
            return;
        }

        for (Vector3i pos : spawnedPositions) {
            clearBlock(world, pos);
        }

        spawnedPositions.clear();

        LOGGER.at(Level.INFO).log("Cleared spawned ritual objects.");
    }

    private static boolean setBlock(World world, Vector3i pos, String blockId) {
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));

        if (chunk == null) {
            LOGGER.at(Level.WARNING).log(
                    "Cannot set block '%s' at %s: chunk is null.",
                    blockId,
                    pos
            );
            return false;
        }

        return chunk.setBlock(pos.x, pos.y, pos.z, blockId);
    }

    private static boolean clearBlock(World world, Vector3i pos) {
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));

        if (chunk == null) {
            LOGGER.at(Level.WARNING).log(
                    "Cannot clear block at %s: chunk is null.",
                    pos
            );
            return false;
        }

        return chunk.setBlock(pos.x, pos.y, pos.z, BlockType.EMPTY);
    }

    public static List<Vector3i> getSpawnedPositions() {
        return List.copyOf(spawnedPositions);
    }
}
