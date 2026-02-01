package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.Create;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class WarfarePartialModels {
    public static final PartialModel

            // TURRET
            TURRET_COG = AllPartialModels.ARM_COG,
            TURRET_BASE = AllPartialModels.ARM_BASE,
            TURRET_ARM = block("mechanical_turret/arm"),
            TURRET_HEAD = block("mechanical_turret/head"),
            TURRET_NOZZLE = block("mechanical_turret/nozzle"),

            // Vertical Bulb
            VERTICAL_BULB = block("vertical_bulb/bulb"),
            VERTICAL_BULB_GLOW = block("vertical_bulb/glow"),

            // Overlay
            ASSEMBLY_OVERLAY = block("overlay/assembling");



    // Some machines and contraption use the same base elements of the create mod
    // To prevent having redundant models be loaded into memory, lets simply use the ones of the create mod.
    private static PartialModel createBlock(String path) {
        return PartialModel.of(Create.asResource("block/" + path));
    }

    private static PartialModel block(String path) {
        return PartialModel.of(CreateWarfare.asLoc("block/" + path));
    }

    public static void prepare() {}
}
