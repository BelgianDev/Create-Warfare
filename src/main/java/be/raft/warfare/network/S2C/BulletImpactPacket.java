package be.raft.warfare.network.S2C;

import be.raft.warfare.registry.WarfarePackets;
import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public record BulletImpactPacket(@NotNull BlockPos pos, @NotNull Direction face, float faceX, float faceY) implements ClientboundPacketPayload {
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
    @OnlyIn(Dist.CLIENT)
    public void handle(LocalPlayer player) {
        ClientLevel level = player.clientLevel;
        if (level == null)
            return;

        BlockState blockstate = level.getBlockState(this.pos);
        Direction face = this.face;

        Vec3 hitVec = this.getHitVec();
        Vec3i faceNormal = face.getNormal();
        for (int i = 0; i < 5; i++) {
            Vec3 velocity = new Vec3(faceNormal.getX(), faceNormal.getY(), faceNormal.getZ())
                    .add(level.random.nextDouble() * 0.3, level.random.nextDouble() * 0.3, level.random.nextDouble() * 0.3);

            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), false, hitVec.x, hitVec.y, hitVec.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private byte faceXAsByte() {
        return (byte) (this.faceX * 255f);
    }

    private byte faceYAsByte() {
        return (byte) (this.faceY * 255f);
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

    @Override
    public PacketTypeProvider getTypeProvider() {
        return WarfarePackets.BULLET_IMPACT;
    }
}
