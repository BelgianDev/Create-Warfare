package be.raft.warfare.content.block;

import be.raft.warfare.content.WarfareBlockEntities;
import be.raft.warfare.content.WarfareShapes;
import be.raft.warfare.content.block.entity.MechanicalTurretBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalTurretBlock extends KineticBlock implements ICogWheel, IBE<MechanicalTurretBlockEntity> {
    public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");

    public MechanicalTurretBlock(Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState().setValue(CEILING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(CEILING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(CEILING, context.getClickedFace() == Direction.DOWN);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return state.getValue(CEILING) ? WarfareShapes.TURRET_CEILLING : WarfareShapes.TURRET;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<MechanicalTurretBlockEntity> getBlockEntityClass() {
        return MechanicalTurretBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalTurretBlockEntity> getBlockEntityType() {
        return WarfareBlockEntities.MECHANICAL_TURRET.get();
    }
}
