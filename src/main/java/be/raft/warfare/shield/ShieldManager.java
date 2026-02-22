package be.raft.warfare.shield;

import net.createmod.catnip.data.WorldAttached;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

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
}
