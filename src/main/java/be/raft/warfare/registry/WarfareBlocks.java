package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.block.*;
import be.raft.warfare.item.RocketControllerBlockItem;
import be.raft.warfare.item.ThrusterBlockItem;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.foundation.data.*;
import com.simibubi.create.infrastructure.config.CStress;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

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
            .blockstate((ctx, prov) -> prov.getVariantBuilder(ctx.get())
                    .forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(state.getValue(RocketControllerBlock.NO_DYNAMIC_BULB) ? prov.models().getExistingFile(prov.modLoc("block/rocket_controller/item")) : AssetLookup.partialBaseModel(ctx, prov))
                            .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360)
                            .build()
                    )
            )
            .item(RocketControllerBlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<LaunchPadBlock> LAUNCH_PAD = REGISTRATE.block("launch_pad", LaunchPadBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(prop -> prop.mapColor(MapColor.TERRACOTTA_GRAY).noOcclusion())
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.standardModel(ctx, prov)))
            .item(BlockItem::new).build()
            .register();

    public static final BlockEntry<ThrusterBlock> THRUSTER = REGISTRATE.block("thruster", ThrusterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(prop -> prop.mapColor(MapColor.TERRACOTTA_GRAY).noOcclusion())
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(ThrusterBlockItem::new)
            .transform(customItemModel())
            .register();

    // Shield
    public static final BlockEntry<ShieldCoilBlock> SHIELD_COIL = REGISTRATE.block("shield_coil", ShieldCoilBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(TagGen.pickaxeOnly())
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .item(BlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<ShieldControllerBlock> SHIELD_CONTROLLER = REGISTRATE.block("shield_controller", ShieldControllerBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(axeOrPickaxe())
            .blockstate(BlockStateGen.horizontalAxisBlockProvider(true))
            .item()
            .transform(customItemModel())
            .register();

    public static void register() {}
}
