package be.raft.warfare.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Doesn't do anything, special simply prevents the placing of it anywhere.
 */
public class ThrusterBlockItem extends BlockItem {
    public ThrusterBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull SoundEvent getPlaceSound(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player entity) {
        return super.getPlaceSound(state, world, pos, entity);
    }
}
