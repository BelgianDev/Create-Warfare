package be.raft.warfare.rocket.platform;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.entity.PlatformSelectionEntity;
import be.raft.warfare.item.RocketControllerBlockItem;
import be.raft.warfare.network.C2S.CreatePlatformSelectionPacket;
import be.raft.warfare.network.C2S.RemovePlatformSelectionPacket;
import be.raft.warfare.registry.WarfareBlocks;
import be.raft.warfare.registry.WarfareDataComponents;
import com.google.common.base.Objects;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHelper;
import com.simibubi.create.foundation.utility.RaycastHelper;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is a modified version of {@link com.simibubi.create.content.contraptions.glue.SuperGlueSelectionHandler} from Create which is licensed under <a href="https://github.com/Creators-of-Create/Create?tab=License-1-ov-file#readme">MIT - The Create Team / The Creators of Create</a>
 */
public class RocketPlatformSelectionHandler {
    private static final int PASSIVE = 0xB48CFF;
    private static final int HIGHLIGHT = 0xB48CFF;
    private static final int SUCCESS = 0xD16BFF;
    private static final int FAIL = 0xc5b548;

    private final Object clusterOutlineSlot = new Object();
    private final Object bbOutlineSlot = new Object();
    private int clusterCooldown;

    private BlockPos firstPos;
    private BlockPos hoveredPos;
    private Set<BlockPos> currentCluster;

    private PlatformSelectionEntity selected;

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        BlockPos hovered = null;
        ItemStack stack = player.getMainHandItem();

        if (!isController(stack)) {
            if (firstPos != null)
                discard();
            return;
        }

        if (clusterCooldown > 0) {
            if (clusterCooldown == 25)
                player.displayClientMessage(CommonComponents.EMPTY, true);
            Outliner.getInstance().keep(clusterOutlineSlot);
            clusterCooldown--;
        }

        AABB scanArea = player.getBoundingBox()
                .inflate(32, 16, 32);

        List<PlatformSelectionEntity> selectionsNearby = mc.level.getEntitiesOfClass(PlatformSelectionEntity.class, scanArea);

        selected = null;
        if (firstPos == null) {
            double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
            Vec3 traceOrigin = player.getEyePosition();
            Vec3 traceTarget = RaycastHelper.getTraceTarget(player, range, traceOrigin);

            double bestDistance = Double.MAX_VALUE;
            for (PlatformSelectionEntity selectionEntity : selectionsNearby) {
                Optional<Vec3> clip = selectionEntity.getBoundingBox()
                        .clip(traceOrigin, traceTarget);
                if (clip.isEmpty())
                    continue;
                Vec3 vec3 = clip.get();
                double distanceToSqr = vec3.distanceToSqr(traceOrigin);
                if (distanceToSqr > bestDistance)
                    continue;
                selected = selectionEntity;
                bestDistance = distanceToSqr;
            }

            for (PlatformSelectionEntity selectionEntity : selectionsNearby) {
                boolean h = clusterCooldown == 0 && selectionEntity == selected;
                AllSpecialTextures faceTex = h ? AllSpecialTextures.CUTOUT_CHECKERED : null;
                Outliner.getInstance().showAABB(selectionEntity, selectionEntity.getBoundingBox())
                        .colored(h ? HIGHLIGHT : PASSIVE)
                        .withFaceTextures(faceTex, faceTex)
                        .disableLineNormals()
                        .lineWidth(h ? 1 / 16f : 1 / 64f);
            }
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK)
            hovered = ((BlockHitResult) hitResult).getBlockPos();

        if (hovered == null) {
            hoveredPos = null;
            return;
        }

        if (firstPos != null && !firstPos.closerThan(hovered, 24)) {
            player.displayClientMessage(CreateWarfare.translatable("rocket_controller.big")
                    .withColor(FAIL), true);
            return;
        }

        boolean cancel = player.isShiftKeyDown();
        if (cancel && firstPos == null)
            return;

        AABB currentSelectionBox = getCurrentSelectionBox();

        boolean unchanged = Objects.equal(hovered, hoveredPos);

        if (unchanged) {
            if (currentCluster != null) {
                boolean isVertical = this.firstPos.getY() != hovered.getY();
                boolean tooSmall = this.isAreaToSmall(hovered);
                boolean isLaunchPad = this.isSelectedBlockLaunchPad(hovered, mc.level);
                boolean intersects = RocketPlatformSelectionHelper.rocketPlatformIntersects(mc.level, this.firstPos, hovered);

                int color = HIGHLIGHT;
                String key = "rocket_controller.confirm";

                if (cancel) {
                    color = FAIL;
                    key = "rocket_controller.discard";
                } else if (tooSmall) {
                    color = FAIL;
                    key = "rocket_controller.small";
                } else if (isVertical) {
                    color = FAIL;
                    key = "rocket_controller.not_flat";
                } else if (intersects) {
                    color = FAIL;
                    key = "rocket_controller.intersects";
                } else if (!isLaunchPad) {
                    color = FAIL;
                    key = "rocket_controller.not_pad";
                }

                player.displayClientMessage(CreateWarfare.translatable(key)
                        .withColor(color), true);

                if (currentSelectionBox != null)
                    Outliner.getInstance().showAABB(bbOutlineSlot, currentSelectionBox)
                            .colored(color)
                            .withFaceTextures(AllSpecialTextures.CUTOUT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
                            .disableLineNormals()
                            .lineWidth(1 / 16f);

                Outliner.getInstance().showCluster(clusterOutlineSlot, currentCluster)
                        .colored(HIGHLIGHT)
                        .disableLineNormals()
                        .lineWidth(1 / 64f);
            }

            return;
        }

        hoveredPos = hovered;
        currentCluster = SuperGlueSelectionHelper.searchGlueGroup(mc.level, firstPos, hoveredPos, true);
    }

