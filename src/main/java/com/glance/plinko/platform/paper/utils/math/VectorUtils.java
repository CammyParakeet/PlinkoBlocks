package com.glance.plinko.platform.paper.utils.math;

import lombok.experimental.UtilityClass;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@UtilityClass
public class VectorUtils {

    public static final float EPS = 1e-6f;

    public @NotNull Vector getBukkit(@NotNull Vector3f joml) {
        return new Vector(joml.x, joml.y, joml.z);
    }

}
