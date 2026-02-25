package be.raft.warfare.block.entity;

import be.raft.warfare.shield.ShieldEntry;
import be.raft.warfare.shield.ShieldManager;
import be.raft.warfare.shield.ShieldStore;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.DirectionalShaftHalvesBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;

public class ShieldControllerBlockEntity extends DirectionalShaftHalvesBlockEntity {
    // TODO: Replace with a config options.
    private static final int MAX_COIL_HEIGHT = 16;
    private static final int RANGE_PER_COIL = 32;
    private static final int BASE_STRESS = 10240 / 64;

    private int coilHeight;
    private boolean coilDirty;

    public ShieldControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        this.setLazyTickRate(10);
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        compound.putInt("coilHeight", this.coilHeight);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        this.coilHeight = compound.getInt("coilHeight");

        if (!clientPacket)
            this.markCoilDirty();
    }

    public int getCoilHeight() {
        return this.coilHeight;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        if (this.coilDirty) {
            this.updateCoils();
            this.coilDirty = false;
        }
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);

        if (this.isSpeedRequirementFulfilled())
            this.activateShield();
        else
            this.deactivateShield();
    }

    public void markCoilDirty() {
        if (!this.level.isClientSide)
            this.coilDirty = true;
    }

    private void deactivateShield() {
        this.executeOnCoils(ShieldCoilBlockEntity::deactivate);
    }

    private void activateShield() {
        this.executeOnCoils(ShieldCoilBlockEntity::activate);

        if (this.level.isClientSide)
            return;

        AABB boundingBox = new AABB(this.getBlockPos()).inflate(this.coilHeight * RANGE_PER_COIL);
        ShieldEntry shield = new ShieldEntry(this.getBlockPos(), boundingBox);

        ShieldManager.getStore(this.level).add(shield, (ServerLevel) this.level);
    }

    @Override
    public float calculateStressApplied() {
        if (this.coilHeight == 0) {
            this.lastStressApplied = 1;
            return 1;
        }

        float impact = (float) (BASE_STRESS * this.coilHeight); // TODO: Change for an expontential formula.
        this.lastStressApplied = impact;
        return impact;
    }

    private void executeOnCoils(Consumer<ShieldCoilBlockEntity> consumer) {
        for (int i = 0; i < this.coilHeight; i++) {
            ShieldCoilBlockEntity coil = (ShieldCoilBlockEntity) this.level.getBlockEntity(this.getBlockPos().above(i + 1));
            consumer.accept(coil);
        }
    }

    private void updateCoils() {
        BlockPos pos = this.getBlockPos();

        boolean endReached = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < MAX_COIL_HEIGHT; i++) {
            cursor.move(Direction.UP);
            BlockEntity be = this.level.getBlockEntity(cursor);

            if (be instanceof ShieldCoilBlockEntity coil) {
                if (endReached) {
                    coil.clearController(); // Makes sure that disconnected coils are deactivated.
                    continue;
                }

                coil.setControllerPos(pos);
                continue;
            }

            if (!endReached) {
                this.coilHeight = i;
                endReached = true;
            }

            if (be instanceof ShieldControllerBlockEntity) {
                break; // If another controller is on top, we don't want to mess with its coils.
            }
         }

        if (!endReached)
            this.coilHeight = MAX_COIL_HEIGHT;

        this.notifyUpdate();
        this.notifyNetwork();
    }

    private void notifyNetwork() {
        if (!this.hasNetwork() || this.speed == 0)
            return;

        KineticNetwork network = this.getOrCreateNetwork();
        network.updateStressFor(this, calculateStressApplied());
    }
}
