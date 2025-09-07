package com.glance.plinko.platform.paper.display;

import org.bukkit.Material;
import org.bukkit.util.Vector;

public record DisplayOptions(
    Type type,
    Material material,
    Vector scale,
    boolean startInterpolation
) {
    public static DisplayOptions defaultBlock(Material material) {
        return new DisplayOptions(
                Type.BLOCK,
                material,
                new Vector(1.0, 1.0, 1.0),
                false
        );
    }

    public static DisplayOptions defaultItem(Material material) {
        return new DisplayOptions(
                Type.ITEM,
                material,
                new Vector(1.0, 1.0, 1.0),
                false
        );
    }

    public static DisplayOptions marker(Material material) {
        return new DisplayOptions(
                Type.ITEM,
                material,
                new Vector(0.05, 0.05, 0.05),
                false
        );
    }

    public static DisplayOptions blockMarker(Material material) {
        return new DisplayOptions(
                Type.BLOCK,
                material,
                new Vector(0.05, 0.05, 0.05),
                false
        );
    }

    public enum Type {
        ITEM, BLOCK, TEXT
    }
}
