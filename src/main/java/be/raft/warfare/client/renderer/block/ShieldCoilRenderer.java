package be.raft.warfare.client.renderer.block;

import be.raft.warfare.block.entity.ShieldCoilBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class ShieldCoilRenderer extends KineticBlockEntityRenderer<ShieldCoilBlockEntity> {
    public ShieldCoilRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
