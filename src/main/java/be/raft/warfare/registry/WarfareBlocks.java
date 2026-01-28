package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.block.MechanicalTurretBlock;
import be.raft.warfare.block.RocketControllerBlock;
import com.simibubi.create.foundation.data.*;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class WarfareBlocks {
    private static final CreateRegistrate REGISTRATE = CreateWarfare.REGISTRATE;

    public static final BlockEntry<MechanicalTurretBlock> MECHANICAL_TURRET = REGISTRATE.block("mechanical_turret", MechanicalTurretBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(prop -> prop.mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(TagGen.axeOrPickaxe())
            .blockstate((ctx, provider) -> provider.getVariantBuilder(ctx.get())
                    .forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(AssetLookup.partialBaseModel(ctx, provider))
                            .rotationX(state.getValue(MechanicalTurretBlock.CEILING) ? 180 : 0)
                            .build()
                    )
            )
            .item(BlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<RocketControllerBlock> ROCKET_CONTROLLER = REGISTRATE.block("rocket_controller", RocketControllerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(prop -> prop.mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(TagGen.axeOrPickaxe())
            .blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(BlockItem::new)
            .transform(customItemModel())
            .register();

    public static void register() {}
}
