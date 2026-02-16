package be.raft.warfare.client.visual;

import be.raft.warfare.block.entity.ShieldCoilBlockEntity;
import be.raft.warfare.registry.WarfarePartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;

public class ShieldCoilVisual extends SingleAxisRotatingVisual<ShieldCoilBlockEntity> {
    public ShieldCoilVisual(VisualizationContext context, ShieldCoilBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(WarfarePartialModels.SHIELD_COIL));
    }


}
