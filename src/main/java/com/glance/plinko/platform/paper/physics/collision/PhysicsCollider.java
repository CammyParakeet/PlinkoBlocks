package com.glance.plinko.platform.paper.physics.collision;

import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class PhysicsCollider {

    public CollisionResult resolve(@NotNull PhysicsShape a, @NotNull PhysicsShape b) {
        return a.collide(b);
    }

    public CollisionResult resolveOBBvsOBB(OrientedBox a, OrientedBox b) {
        return PhysicsSeparatingAxis.resolveOBBvsOBB(a, b);
    }

}
