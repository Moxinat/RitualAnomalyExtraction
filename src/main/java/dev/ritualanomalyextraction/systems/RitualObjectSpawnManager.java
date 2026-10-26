package dev.ritualanomalyextraction.systems;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import dev.ritualanomalyextraction.Constants;
import dev.ritualanomalyextraction.RitualGame;
import dev.ritualanomalyextraction.RitualGameManager;
import dev.ritualanomalyextraction.utils.WorldUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class RitualObjectSpawnManager {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private RitualObjectSpawnManager() {
    }

    public static void spawnRandomRitualObjects(World world){
        if (world == null) {
            LOGGER.at(Level.WARNING).log("Cannot spawn ritual objects: world is null.");
            return;
        }
        var game = RitualGameManager.gameFromWorldName(world.getName());
        if (game == null) {
            LOGGER.at(Level.WARNING).log(Constants.NOT_A_GAME_WORLD.getRawText());
            return;
        }
        spawnRandomRitualObjects(world,game);
    }
    public static void spawnRandomRitualObjects(@NonNullDecl World world,@NonNullDecl RitualGame game) {
        var ritualItemList = new ArrayList<>(game.getConfig().getItemIdModelIdMap().keySet());

        if (game.getConfig().getPossibleItemLocations().size() < ritualItemList.size()) {
            LOGGER.at(Level.WARNING).log("Not enough ritual object spawn spots configured.");
            return;
        }

        clearSpawnedObjects(world);

        List<Vector3i> shuffledSpots = new ArrayList<>(game.getConfig().getPossibleItemLocations());
        Collections.shuffle(shuffledSpots);
        Collections.shuffle(ritualItemList);

        for (int i = 0; i < 4; i++) {
            String blockId = ritualItemList.get(i);
            var blockType = BlockType.getAssetMap().getAsset(blockId);

            Vector3i pos = shuffledSpots.get(i);

            WorldUtil.setBlock(world, pos, blockType,()->{
                game.addRitualItemPosition(blockType,new Vector3i(pos));
                LOGGER.at(Level.INFO).log("Spawned ritual object '%s' at %s.", blockId, pos);
            },()-> LOGGER.at(Level.WARNING).log("Failed to spawn ritual object '%s' at %s.", blockId, pos));
        }
    }
    public static void clearSpawnedObjects(World world) {
        if (world == null) {
            LOGGER.at(Level.WARNING).log("Cannot clear ritual objects: world is null.");
            return;
        }
        var game = RitualGameManager.gameFromWorldName(world.getName());
        if (game == null) {
            LOGGER.at(Level.WARNING).log(Constants.NOT_A_GAME_WORLD.getRawText());
            return;
        }
        clearSpawnedObjects(world,game);
    }
    public static void clearSpawnedObjects(@NonNullDecl World world,RitualGame game) {
        for (var pair : game.consumeRitualItemPositions()) {
            WorldUtil.setBlock(world, pair.right(),BlockType.EMPTY,null,null);
        }

        LOGGER.at(Level.INFO).log("Cleared spawned ritual objects.");
    }
}
