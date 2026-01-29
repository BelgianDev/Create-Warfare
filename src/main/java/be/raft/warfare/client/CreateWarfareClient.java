package be.raft.warfare.client;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.rocket.platform.RocketPlatformSelectionHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CreateWarfare.ID, dist = Dist.CLIENT)
public class CreateWarfareClient {
    private final RocketPlatformSelectionHandler platformSelectionHandler = new RocketPlatformSelectionHandler();

    public CreateWarfareClient(IEventBus modBus) {
        IEventBus neoBus = NeoForge.EVENT_BUS;

        neoBus.addListener(this::onTick);
        neoBus.addListener(this::onClickInput);
    }

    private void onTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null)
            return;

        this.platformSelectionHandler.tick();
    }

    private void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null)
            return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) return;

        KeyMapping key = event.getKeyMapping();

        if (key == minecraft.options.keyUse || key == minecraft.options.keyAttack) {
            if (this.platformSelectionHandler.onMouseInput(key == minecraft.options.keyAttack))
                event.setCanceled(true);
        }
    }
}
