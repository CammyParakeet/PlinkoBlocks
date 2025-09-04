package com.glance.plinko.platform.paper.animation;

import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record PlinkoKeyframe(
        Transformation transform,
        boolean collisionOccurred,
        int tick
) {

    public Vector position() {
        return new Vector(
                transform.getTranslation().x,
                transform.getTranslation().y,
                transform.getTranslation().z
        );
    }

    public Quaternionf rotation() {
        return transform.getLeftRotation();
    }

    public Vector scale() {
        return new Vector(
                transform.getScale().x,
                transform.getScale().y,
                transform.getScale().z
        );
    }

    public Vector3f scale3f() {
        return transform.getScale();
    }

}
