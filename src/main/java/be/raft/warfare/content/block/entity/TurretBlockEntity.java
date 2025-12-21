package be.raft.warfare.content.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TurretBlockEntity extends KineticBlockEntity {
    public LerpedFloat baseAngle;
    public LerpedFloat armAngle;
    public LerpedFloat headAngle;

    public TurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);

        this.baseAngle = LerpedFloat.angular();
        this.baseAngle.startWithValue(0);

        this.armAngle = LerpedFloat.angular();
        this.armAngle.startWithValue(0);

        this.headAngle = LerpedFloat.angular();
        this.headAngle.startWithValue(0);
    }

    @Override
    public void tick() {
        this.baseAngle.setValue(this.baseAngle.getValue() + 1);
        this.headAngle.setValue(0);
    }
}
