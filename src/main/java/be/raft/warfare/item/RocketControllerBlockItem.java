package be.raft.warfare.item;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.registry.WarfareBlockEntities;
import be.raft.warfare.registry.WarfareDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RocketControllerBlockItem extends BlockItem {

    public RocketControllerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        return false;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();

        if (player == null)
            return InteractionResult.FAIL;

        if (player.isShiftKeyDown() && stack.has(WarfareDataComponents.ROCKET_CONTROLLER_PLATFORM)) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            stack.remove(WarfareDataComponents.ROCKET_CONTROLLER_PLATFORM);
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

            player.displayClientMessage(CreateWarfare.translatable("rocket_controller.clear"), true);
            return InteractionResult.SUCCESS;
        }

        if (!stack.has(WarfareDataComponents.ROCKET_CONTROLLER_PLATFORM))
            return InteractionResult.FAIL;

        InteractionResult result = super.useOn(context);
        stack.remove(WarfareDataComponents.ROCKET_CONTROLLER_PLATFORM);
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        return result;
    }
}
