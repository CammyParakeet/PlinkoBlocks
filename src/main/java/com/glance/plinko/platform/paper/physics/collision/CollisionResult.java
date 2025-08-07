package com.glance.plinko.platform.paper.physics.collision;

import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import org.joml.Vector3f;

public record CollisionResult(
   Vector3f contactPoint,
   Vector3f normal,
   float penetrationDepth,
   PhysicsShape other
) {}
