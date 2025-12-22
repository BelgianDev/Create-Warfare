package be.raft.warfare.client.visual;

import be.raft.warfare.content.WarfarePartialModels;
import be.raft.warfare.content.block.TurretBlock;
import be.raft.warfare.content.block.entity.TurretBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmRenderer;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
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

public class TurretVisual extends SingleAxisRotatingVisual<TurretBlockEntity> implements SimpleDynamicVisual {
    private final RecyclingPoseStack poseStack;
    private final boolean ceiling;

    // Base
    private final TransformedInstance base;
    private float baseAngle = Float.NaN;

    // Arm
    private final TransformedInstance arm;

    // Head
    private final TransformedInstance head;
    private float headAngle = Float.NaN;


    private final List<TransformedInstance> parts;

    public TurretVisual(VisualizationContext context, TurretBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(WarfarePartialModels.TURRET_COG));

        this.poseStack = new RecyclingPoseStack();
        this.ceiling = this.blockState.getValue(TurretBlock.CEILING);

        // Load models
        this.base = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_BASE)).createInstance();
        this.arm = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_ARM)).createInstance();
        this.head = instancerProvider().instancer(InstanceTypes.TRANSFORMED,
                Models.partial(WarfarePartialModels.TURRET_HEAD)).createInstance();


        this.parts = List.of(this.base, this.arm, this.head);

        PoseTransformStack tfm = TransformStack.of(this.poseStack);
        tfm.translate(getVisualPosition());
        tfm.center();

        if (this.ceiling) tfm.rotateXDegrees(180);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        float tick = ctx.partialTick();

        this.baseAngle = this.blockEntity.baseAngle.getValue(tick);
        this.headAngle = this.blockEntity.headAngle.getValue(tick);

        this.updateRenderer();
    }

    private void updateRenderer() {
        this.poseStack.pushPose();

        PoseTransformStack tfm = TransformStack.of(this.poseStack);

        ArmRenderer.transformBase(tfm, this.baseAngle);
        this.base.setTransform(this.poseStack).setChanged();

        ArmRenderer.transformLowerArm(tfm, 0);
        this.arm.setTransform(this.poseStack).setChanged();

        // ArmRenderer.transformHead(tfm, this.headAngle);

        tfm.rotateXDegrees(-135);
        tfm.translate(0, 0.55, 0.4);
        tfm.rotateXDegrees(this.headAngle);
        this.head.setTransform(this.poseStack).setChanged();

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
        this.parts.forEach(AbstractInstance::delete);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        this.parts.forEach(consumer);
    }
}
