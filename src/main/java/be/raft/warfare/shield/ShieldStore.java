package be.raft.warfare.shield;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShieldStore extends SavedData {
    private final Map<BlockPos, Shield> shields;
    private final Long2ObjectOpenHashMap<ObjectArrayList<Shield>> chunkIndex;

    public ShieldStore() {
        this.shields = new ConcurrentHashMap<>();
        this.chunkIndex = new Long2ObjectOpenHashMap<>();
    }

    public ShieldStore(CompoundTag tag, HolderLookup.Provider provider) {
        this();
        this.load(tag, provider);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag shieldsTag = new ListTag(Tag.TAG_COMPOUND);

        for (Shield entry : this.shields.values()) {
            shieldsTag.add(Shield.CODEC.encodeStart(NbtOps.INSTANCE, entry).getOrThrow());
        }

        tag.put("shields", shieldsTag);
        return tag;
    }

    public void load(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        this.shields.clear();
        ListTag shieldsTag = tag.getList("shields", Tag.TAG_COMPOUND);

        for (Tag value : shieldsTag) {
            Shield entry = Shield.CODEC.parse(NbtOps.INSTANCE, value).getOrThrow();
            this.shields.put(entry.controllerPos(), entry);
        }

        this.rebuildChunkIndexes();
    }

    public void add(Shield entry) {
        this.shields.put(entry.controllerPos(), entry);

        this.setDirty();
        this.rebuildChunkIndexes();
    }

    public void markAsChanged(Shield entry, boolean resize) {
        this.setDirty();

        if (resize || !this.shields.containsKey(entry.controllerPos())) {
            this.add(entry);
            return;
        }
    }

    public void remove(BlockPos pos) {
        if (this.shields.remove(pos) == null)
            return;

        this.setDirty();
        this.rebuildChunkIndexes();
    }

    public List<Shield> getAll() {
        return List.copyOf(this.shields.values());
    }

    public Shield get(BlockPos pos) {
        return this.shields.get(pos);
    }

    public Shield getShieldContaining(Vec3 pos) {
        long key = ChunkPos.asLong((int) Math.floor(pos.x) >> 4, (int) Math.floor(pos.z) >> 4);

        ObjectArrayList<Shield> candidates = this.chunkIndex.get(key);
        if (candidates == null)
            return null;

        for (Shield entry : candidates) {
            if (entry.boundingBox().contains(pos)) {
                return entry;
            }
        }

        return null;
    }

    public Shield getFirstIntersecting(AABB query) {
        int minCx = SectionPos.blockToSectionCoord(Mth.floor(query.minX));
        int maxCx = SectionPos.blockToSectionCoord(Mth.floor(query.maxX));
        int minCz = SectionPos.blockToSectionCoord(Mth.floor(query.minZ));
        int maxCz = SectionPos.blockToSectionCoord(Mth.floor(query.maxZ));


        ObjectOpenHashSet<Shield> seen = new ObjectOpenHashSet<>();

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                ObjectArrayList<Shield> candidates = this.chunkIndex.get(ChunkPos.asLong(cx, cz));
                if (candidates == null)
                    continue;

                for (Shield entry : candidates) {
                    if (!seen.add(entry)) continue;
                    if (entry.boundingBox().intersects(query)) {
                        return entry;
                    }
                }
            }
        }
        return null;
    }

    public List<Shield> getShieldsInChunk(ChunkPos pos) {
        List<Shield> shields = this.chunkIndex.get(ChunkPos.asLong(pos.x, pos.z));
        if (shields == null)
            return List.of();

        return List.copyOf(shields);
    }

    public List<Shield> getNearby(Vec3 pos, double radius) {
        int cx = SectionPos.blockToSectionCoord(Mth.floor(pos.x));
        int cz = SectionPos.blockToSectionCoord(Mth.floor(pos.z));

        int chunkRadius = Math.max(0, (int) Math.ceil(radius / 16.0));

        ObjectOpenHashSet<Shield> seen = new ObjectOpenHashSet<>();
        ObjectArrayList<Shield> out = new ObjectArrayList<>();

        double r2 = radius * radius;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ObjectArrayList<Shield> candidates = this.chunkIndex.get(ChunkPos.asLong(cx + dx, cz + dz));
                if (candidates == null) continue;

                for (Shield entry : candidates) {
                    if (!seen.add(entry)) continue;

                    if (entry.boundingBox().distanceToSqr(pos) <= r2) {
                        out.add(entry);
                    }
                }
            }
        }

        return out;
    }

    private void rebuildChunkIndexes() {
        this.chunkIndex.clear();
        for (Shield shield : this.shields.values()) {
            this.indexEntryInto(shield);
        }
    }

    private void indexEntryInto(Shield entry) {
        SectionPos minChunkPos = SectionPos.of(entry.from());
        SectionPos maxChunkPos = SectionPos.of(entry.to());

        for (int cx = minChunkPos.x(); cx <= maxChunkPos.x(); cx++) {
            for (int cz = minChunkPos.z(); cz <= maxChunkPos.z(); cz++) {
                long key = ChunkPos.asLong(cx, cz);
                this.chunkIndex.computeIfAbsent(key, k -> new ObjectArrayList<>()).add(entry);
            }
        }
    }
}
