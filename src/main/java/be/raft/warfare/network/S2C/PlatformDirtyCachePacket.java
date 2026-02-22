package be.raft.warfare.network.S2C;

import be.raft.warfare.entity.PlatformSelectionEntity;
import be.raft.warfare.registry.WarfarePackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record PlatformDirtyCachePacket(int entityId) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, PlatformDirtyCachePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PlatformDirtyCachePacket::entityId,
            PlatformDirtyCachePacket::new
    );

    @Override
    public void handle(LocalPlayer player) {
        final Level level = player.level();
        if (level == null)
            return;

        final Entity entity = level.getEntity(this.entityId);
        if (entity == null)
            return;

        if (!(entity instanceof PlatformSelectionEntity platform))
            return;

        platform.markCachedAssemblyAreasDirty();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.PLATFORM_DIRTY_CACHE;
    }
}
