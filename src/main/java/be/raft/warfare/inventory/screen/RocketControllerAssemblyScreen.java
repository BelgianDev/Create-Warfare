package be.raft.warfare.inventory.screen;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.block.RocketControllerBlock;
import be.raft.warfare.block.entity.RocketControllerBlockEntity;
import be.raft.warfare.network.C2S.UpdateRocketControllerPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.station.StationEditPacket;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class RocketControllerAssemblyScreen extends AbstractSimiScreen {
    private final AllGuiTextures background;
    private final RocketControllerBlockEntity blockEntity;

    private IconButton exitButton;
    private IconButton cancelAssembly;
    private IconButton assembleButton;

    public RocketControllerAssemblyScreen(RocketControllerBlockEntity blockEntity) {
        super(blockEntity.getBlockState().getBlock().getName());

        this.blockEntity = blockEntity;
        this.background = AllGuiTextures.STATION_ASSEMBLING;
    }

    @Override
    protected void init() {
        this.setWindowSize(this.background.getWidth(), this.background.getHeight());
        super.init();
        clearWidgets();

        this.exitButton = new IconButton(this.guiLeft + this.background.getWidth() - 33, this.guiTop + this.background.getHeight() - 24, AllIcons.I_PRIORITY_VERY_LOW);
        this.exitButton.withCallback(this::onClose);
        this.exitButton.setToolTip(CreateWarfare.translatable("rocket_controller.gui.close_window"));
        addRenderableWidget(this.exitButton);

        this.cancelAssembly = new IconButton(this.guiLeft + this.background.getWidth() - 66, this.guiTop + this.background.getHeight() - 24, AllIcons.I_DISABLE);
        this.cancelAssembly.withCallback(this::onCancelAssembly);
        this.cancelAssembly.setToolTip(CreateWarfare.translatable("rocket_controller.gui.cancel_assembly"));
        addRenderableWidget(this.cancelAssembly);

        this.assembleButton = new IconButton(this.guiLeft + 8, this.guiTop + this.background.getHeight() - 24, AllIcons.I_CONFIRM);
        this.assembleButton.withCallback(this::onAssemble);
        this.assembleButton.setToolTip(CreateWarfare.translatable("rocket_controller.gui.assemble"));
        addRenderableWidget(this.assembleButton);
    }

    @Override
    protected void renderWindow(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.background.render(graphics, this.guiLeft, this.guiTop);
        this.renderBlock(graphics);
    }

    private void renderBlock(GuiGraphics graphics) {
        PoseStack ms = graphics.pose();
        ms.pushPose();

        PoseTransformStack msr = TransformStack.of(ms);
        msr.pushPose()
                .translate(this.guiLeft + this.background.getWidth() + 4, this.guiTop + this.background.getHeight() + 4, 100)
                .scale(40)
                .rotateXDegrees(-22)
                .rotateYDegrees(63);

        GuiGameElement.of(this.blockEntity.getBlockState()
                        .setValue(BlockStateProperties.WATERLOGGED, false)
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)
                        .setValue(RocketControllerBlock.NO_DYNAMIC_BULB, true)
                ).render(graphics);

        ms.popPose();
    }

    private void onCancelAssembly() {
        CatnipServices.NETWORK.sendToServer(new UpdateRocketControllerPacket(this.blockEntity.getBlockPos(), false));
        this.minecraft.setScreen(new RocketControllerScreen(this.blockEntity));
    }

    private void onAssemble() {

    }

    @Override
    public void removed() {

    }
}

