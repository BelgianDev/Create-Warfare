package be.raft.warfare.block.entity;

import be.raft.warfare.block.RocketControllerBlock;
import be.raft.warfare.entity.PlatformSelectionEntity;
import be.raft.warfare.registry.WarfareDataComponents;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RocketControllerBlockEntity extends SmartBlockEntity {
    private static final int MAX_PLATFORM_SEARCH_RANGE = 16;

    // Both
    private @Nullable UUID platformIdentifier;
    private @Nullable PlatformSelectionEntity cachedSelectionEntity;

    // Client
    private final LerpedFloat bulbGlow;

    public RocketControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        setLazyTickRate(20);

        this.bulbGlow = LerpedFloat.linear();
        this.bulbGlow.chase(0, 0.5f, LerpedFloat.Chaser.EXP);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level.isClientSide)
            this.bulbGlow.tickChaser();

        if (!this.platformEntityAccessible()) {
            if (this.level.isClientSide && this.cachedSelectionEntity != null)
                this.pulse();

            this.cachedSelectionEntity = null;
            return;
        }
    }

    @Override
    public void lazyTick() {
        if (this.platformEntityAccessible())
            return;

        this.lookForPlatform();
    }

    public float getBulbGlow(float partialTicks) {
        return this.bulbGlow.getValue(partialTicks);
    }

    public void pulse() {
        this.bulbGlow.setValue(2);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.platformIdentifier = componentInput.get(WarfareDataComponents.ROCKET_CONTROLLER_PLATFORM);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        if (this.platformIdentifier != null)
            tag.putUUID("PlatformIdentifier", this.platformIdentifier);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        this.platformIdentifier = tag.hasUUID("PlatformIdentifier") ? tag.getUUID("PlatformIdentifier") : null;
    }

    public boolean platformEntityAccessible() {
        return this.cachedSelectionEntity != null && this.cachedSelectionEntity.isAlive();
    }

    private void lookForPlatform() {
        if (this.platformIdentifier == null)
            return;

        List<PlatformSelectionEntity> candidates = this.level.getEntitiesOfClass(PlatformSelectionEntity.class,
                new AABB(this.getBlockPos()).inflate(MAX_PLATFORM_SEARCH_RANGE));

        for (PlatformSelectionEntity candidate : candidates) {
            if (candidate.getPlatformIdentifier().equals(this.platformIdentifier)) {
                this.cachedSelectionEntity = candidate;
                break;
            }
        }
    }

    private void updateBlockState(BlockState newState) {
        this.level.setBlock(this.getBlockPos(), newState, Block.UPDATE_ALL);
    }

    public boolean isInAssembly() {
        return this.getBlockState().getValue(RocketControllerBlock.ASSEMBLING);
    }

    public boolean enterAssembly() {
        if (this.isInAssembly())
            return false;

        this.updateBlockState(this.getBlockState().setValue(RocketControllerBlock.ASSEMBLING, true));
        return true;
    }

    public boolean exitAssembly() {
        if (!this.isInAssembly())
            return false;

        this.updateBlockState(this.getBlockState().setValue(RocketControllerBlock.ASSEMBLING, false));
        return true;
    }
}
