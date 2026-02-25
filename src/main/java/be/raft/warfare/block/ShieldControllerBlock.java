package be.raft.warfare.block;

import be.raft.warfare.block.entity.ShieldCoilBlockEntity;
import be.raft.warfare.block.entity.ShieldControllerBlockEntity;
import be.raft.warfare.registry.WarfareBlockEntities;
import be.raft.warfare.registry.WarfareShapes;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ShieldControllerBlock extends HorizontalAxisKineticBlock implements IBE<ShieldControllerBlockEntity> {
    public ShieldControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ShieldControllerBlockEntity be = this.getBlockEntity(level, pos);
        if (level.isClientSide)
            player.displayClientMessage(Component.literal("Coils: " + be.getCoilHeight()), true);

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return WarfareShapes.SHIELD_CONTROLLER;
    }

    @Override
    public void onNeighborChange(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockPos neighbor) {
        if (pos.above().equals(neighbor)) {
            this.markShieldDirty(pos, level);
        }

        super.onNeighborChange(state, level, pos, neighbor);
    }

    private void markShieldDirty(BlockPos pos, LevelReader level) {
        ShieldControllerBlockEntity be = this.getBlockEntity(level, pos);
        if (be != null)
            be.markCoilDirty();
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
        return face.getAxis() == state.getValue(HORIZONTAL_AXIS) || face == Direction.UP;
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.FAST;
    }
}
