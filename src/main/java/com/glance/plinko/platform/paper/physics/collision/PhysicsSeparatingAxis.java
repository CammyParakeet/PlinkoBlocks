package com.glance.plinko.platform.paper.physics.collision;

import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import lombok.experimental.UtilityClass;
import org.joml.Matrix3f;
import org.joml.Vector3f;

@UtilityClass
public class PhysicsSeparatingAxis {

    // Total number of axes to check in OBB vs OBB SAT 3D
    private final int MAX_SAT_AXES = 15;

    // Threshold for treating a cross product as invalid (too close to zero length)
    private final float MIN_AXIS_LENGTH_SQUARED = 1e-6f;

    // For center contact point fallback: move this multiple between centers
    private final float CONTACT_POINT_BLEND = 0.5F;

    /**
     * Resolves collision between 2 oriented bounding boxes (OBBs)
     * using SAT (Separating Axis Theorem)
     *
     * @param a First OBB
     * @param b Second OBB
     * @return {@link CollisionResult} if intersecting, otherwise null
     */
    public CollisionResult resolveOBBvsOBB(OrientedBox a, OrientedBox b) {
        // Local axes in Worldspace
        Vector3f[] axesA = a.axes();
        Vector3f[] axesB = b.axes();

        // Vector between box centers A->B
        Vector3f d = new Vector3f(b.center()).sub(a.center());

        float smallestOverlap = Float.POSITIVE_INFINITY;
        Vector3f smallestAxis = null;

        // 3d has 15 axes to check SAT
        Vector3f[] textAxes = new Vector3f[MAX_SAT_AXES];
        System.arraycopy(axesA, 0, textAxes, 0, 3); // 3 from A
        System.arraycopy(axesB, 0, textAxes, 3, 3); // 3 from B

        // Compute 9 cross products between edges of A and B
        int axisCount = 6;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector3f cross = new Vector3f(axesA[i]).cross(axesB[j]);
                if (cross.lengthSquared() > MIN_AXIS_LENGTH_SQUARED) {
                    textAxes[axisCount++] = cross.normalize(new Vector3f());
                }
            }
        }

        // Now check all potential separating axis
        for (int i = 0; i < axisCount; i++) {
            Vector3f axis = textAxes[i];

            float projectionA = projectOBB(a, axis);
            float projectionB = projectOBB(b, axis);
            float centerDistance = Math.abs(d.dot(axis));

            float overlap = (projectionA + projectionB) - centerDistance;

            if (overlap <= 0) return null; // separation axis found with no collision

            // Track axis with the smallest overlap (deepest penetration)
            if (overlap < smallestOverlap) {
                smallestOverlap = overlap;
                smallestAxis = axis;
            }
        }

        if (smallestAxis == null) return null;

        // return basic collision result using the smallest overlap axis - TODO based on other factors
        Vector3f contact = new Vector3f(a.center().add(d.normalize().mul(CONTACT_POINT_BLEND)));
        Vector3f normal = new Vector3f(smallestAxis).normalize();

        return new CollisionResult(contact, normal, smallestOverlap, b);
    }

    /**
     * Projects the box onto the given axis to get the radius along that axis
     *
     * @param box The box to project
     * @param axis The normalized axis
     * @return The projected radius along the axis
     */
    private float projectOBB(OrientedBox box, Vector3f axis) {
        Matrix3f rot = box.rotation();
        Vector3f half = box.halfSize();

        float projection = 0f;

        // For each local axis of the box: project it and scale by half the radius
        for (int i = 0; i < 3; i++) {
            Vector3f localAxis = rot.getColumn(i, new Vector3f());
            float scale = half.get(i);
            projection += Math.abs(axis.dot(localAxis) * scale);
        }

        return projection;
    }

}
