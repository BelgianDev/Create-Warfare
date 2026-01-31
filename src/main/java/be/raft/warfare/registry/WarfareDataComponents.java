package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class WarfareDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateWarfare.ID);

    public static final DataComponentType<UUID> ROCKET_CONTROLLER_PLATFORM = register("rocket_controller_platform",
            builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC)
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
