package be.raft.warfare.rocket.platform;

import net.minecraft.world.phys.AABB;

import java.util.UUID;

public record RocketPlatform(UUID id, int y, int minX, int minZ, int maxX, int maxZ) {
    public RocketPlatform {
    }

    public AABB getBoundingBox() {
        return new AABB(this.minX, this.y, this.minZ, this.maxX, this.y + 1, this.maxZ);
    }
}
