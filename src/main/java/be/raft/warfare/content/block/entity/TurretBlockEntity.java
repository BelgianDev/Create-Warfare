package be.raft.warfare.content.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
    private static final int TURRET_EYE_LEVEL = 1;

    // Server
    private final AABB targetingBoundingBox = new AABB(this.getBlockPos()).inflate(RANGE);
    private LivingEntity target;
    private int targetRefreshCounter;

    // Client
    public LerpedFloat baseAngle;
    public LerpedFloat armAngle;
    public LerpedFloat headAngle;

    public TurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        this.targetRefreshCounter = 5;

        this.baseAngle = LerpedFloat.angular();
        this.baseAngle.startWithValue(0);

        this.armAngle = LerpedFloat.angular();
        this.armAngle.startWithValue(0);

        this.headAngle = LerpedFloat.angular();
        this.headAngle.startWithValue(0);
    }

    @Override
    public void tick() {
        if (this.level == null)
            return;

        super.tick();

        if (this.level.isClientSide) {
            this.baseAngle.tickChaser();
            this.headAngle.tickChaser();
            return;
        }

        this.targetRefreshCounter--;
        if (this.targetRefreshCounter <= 0) {
            this.lookForTarget();
            if (this.target != null)
                this.lookAt(this.target.position(), (float) this.target.getBoundingBox().getYsize() / 2, true);

            // this.broadcastDebug("Angles: [ Base: " + this.baseAngle.getValue() + " ; Head: " + this.headAngle.getValue() + " ]");
            this.targetRefreshCounter = 5;
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        compound.putFloat("BaseAngle", this.baseAngle.getValue());
        compound.putFloat("HeadAngle", this.headAngle.getValue());
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        if (clientPacket) {
            this.baseAngle.chase(compound.getFloat("BaseAngle"), 0.3F, LerpedFloat.Chaser.EXP);
            this.headAngle.chase(compound.getFloat("HeadAngle"), 0.3F, LerpedFloat.Chaser.EXP);
        } else {
            // Makes sure that the values are instantly set on the server, instead of interpolating them.
            this.baseAngle.setValue(compound.getFloat("BaseAngle"));
            this.headAngle.setValue(compound.getFloat("HeadAngle"));
        }
    }

    @Override
    public @NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void lookForTarget() {
        if (this.level == null)
            return;

        AABB region = AABB.ofSize(this.getBlockPos().getCenter(), RANGE * 2, RANGE * 2, RANGE * 2);
        this.target = this.level.getNearestEntity(LivingEntity.class, TargetingConditions.DEFAULT, null,
                this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), region);
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

        BlockPos turretPos = this.getBlockPos();

        double deltaX = position.x() - (turretPos.getX() + 0.5);
        double deltaY = (position.y() + height) - (turretPos.getY() + TURRET_EYE_LEVEL);
        double deltaZ = position.z() - (turretPos.getZ() + 0.5);

        double horizontalDelta = (float) Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float angle = (float) Math.toDegrees(Math.atan2(-deltaX, -deltaZ));
        float pitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDelta));

        pitch = Mth.clamp(pitch, -90, 90);

        if (this.baseAngle.getValue() == angle && this.headAngle.getValue() == pitch)
            return; // No need to update anything.

        this.baseAngle.setValue(angle);
        this.headAngle.setValue(pitch);

        if (sync)
            this.notifyUpdate();
        else
            this.setChanged(); // Still tell the server the block entity changed
    }

    private void broadcastDebug(String msg) {
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(Component.literal(msg), false);
    }
}
