package be.raft.warfare.client.renderer.block;

import be.raft.warfare.block.RocketControllerBlock;
import be.raft.warfare.block.entity.RocketControllerBlockEntity;
import be.raft.warfare.registry.WarfarePartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector4i;

public class RocketControllerRenderer extends SafeBlockEntityRenderer<RocketControllerBlockEntity> {
    public RocketControllerRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(RocketControllerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = be.getBlockState();

        boolean isAssembling = state.getValue(RocketControllerBlock.ASSEMBLING);
        Direction facing = state.getValue(RocketControllerBlock.FACING);

        PoseTransformStack transform = TransformStack.of(ms);

        Vec3i color = new Vec3i(100, 170, 170);

        ms.pushPose();
        transform.center()
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .uncenter();

        CachedBuffers.partial(WarfarePartialModels.VERTICAL_BULB, state)
                .translate(0.75, 1.25, 0.1875)
                .light(LightTexture.FULL_BRIGHT)
                .color(color.getX(), color.getY(), color.getZ(), 200)
                .renderInto(ms, bufferSource.getBuffer(RenderType.translucent()));

        CachedBuffers.partial(WarfarePartialModels.VERTICAL_BULB_GLOW, state)
                .translate(0.75, 1.25, 0.1875)
                .light(LightTexture.FULL_BRIGHT)
                .color(color.getX(), color.getY(), color.getZ(), 0)
                .disableDiffuse()
                .renderInto(ms, bufferSource.getBuffer(RenderType.translucent()));

        ms.popPose();
    }
}
