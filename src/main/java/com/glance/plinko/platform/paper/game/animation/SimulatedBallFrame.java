package com.glance.plinko.platform.paper.game.animation;

import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public record SimulatedBallFrame(
        Transformation transform,
        Vector3f velocity,
        float mass
) {

    public Vector position() {
        return new Vector(
                transform.getTranslation().x,
                transform.getTranslation().y,
                transform.getTranslation().z
        );
    }

}
