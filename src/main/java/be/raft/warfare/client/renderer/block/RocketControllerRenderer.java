package be.raft.warfare.client.renderer.block;

import be.raft.warfare.block.RocketControllerBlock;
import be.raft.warfare.block.entity.RocketControllerBlockEntity;
import be.raft.warfare.entity.PlatformSelectionEntity;
import be.raft.warfare.registry.WarfarePartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RocketControllerRenderer extends SafeBlockEntityRenderer<RocketControllerBlockEntity> {
    private static final Vec3i PASSIVE = new Vec3i(100, 255, 170);
    private static final Vec3i ASSEMBLING = new Vec3i(10, 130, 242);
    private static final Vec3i INVALID = new Vec3i(255, 0, 0);

    private static final int VALID_AREA_COLOR = 0x96B5FF;
    private static final int THRUSTER_PRESENT_COLOR = 0xCAFF96;

    public RocketControllerRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public boolean shouldRenderOffScreen(RocketControllerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96 * 2;
    }

    @Override
    protected void renderSafe(RocketControllerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (state.getValue(RocketControllerBlock.NO_DYNAMIC_BULB))
            return;

        Vec3i color = PASSIVE;

        if (!be.platformEntityAccessible()) {
            color = INVALID;
        } else if (be.isInAssembly()) {
            color = ASSEMBLING;
        }

        // Create LinkBulbRenderer - Start
        int glow = 0;
        float lerpedGlow = be.getBulbGlow(partialTicks);
        if (lerpedGlow >= .125f) {
            lerpedGlow = (float) (1 - (2 * Math.pow(lerpedGlow - .75f, 2)));
            lerpedGlow = Mth.clamp(lerpedGlow, -1, 1);

            glow = (int) (200 * lerpedGlow);
        }
        // Create LinkBulbRenderer - End

        Direction facing = state.getValue(RocketControllerBlock.FACING);

        // Render bulbs using the PoseStack rotation (keeps their original position correct)
        ms.pushPose();
        PoseTransformStack transform = TransformStack.of(ms);
        transform.center()
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .uncenter();

        CachedBuffers.partial(WarfarePartialModels.VERTICAL_BULB, state)
                .translate(0.75, 1.25, 0.1875)
                .light(LightTexture.FULL_BRIGHT)
                .color(color.getX(), color.getY(), color.getZ(), 255)
                .renderInto(ms, bufferSource.getBuffer(RenderType.translucent()));

        CachedBuffers.partial(WarfarePartialModels.VERTICAL_BULB_GLOW, state)
                .translate(0.75, 1.25, 0.1875)
                .light(LightTexture.FULL_BRIGHT)
                .color(glow, glow, glow, 255)
                .disableDiffuse()
                .renderInto(ms, bufferSource.getBuffer(RenderTypes.additive()));

        ms.popPose();


        if (be.isInAssembly() && be.platformEntityAccessible())
            this.renderAssemblingAreas(be, ms, bufferSource, facing);
    }

    private void renderAssemblingAreas(RocketControllerBlockEntity be, PoseStack ms, MultiBufferSource bufferSource, Direction controllerFacing) {
        PlatformSelectionEntity entity = be.getPlatformSelectionEntity();
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();

        if (entity.cachedAssemblyAreasDirty())
            entity.computeAssemblyAreas();

        ms.pushPose();
        for (BlockPos area : entity.getCachedAssemblyAreas()) {
            boolean hasThruster = !level.getBlockState(area.above()).isAir();

            int dx = area.getX() - pos.getX();
            int dy = area.getY() - pos.getY();
            int dz = area.getZ() - pos.getZ();

            CachedBuffers.partialFacing(WarfarePartialModels.ASSEMBLY_OVERLAY, be.getBlockState(), Direction.SOUTH)
                    .light(LevelRenderer.getLightColor(level, area))
                    .color(hasThruster ? THRUSTER_PRESENT_COLOR : VALID_AREA_COLOR)
                    .translate(dx, dy + 0.001f, dz)
                    .renderInto(ms, bufferSource.getBuffer(RenderType.cutoutMipped()));
        }
        ms.popPose();
    }
}
