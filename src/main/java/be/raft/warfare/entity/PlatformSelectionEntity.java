package be.raft.warfare.entity;

import be.raft.warfare.registry.WarfareEntities;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;

public class PlatformSelectionEntity extends Entity implements IEntityWithComplexSpawn {
    public PlatformSelectionEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    public PlatformSelectionEntity(Level world, AABB boundingBox) {
        this(WarfareEntities.PLATFORM_SELECTION.get(), world);
        setBoundingBox(boundingBox);
        resetPositionToBB();
    }

    public void resetPositionToBB() {
        AABB bb = getBoundingBox();
        setPosRaw(bb.getCenter().x, bb.minY, bb.getCenter().z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {}

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        xRotO = getXRot();
        yRotO = getYRot();
        walkDistO = walkDist;
        xo = getX();
        yo = getY();
        zo = getZ();

        if (getBoundingBox().getXsize() == 0)
            discard();
    }

    @Override
    public void setPos(double x, double y, double z) {
        AABB bb = getBoundingBox();
        setPosRaw(x, y, z);
        Vec3 center = bb.getCenter();
        setBoundingBox(bb.move(-center.x, -bb.minY, -center.z)
                .move(x, y, z));
    }

    @Override
    public void move(MoverType typeIn, Vec3 pos) {
        if (!level().isClientSide && isAlive() && pos.lengthSqr() > 0.0D)
            discard();
    }

    @Override
    public void push(double x, double y, double z) {
        if (!level().isClientSide && isAlive() && x * x + y * y + z * z > 0.0D)
            discard();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return super.getDimensions(pose).withEyeHeight(0.0F);
    }

    @Override
    public void push(Entity entityIn) {
        super.push(entityIn);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        Vec3 position = position();
        writeBoundingBox(compound, getBoundingBox().move(position.scale(-1)));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        Vec3 position = position();
        setBoundingBox(readBoundingBox(compound).move(position));
    }

    public static void writeBoundingBox(CompoundTag compound, AABB bb) {
        compound.put("From", VecHelper.writeNBT(new Vec3(bb.minX, bb.minY, bb.minZ)));
        compound.put("To", VecHelper.writeNBT(new Vec3(bb.maxX, bb.maxY, bb.maxZ)));
    }

    public static AABB readBoundingBox(CompoundTag compound) {
        Vec3 from = VecHelper.readNBT(compound.getList("From", Tag.TAG_DOUBLE));
        Vec3 to = VecHelper.readNBT(compound.getList("To", Tag.TAG_DOUBLE));
        return new AABB(from, to);
    }

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public float rotate(Rotation transformRotation) {
        AABB bb = getBoundingBox().move(position().scale(-1));
        if (transformRotation == Rotation.CLOCKWISE_90 || transformRotation == Rotation.COUNTERCLOCKWISE_90)
            setBoundingBox(new AABB(bb.minZ, bb.minY, bb.minX, bb.maxZ, bb.maxY, bb.maxX).move(position()));
        return super.rotate(transformRotation);
    }

    @Override
    public float mirror(Mirror transformMirror) {
        return super.mirror(transformMirror);
    }

    @Override
    public void thunderHit(ServerLevel world, LightningBolt lightningBolt) {}

    @Override
    public void refreshDimensions() {}

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        CompoundTag compound = new CompoundTag();
        addAdditionalSaveData(compound);
        buffer.writeNbt(compound);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        readAdditionalSaveData(additionalData.readNbt());
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    public boolean contains(BlockPos pos) {
        return getBoundingBox().contains(Vec3.atCenterOf(pos));
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }
}
