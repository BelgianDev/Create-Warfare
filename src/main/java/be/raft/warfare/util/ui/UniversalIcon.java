package be.raft.warfare.util.ui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * Adapter class that extends {@link AllIcons} to allow rendering of icons from any atlas.
 * <br><br>
 * The code contained in this file is mostly copied from {@link AllIcons}, which is licensed under <a href="https://github.com/Creators-of-Create/Create?tab=License-1-ov-file#readme">MIT - The Create Team / The Creators of Create</a>
 */
public class UniversalIcon extends AllIcons implements ScreenElement {
    private final ResourceLocation atlas;
    private final int atlasSize;
    private final int iconX, iconY;

    public UniversalIcon(@NotNull ResourceLocation atlas, int atlasSize, int x, int y) {
        super(x, y);
        Preconditions.checkNotNull(atlas, "Icon atlas cannot be null!");

        this.atlas = atlas;
        this.atlasSize = atlasSize;

        this.iconX = x * 16;
        this.iconY = y * 16;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void bind() {
        RenderSystem.setShaderTexture(0, this.atlas);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(this.atlas, x, y, this.iconX, this.iconY, 16, 16, this.atlasSize, this.atlasSize);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack ms, MultiBufferSource buffer, int color) {
        VertexConsumer builder = buffer.getBuffer(RenderType.text(this.atlas));
        Matrix4f matrix = ms.last().pose();
        Color rgb = new Color(color);
        int light = LightTexture.FULL_BRIGHT;

        Vec3 vec1 = new Vec3(0, 0, 0);
        Vec3 vec2 = new Vec3(0, 1, 0);
        Vec3 vec3 = new Vec3(1, 1, 0);
        Vec3 vec4 = new Vec3(1, 0, 0);

        float u1 = iconX * 1f / this.atlasSize;
        float u2 = (iconX + 16) * 1f / this.atlasSize;
        float v1 = iconY * 1f / this.atlasSize;
        float v2 = (iconY + 16) * 1f / this.atlasSize;

        vertex(builder, matrix, vec1, rgb, u1, v1, light);
        vertex(builder, matrix, vec2, rgb, u1, v2, light);
        vertex(builder, matrix, vec3, rgb, u2, v2, light);
        vertex(builder, matrix, vec4, rgb, u2, v1, light);
    }

    @OnlyIn(Dist.CLIENT)
    public void vertex(VertexConsumer builder, Matrix4f matrix, Vec3 vec, Color rgb, float u, float v, int light) {
        builder.addVertex(matrix, (float) vec.x, (float) vec.y, (float) vec.z)
                .setColor(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 255)
                .setUv(u, v)
                .setLight(light);
    }
}
