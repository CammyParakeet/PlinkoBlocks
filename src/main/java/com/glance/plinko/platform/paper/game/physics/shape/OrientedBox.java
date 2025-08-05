package com.glance.plinko.platform.paper.game.physics.shape;

import com.glance.plinko.platform.paper.game.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.game.physics.collision.PhysicsCollider;
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
}
