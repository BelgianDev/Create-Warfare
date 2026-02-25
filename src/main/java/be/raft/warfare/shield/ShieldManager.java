package be.raft.warfare.shield;

import be.raft.warfare.network.S2C.ShieldUpdatePacket;
import net.createmod.catnip.data.WorldAttached;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Server and client side component keeps shields and syncs them across the network.
 */

public class ShieldManager {
    private static final WorldAttached<ShieldStore> STORES = new WorldAttached<>(ShieldManager::createStore);

    private static ShieldStore createStore(LevelAccessor level) {
        if (level.isClientSide())
            return new ShieldStore(); // Shields are synced across the network

        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(ShieldStore::new, ShieldStore::new, DataFixTypes.LEVEL), "shields");
    }

    public static ShieldStore getStore(LevelAccessor level) {
        return STORES.get(level);
    }

    public static void initialize() {
        NeoForge.EVENT_BUS.addListener(ShieldManager::onChunkSent);
        NeoForge.EVENT_BUS.addListener(ShieldManager::onChunkDropped);
    }

    public void sendToTrackingPlayers(ShieldEntry entry, ServerLevel level, boolean removal) {
        int minCx = SectionPos.blockToSectionCoord(Mth.floor(entry.boundingBox().minX));
        int maxCx = SectionPos.blockToSectionCoord(Mth.floor(entry.boundingBox().maxX));
        int minCz = SectionPos.blockToSectionCoord(Mth.floor(entry.boundingBox().minZ));
        int maxCz = SectionPos.blockToSectionCoord(Mth.floor(entry.boundingBox().maxZ));

        ShieldUpdatePacket packet = removal ? ShieldUpdatePacket.remove(entry) : ShieldUpdatePacket.addOrUpdate(entry);

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                // TODO: Optimize this, right now if a player is tracking multiple chunks where the shield is also present, it will receive the packet multiple times.
                PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(cx, cz), packet);
            }
        }
    }

    private static void onChunkSent(ChunkWatchEvent.Sent event) {
        ShieldStore store = ShieldManager.getStore(event.getLevel());
        List<ShieldEntry> shields = store.getShieldsInChunk(event.getPos());

        for (ShieldEntry shield : shields) {
            CatnipServices.NETWORK.sendToClient(event.getPlayer(), ShieldUpdatePacket.addOrUpdate(shield));
        }
    }

    private static void onChunkDropped(ChunkWatchEvent.UnWatch event) {
        ShieldStore store = ShieldManager.getStore(event.getLevel());
        List<ShieldEntry> shields = store.getShieldsInChunk(event.getPos());

        for (ShieldEntry shield : shields) {
            CatnipServices.NETWORK.sendToClient(event.getPlayer(), ShieldUpdatePacket.remove(shield));
        }
    }
}
