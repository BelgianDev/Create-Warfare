package be.raft.warfare.shield;

import be.raft.warfare.network.S2C.ShieldUpdatePacket;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShieldStore extends SavedData {
    private final ObjectArrayList<ShieldEntry> shields;
    private final Long2ObjectOpenHashMap<ObjectArrayList<ShieldEntry>> chunkIndex;

    public ShieldStore() {
        this.shields = new ObjectArrayList<>();
        this.chunkIndex = new Long2ObjectOpenHashMap<>();
    }

    public ShieldStore(CompoundTag tag, HolderLookup.Provider provider) {
        this();
        this.load(tag, provider);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        ListTag shieldsTag = new ListTag(Tag.TAG_COMPOUND);

        for (ShieldEntry entry : this.shields) {
            shieldsTag.add(ShieldEntry.CODEC.encodeStart(NbtOps.INSTANCE, entry).getOrThrow());
        }

        tag.put("shields", shieldsTag);
        return tag;
    }

    public void load(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        this.shields.clear();
        ListTag shieldsTag = tag.getList("shields", Tag.TAG_COMPOUND);

        for (Tag value : shieldsTag) {
            ShieldEntry entry = ShieldEntry.CODEC.parse(NbtOps.INSTANCE, value).getOrThrow();
            this.shields.add(entry);
        }

        this.rebuildChunkIndexes();
    }

    public void add(ShieldEntry entry, ServerLevel level) {
        this.shields.add(entry);

        this.setDirty();
        this.indexEntryInto(entry);
    }

    public void putOrUpdate(ShieldEntry entry) {
        for (int i = 0; i < this.shields.size(); i++) {
            ShieldEntry existing = this.shields.get(i);
            if (existing.origin().equals(entry.origin())) {
                this.shields.set(i, entry);
                this.rebuildChunkIndexes();
                this.setDirty();
                return;
            }
        }

        this.shields.add(entry);
        this.indexEntryInto(entry);
        this.setDirty();
    }

    public void remove(BlockPos pos) {
        if (!this.shields.removeIf(entry -> entry.origin().equals(pos)))
            return;

        this.setDirty();
        this.rebuildChunkIndexes();
    }

    public List<ShieldEntry> getAll() {
        return this.shields;
    }

    public ShieldEntry getShieldContaining(Vec3 pos) {
        long key = ChunkPos.asLong((int) Math.floor(pos.x) >> 4, (int) Math.floor(pos.z) >> 4);

        ObjectArrayList<ShieldEntry> candidates = this.chunkIndex.get(key);
        if (candidates == null)
            return null;

        for (ShieldEntry entry : candidates) {
            if (entry.boundingBox().contains(pos)) {
                return entry;
            }
        }

        return null;
    }

    public ShieldEntry getFirstIntersecting(AABB query) {
        int minCx = SectionPos.blockToSectionCoord(Mth.floor(query.minX));
        int maxCx = SectionPos.blockToSectionCoord(Mth.floor(query.maxX));
        int minCz = SectionPos.blockToSectionCoord(Mth.floor(query.minZ));
        int maxCz = SectionPos.blockToSectionCoord(Mth.floor(query.maxZ));


        ObjectOpenHashSet<ShieldEntry> seen = new ObjectOpenHashSet<>();

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                ObjectArrayList<ShieldEntry> candidates = this.chunkIndex.get(ChunkPos.asLong(cx, cz));
                if (candidates == null)
                    continue;

                for (ShieldEntry entry : candidates) {
                    if (!seen.add(entry)) continue;
                    if (entry.boundingBox().intersects(query)) {
                        return entry;
                    }
                }
            }
        }
        return null;
    }

    public List<ShieldEntry> getShieldsInChunk(ChunkPos pos) {
        List<ShieldEntry> shields = this.chunkIndex.get(ChunkPos.asLong(pos.x, pos.z));
        if (shields == null)
            return List.of();

        return List.copyOf(shields);
    }

    public List<ShieldEntry> getNearby(Vec3 pos, double radius) {
        int cx = SectionPos.blockToSectionCoord(Mth.floor(pos.x));
        int cz = SectionPos.blockToSectionCoord(Mth.floor(pos.z));

        int chunkRadius = Math.max(0, (int) Math.ceil(radius / 16.0));

        ObjectOpenHashSet<ShieldEntry> seen = new ObjectOpenHashSet<>();
        ObjectArrayList<ShieldEntry> out = new ObjectArrayList<>();

        double r2 = radius * radius;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ObjectArrayList<ShieldEntry> candidates = this.chunkIndex.get(ChunkPos.asLong(cx + dx, cz + dz));
                if (candidates == null) continue;

                for (ShieldEntry entry : candidates) {
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
        for (ShieldEntry shield : this.shields) {
            this.indexEntryInto(shield);
        }
    }

    private void indexEntryInto(ShieldEntry entry) {
        AABB bb = entry.boundingBox();

        int minCx = SectionPos.blockToSectionCoord(Mth.floor(bb.minX));
        int maxCx = SectionPos.blockToSectionCoord(Mth.floor(bb.maxX));
        int minCz = SectionPos.blockToSectionCoord(Mth.floor(bb.minZ));
        int maxCz = SectionPos.blockToSectionCoord(Mth.floor(bb.maxZ));

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                this.chunkIndex.computeIfAbsent(key, k -> new ObjectArrayList<>()).add(entry);
            }
        }
    }
}
