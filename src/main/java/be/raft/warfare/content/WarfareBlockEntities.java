package be.raft.warfare.content;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.client.renderer.block.TurretRenderer;
import be.raft.warfare.client.visual.TurretVisual;
import be.raft.warfare.content.block.entity.TurretBlockEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class WarfareBlockEntities {
    private static final CreateRegistrate REGISTRATE = CreateWarfare.REGISTRATE;

    public static final BlockEntityEntry<TurretBlockEntity> MECHANICAL_TURRET = REGISTRATE
            .blockEntity("mechanical_turret", TurretBlockEntity::new)
            .visual(() -> TurretVisual::new)
            .validBlocks(WarfareBlocks.MECHANICAL_TURRET)
            .renderer(() -> TurretRenderer::new)
            .register();

    public static void register() {}

}
