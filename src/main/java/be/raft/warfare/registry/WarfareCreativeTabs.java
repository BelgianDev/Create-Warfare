package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class WarfareCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateWarfare.ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WARFARE_TAB = REGISTER.register("warfare",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.warfare"))
                    .icon(WarfareBlocks.MECHANICAL_TURRET::asStack)
                    .build());

    public static void register(@NotNull IEventBus bus) {
        REGISTER.register(bus);
    }
}