    private boolean isController(ItemStack stack) {
        return stack.getItem() instanceof RocketControllerBlockItem;
    }

    private AABB getCurrentSelectionBox() {
        return firstPos == null || hoveredPos == null ? null : new AABB(Vec3.atLowerCornerOf(firstPos), Vec3.atLowerCornerOf(hoveredPos)).expandTowards(1, 1, 1);
    }

    private boolean isAreaToSmall(BlockPos hoveredPos) {
        if (this.firstPos == null)
            return false;

        return Mth.abs(this.firstPos.getX() - hoveredPos.getX()) < 2 || Mth.abs(this.firstPos.getZ() - hoveredPos.getZ()) < 2;
    }

    private boolean isAreaHorizontal(BlockPos hoveredPos) {
        if (this.firstPos == null)
            return false;

        return this.firstPos.getY() == hoveredPos.getY();
    }

    private boolean isAreaValid(BlockPos hoveredPos) {
        return !this.isAreaToSmall(hoveredPos) && this.isAreaHorizontal(hoveredPos);
    }

    private boolean isSelectedBlockLaunchPad(BlockPos pos, Level level) {
        return level.getBlockState(pos).is(WarfareBlocks.LAUNCH_PAD.get());
    }

    public boolean onMouseInput(boolean attack) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        ItemStack stack = player.getMainHandItem();

        if (!isController(stack) || !player.mayBuild())
            return false;

        if (attack) {
            if (selected == null)
                return false;

            CatnipServices.NETWORK.sendToServer(new RemovePlatformSelectionPacket(this.selected.getId()));
            selected = null;
            clusterCooldown = 0;
            return true;
        }

        if (stack.has(WarfareDataComponents.ROCKET_CONTROLLER_PLATFORM)) {
            return false;
        }

        if (player.isShiftKeyDown()) {
            if (firstPos != null) {
                discard();
                return true;
            }
            return false;
        }

        if (hoveredPos == null)
            return false;

        if (firstPos != null && currentCluster != null) {
            boolean canReach = currentCluster.contains(hoveredPos);
            boolean isValid = this.isAreaValid(this.hoveredPos);
            boolean isLaunchPad = this.isSelectedBlockLaunchPad(this.hoveredPos, level);
            boolean intersects = RocketPlatformSelectionHelper.rocketPlatformIntersects(level, this.firstPos, hoveredPos);

            if (!canReach|| !isValid || !isLaunchPad || intersects) {
                AllSoundEvents.DENY.playAt(player.level(), this.hoveredPos, 0.5F, 0.95F, false);
                return true;
            }

            confirm();
            return true;
        }

        if (!this.isSelectedBlockLaunchPad(this.hoveredPos, level)) {
            AllSoundEvents.DENY.playAt(player.level(), this.hoveredPos, 0.5F, 0.95F, false);
            player.displayClientMessage(CreateWarfare.translatable("rocket_controller.not_pad")
                    .withColor(FAIL), true);

            return true;
        }

        firstPos = hoveredPos;
        player.displayClientMessage(CreateWarfare.translatable("rocket_controller.first_pos"), true);
        AllSoundEvents.CONFIRM_2.playAt(level, firstPos, 0.5F, 0.85F, false);
        level.playSound(player, firstPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);
        return true;
    }

    public void discard() {
        LocalPlayer player = Minecraft.getInstance().player;
        currentCluster = null;
        firstPos = null;
        player.displayClientMessage(CreateWarfare.translatable("rocket_controller.clear"), true);
        clusterCooldown = 0;
    }

    public void confirm() {
        LocalPlayer player = Minecraft.getInstance().player;
        CatnipServices.NETWORK.sendToServer(new CreatePlatformSelectionPacket(firstPos, hoveredPos));
        AllSoundEvents.CONFIRM.playAt(player.level(), hoveredPos, 0.5F, 0.95F, false);
        player.level().playSound(player, hoveredPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75f, 1);

        if (currentCluster != null)
            Outliner.getInstance().showCluster(clusterOutlineSlot, currentCluster)
                    .colored(SUCCESS)
                    .withFaceTextures(AllSpecialTextures.CUTOUT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
                    .disableLineNormals()
                    .lineWidth(1 / 24f);

        discard();
        player.displayClientMessage(CreateWarfare.translatable("rocket_controller.success").withColor(SUCCESS), true);
        clusterCooldown = 40;
    }

}
