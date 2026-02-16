package be.raft.warfare.client.visual;

import be.raft.warfare.block.entity.ShieldControllerBlockEntity;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class ShieldControllerVisual extends KineticBlockEntityVisual<ShieldControllerBlockEntity> {
    private final EnumMap<Direction, RotatingInstance> instances = new EnumMap<>(Direction.class);

    public ShieldControllerVisual(VisualizationContext context, ShieldControllerBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        Direction.Axis axis = blockEntity.getBlockState().getValue(HorizontalAxisKineticBlock.HORIZONTAL_AXIS);

        var instancer = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));
        for (Direction facing : Direction.values()) {
            if (facing == Direction.DOWN)
                continue;

            final Direction.Axis dirAxis = facing.getAxis();
            if (axis == dirAxis) {
                continue;
            }

            RotatingInstance instance = instancer.createInstance();

            instance.setup(blockEntity, axis, this.blockEntity.getSpeed())
                    .setPosition(getVisualPosition())
                    .rotateToFace(Direction.SOUTH, facing)
                    .setChanged();

            instances.put(facing, instance);
        }
    }

    @Override
    public void update(float pt) {
        for (Map.Entry<Direction, RotatingInstance> key : this.instances.entrySet()) {
            Direction direction = key.getKey();
            Direction.Axis axis = direction.getAxis();

            key.getValue().setup(this.blockEntity, axis, this.blockEntity.getSpeed()).setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(this.instances.values().toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        this.instances.values().forEach(AbstractInstance::delete);
        this.instances.clear();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        this.instances.values().forEach(consumer);
    }
}
