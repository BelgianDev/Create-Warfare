package be.raft.warfare.network.S2C;

import be.raft.warfare.registry.WarfarePackets;
import be.raft.warfare.shield.Shield;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;

public record ShieldCreatePacket(Shield shield) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, ShieldCreatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.shield.controllerPos(),
            BlockPos.STREAM_CODEC, packet -> packet.shield.from(),
            BlockPos.STREAM_CODEC, packet -> packet.shield.to(),
            ByteBufCodecs.BOOL, packet -> packet.shield.removed(),
            ShieldCreatePacket::new
    );

    public ShieldCreatePacket(BlockPos controllerPos, BlockPos from, BlockPos to, boolean removal) {
        this(new Shield(controllerPos, AABB.encapsulatingFullBlocks(from, to), removal));
    }

    @Override
    public void handle(LocalPlayer player) {

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.SHIELD_CREATE;
    }
}
