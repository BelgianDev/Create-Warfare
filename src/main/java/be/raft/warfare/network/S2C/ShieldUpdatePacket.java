package be.raft.warfare.network.S2C;

import be.raft.warfare.registry.WarfarePackets;
import be.raft.warfare.shield.ShieldEntry;
import be.raft.warfare.shield.ShieldManager;
import be.raft.warfare.shield.ShieldStore;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ShieldUpdatePacket(@NotNull BlockPos origin, @Nullable BlockPos from, @Nullable BlockPos to) implements ClientboundPacketPayload {
    public static final StreamCodec<ByteBuf, ShieldUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ShieldUpdatePacket::origin,
            BlockPos.STREAM_CODEC, ShieldUpdatePacket::from,
            BlockPos.STREAM_CODEC, ShieldUpdatePacket::to,
            ShieldUpdatePacket::new
    );

    public static ShieldUpdatePacket remove(ShieldEntry entry) {
        return ShieldUpdatePacket.remove(entry.origin());
    }

    public static ShieldUpdatePacket remove(BlockPos origin) {
        return new ShieldUpdatePacket(origin, null, null);
    }

    public static ShieldUpdatePacket addOrUpdate(ShieldEntry entry) {
        AABB bb = entry.boundingBox();

        int maxX = Mth.floor(bb.maxX);
        int maxY = Mth.floor(bb.maxY);
        int maxZ = Mth.floor(bb.maxZ);

        int minX = Mth.floor(bb.minX);
        int minY = Mth.floor(bb.minY);
        int minZ = Mth.floor(bb.minZ);

        return new ShieldUpdatePacket(entry.origin(), new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    private ShieldEntry asEntry() {
        return ShieldEntry.span(this.origin, this.from, this.to);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        Level level = player.level();
        ShieldStore store = ShieldManager.getStore(level);

        if (this.to == null || this.from == null) {
            store.remove(this.origin);
            return;
        }

        store.putOrUpdate(this.asEntry());
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.SHIELD_UPDATE_PACKET;
    }
}
