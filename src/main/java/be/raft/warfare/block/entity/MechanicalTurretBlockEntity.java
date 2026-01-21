package be.raft.warfare.block.entity;

import be.raft.warfare.registry.WarfareBlockEntities;
import be.raft.warfare.registry.WarfareEntities;
import be.raft.warfare.block.MechanicalTurretBlock;
import be.raft.warfare.entity.BulletEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class MechanicalTurretBlockEntity extends TurretBlockEntity<LivingEntity> {
    private static final int RANGE = 20; // TODO: Replace with a config option.
    private static final float SPEED_MODIFIER = 0.005f; // TODO: Replace with a config option.

    private static final float TURRET_EYE_LEVEL = 1f;
    private static final DustParticleOptions SMOKE_PARTICLE_OPTION = new DustParticleOptions(new Color(0xAAAAAA).asVectorF(), 1.2f);

    // Server
    private final AABB cachedBoundingBox;

    // Both
    private final boolean ceiling;

    private float targetBaseAngle;
    private float targetHeadAngle;

    public final LerpedFloat baseAngle;
    public final LerpedFloat headAngle;

    // Client Only (Visual)
    public final LerpedFloat nozzleLeftScale;
    public final LerpedFloat nozzleRightScale;

    private int animationTick = 0;

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, WarfareBlockEntities.MECHANICAL_TURRET.get(), (be, ctx) -> be.itemHandler);
    }

    public MechanicalTurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        this.cachedBoundingBox = new AABB(this.getBlockPos()).inflate(RANGE);
        this.ceiling = state.getValue(MechanicalTurretBlock.CEILING);

        this.targetBaseAngle = 0;
        this.targetHeadAngle = 0;

        this.baseAngle = LerpedFloat.angular();
        this.headAngle = LerpedFloat.angular();

        this.nozzleRightScale = LerpedFloat.linear();
        this.nozzleRightScale.startWithValue(1);
        this.nozzleLeftScale = LerpedFloat.linear();
        this.nozzleLeftScale.startWithValue(1);
    }

    @Override
    protected void onTargetChange() {
        this.computeAngles();

        if (this.baseAngle.getChaseTarget() != this.targetBaseAngle)
            this.baseAngle.chase(this.targetBaseAngle, speed * SPEED_MODIFIER, LerpedFloat.Chaser.EXP);

        if (this.headAngle.getChaseTarget() != this.targetHeadAngle)
            this.headAngle.chase(this.targetHeadAngle, speed * SPEED_MODIFIER, LerpedFloat.Chaser.EXP);
    }

    @Override
    protected void tickMovement() {
        float speed = Math.abs(this.getSpeed());

        this.baseAngle.updateChaseSpeed(speed * SPEED_MODIFIER);
        this.headAngle.updateChaseSpeed(speed * SPEED_MODIFIER);

        this.baseAngle.tickChaser();
        this.headAngle.tickChaser();

        if (this.level.isClientSide) {
            this.nozzleLeftScale.tickChaser();
            this.nozzleRightScale.tickChaser();

            if (this.hasTarget() && this.turretSettled() && this.getSpeed() != 0) {
                this.animationTick++;
                float oscillation = 1.05f + Mth.sin(this.animationTick * 2.5f) * 0.03f;
                
                this.nozzleLeftScale.chase(oscillation, 0.9f, LerpedFloat.Chaser.EXP);
                this.nozzleRightScale.chase(oscillation, 0.9f, LerpedFloat.Chaser.EXP);
            } else {
                this.nozzleLeftScale.chase(1.0f, 0.2f, LerpedFloat.Chaser.EXP);
                this.nozzleRightScale.chase(1.0f, 0.2f, LerpedFloat.Chaser.EXP);

                this.animationTick = 0;
            }
        }
    }

    private void computeAngles() {
        Position position = this.targetPosition();
        BlockPos turretPos = this.getBlockPos();

        float height = this.isTargetEntity() ? this.getTarget().getEyeHeight() / 2 : 0;

        double deltaX = position.x() - (turretPos.getX() + 0.5);
        double deltaY = (position.y() + height) - (turretPos.getY() + (this.ceiling ? -TURRET_EYE_LEVEL : TURRET_EYE_LEVEL));
        double deltaZ = position.z() - (turretPos.getZ() + 0.5);

        double horizontalDelta = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float angle = (float) Math.toDegrees(Math.atan2(-deltaX, -deltaZ));
        float pitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDelta));
        pitch = Mth.clamp(pitch, -35, 90); // Stay within bounds of the model

        if (this.ceiling) { // Invert values for ceiling turrets.
            angle = -(angle + 180);
            pitch = -pitch;
        }

        if (this.targetBaseAngle == angle && this.targetHeadAngle == pitch)
            return; // No need to update anything.

        this.targetBaseAngle = angle;
        this.targetHeadAngle = pitch;
    }

    @Override
    public boolean turretSettled() {
        float baseDiff = Mth.degreesDifference(this.baseAngle.getValue(), this.targetBaseAngle);
        float headDiff = Math.abs(this.headAngle.getValue() - this.targetHeadAngle);

        return Math.abs(baseDiff) < 1.0f && headDiff < 1.0f;
    }

    @Override
    public @NotNull AABB getTargetingBoundingBox() {
        return this.cachedBoundingBox;
    }

    @Override
    public boolean canTarget(@NotNull LivingEntity entity) {
        return entity.isAlive() && entity.canBeSeenAsEnemy();
    }

    @Override
    public @NotNull Class<LivingEntity> targetClass() {
        return LivingEntity.class;
    }

    private Vec3 getFirePoint() {
        return this.getBlockPos().getCenter().add(0, this.ceiling ? -TURRET_EYE_LEVEL : TURRET_EYE_LEVEL, 0);
    }

    private Vec3 getNozzlePosition(boolean left) {
        Vec3 pos = this.getBlockPos().getCenter();
        float currentHeadAngle = this.headAngle.getValue();
        float currentBaseAngle = this.baseAngle.getValue();

        double xOffset = left ? 0.185 : -0.185;
        Vec3 nozzlePos = new Vec3(xOffset, 0.25, -0.9);
        nozzlePos = nozzlePos.xRot((float) Math.toRadians(-currentHeadAngle));
        nozzlePos = nozzlePos.add(0, 0.55, 0.4);
        nozzlePos = nozzlePos.add(0, 6 / 16d, 0);
        nozzlePos = nozzlePos.yRot((float) Math.toRadians(currentBaseAngle));

        if (this.ceiling)
            nozzlePos = new Vec3(nozzlePos.x, -nozzlePos.y, -nozzlePos.z);

        return pos.add(nozzlePos);
    }

    @Override
    public void shoot() {
        if (this.level.isClientSide) {
            Vec3 leftNozzle = this.getNozzlePosition(true);
            Vec3 rightNozzle = this.getNozzlePosition(false);

            this.level.addParticle(SMOKE_PARTICLE_OPTION, leftNozzle.x, leftNozzle.y, leftNozzle.z, 0, 0, 0);
            this.level.addParticle(SMOKE_PARTICLE_OPTION, rightNozzle.x, rightNozzle.y, rightNozzle.z, 0, 0, 0);

            return;
        }

        Vec3 firePoint = this.getFirePoint();
        Position targetPos = this.targetPosition();

        double x = targetPos.x();
        double y = this.isTargetEntity() ? this.getTarget().getEyeY() - 1.1F : targetPos.y();
        double z = targetPos.z();

        double deltaX = x - firePoint.x;
        double deltaY = y - firePoint.y;
        double deltaZ = z - firePoint.z;

        BulletEntity bullet = new BulletEntity(WarfareEntities.BULLET.get(), this.level, this.getBlockPos());

        bullet.setOwner(null);
        bullet.setPos(firePoint);
        bullet.shoot(deltaX, deltaY, deltaZ, 1.6F, 5F);

        this.level.addFreshEntity(bullet);
    }

    @Override
    public @NotNull ValueBoxTransform targetingBoxTransform() {
        return new TurretBaseValueBoxTransform();
    }

    private static class TurretBaseValueBoxTransform extends CenteredSideValueBoxTransform {
        public TurretBaseValueBoxTransform() {
            super((blockState, direction) -> !direction.getAxis().isVertical());
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            int yPos = state.getValue(MechanicalTurretBlock.CEILING) ? 16 - 3 : 3;
            Vec3 location = VecHelper.voxelSpace(8, yPos, 15.5);
            location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            return location;
        }
    }
}
