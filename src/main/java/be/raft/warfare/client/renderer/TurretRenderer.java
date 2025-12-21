package be.raft.warfare.client.renderer;

import be.raft.warfare.content.WarfarePartialModels;
import be.raft.warfare.content.block.entity.TurretBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class TurretRenderer extends KineticBlockEntityRenderer<TurretBlockEntity> {

    public TurretRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(TurretBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

    @Override
    public boolean shouldRenderOffScreen(TurretBlockEntity blockEntity) {
        return true;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(TurretBlockEntity be, BlockState state) {
        return CachedBuffers.partial(WarfarePartialModels.TURRET_COG, state);
    }
}
