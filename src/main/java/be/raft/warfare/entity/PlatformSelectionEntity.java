package be.raft.warfare.entity;

import be.raft.warfare.registry.WarfareBlocks;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This is a modified version of {@link com.simibubi.create.content.contraptions.glue.SuperGlueEntity} from Create which is licensed under <a href="https://github.com/Creators-of-Create/Create?tab=License-1-ov-file#readme">MIT - The Create Team / The Creators of Create</a>
 */
public class PlatformSelectionEntity extends Entity implements IEntityWithComplexSpawn {
    private final Set<BlockPos> cachedAssemblyAreas;
    private boolean cachedAssemblyAreasDirty;

    private UUID platformIdentifier;
    private boolean isAssembling;
    private int lazyTick;

    public PlatformSelectionEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.cachedAssemblyAreas = new HashSet<>();
        this.platformIdentifier = UUID.randomUUID();

        this.isAssembling = false;

        this.lazyTick = 20;
        this.cachedAssemblyAreasDirty = true;
    }

    public PlatformSelectionEntity(Level world, AABB boundingBox, UUID platformIdentifier) {
        this(WarfareEntities.PLATFORM_SELECTION.get(), world);
        this.platformIdentifier = platformIdentifier;

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

        this.lazyTick--;
        if (this.lazyTick <= 0) {
            this.lazyTick();
            this.lazyTick = 20;
        }
    }

    private void lazyTick() {
        if (!this.cachedAssemblyAreasDirty)
            return;

        this.computeAssemblyAreas();
    }

    public boolean cachedAssemblyAreasDirty() {
        return this.cachedAssemblyAreasDirty;
    }

    public void markCachedAssemblyAreasDirty() {
        this.cachedAssemblyAreasDirty = true;
    }

    public Set<BlockPos> getCachedAssemblyAreas() {
        return Set.copyOf(this.cachedAssemblyAreas);
    }

    public void computeAssemblyAreas() {
        System.out.println("Computing thrusters!");

        this.cachedAssemblyAreas.clear();

        int y = (int) this.getBoundingBox().minY;
        int minX = (int) Math.floor(this.getBoundingBox().minX);
        int minZ = (int) Math.floor(this.getBoundingBox().minZ);

        int maxX = (int) Math.ceil(this.getBoundingBox().maxX) - 1;
        int maxZ = (int) Math.ceil(this.getBoundingBox().maxZ) - 1;

        if (maxX - minX + 1 < 3 || maxZ - minZ + 1 < 3)
            return;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX + 1; x <= maxX - 1; x++) {
            for (int z = minZ + 1; z <= maxZ - 1; z++) {

                boolean valid = true;
                for (int dx = -1; dx <= 1 && valid; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        cursor.set(x + dx, y, z + dz);
                        if (!this.level().getBlockState(cursor).is(WarfareBlocks.LAUNCH_PAD.get())) {
                            valid = false;
                            break;
                        }
                    }
                }

                if (valid)
                    this.cachedAssemblyAreas.add(new BlockPos(x, y, z));
            }
        }

        this.cachedAssemblyAreasDirty = false;
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
    public void move(@NotNull MoverType typeIn, @NotNull Vec3 pos) {
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
    public void push(@NotNull Entity entityIn) {
        super.push(entityIn);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        Vec3 position = position();
        writeBoundingBox(compound, getBoundingBox().move(position.scale(-1)));

        compound.putUUID("PlatformIdentifier", this.platformIdentifier);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        Vec3 position = position();
        setBoundingBox(readBoundingBox(compound).move(position));

        this.platformIdentifier = compound.getUUID("PlatformIdentifier");
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

    public UUID getPlatformIdentifier() {
        return platformIdentifier;
    }

    public boolean isAssembling() {
        return isAssembling;
    }

    public void setAssembling(boolean assembling) {
        isAssembling = assembling;
    }
}
