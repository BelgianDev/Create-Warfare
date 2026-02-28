package be.raft.warfare.network.S2C;

import be.raft.warfare.registry.WarfarePackets;
import be.raft.warfare.shield.Shield;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShieldDiscardPacket(BlockPos controllerPos) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, ShieldDiscardPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ShieldDiscardPacket::controllerPos,
            ShieldDiscardPacket::new
    );

    @Override
    public void handle(LocalPlayer player) {

    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.SHIELD_DISCARD;
    }
}
