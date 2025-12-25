package be.raft.warfare;

import be.raft.warfare.content.*;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CreateWarfare.ID)
public class CreateWarfare {
    public static final String ID = "warfare";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
            .defaultCreativeTab(WarfareCreativeTabs.WARFARE_TAB.getKey())
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );


    public CreateWarfare(IEventBus bus, ModContainer container) {
        WarfareCreativeTabs.register(bus);
        REGISTRATE.registerEventListeners(bus);

        WarfarePartialModels.prepare();
        WarfareBlockEntities.register();
        WarfareBlocks.register();
        WarfareItems.register();
        WarfareEntities.register();
    }

    public static ResourceLocation asLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

}
