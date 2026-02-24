package be.raft.warfare.block.entity;

import com.google.common.base.Preconditions;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShieldCoilBlockEntity extends KineticBlockEntity {
    private @Nullable BlockPos controllerPos;
    private boolean active;

    public ShieldCoilBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void markDirty() {
        ShieldControllerBlockEntity be = this.getController();
        if (be != null)
            be.markCoilDirty();
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        if (this.controllerPos == null)
            return;

        compound.putInt("controllerY", this.controllerPos.getY());
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        if (compound.contains("controllerY"))
            this.controllerPos = this.getBlockPos().atY(compound.getInt("controllerY"));
        else this.controllerPos = null;
    }

    public void setControllerPos(@NotNull BlockPos pos) {
        Preconditions.checkState(!this.level.isClientSide, "Controller position cannot be set on the client side!");
        Preconditions.checkArgument(this.getBlockPos().atY(pos.getY()).equals(pos), "Block position must be at the same Y level as the coil");

        this.controllerPos = pos;
        this.notifyUpdate();
    }

    public void clearController() {
        Preconditions.checkState(!this.level.isClientSide, "Controller position cannot be set on the client side!");

        this.controllerPos = null;
        this.notifyUpdate();
    }

    public @Nullable BlockPos getControllerPos() {
        return this.controllerPos;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public ShieldControllerBlockEntity getController() {
        if (this.controllerPos == null)
            return null;

        BlockEntity be = this.level.getBlockEntity(this.controllerPos);
        if (be instanceof ShieldControllerBlockEntity controller)
            return controller;

        return null;
    }
}
