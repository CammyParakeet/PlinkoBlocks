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
 */
public record OrientedBox(
    Vector3f center,
    Vector3f halfSize,
    Matrix3f rotation
) implements PhysicsShape {

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

}
