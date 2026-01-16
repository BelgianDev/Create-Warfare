package be.raft.warfare.client.visual;

import be.raft.warfare.content.WarfarePartialModels;
import be.raft.warfare.content.block.MechanicalTurretBlock;
import be.raft.warfare.content.block.entity.MechanicalTurretBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.util.RecyclingPoseStack;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;

import java.util.List;
import java.util.function.Consumer;

public class TurretVisual extends SingleAxisRotatingVisual<MechanicalTurretBlockEntity> implements SimpleDynamicVisual {
    private final RecyclingPoseStack poseStack;
    private final boolean ceiling;
    private final List<TransformedInstance> parts;

    // Base
    private final TransformedInstance base;
    private float baseAngle = Float.NaN;

    // Arm
    private final TransformedInstance arm;

    // Head
    private final TransformedInstance head;
    private float headAngle = Float.NaN;

    // Nozzles
    private final TransformedInstance nozzleRight;
    private float nozzleRightScale;

    private final TransformedInstance nozzleLeft;
    private float nozzleLeftScale;

    public TurretVisual(VisualizationContext context, MechanicalTurretBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(WarfarePartialModels.TURRET_COG));

        this.poseStack = new RecyclingPoseStack();
        this.ceiling = this.blockState.getValue(MechanicalTurretBlock.CEILING);

        // Load models
        this.base = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_BASE)).createInstance();
        this.arm = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_ARM)).createInstance();
        this.head = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_HEAD)).createInstance();
        this.nozzleRight = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_NOZZLE)).createInstance();
        this.nozzleLeft = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_NOZZLE)).createInstance();


        this.parts = List.of(this.base, this.arm, this.head, this.nozzleRight, this.nozzleLeft);

        PoseTransformStack tfm = TransformStack.of(this.poseStack);
        tfm.translate(this.getVisualPosition());
        tfm.center();

        if (this.ceiling) tfm.rotateXDegrees(180);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float tick = ctx.partialTick();

        this.baseAngle = this.blockEntity.baseAngle.getValue(tick);
        this.headAngle = this.blockEntity.headAngle.getValue(tick);

        this.nozzleRightScale = this.blockEntity.nozzleRightScale.getValue(tick);
        this.nozzleLeftScale = this.blockEntity.nozzleLeftScale.getValue(tick);

        this.updateRenderer();
    }

    private void updateRenderer() {
        this.poseStack.pushPose();

        PoseTransformStack stack = TransformStack.of(this.poseStack);

        stack.translate(0, 4 / 16d, 0);
        stack.rotateYDegrees(this.baseAngle);
        this.base.setTransform(this.poseStack).setChanged();

        stack.translate(0, 2 / 16d, 0);
        stack.rotateXDegrees(135);
        this.arm.setTransform(this.poseStack).setChanged();

        stack.rotateXDegrees(-135);
        stack.translate(0, 0.55, 0.4);
        stack.rotateXDegrees(this.headAngle);
        this.head.setTransform(this.poseStack).setChanged();

        // Left Nozzle
        stack.pushPose();
        stack.translate(0.185, 0.25, -0.6);
        stack.scale(this.nozzleLeftScale);
        this.nozzleLeft.setTransform(this.poseStack).setChanged();
        stack.popPose();

        // Right Nozzle
        stack.pushPose();
        stack.translate(-0.185, 0.25, -0.6);
        stack.scale(this.nozzleRightScale);
        this.nozzleRight.setTransform(this.poseStack).setChanged();
        stack.popPose();

        this.poseStack.popPose();
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        relight(this.parts.toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        super._delete();
        this.parts.forEach(Instance::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        this.parts.forEach(consumer);
    }
}
