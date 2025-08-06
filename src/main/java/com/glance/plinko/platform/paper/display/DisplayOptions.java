package com.glance.plinko.platform.paper.display;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public record DisplayOptions(
    Type type,
    Material material,
    Vector scale
) {
    public static DisplayOptions defaultBlock(Material material) {
        return new DisplayOptions(
                Type.BLOCK,
                material,
                new Vector(1.0, 1.0, 1.0)
        );
    }

    enum Type {
        ITEM, BLOCK, TEXT
    }
}
