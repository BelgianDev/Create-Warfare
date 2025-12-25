package be.raft.warfare.content;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.client.renderer.entity.BulletRenderer;
import be.raft.warfare.content.entity.BulletEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;

public class WarfareEntities {
    private static final CreateRegistrate REGISTRATE = CreateWarfare.REGISTRATE;

    public static final EntityEntry<BulletEntity> BULLET = REGISTRATE.entity("bullet", BulletEntity::new, MobCategory.MISC)
            .renderer(() -> BulletRenderer::new)
            .properties(builder -> builder
                    .noSummon().noSave()
                    .canSpawnFarFromPlayer()
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.13F)
                    .clientTrackingRange(4)
                    .updateInterval(20))
            .register();

    public static void register() {}
}
