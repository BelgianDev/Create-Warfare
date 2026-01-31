package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import com.google.common.base.Preconditions;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter class that allows the use of the same simplicity of {@link AllGuiTextures} while allowing any enum to use it.
 * <br><br>
 * The code contained in this file is mostly copied from {@link AllGuiTextures}, which is licensed under <a href="https://github.com/Creators-of-Create/Create?tab=License-1-ov-file#readme">MIT - The Create Team / The Creators of Create</a>
 */
public enum WarfareGuiTextures implements ScreenElement, TextureSheetSegment {

    // Rocket Controller
    ROCKET_CONTROLLER("rocket_controller", 200, 158),
    ROCKET("rocket_controller", 0, 169, 50, 90),
    MISSILE("rocket_controller", 51, 169, 50, 90),

    LAUNCH_BUTTON("rocket_controller", 256 - 47, 0, 47, 47),
    LAUNCH_BUTTON_HOVER("rocket_controller", 256 - 47, 47, 47, 47),
    LAUNCH_BUTTON_DOWN("rocket_controller", 256 - 47, 94, 47, 47),
    LAUNCH_BUTTON_DISABLED("rocket_controller", 256 - 47, 141, 47, 47),
    ;

    private final ResourceLocation location;
    private final int width, height;
    private final int startX, startY;

    WarfareGuiTextures(@NotNull String location, int startX, int startY, int width, int height) {
        Preconditions.checkNotNull(location, "Texture location cannot be null!");

        this.location = CreateWarfare.asLoc("textures/gui/" + location + ".png");
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;
    }

    WarfareGuiTextures(@NotNull String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    @Override
    public int getStartX() {
        return this.startX;
    }

    @Override
    public int getStartY() {
        return this.startY;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(@NotNull GuiGraphics graphics, int x, int y) {
        graphics.blit(this.location, x, y, this.startX, this.startY, this.width, this.height);
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, Color color) {
        this.bind();
        UIRenderHelper.drawColoredTexture(graphics, color, x, y, this.startX, this.startY, this.width, this.height);
    }

    @Override
    public @NotNull ResourceLocation getLocation() {
        return this.location;
    }
}
