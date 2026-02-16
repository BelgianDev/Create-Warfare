package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.block.entity.RocketControllerBlockEntity;
import be.raft.warfare.block.entity.ShieldCoilBlockEntity;
import be.raft.warfare.block.entity.ShieldControllerBlockEntity;
import be.raft.warfare.client.renderer.block.RocketControllerRenderer;
import be.raft.warfare.client.renderer.block.ShieldCoilRenderer;
import be.raft.warfare.client.renderer.block.ShieldControllerRenderer;
import be.raft.warfare.client.renderer.block.TurretRenderer;
import be.raft.warfare.client.visual.ShieldCoilVisual;
import be.raft.warfare.client.visual.ShieldControllerVisual;
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

    public static final BlockEntityEntry<RocketControllerBlockEntity> ROCKET_CONTROLLER = REGISTRATE
            .blockEntity("rocket_controller", RocketControllerBlockEntity::new)
            .validBlocks(WarfareBlocks.ROCKET_CONTROLLER)
            .renderer(() -> RocketControllerRenderer::new)
            .register();

    public static final BlockEntityEntry<ShieldCoilBlockEntity> SHIELD_COIL = REGISTRATE
            .blockEntity("shield_coil", ShieldCoilBlockEntity::new)
            .visual(() -> ShieldCoilVisual::new)
            .validBlocks(WarfareBlocks.SHIELD_COIL)
            .renderer(() -> ShieldCoilRenderer::new)
            .register();

    public static final BlockEntityEntry<ShieldControllerBlockEntity> SHIELD_CONTROLLER = REGISTRATE
            .blockEntity("shield_controller", ShieldControllerBlockEntity::new)
            .visual(() -> ShieldControllerVisual::new)
            .validBlocks(WarfareBlocks.SHIELD_CONTROLLER)
            .renderer(() -> ShieldControllerRenderer::new)
            .register();

    public static void register(IEventBus bus) {
        bus.addListener(WarfareBlockEntities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        MechanicalTurretBlockEntity.registerCapabilities(event);
    }
}
