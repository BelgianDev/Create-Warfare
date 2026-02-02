package be.raft.warfare.block;

import be.raft.warfare.entity.PlatformSelectionEntity;
import be.raft.warfare.network.S2C.PlatformDirtyCachePacket;
import be.raft.warfare.registry.WarfareShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LaunchPadBlock extends Block implements IWrenchable, ProperWaterloggedBlock {
    public LaunchPadBlock(Properties properties) {
        super(properties);

        registerDefaultState(this.defaultBlockState()
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        this.updateAttachedPlatform(level, pos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        this.updateAttachedPlatform(level, pos);
    }

    private void updateAttachedPlatform(Level level, BlockPos pos) {
        PlatformSelectionEntity platform = this.retrievePlatform(level, pos);
        if (platform == null)
            return;

        platform.markCachedAssemblyAreasDirty();
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, level.getChunkAt(pos).getPos(), new PlatformDirtyCachePacket(platform.getId()));
    }

    public @Nullable PlatformSelectionEntity retrievePlatform(Level level, BlockPos pos) {
        List<PlatformSelectionEntity> candidates = level.getEntitiesOfClass(PlatformSelectionEntity.class, new AABB(pos).inflate(3));
        for (PlatformSelectionEntity candidate : candidates) {
            if (candidate.contains(pos)) {
                return candidate;
            }
        }

        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(WATERLOGGED));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.withWater(this.defaultBlockState(), context);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        this.updateWater(level, state, pos);
        return state;
    }

    @Override
    protected @NotNull FluidState getFluidState(@NotNull BlockState state) {
        return this.fluidState(state);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return WarfareShapes.LAUNCH_PAD;
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.BLOCK;
    }
}
