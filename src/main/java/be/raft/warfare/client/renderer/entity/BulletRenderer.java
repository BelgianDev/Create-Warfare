package be.raft.warfare.client.renderer.entity;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.content.entity.BulletEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BulletRenderer extends ArrowRenderer<BulletEntity> {
    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BulletEntity entity) {
        return CreateWarfare.asLoc("bullet");
    }
}
