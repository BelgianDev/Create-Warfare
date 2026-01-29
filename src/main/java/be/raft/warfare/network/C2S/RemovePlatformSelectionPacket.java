package be.raft.warfare.network.C2S;

import be.raft.warfare.entity.PlatformSelectionEntity;
import be.raft.warfare.registry.WarfarePackets;
import com.simibubi.create.AllSoundEvents;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public record RemovePlatformSelectionPacket(int entityId) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, RemovePlatformSelectionPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RemovePlatformSelectionPacket::entityId,
            RemovePlatformSelectionPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        Entity entity = player.level().getEntity(this.entityId);
        if (!(entity instanceof PlatformSelectionEntity))
            return;

        double range = 32;
        if (player.distanceToSqr(entity.position()) > range * range)
            return;

        AllSoundEvents.DENY.play(player.level(), null, entity.position(), 0.5F, 0.5F);
        entity.discard();
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.REMOVE_PLATFORM_SELECTION;
    }
}
