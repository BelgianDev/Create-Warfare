package be.raft.warfare.block.entity;

import be.raft.warfare.inventory.handler.TurretItemHandler;
import be.raft.warfare.registry.WarfareIcons;
import be.raft.warfare.registry.WarfareItems;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;

/**
 * Abstract Turret Block Entity, used as the base of all other turrets.
 * <br><br>
 * The way the turrets work is to target either an entity directly or a position the server sends them.
 * <br><br>
 * A target entity offers way more precision for animations and shooting,
 * but they would only work for entities the client knows about, which is not always the case.
 * <br>
 * A target position on the other side can target positions really far away and also provides a way for future features
 * such as a radar to send a target position to the turret.
 *
 * @param <E> entity type the turret can target.
 */
public abstract class TurretBlockEntity<E extends LivingEntity> extends KineticBlockEntity {
    private static final int TARGET_REFRESH_RATE = 4 * 20; // TODO: Replace with a config option.
    private static final int DEFAULT_CLIENT_REFRESH_RATE = 5 * 20;

    protected ScrollOptionBehaviour<TargetingMode> targetingMode;

    // Server
    private int targetRefreshCounter;
    private boolean targetRefreshingEnabled;

    private int clientRefreshRate;
    private int clientRefreshCounter;

    // Inventory (Both)
    protected final TurretItemHandler itemHandler;
    private ItemStack magazine;

    // Targets (Shared values)
    private @Nullable E target;
    private @Nullable Position targetPosition;

    // Both (Infer)
    private boolean targetChanged;
    private Position lastTargetPos;

    public TurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.targetRefreshCounter = TARGET_REFRESH_RATE;
        this.targetRefreshingEnabled = true;

        this.clientRefreshRate = DEFAULT_CLIENT_REFRESH_RATE;
        this.clientRefreshCounter = this.clientRefreshRate;

        this.targetChanged = false;

        this.itemHandler = new TurretItemHandler(this);
        this.magazine = ItemStack.EMPTY;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        this.targetingMode = new ScrollOptionBehaviour<>(TargetingMode.class,
                Component.translatable("ui.warfare.turret.targeting_mode"), this, this.targetingBoxTransform());

        this.targetingMode.setValue(2);

        behaviours.add(this.targetingMode);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void tick() {
        super.tick();

        this.tickMovement();
        if (this.getSpeed() == 0)
            return; // The turret is not powered at all, so we don't do anything.

        this.updateTargetPos();
        if (this.targetChanged) {
            this.onTargetChange();
            this.targetChanged = false;
        }

        if (this.hasTarget() && this.turretSettled()) {
            this.shoot();

            if (this.level.isClientSide)
                return;

            this.targetRefreshCounter--;
            if (this.targetRefreshCounter <= 0 && this.targetRefreshingEnabled)
                this.lookForTarget();
        }

        if (this.level.isClientSide)
            return;

        this.clientRefreshCounter--;
        if (this.clientRefreshCounter <= 0 && this.hasTarget()) {
            this.clientRefreshCounter = this.clientRefreshRate;
            this.sendData();
        }

        if (!this.hasTarget() && this.targetRefreshingEnabled)
            this.lookForTarget();
    }

    @Override
    public @NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void updateTargetPos() {
        if (!this.isTargetEntity())
            return; // Not an entity, no need to change anything.

        if (this.lastTargetPos == null || !this.lastTargetPos.equals(this.target.position())) {
            this.lastTargetPos = this.target.position();
            this.targetChanged = true;
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);

        if (!clientPacket)
            return;

        if (!this.hasTarget()) {
            compound.putInt("target", -1);
            return;
        }

        Position pos = this.targetPosition();

        compound.putInt("target", this.target == null ? -1 : this.target.getId());
        compound.putDouble("x", pos.x());
        compound.putDouble("y", pos.y());
        compound.putDouble("z", pos.z());
    }

    @Override
    @SuppressWarnings("unchecked")
    @OverridingMethodsMustInvokeSuper
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        if (!clientPacket)
            return;

        int entityId = compound.getInt("target");
        if (!compound.contains("x") && entityId == -1) { // Clear entity target.
            this.target = null;
            this.targetPosition = null;

            return;
        }

        Entity entity = this.level.getEntity(entityId);
        if (entity != null) {
            if (entity.equals(this.target))
                return;

            this.target = (E) entity;
            this.targetPosition = null;
            this.targetChanged = true;

            return;
        }

        // Fall back to the recorded position in-case of the client doesn't know about the entity.
        Vec3 pos = new Vec3(compound.getDouble("x"), compound.getDouble("y"), compound.getDouble("z"));
        if (pos.equals(this.targetPosition))
            return;

