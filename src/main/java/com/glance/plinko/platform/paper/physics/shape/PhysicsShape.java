package com.glance.plinko.platform.paper.physics.shape;

import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import org.joml.Vector3f;

public sealed interface PhysicsShape permits OrientedBox {
    Vector3f center();

    /**
     * Resolve a collision against another shape
     * @return null if no collision, otherwise a response
     */
    CollisionResult collide(PhysicsShape other);
}
