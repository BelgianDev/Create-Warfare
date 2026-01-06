package be.raft.warfare.network.S2C;

import be.raft.warfare.CreateWarfare;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public record BulletImpactPacket(@NotNull BlockPos pos, @NotNull Direction face, float faceX, float faceY) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BulletImpactPacket> TYPE =
            new CustomPacketPayload.Type<>(CreateWarfare.asLoc("bullet_impact"));

    public static final StreamCodec<ByteBuf, BulletImpactPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BulletImpactPacket::pos,
            Direction.STREAM_CODEC, BulletImpactPacket::face,
            ByteBufCodecs.BYTE, BulletImpactPacket::faceXAsByte,
            ByteBufCodecs.BYTE, BulletImpactPacket::faceYAsByte,
            BulletImpactPacket::new
    );

    public BulletImpactPacket(BlockPos pos, Direction face, byte faceX, byte faceY) {
        this(pos, face, (faceX & 0xFF) / 255.0f, (faceY & 0xFF) / 255.0f);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    // Used to define where on the face particle should spawn, but since a block face is quite small we don't need a lot of precision and bytes help with the network load.
    private byte faceXAsByte() {
        return (byte) (this.faceX * 255f);
    }

    private byte faceYAsByte() {
        return (byte) (this.faceY * 255f);
    }

    public static void handle(@NotNull BulletImpactPacket payload, @NotNull IPayloadContext context) {
        Level level = Minecraft.getInstance().level;
        if (level == null)
            return;

        BlockState blockstate = level.getBlockState(payload.pos);
        Direction face = payload.face;

        Vec3 hitVec = payload.getHitVec();
        Vec3i faceNormal = face.getNormal();
        for (int i = 0; i < 5; i++) {
            Vec3 velocity = new Vec3(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ())
                    .add(level.random.nextDouble() * 0.3, level.random.nextDouble() * 0.3, level.random.nextDouble() * 0.3);

            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), false, hitVec.x, hitVec.y, hitVec.z, velocity.x, velocity.y, velocity.z);
        }
    }

    public Vec3 getHitVec() {
        double x = this.pos.getX();
        double y = this.pos.getY();
        double z = this.pos.getZ();

        Direction.Axis axis = this.face.getAxis();
        double offset = this.face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : 0.0;

        return switch (axis) {
            case X -> new Vec3(x + offset, y + this.faceX, z + this.faceY);
            case Y -> new Vec3(x + this.faceX, y + offset, z + this.faceY);
            case Z -> new Vec3(x + this.faceX, y + this.faceY, z + offset);
        };
    }
}
