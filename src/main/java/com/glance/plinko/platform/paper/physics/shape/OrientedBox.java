package com.glance.plinko.platform.paper.physics.shape;

import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.collision.PhysicsCollider;
import org.joml.Matrix3f;
import org.joml.Vector3f;

/**
 * Represents an oriented bounding box (OBB) for 3D collision
 * <p>
 * Used for both simulated ball bodies and static pegs
 *
 * @param center Center position in world space
 * @param halfSize Half-size along local xyz axes
 * @param rotation 3x3 rotation matrix defining orientation
 * @param scale Per-axis scaling applied to the halfSize
 */
public record OrientedBox(
    Vector3f center,
    Vector3f halfSize,
    Matrix3f rotation,
    Vector3f scale
) implements PhysicsShape {

    public OrientedBox(Vector3f center, Vector3f halfSize, Matrix3f rotation) {
        this(center, halfSize, rotation, new Vector3f(1F));
    }

    @Override
    public CollisionResult collide(PhysicsShape other) {
        if (other instanceof OrientedBox box) {
            return PhysicsCollider.resolveOBBvsOBB(this, box);
        }

        return null;
    }

    /**
     * @return the 3 local unit axes transformed into world space
     */
    public Vector3f[] axes() {
        return new Vector3f[] {
            rotation.getColumn(0, new Vector3f()), // local X axis
            rotation.getColumn(1, new Vector3f()), // local Y axis
            rotation.getColumn(2, new Vector3f()) // local Z axis
        };
    }

    /** 8 world-space corners of an OBB. Index by bits: (sx, sy, sz) in {0,1} */
    public Vector3f[] corners() {
        Vector3f worldCenter = new Vector3f(center);

        Vector3f axisX = rotation.getColumn(0, new Vector3f()).mul(halfSize.x * scale.x);
        Vector3f axisY = rotation.getColumn(1, new Vector3f()).mul(halfSize.y * scale.y);
        Vector3f axisZ = rotation.getColumn(2, new Vector3f()).mul(halfSize.z * scale.z);

        Vector3f[] corners = new Vector3f[8];
        int index = 0;

        for (int signX = -1; signX <= 1; signX += 2)
            for (int signY = -1; signY <= 1; signY += 2)
                for (int signZ = -1; signZ <= 1; signZ += 2)
                    corners[index++] = new Vector3f(worldCenter)
                            .fma(signX, axisX)
                            .fma(signY, axisY)
                            .fma(signZ, axisZ);

        return corners;
    }

}
