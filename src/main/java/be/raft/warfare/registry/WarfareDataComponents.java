package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class WarfareDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateWarfare.ID);

    public static final DataComponentType<BlockPos> ROCKET_CONTROLLER_SELECTED_POS = register("rocket_controller_selected_pos",
            builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
    );

    private static <T>DataComponentType<T> register(@NotNull String name, @NotNull UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> component = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> component);
        return component;
    }

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
