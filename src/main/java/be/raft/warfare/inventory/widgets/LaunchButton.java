package be.raft.warfare.inventory.widgets;

import be.raft.warfare.registry.WarfareGuiTextures;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllKeys;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LaunchButton extends AbstractSimiWidget {

    public LaunchButton(int x, int y) {
        super(x, y, 47, 47);
    }

    @Override
    public void doRender(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible)
            return;

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.getHeight();
        WarfareGuiTextures button = WarfareGuiTextures.LAUNCH_BUTTON;

        if (!this.active) {
            button = WarfareGuiTextures.LAUNCH_BUTTON_DISABLED;
        } else if (this.isHovered) {
            if (AllKeys.isMouseButtonDown(0))
                button = WarfareGuiTextures.LAUNCH_BUTTON_DOWN;
            else
                button = WarfareGuiTextures.LAUNCH_BUTTON_HOVER;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        button.render(graphics, this.getX(), this.getY());
    }

    public void setTooltip(@NotNull Component tooltip) {
        super.setTooltip(Tooltip.create(tooltip));
    }
}
