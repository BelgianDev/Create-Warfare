package be.raft.warfare.registry;

import com.simibubi.create.AllShapes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WarfareShapes {
    public static final VoxelShape
            TURRET = shape(2, 0, 2, 14, 10, 14).add(3, 0, 3, 13, 14, 13)
            .add(0, 0, 0, 16, 6, 16)
            .build(),

            TURRET_CEILLING = shape(2, 6, 2, 14, 16, 14).add(3, 2, 3, 13, 16, 13)
                    .add(0, 10, 0, 16, 16, 16)
                    .build(),

            ROCKET_CONTROLLER = shape(0, 0, 0, 16, 2, 16).add(1, 0, 1, 15, 13, 15)
                    .build(),

            LAUNCH_PAD = shape(0, 0, 0, 16, 7, 16)
                    .build(),

            SHIELD_COIL = shape(3, 1, 3, 13, 15, 13)
                    .add(5, 0, 0, 11, 16, 2)
                    .add(0, 0, 5, 2, 16, 11)
                    .add(5, 0, 14, 11, 16, 16)
                    .add(14, 0, 5, 16, 16, 11)
                    .build(),

            SHIELD_CONTROLLER = shape(0, 0, 0, 16, 4, 16)
                    .add(1, 5, 1, 15, 10, 15)
                    .add(0, 10, 0, 16, 16, 16)
                    .build();


    private static AllShapes.Builder shape(VoxelShape shape) {
        return new AllShapes.Builder(shape);
    }

    private static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }
}
