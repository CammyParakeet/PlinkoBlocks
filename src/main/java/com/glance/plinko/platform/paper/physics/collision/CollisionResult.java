package com.glance.plinko.platform.paper.physics.collision;

import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import org.joml.Vector3f;

import java.util.List;

/**
 * Metadata for a physics shape collision
 */
public record CollisionResult(
   Vector3f normal,
   float penetrationDepth,
   AxisType axisType,
   ContactTopology topology,
   int indexA,
   int indexB,
   List<Vector3f> contactPoints, // world-space points on the contact patch/line
   Vector3f centroid,
   PhysicsShape other
) {

    public enum AxisType {
        FACE_A, FACE_B, EDGE_CROSS
    }

    public enum ContactTopology {
        FACE_FACE, EDGE_EDGE
    }

}
