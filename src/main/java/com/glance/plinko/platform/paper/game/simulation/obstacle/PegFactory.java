package com.glance.plinko.platform.paper.game.simulation.obstacle;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PegFactory {

    public static PlinkoObject createPeg(
        @NotNull Location loc,
        float scale
    ) {
        Vector3f center = new Vector3f(
            (float) loc.getX(),
            (float) loc.getY(),
            (float) loc.getZ()
        );

        Vector3f halfSize = new Vector3f(scale / 2F);
        Matrix3f rotationMat = new Matrix3f().rotateY((float) Math.toRadians(45));

        OrientedBox box = new OrientedBox(center, halfSize, rotationMat);

        PlinkoObject peg = new PlinkoObject(
            box,
            Float.POSITIVE_INFINITY, // infinite mass
            0f
        );

        peg.setPosition(center);
        peg.setScale(new Vector3f(scale));
        peg.setRotation(new Quaternionf().rotateY((float) Math.toRadians(45)));

        return peg;
    }

}
