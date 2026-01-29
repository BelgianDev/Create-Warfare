package be.raft.warfare.network.C2S;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.entity.PlatformSelectionEntity;
import be.raft.warfare.registry.WarfarePackets;
import be.raft.warfare.rocket.platform.RocketPlatformSelectionHelper;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record CreatePlatformSelectionPacket(BlockPos from, BlockPos to) implements ServerboundPacketPayload {
    public static final StreamCodec<ByteBuf, CreatePlatformSelectionPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CreatePlatformSelectionPacket::from,
            BlockPos.STREAM_CODEC, CreatePlatformSelectionPacket::to,
            CreatePlatformSelectionPacket::new
    );

    @Override
    public void handle(ServerPlayer player) {
        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 2;
        if (player.distanceToSqr(Vec3.atCenterOf(this.to)) > range * range)
            return;

        if (!this.to.closerThan(this.from, 25))
            return;

        if (RocketPlatformSelectionHelper.rocketPlatformIntersects(player.level(), this.from, this.to))
            return;

        AABB boundingBox = SuperGlueEntity.span(this.from, this.to);
        PlatformSelectionEntity entity = new PlatformSelectionEntity(player.level(), boundingBox);
        player.level().addFreshEntity(entity);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.CREATE_PLATFORM_SELECTION;
    }
}
