package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.network.C2S.CreatePlatformSelectionPacket;
import be.raft.warfare.network.C2S.RemovePlatformSelectionPacket;
import be.raft.warfare.network.S2C.BulletImpactPacket;
import com.simibubi.create.Create;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.fml.ModContainer;

import java.util.Locale;

public enum WarfarePackets implements BasePacketPayload.PacketTypeProvider {
    // Server -> Client
    BULLET_IMPACT(BulletImpactPacket.class, BulletImpactPacket.CODEC),

    // Client -> Server
    CREATE_PLATFORM_SELECTION(CreatePlatformSelectionPacket.class, CreatePlatformSelectionPacket.CODEC),
    REMOVE_PLATFORM_SELECTION(RemovePlatformSelectionPacket.class, RemovePlatformSelectionPacket.CODEC)
    ;

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> WarfarePackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        String name = this.name().toLowerCase(Locale.ROOT);
        this.type = new CatnipPacketRegistry.PacketType<>(
                new CustomPacketPayload.Type<>(Create.asResource(name)),
                clazz, codec
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) this.type.type();
    }

    public static void register(ModContainer container) {
        CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(CreateWarfare.ID, container.getModInfo().getVersion().toString());
        for (WarfarePackets packet : WarfarePackets.values()) {
            packetRegistry.registerPacket(packet.type);
        }

        packetRegistry.registerAllPackets();
    }
}
