package be.raft.warfare.registry;

import be.raft.warfare.CreateWarfare;
import be.raft.warfare.util.ui.UniversalIcon;
import net.minecraft.resources.ResourceLocation;

public class WarfareIcons extends UniversalIcon {
    private static final ResourceLocation ATLAS = CreateWarfare.asLoc("textures/gui/icons.png");
    private static final int ATLAS_SIZE = 256;

    private static int x = 0, y = -1;

    public static final UniversalIcon
                MONSTER_HEAD = newRow(),
                PLAYER_HEAD = next(),
                MOB_HEAD = next();

    private static WarfareIcons next() {
        return new WarfareIcons(++x, y);
    }

    private static WarfareIcons newRow() {
        return new WarfareIcons(x = 0, ++y);
    }

    public WarfareIcons(int x, int y) {
        super(ATLAS, ATLAS_SIZE, x, y);
    }
}
