package be.raft.warfare.rocket.platform;

import be.raft.warfare.entity.PlatformSelectionEntity;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class RocketPlatformSelectionHelper {
    private static final int MAX_LOOK_UP_RANGE = 16;

    public static List<PlatformSelectionEntity> searchForRocketPlatforms(LevelAccessor level, BlockPos start, BlockPos end) {
        return level.getEntitiesOfClass(PlatformSelectionEntity.class, SuperGlueEntity.span(start, end).inflate(MAX_LOOK_UP_RANGE));
    }

    public static boolean rocketPlatformIntersects(LevelAccessor accessor, BlockPos start, BlockPos end) {
        List<PlatformSelectionEntity> platforms = searchForRocketPlatforms(accessor, start, end);
        AABB currentBB = SuperGlueEntity.span(start, end);

        for (PlatformSelectionEntity platform : platforms) {
            if (platform.getBoundingBox().intersects(currentBB))
                return true;
        }

        return false;
    }
}
