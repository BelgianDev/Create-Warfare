package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.client.renderer.block.TurretRenderer;
import be.raft.warfare.client.visual.TurretVisual;
import be.raft.warfare.block.entity.MechanicalTurretBlockEntity;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class WarfareBlockEntities {
    private static final CreateRegistrate REGISTRATE = CreateWarfare.REGISTRATE;

    public static final BlockEntityEntry<MechanicalTurretBlockEntity> MECHANICAL_TURRET = REGISTRATE
            .blockEntity("mechanical_turret", MechanicalTurretBlockEntity::new)
            .visual(() -> TurretVisual::new)
            .validBlocks(WarfareBlocks.MECHANICAL_TURRET)
            .renderer(() -> TurretRenderer::new)
            .register();

    public static void register(IEventBus bus) {
        bus.addListener(WarfareBlockEntities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        MechanicalTurretBlockEntity.registerCapabilities(event);
    }
}
