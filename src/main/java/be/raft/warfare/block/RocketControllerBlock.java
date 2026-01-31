package be.raft.warfare.block;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.block.entity.RocketControllerBlockEntity;
import be.raft.warfare.inventory.screen.RocketControllerAssemblyScreen;
import be.raft.warfare.inventory.screen.RocketControllerScreen;
import be.raft.warfare.registry.WarfareBlockEntities;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class RocketControllerBlock extends HorizontalDirectionalBlock implements IBE<RocketControllerBlockEntity>, ProperWaterloggedBlock, IWrenchable {
    public static final MapCodec<RocketControllerBlock> CODEC = simpleCodec(RocketControllerBlock::new);
    public static final BooleanProperty ASSEMBLING = BooleanProperty.create("assembling");
    public static final BooleanProperty NO_DYNAMIC_BULB = BooleanProperty.create("no_dynamic_bulb");

    public RocketControllerBlock(Properties properties) {
        super(properties);

        registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ASSEMBLING, false)
                .setValue(WATERLOGGED, false)
                .setValue(NO_DYNAMIC_BULB, false)
        );
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (player == null || player.isShiftKeyDown() || AllItems.WRENCH.isIn(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> withBlockEntityDo(level, pos, be -> displayScreen(be, player)));
        return ItemInteractionResult.SUCCESS;
    }

    @OnlyIn(Dist.CLIENT)
    private void displayScreen(RocketControllerBlockEntity be, Player player) {
        if (!(player instanceof LocalPlayer))
            return;

        if (!be.platformEntityAccessible()) {
            player.displayClientMessage(CreateWarfare.translatable("rocket_controller.unbound").withStyle(ChatFormatting.RED), true);
            return;
        }

        ScreenOpener.open(be.isInAssembly() ? new RocketControllerAssemblyScreen(be) : new RocketControllerScreen(be));
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, ASSEMBLING, WATERLOGGED, NO_DYNAMIC_BULB));
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.withWater(this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite()), context);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        this.updateWater(level, state, pos);
        return state;
    }

    @Override
    protected @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected @NotNull FluidState getFluidState(@NotNull BlockState state) {
        return this.fluidState(state);
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return AllShapes.STATION;
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<RocketControllerBlockEntity> getBlockEntityClass() {
        return RocketControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RocketControllerBlockEntity> getBlockEntityType() {
        return WarfareBlockEntities.ROCKET_CONTROLLER.get();
    }
}
