package be.raft.warfare.shield;

import be.raft.warfare.network.S2C.ShieldCreatePacket;
import be.raft.warfare.network.S2C.ShieldDiscardPacket;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class Shield {
    private static final int COOLDOWN = 20 * 100;

    public static Codec<Shield> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("controller").forGetter(Shield::controllerPos),
                    BlockPos.CODEC.fieldOf("from").forGetter(Shield::from),
                    BlockPos.CODEC.fieldOf("to").forGetter(Shield::to),
                    Codec.INT.fieldOf("attackCooldown").forGetter(Shield::attackCooldown),
                    Codec.FLOAT.fieldOf("stressMultiplier").forGetter(Shield::stressMultiplier),
                    Codec.BOOL.fieldOf("forRemoval").forGetter(Shield::markedForRemoval)
            ).apply(instance, Shield::new)
    );

    private final BlockPos controllerPos;
    private AABB boundingBox;

    // Server side only
    private final Set<ServerPlayer> trackingPlayers;

    private int attackCooldown;
    private float stressMultiplier;

    // Special mechanism to prevent cheesing the shield by quickly breaking it and placing it back.
    // This imposes that the shield must come back to a normal state before being actually removed.
    // This way if removed by a player the shield stays in memory, while being disabled, if the controller is replaced it will keep the shield.
    private boolean removal;
    private boolean removed;

    public Shield(BlockPos controllerPos, AABB boundingBox) {
        this(controllerPos, boundingBox, 0, 1, false);
    }

    public Shield(BlockPos controllerPos, AABB boundingBox, boolean forRemoval) {
        this(controllerPos, boundingBox, 0, 1, forRemoval);
    }

    public Shield(BlockPos controllerPos, BlockPos from, BlockPos to, int attackCooldown, float stressMultiplier) {
        this(controllerPos, AABB.encapsulatingFullBlocks(from, to), attackCooldown, stressMultiplier, false);
    }

    private Shield(BlockPos controllerPos, BlockPos from, BlockPos to, int attackCooldown, float stressMultiplier, boolean forRemoval) {
        this(controllerPos, AABB.encapsulatingFullBlocks(from, to), attackCooldown, stressMultiplier, forRemoval);
    }

    public Shield(BlockPos controllerPos, AABB boundingBox, int attackCooldown, float stressMultiplier) {
        this(controllerPos, boundingBox, attackCooldown, stressMultiplier, false);
    }

    private Shield(BlockPos controllerPos, AABB boundingBox, int attackCooldown, float stressMultiplier, boolean forRemoval) {
        this.controllerPos = controllerPos;
        this.boundingBox = boundingBox;

        this.attackCooldown = attackCooldown;
        this.stressMultiplier = stressMultiplier;

        this.trackingPlayers = new HashSet<>();
        this.removal = forRemoval;
    }

    public void tick(LevelAccessor level) {
        if (level.isClientSide() || this.removed)
            return;

        boolean changed = false;

        if (this.attackCooldown == 0 && this.stressMultiplier == 0)
            return;

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
            changed = true;
        }

        if (this.stressMultiplier > 1f && this.attackCooldown == 0) {
            this.stressMultiplier -= 0.01f;
            changed = true;
        }

        if (changed)
            this.setChanged(level, false);

        if (!changed && this.removal)
            this.remove(level);
    }

    public void damage(int damage) {
        this.attackCooldown = COOLDOWN;
        this.stressMultiplier = this.stressMultiplier + damage;
    }

    public float stressMultiplier() {
        return this.stressMultiplier;
    }

    public int attackCooldown() {
        return this.attackCooldown;
    }

    public @NotNull AABB boundingBox() {
        return this.boundingBox;
    }

    public @NotNull BlockPos controllerPos() {
        return this.controllerPos;
    }

    public @NotNull BlockPos from() {
        int x = Mth.floor(this.boundingBox.minX);
        int y = Mth.floor(this.boundingBox.minY);
        int z = Mth.floor(this.boundingBox.minZ);

        return new BlockPos(x, y, z);
    }

    public @NotNull BlockPos to() {
        int x = Mth.floor(this.boundingBox.maxX);
        int y = Mth.floor(this.boundingBox.maxY);
        int z = Mth.floor(this.boundingBox.maxZ);

        return new BlockPos(x, y, z);
    }

    public void markForRemoval() {
        Preconditions.checkState(!this.removed, "Shield is marked as removed!");
        this.removal = true;
    }

    public void unmarkForRemoval() {
        Preconditions.checkState(!this.removed, "Shield is marked as removed!");
        this.removal = false;
    }

    public boolean markedForRemoval() {
        return this.removal || this.removed;
    }

    public boolean removed() {
        return this.removed;
    }

    private void remove(LevelAccessor level) {
        Preconditions.checkState(!this.removed, "Shield is marked as removed!");

        this.removed = true;

        ShieldStore store = ShieldManager.getStore(level);
        store.remove(this.controllerPos);

        if (level.isClientSide())
            return;

        ShieldDiscardPacket packet = new ShieldDiscardPacket(this.controllerPos);
        this.trackingPlayers.forEach(player -> CatnipServices.NETWORK.sendToClient(player, packet));
    }

    public void addTrackingPlayer(ServerPlayer player) {
        Preconditions.checkState(!this.removed, "Shield is marked as removed!");

        if (this.trackingPlayers.add(player))
            CatnipServices.NETWORK.sendToClient(player, new ShieldCreatePacket(this));
    }

    public void removeTrackingPlayer(ServerPlayer player) {
        Preconditions.checkState(!this.removed, "Shield is marked as removed!");

        if (this.trackingPlayers.remove(player))
            CatnipServices.NETWORK.sendToClient(player, new ShieldDiscardPacket(this.controllerPos));
    }

    public void setSize(int radius, LevelAccessor level) {
        Preconditions.checkState(!this.removed, "Shield is marked as removed!");

        this.boundingBox = new AABB(this.controllerPos).inflate(radius);
        this.setChanged(level, true);
    }

    private void setChanged(LevelAccessor level, boolean resize) {
        ShieldStore store = ShieldManager.getStore(level);
        store.markAsChanged(this, resize);

        if (!resize || level.isClientSide())
            return;

        ShieldCreatePacket packet = new ShieldCreatePacket(this);
        this.trackingPlayers.forEach(player -> CatnipServices.NETWORK.sendToClient(player, packet));
    }
}
