package be.raft.warfare.network;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.network.S2C.BulletImpactPacket;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class WarfareNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event, ModContainer container) {
        String protocolVersion = container.getModInfo().getVersion().toString();
        CreateWarfare.LOGGER.info("Registering network packets and handlers [Protocol Version: {}]", protocolVersion);

        final PayloadRegistrar registrar = new PayloadRegistrar(protocolVersion);
        registrar.playToClient(BulletImpactPacket.TYPE, BulletImpactPacket.CODEC, BulletImpactPacket::handle);
    }
}
