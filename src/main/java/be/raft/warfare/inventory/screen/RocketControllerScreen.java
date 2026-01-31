package be.raft.warfare.inventory.screen;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.block.RocketControllerBlock;
import be.raft.warfare.block.entity.RocketControllerBlockEntity;
import be.raft.warfare.inventory.widgets.LaunchButton;
import be.raft.warfare.network.C2S.UpdateRocketControllerPacket;
import be.raft.warfare.registry.WarfareGuiTextures;
import com.mojang.blaze3d.vertex.PoseStack;
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

public class RocketControllerScreen extends AbstractSimiScreen {
    private final WarfareGuiTextures background;
    private final RocketControllerBlockEntity blockEntity;

    private IconButton exitButton;
    private IconButton enterAssemblyButton;
    private LaunchButton launchButton;

    private boolean assembling = false;

    public RocketControllerScreen(RocketControllerBlockEntity blockEntity) {
        super(blockEntity.getBlockState().getBlock().getName());

        this.blockEntity = blockEntity;
        this.background = WarfareGuiTextures.ROCKET_CONTROLLER;
    }

    @Override
    protected void init() {
        this.setWindowSize(this.background.getWidth(), this.background.getHeight());
        super.init();
        clearWidgets();

        this.exitButton = new IconButton(this.guiLeft + this.background.getWidth() - 33, this.guiTop + this.background.getHeight() - 24, AllIcons.I_CONFIRM);
        this.exitButton.withCallback(this::onClose);
        this.exitButton.setToolTip(CreateWarfare.translatable("rocket_controller.gui.close_window"));
        addRenderableWidget(this.exitButton);

        this.enterAssemblyButton = new IconButton(this.guiLeft + this.background.getWidth() - 66, this.guiTop + this.background.getHeight() - 24, AllIcons.I_PLACEMENT_SETTINGS);
        this.enterAssemblyButton.withCallback(this::onAssemble);
        this.enterAssemblyButton.setToolTip(CreateWarfare.translatable("rocket_controller.gui.enter_assembly"));
        addRenderableWidget(this.enterAssemblyButton);

        this.launchButton = new LaunchButton(this.guiLeft + 28, this.guiTop + 66);
        this.launchButton.withCallback(this::onLaunch);
        this.launchButton.setTooltip(CreateWarfare.translatable("rocket_controller.gui.launch"));
        addRenderableWidget(this.launchButton);
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

    private void onAssemble() {
        this.assembling = true;
        this.minecraft.setScreen(new RocketControllerAssemblyScreen(this.blockEntity));
    }

    private void onLaunch() {

    }

    @Override
    public void removed() {
        if (!this.assembling)
            return;

        CatnipServices.NETWORK.sendToServer(new UpdateRocketControllerPacket(this.blockEntity.getBlockPos(), true));
    }
}

