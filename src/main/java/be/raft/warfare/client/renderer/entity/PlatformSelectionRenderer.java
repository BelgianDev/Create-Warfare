package be.raft.warfare.client.renderer.entity;

import be.raft.warfare.entity.PlatformSelectionEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PlatformSelectionRenderer extends EntityRenderer<PlatformSelectionEntity> {
    public PlatformSelectionRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull PlatformSelectionEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRender(@NotNull PlatformSelectionEntity entity, @NotNull Frustum frustum, double x, double y, double z) {
        return false;
    }
}
