package be.raft.warfare.inventory.handler;

import be.raft.warfare.block.entity.TurretBlockEntity;
import com.google.common.base.Preconditions;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class TurretItemHandler implements IItemHandler {
    private final TurretBlockEntity<?> turretBlockEntity;

    public TurretItemHandler(@NotNull TurretBlockEntity<?> blockEntity) {
        Preconditions.checkNotNull(blockEntity, "Turret block entity cannot be null!");
        this.turretBlockEntity = blockEntity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.turretBlockEntity.getMagazine();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!this.turretBlockEntity.canAcceptMagazine(stack))
            return stack;

        ItemStack currentItem = this.turretBlockEntity.getMagazine();
        if (currentItem.isEmpty()) {
            if (!simulate)
                this.turretBlockEntity.setMagazine(stack);

            return ItemStack.EMPTY;
        }

        int currentCount = currentItem.getCount();
        int fillStack = currentItem.getMaxStackSize() - currentCount;

        if (fillStack > stack.getCount()) {
            if (!simulate)
                currentItem.setCount(currentCount + stack.getCount());

            return ItemStack.EMPTY;
        }

        int remaining = stack.getCount() - fillStack;

        if (!simulate)
            currentItem.setCount(currentItem.getMaxStackSize());

        return stack.copyWithCount(remaining);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack item = this.turretBlockEntity.getMagazine().copy();
        ItemStack split = item.split(amount);

        if (!simulate)
            this.turretBlockEntity.setMagazine(item);

        return split;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Math.min(getStackInSlot(slot).getMaxStackSize(), 64);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.turretBlockEntity.isItemValidMagazine(stack);
    }
}
