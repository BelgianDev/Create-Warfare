package be.raft.warfare.block;

import be.raft.warfare.block.entity.ShieldControllerBlockEntity;
import be.raft.warfare.registry.WarfareBlockEntities;
import be.raft.warfare.registry.WarfareShapes;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ShieldControllerBlock extends HorizontalAxisKineticBlock implements IBE<ShieldControllerBlockEntity> {
    public ShieldControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis preferredAxis = getPreferredHorizontalAxis(context);
        if (preferredAxis != null)
            return this.defaultBlockState().setValue(HORIZONTAL_AXIS, preferredAxis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);

        return this.defaultBlockState().setValue(HORIZONTAL_AXIS, context.getHorizontalDirection().getClockWise().getAxis());
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return WarfareShapes.SHIELD_CONTROLLER;
    }

    @Override
    public Class<ShieldControllerBlockEntity> getBlockEntityClass() {
        return ShieldControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ShieldControllerBlockEntity> getBlockEntityType() {
        return WarfareBlockEntities.SHIELD_CONTROLLER.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() != state.getValue(HORIZONTAL_AXIS) && face != Direction.DOWN;
    }
}
