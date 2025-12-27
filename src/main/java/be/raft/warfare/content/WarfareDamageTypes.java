package be.raft.warfare.content;

import be.raft.warfare.CreateWarfare;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class WarfareDamageTypes {
    public static final ResourceKey<DamageType> BULLET_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, CreateWarfare.asLoc("bullet"));

    public static Holder.Reference<DamageType> createHolder(RegistryAccess access, ResourceKey<DamageType> type) {
        return access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
    }
}
