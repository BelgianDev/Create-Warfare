package be.raft.warfare.listener;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.network.S2C.ShieldUpdatePacket;
import be.raft.warfare.shield.ShieldEntry;
import be.raft.warfare.shield.ShieldManager;
import be.raft.warfare.shield.ShieldStore;
import net.createmod.catnip.platform.CatnipServices;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

import java.util.List;

@EventBusSubscriber(modid = CreateWarfare.ID)
public class ChunkListener {

    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        ShieldStore store = ShieldManager.getStore(event.getLevel());
        List<ShieldEntry> shields = store.getShieldsInChunk(event.getPos());

        for (ShieldEntry shield : shields) {
            CatnipServices.NETWORK.sendToClient(event.getPlayer(), ShieldUpdatePacket.addOrUpdate(shield));
        }
    }

    @SubscribeEvent
    public static void onChunkDropped(ChunkWatchEvent.UnWatch event) {
        ShieldStore store = ShieldManager.getStore(event.getLevel());
        List<ShieldEntry> shields = store.getShieldsInChunk(event.getPos());

        for (ShieldEntry shield : shields) {
            CatnipServices.NETWORK.sendToClient(event.getPlayer(), ShieldUpdatePacket.remove(shield));
        }
    }
}
