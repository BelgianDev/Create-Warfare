package be.raft.warfare.block;

import be.raft.warfare.block.entity.ShieldCoilBlockEntity;
import be.raft.warfare.registry.WarfareBlockEntities;
import be.raft.warfare.registry.WarfareShapes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ShieldCoilBlock extends AbstractSimpleShaftBlock {
    public ShieldCoilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onNeighborChange(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        if (pos.above().equals(neighbor) || pos.below().equals(neighbor)) {
            this.markShieldDirty(pos, level);
        }

        super.onNeighborChange(state, level, pos, neighbor);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        this.markShieldDirty(pos, world);
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private void markShieldDirty(BlockPos pos, LevelReader level) {
        ShieldCoilBlockEntity be = (ShieldCoilBlockEntity) this.getBlockEntity(level, pos);
        be.markDirty();
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return WarfareShapes.SHIELD_COIL;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis().isVertical();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return WarfareBlockEntities.SHIELD_COIL.get();
    }
}
