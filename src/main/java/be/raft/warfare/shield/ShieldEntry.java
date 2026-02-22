package be.raft.warfare.shield;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record ShieldEntry(BlockPos origin, AABB boundingBox) {
    public static Codec<ShieldEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("origin").forGetter(ShieldEntry::origin),
            Vec3.CODEC.fieldOf("from").forGetter(entry -> entry.boundingBox.getMinPosition()),
            Vec3.CODEC.fieldOf("to").forGetter(entry -> entry.boundingBox.getMaxPosition())
        ).apply(instance, ShieldEntry::new)
    );

    public static ShieldEntry span(BlockPos origin, BlockPos start, BlockPos end) {
        return new ShieldEntry(origin, new AABB(Vec3.atLowerCornerOf(start), Vec3.atLowerCornerOf(end)).expandTowards(1, 1, 1));
    }

    public ShieldEntry(BlockPos pos, Vec3 from, Vec3 to) {
        this(pos, new AABB(from, to));
    }
}