        this.targetPosition = pos;
        this.target = null;
        this.targetChanged = true;
    }

    private void resetClientRefreshCounter() {
        this.clientRefreshCounter = this.clientRefreshRate;
    }

    private boolean isValidTarget(E entity) {
        return this.targetingMode.get().testEntity(entity) && this.canTarget(entity);
    }

    /**
     * Retrieve the magazine item the turret uses.
     *
     * @return magazine item.
     */
    public ItemStack getMagazine() {
        return this.magazine;
    }

    /**
     * Sets the magazine of the turret.
     *
     * @param magazine magazine item to set.
     */
    public void setMagazine(ItemStack magazine) {
        this.magazine = magazine;
    }

    /**
     * Checks whether the turret can accept the item.
     * <br>
     * This does not check whether the stack can fully be input, so take into account some remaining could stay.
     *
     * @param stack stack to check.
     *
     * @return {@code true} if the stack can be used as a magazine, {@code false} otherwise.
     */
    public final boolean canAcceptMagazine(ItemStack stack) {
        if (stack == null || !this.isItemValidMagazine(stack))
            return false;

        if (this.magazine.isEmpty() || this.magazine.getCount() == this.magazine.getMaxStackSize())
            return true;

        return this.magazine.is(stack.getItem());
    }

    /**
     * Checks whether the turret can accept the given item as a magazine.
     *
     * @param stack stack to check.
     *
     * @return {@code true} if the stack can be used as a magazine, {@code false} otherwise.
     *
     * @implNote this should only check the item stack state, not the actual state of the turret.
     * This is also used by {@link net.neoforged.neoforge.items.IItemHandler#isItemValid(int, ItemStack)},
     * and checking the turret state could worsen performance.
     *
     * @see net.neoforged.neoforge.items.IItemHandler#isItemValid(int, ItemStack)
     */
    public boolean isItemValidMagazine(@NotNull ItemStack stack) {
        // TODO: Eventually change the bullets to be ItemComponent-driven
        return stack.getItem() == WarfareItems.BULLET.asItem();
    }

    /**
     * Used to set the rate to which the server will resend the target position to the client.
     * <br><br>
     * This is used in the case where the client doesn't know about the target entity, and refreshing the pos will update the pos on the client.
     * <br>
     * Usually a slow rate is enough, as if the client doesn't know about the entity, it means it's far away and the player won't be able to perceive that
     * aiming is off.
     * <br><br>
     * <b>Note:</b> every time the client receives a refresh packet from the server, it will recheck if the entity is available on the client,
     * so it can be slow, but this will also affect how fast the client will pick up with the entity.
     *
     * @param ticks tick rate, by default, it's 5 seconds.
     */
    protected final void setClientRefreshRate(int ticks) {
        this.clientRefreshRate = ticks;
    }

    /**
     * Forces the turret to look for a new target.
     */
    public void lookForTarget() {
        AABB targetingBoundingBox = this.getTargetingBoundingBox();

        List<E> entities = this.level.getEntitiesOfClass(this.targetClass(), targetingBoundingBox, this::isValidTarget);
        E entity = this.level.getNearestEntity(entities, TargetingConditions.DEFAULT, null,
                this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());

        if (entity == null) {
            this.clearTarget();
            return;
        }

        this.setTarget(entity);
    }

    /**
     * Clears the target of the turret.
     */
    public void clearTarget() {
        if (this.target == null && this.targetPosition == null)
            return;

        this.target = null;
        this.targetPosition = null;

        this.notifyUpdate();
    }

    /**
     * Sets the target of the turret to the given entity.
     *
     * @param target target entity.
     *
     * @return {@code true} if the target was successfully set, {@code false} otherwise.
     */
    public final boolean setTarget(@NotNull E target) {
        Preconditions.checkState(!this.level.isClientSide, "Cannot set target on client side!");
        Preconditions.checkNotNull(target, "Target entity cannot be null!");

        if (!this.isValidTarget(target))
            return false;

        this.targetRefreshCounter = TARGET_REFRESH_RATE;
        if (this.target == target)
            return true;

        this.targetPosition = null;
        this.target = target;
        this.targetChanged = true;

        this.resetClientRefreshCounter();
        this.notifyUpdate();

        return true;
    }

    /**
     * Sets the target of the turret to the given position.
     *
     * @param target target position.
     */
    public final void setTarget(@NotNull Position target) {
        Preconditions.checkState(!this.level.isClientSide, "Cannot set target on client side!");
        Preconditions.checkNotNull(target, "Target entity cannot be null!");

        this.targetRefreshCounter = TARGET_REFRESH_RATE;
        if (this.targetPosition == target)
            return;

        this.targetPosition = target;
        this.target = null;

        this.targetChanged = true;

        this.resetClientRefreshCounter();
        this.notifyUpdate();
    }

    /**
     * Sets whether the turret should refresh its target automatically.
     *
     * @param enabled {@code true} to enable automatic refreshing, {@code false} to disable it.
     */
    public final void setTargetRefreshingEnabled(boolean enabled) {
        Preconditions.checkState(!this.level.isClientSide, "Cannot set refreshing on client side!");
        this.targetRefreshingEnabled = enabled;
    }

    /**
     * Whether the turret will refresh its target after a bit of time.
     *
     * @return {@code true} if the turret will refresh its target, {@code false} otherwise.
     */
    public final boolean isTargetRefreshingEnabled() {
        return this.targetRefreshingEnabled;
    }

    /**
     * Retrieve the target position.
     *
     * @return the target position, or {@code null} if the turret doesn't target a position.
     */
    public final @Nullable Position targetPosition() {
        return this.target != null ? this.target.position() : this.targetPosition;
    }

    /**
     * Checks whether the turret has a target.
     *
     * @return {@code true} if the turret has a target, {@code false} otherwise.
     */
    public final boolean hasTarget() {
        if (this.targetPosition != null)
            return true;

        return this.target != null && this.isValidTarget(this.target);
    }

    /**
     * Checks whether the turret currently targets an entity.
     *
     * @return {@code true} if the turret targets an entity, {@code false} otherwise.
     */
    public final boolean isTargetEntity() {
        return this.target != null;
    }

    /**
     * Checks whether the turret currently targets a position.
     *
     * @return {@code true} if the turret targets a position, {@code false} otherwise.
     */
    public final boolean isTargetPosition() {
        return this.targetPosition != null;
    }

    /**
     * Retrieve the entity target.
     *
     * @return entity target, or {@code null} if the turret doesn't target an entity.
     */
    public final @Nullable E getTarget() {
        return this.target;
    }

    /**
     * Called when the target of the turret changes.
     */
    protected abstract void onTargetChange();

    /**
     * Called on every tick to update the turret's movement.
     */
    protected abstract void tickMovement();

    /**
     * Checks whether the turret is settled, meaning it has stopped moving, {@link #hasTarget()} is still {@code true},
     * that means the turret is now aiming at the target.
     *
     * @return {@code true} if the turret is settled, {@code false} otherwise.
     */
    public abstract boolean turretSettled();

    /**
     * Retrieve the targeting bounding box of the turret.
     *
     * @return bounding box, where the turret can target entities.
     */
    public abstract @NotNull AABB getTargetingBoundingBox();

    /**
     * Checks if the turret can target the given entity.
     *
     * @param entity the entity to check.
     *
     * @return {@code true} if the entity can be targeted, {@code false} otherwise.
     */
    public abstract boolean canTarget(@NotNull E entity);

    /**
     * Retrieve the entity class the turret can target.
     *
     * @return target class.
     */
    public abstract @NotNull Class<E> targetClass();

    /**
     * Called when the turret is supposed to shoot the target, this is called on the server and client side!
     */
    public abstract void shoot();

    /**
     * Used to properly set where the targeting box on the turret should be located.
     *
     * @return targeting box transform.
     */
    public abstract @NotNull ValueBoxTransform targetingBoxTransform();

    public enum TargetingMode implements INamedIconOptions {
        ALL(AllIcons.I_WHITELIST, entity -> entity instanceof Mob || entity instanceof Player),
        PLAYERS(WarfareIcons.PLAYER_HEAD, entity -> entity instanceof Player),
        MONSTERS(WarfareIcons.MONSTER_HEAD, entity -> entity instanceof Monster),
        PASSIVES(WarfareIcons.MOB_HEAD, entity -> entity instanceof Mob && !(entity instanceof Monster));

        private final String translationKey;
        private final AllIcons icon;
        private final Predicate<Entity> entityFilter;

        TargetingMode(AllIcons icon, Predicate<Entity> entityFilter) {
            this.icon = icon;
            this.translationKey = "ui.warfare.turret.targeting_mode." + name().toLowerCase();
            this.entityFilter = entityFilter;
        }

        @Override
        public AllIcons getIcon() {
            return this.icon;
        }

        @Override
        public String getTranslationKey() {
            return this.translationKey;
        }

        public boolean testEntity(Entity entity) {
            return this.entityFilter.test(entity);
        }
    }
}
