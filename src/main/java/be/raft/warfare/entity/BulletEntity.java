package be.raft.warfare.entity;

import be.raft.warfare.registry.WarfareDamageTypes;
import be.raft.warfare.network.S2C.BulletImpactPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class BulletEntity extends Projectile {
    private static final byte BULLET_HIT = 0x01;

    private final BlockPos turretPos;

    public BulletEntity(EntityType<BulletEntity> type, Level level) {
        super(type, level);
        this.turretPos = BlockPos.ZERO;
    }

    public BulletEntity(EntityType<BulletEntity> type, Level level, BlockPos turretPos) {
        super(type, level);
        this.turretPos = turretPos;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());

        HitResult result = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));

        if (result.getType() != HitResult.Type.MISS)
            end = result.getLocation();

        EntityHitResult entityResult = ProjectileUtil.getEntityHitResult(this.level(), this, start, end, this.getBoundingBox().inflate(2), this::canHitEntity);
        if (entityResult != null)
            result = entityResult;

        if (result.getType() == HitResult.Type.MISS) { // We didn't hit anything
            this.updatePos();
            return;
        }

        if (EventHooks.onProjectileImpact(this, result)) { // Another mod prevented the projectile from hitting anything
            this.discard(); // No point in continuing, and could cause ghost bullets to stay in the world.
            return;
        }

        // this.level().broadcastEntityEvent(this, SPAWN_PARTICLE_ID);
        switch (result) {
            case BlockHitResult blockRes -> this.onHitBlock(blockRes);
            case EntityHitResult entityRes -> this.onHitEntity(entityRes);
            default -> {}
        }

        this.discard();
    }

    private void updatePos() {
        double newX = this.getX() + this.getDeltaMovement().x();
        double nexY = this.getY() + this.getDeltaMovement().y();
        double newZ = this.getZ() + this.getDeltaMovement().z();

        this.setPos(newX, nexY, newZ);
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity target) {
        return target instanceof LivingEntity && super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        if (this.level().isClientSide)
            return;

        LivingEntity entity = (LivingEntity) result.getEntity();
        DamageSource source = new DamageSource(WarfareDamageTypes.createHolder(this.level().registryAccess(), WarfareDamageTypes.BULLET_DAMAGE), this.turretPos.getCenter());

        entity.hurt(source, 0.5f);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        if (this.level().isClientSide)
            return;

        BlockPos pos = result.getBlockPos();
        Vec3 hitVec = result.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        Direction face = result.getDirection();

        float faceX = (float) (face.getAxis() == Direction.Axis.X ? hitVec.y : hitVec.x);
        float faceY = (float) (face.getAxis() == Direction.Axis.Z ? hitVec.y : hitVec.z);

        BulletImpactPacket packet = new BulletImpactPacket(pos, face, faceX, faceY);
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) this.level(), this.level().getChunkAt(result.getBlockPos()).getPos(), packet);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {

    }
}
