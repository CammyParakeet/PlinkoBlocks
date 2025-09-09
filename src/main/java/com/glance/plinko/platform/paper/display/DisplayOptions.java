package com.glance.plinko.platform.paper.display;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public record DisplayOptions(
    Type type,
    Material material,
    Vector scale,
    Quaternionf rotation,
    boolean startInterpolation
) {
    public static DisplayOptions defaultBlock(Material material) {
        return new DisplayOptions(
                Type.BLOCK,
                material,
                new Vector(1.0, 1.0, 1.0),
                new Quaternionf(),
                false
        );
    }

    public static DisplayOptions defaultItem(Material material) {
        return new DisplayOptions(
                Type.ITEM,
                material,
                new Vector(1.0, 1.0, 1.0),
                new Quaternionf(),
                false
        );
    }

    public static DisplayOptions marker(Material material) {
        return new DisplayOptions(
                Type.ITEM,
                material,
                new Vector(0.05, 0.05, 0.05),
                new Quaternionf(),
                false
        );
    }

    public static DisplayOptions marker(Material material, double scale) {
        return new DisplayOptions(
                Type.ITEM,
                material,
                new Vector(scale, scale, scale),
                new Quaternionf(),
                false
        );
    }

    public static DisplayOptions blockMarker(Material material) {
        return new DisplayOptions(
                Type.BLOCK,
                material,
                new Vector(0.05, 0.05, 0.05),
                new Quaternionf(),
                false
        );
    }

    public static DisplayOptions fromObject(
        @NotNull Material material,
        @NotNull PlinkoObject obj
    ) {
        return new DisplayOptions(
            Type.ITEM,
            material,
            Vector.fromJOML(obj.getScale()),
            new Quaternionf(obj.getRotation()),
        false
        );
    }

    public enum Type {
        ITEM, BLOCK, TEXT
    }
}
