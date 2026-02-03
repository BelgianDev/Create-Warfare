package be.raft.warfare.client.renderer.block;

import be.raft.warfare.registry.WarfarePartialModels;
import be.raft.warfare.block.entity.MechanicalTurretBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TurretRenderer extends KineticBlockEntityRenderer<MechanicalTurretBlockEntity> {

    public TurretRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(MechanicalTurretBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull MechanicalTurretBlockEntity blockEntity) {
        return true;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(MechanicalTurretBlockEntity be, BlockState state) {
        return CachedBuffers.partial(WarfarePartialModels.TURRET_COG, state);
    }
}
