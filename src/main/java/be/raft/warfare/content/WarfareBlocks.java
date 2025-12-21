package be.raft.warfare.content;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.content.block.TurretBlock;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlock;
import com.simibubi.create.foundation.data.*;
import com.simibubi.create.infrastructure.config.CStress;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class WarfareBlocks {
    private static final CreateRegistrate REGISTRATE = CreateWarfare.REGISTRATE;

    public static final BlockEntry<TurretBlock> MECHANICAL_TURRET = REGISTRATE.block("mechanical_turret", TurretBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(prop -> prop.mapColor(MapColor.TERRACOTTA_YELLOW))
            .transform(TagGen.axeOrPickaxe())
            .blockstate((ctx, provider) -> provider.getVariantBuilder(ctx.get())
                    .forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(AssetLookup.partialBaseModel(ctx, provider))
                            .rotationX(state.getValue(ArmBlock.CEILING) ? 180 : 0)
                            .build()
                    )
            )
            .item(BlockItem::new)
            .transform(customItemModel())
            .register();

    public static void register() {}
}
