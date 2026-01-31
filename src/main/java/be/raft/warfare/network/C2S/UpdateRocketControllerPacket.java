package be.raft.warfare.network.C2S;

import be.raft.warfare.block.entity.RocketControllerBlockEntity;
import be.raft.warfare.registry.WarfarePackets;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class UpdateRocketControllerPacket extends BlockEntityConfigurationPacket<RocketControllerBlockEntity> {
    public static final StreamCodec<ByteBuf, UpdateRocketControllerPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, packet -> packet.pos,
            CatnipStreamCodecBuilders.nullable(ByteBufCodecs.BOOL), packet -> packet.enterAssembly,
            UpdateRocketControllerPacket::new
    );

    private final @Nullable Boolean enterAssembly;

    public UpdateRocketControllerPacket(BlockPos pos, @Nullable Boolean enterAssembly) {
        super(pos);
        this.enterAssembly = enterAssembly;
    }

    @Override
    protected void applySettings(ServerPlayer player, RocketControllerBlockEntity be) {
        if (this.enterAssembly != null) {
            if (this.enterAssembly)
                be.enterAssembly();
            else
                be.exitAssembly();
        }
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.UPDATE_ROCKET_CONTROLLER;
    }
}
