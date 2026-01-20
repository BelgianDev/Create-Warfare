package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

public class WarfareItems {
    private static final CreateRegistrate REGISTRATE = CreateWarfare.REGISTRATE;

    public static final ItemEntry<Item> BULLET = REGISTRATE.item("bullet", Item::new)
            .properties(prop -> prop.stacksTo(64))
            .register();

    public static void register() {}
}
