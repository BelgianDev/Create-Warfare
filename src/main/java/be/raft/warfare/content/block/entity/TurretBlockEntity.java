package be.raft.warfare.content.block.entity;

import be.raft.warfare.content.block.TurretBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class TurretBlockEntity extends KineticBlockEntity {
    private static final int RANGE = 20; // TODO: Replace with a config option.
    private static final float SPEED_MODIFIER = 0.005f; // TODO: Replace with a config option.

    private static final int TARGET_REFRESH_RATE = 4 * 20;
    private static final float TURRET_EYE_LEVEL = 1f;

    // Server
    private final AABB targetingBoundingBox;

    private LivingEntity target;
    private Position lastTargetPosition;
    private int targetRefreshCounter;

    // Shared values
    private float targetBaseAngle;
    private float targetHeadAngle;

    // Both (Local values, those are not shared across the client and server)
    private final boolean ceiling;
    private boolean targetPosChanged;

    // Client
    public LerpedFloat baseAngle;
    public LerpedFloat headAngle;

    public TurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        this.targetingBoundingBox = this.getTargetingBoundingBox();
        this.targetRefreshCounter = TARGET_REFRESH_RATE;

        this.baseAngle = LerpedFloat.angular();
        this.baseAngle.startWithValue(0);

        this.headAngle = LerpedFloat.angular();
        this.headAngle.startWithValue(0);

        this.ceiling = state.getValue(TurretBlock.CEILING);
        this.targetPosChanged = false;

        this.setLazyTickRate(1);
    }

    @Override
    public void tick() {
        if (this.level == null)
            return;

        super.tick();
        if (this.getSpeed() == 0)
            return; // Prevent any calculation if the turret is not powered at all.

        if (this.checkTarget())
            this.lookAt(this.target.position(), (float) this.target.getBoundingBox().getYsize() / 2, true);

        boolean aiming = tickMovement();
        if (this.level.isClientSide)
            return;

        if (this.checkTarget() && aiming)
            this.shootTarget();

        this.targetRefreshCounter--;
        if (this.targetRefreshCounter <= 0 || !this.checkTarget()) {
            this.lookForTarget();
            this.targetRefreshCounter = TARGET_REFRESH_RATE;
        }

        this.broadcastDebug("Angles: [ Base: " + Math.round(this.baseAngle.getValue()) + " ; Head: " + Math.round(this.headAngle.getValue()) + " ; Firing: " + aiming + "]");
    }


    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        // Should probably be replaced by client simulation instead of spamming packets.
        // We can't simply send the entity, as the reach of the turret may be a lot higher than the client's
        compound.putFloat("base", this.targetBaseAngle);
        compound.putFloat("head", this.targetHeadAngle);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        float baseAngle = compound.getFloat("base");
        float headAngle = compound.getFloat("head");

        if (clientPacket) {
            this.targetBaseAngle = baseAngle;
            this.targetHeadAngle = headAngle;

            this.targetPosChanged = true; // Inform the tickMovement that the values were changed.
        } else {
            this.targetBaseAngle = baseAngle;
            this.targetHeadAngle = headAngle;

            // Makes sure that the values are instantly set on the server, instead of interpolating them.
            this.baseAngle.setValue(baseAngle);
            this.headAngle.setValue(headAngle);
        }
    }

    @Override
    public @NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private boolean checkTarget() {
        return this.target != null && this.target.isAlive();
    }

    private void lookForTarget() {
        if (this.level == null)
            return;

        this.target = this.level.getNearestEntity(LivingEntity.class, TargetingConditions.DEFAULT, null,
                this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), this.targetingBoundingBox);
    }

    private AABB getTargetingBoundingBox() {
        return new AABB(this.getBlockPos()).inflate(RANGE);
    }

    /**
     * Forces the turret to look at the given position.
     *
     * @param position position to look at.
     * @param height additional height to add to the position,
     *               should be used to target the center of the target's bounding box.
     * @param sync whether to sync the change to the clients.
     */
    public void lookAt(@NotNull Position position, float height, boolean sync) {
        if (this.level.isClientSide)
            return;

        if (this.lastTargetPosition != null && this.lastTargetPosition.equals(position))
            return; // The entity didn't move, so no need to recompute the angles.

        this.lastTargetPosition = position;
        BlockPos turretPos = this.getBlockPos();

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
        this.targetPosChanged = true;

        if (sync)
            this.notifyUpdate();
        else
            this.setChanged(); // Still tell the server the block entity changed
    }

    public void shootTarget() {
        //broadcastDebug("Shooting!");
    }

    /**
     * Called every tick to update the turret's movement.
     *
     * @return {@code true} if the turret is aiming the target, {@code false} otherwise.
     */
    private boolean tickMovement() {
        float speed = Math.abs(this.getSpeed());
        this.baseAngle.updateChaseSpeed(speed * SPEED_MODIFIER);
        this.headAngle.updateChaseSpeed(speed * SPEED_MODIFIER);

        this.baseAngle.tickChaser();
        this.headAngle.tickChaser();

        if (this.targetPosChanged) {
            this.targetPosChanged = false;
            
            // Always update the target to ensure the chaser knows where to go.
            // LerpedFloat.angular() will handle the shortest-path wrapping internally.
            if (this.baseAngle.getChaseTarget() != this.targetBaseAngle) {
                this.baseAngle.chase(this.targetBaseAngle, speed * SPEED_MODIFIER, LerpedFloat.Chaser.EXP);
            }

            if (this.headAngle.getChaseTarget() != this.targetHeadAngle) {
                this.headAngle.chase(this.targetHeadAngle, speed * SPEED_MODIFIER, LerpedFloat.Chaser.EXP);
            }
        }

        // Don't use .settled() for firing logic, as it requires exact float equality 
        // with a target that might be wrapped. Instead, check if the difference is small.
        float baseDiff = Mth.degreesDifference(this.baseAngle.getValue(), this.targetBaseAngle);
        float headDiff = Math.abs(this.headAngle.getValue() - this.targetHeadAngle);

        return Math.abs(baseDiff) < 1.0f && headDiff < 1.0f;
    }

    private void broadcastDebug(String msg) {
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastAll(new ClientboundSetActionBarTextPacket(Component.literal(msg)));
    }
}
