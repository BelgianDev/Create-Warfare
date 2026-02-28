package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.network.C2S.CreatePlatformSelectionPacket;
import be.raft.warfare.network.C2S.RemovePlatformSelectionPacket;
import be.raft.warfare.network.C2S.UpdateRocketControllerPacket;
import be.raft.warfare.network.S2C.BulletImpactPacket;
import be.raft.warfare.network.S2C.ShieldCreatePacket;
import be.raft.warfare.network.S2C.PlatformDirtyCachePacket;
import be.raft.warfare.network.S2C.ShieldDiscardPacket;
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
    BULLET_IMPACT(BulletImpactPacket.class, BulletImpactPacket.STREAM_CODEC),
    PLATFORM_DIRTY_CACHE(PlatformDirtyCachePacket.class, PlatformDirtyCachePacket.STREAM_CODEC),
    SHIELD_CREATE(ShieldCreatePacket.class, ShieldCreatePacket.STREAM_CODEC),
    SHIELD_DISCARD(ShieldDiscardPacket.class, ShieldDiscardPacket.STREAM_CODEC),

    // Client -> Server
    CREATE_PLATFORM_SELECTION(CreatePlatformSelectionPacket.class, CreatePlatformSelectionPacket.STREAM_CODEC),
    REMOVE_PLATFORM_SELECTION(RemovePlatformSelectionPacket.class, RemovePlatformSelectionPacket.STREAM_CODEC),

    // Client -> Server BlockEntity Config
    UPDATE_ROCKET_CONTROLLER(UpdateRocketControllerPacket.class, UpdateRocketControllerPacket.CODEC)
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
