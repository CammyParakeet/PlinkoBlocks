package com.glance.plinko.platform.paper.physics.collision;

import com.glance.plinko.platform.paper.physics.collision.manifold.Manifold;
import com.glance.plinko.platform.paper.physics.collision.manifold.ManifoldHelper;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PhysicsSeparatingAxis {

    // Total number of axes to check in OBB vs OBB SAT 3D
    private final int MAX_SAT_AXES = 15;

    // Threshold for treating a cross product as invalid (too close to zero length)
    private final float MIN_AXIS_LENGTH_SQUARED = 1e-6f;

    // For center contact point fallback: move this multiple between centers
    private final float CONTACT_POINT_BLEND = 0.5F;

    private final class AxisMeta {
        Vector3f axis;
        float overlap;
        CollisionResult.AxisType axisType;
        int i;
        int j;
    }

    /**
     * Resolves collision between 2 oriented bounding boxes (OBBs)
     * using SAT (Separating Axis Theorem)
     *
     * @param a First OBB
     * @param b Second OBB
     * @return {@link CollisionResult} if intersecting, otherwise null
     */
    public CollisionResult resolveOBBvsOBB(
        OrientedBox a,
        OrientedBox b
    ) {
        // Local axes in Worldspace
        Vector3f[] axesA = a.axes();
        Vector3f[] axesB = b.axes();

        // Vector between box centers A->B
        Vector3f d = new Vector3f(b.center()).sub(a.center());

        AxisMeta best = null;
        List<AxisMeta> candidates = new ArrayList<>(MAX_SAT_AXES);

        for (int i = 0; i < 3; i++) candidates.add(faceAxis(axesA[i],
                CollisionResult.AxisType.FACE_A, i, -1));
        for (int j = 0; j < 3; j++) candidates.add(faceAxis(axesB[j],
                CollisionResult.AxisType.FACE_B, -1, j));

        // Cross product edge axes
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector3f cross = new Vector3f(axesA[i]).cross(axesB[j]);
                if (cross.lengthSquared() > MIN_AXIS_LENGTH_SQUARED) {
                    candidates.add(faceAxis(cross.normalize(new Vector3f()),
                            CollisionResult.AxisType.EDGE_CROSS, i, j));
                }
            }
        }

        // SAT test
        for (AxisMeta m : candidates) {
            float rA = projectOBB(a, m.axis);
            float rB = projectOBB(b, m.axis);
            float dist = Math.abs(d.dot(m.axis));
            float overlap = (rA + rB) - dist;

            if (!Float.isFinite(overlap)) continue;
            if (overlap <= 0F) return null; // separated
            m.overlap = overlap;

            if (best == null || overlap < best.overlap) best = m;
        }

        if (best == null) return null;

        Vector3f normal = new Vector3f(best.axis);
        if (d.dot(normal) < 0F) normal.negate(); // ensure pointing from A to B

        CollisionResult.ContactTopology topology;
        boolean refIsA;
        int relFaceIdx;

        switch (best.axisType) {
            case FACE_A -> {
                topology = CollisionResult.ContactTopology.FACE_FACE;
                refIsA = true;
                relFaceIdx = faceIndexFrom(normal, axesA);
            }
            case FACE_B -> {
                topology = CollisionResult.ContactTopology.FACE_FACE;
                refIsA = false;
                relFaceIdx = faceIndexFrom(new Vector3f(normal).negate(), axesB);
            }
            case EDGE_CROSS -> {
                topology = CollisionResult.ContactTopology.EDGE_EDGE;
                int ia = faceIndexFrom(normal, axesA);
                int ib = faceIndexFrom(normal, axesB);
                float alignA = Math.abs(normal.dot(axesA[ia]));
                float alignB = Math.abs(normal.dot(axesB[ib]));
                refIsA = alignA >= alignB;
                relFaceIdx = refIsA ? ia : ib;
            }
            default -> throw new IllegalStateException("Unknown axis type");
        }

        Manifold manifold;
        if (topology == CollisionResult.ContactTopology.FACE_FACE) {
            manifold = ManifoldHelper.buildFaceFaceManifold(a, b, normal, refIsA, relFaceIdx);
        } else {
            manifold = ManifoldHelper.buildEdgeEdgeManifold(a, b, best.i, best.j, normal);
        }

        int indexA = (best.axisType == CollisionResult.AxisType.FACE_A) ? relFaceIdx : best.i;
        int indexB = (best.axisType == CollisionResult.AxisType.FACE_B) ? relFaceIdx : best.j;

        return new CollisionResult(
            normal.normalize(),
            best.overlap,
            best.axisType,
            topology,
            indexA,
            indexB,
            manifold.getPoints(),
            manifold.getCentroid(),
            b
        );
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
            float halfExtent = half.get(i) * box.scale().get(i);
            projection += Math.abs(axis.dot(localAxis) * halfExtent);
        }

        return projection;
    }

    private int faceIndexFrom(Vector3f n, Vector3f[] axes) {
        int idx = 0;
        float best = -1f;
        for (int i = 0; i < 3; i++) {
            float d = Math.abs(n.dot(axes[i]));
            if (d > best) { best = d; idx = i; }
        }
        return idx;
    }

    private AxisMeta faceAxis(
        @NotNull Vector3f axis,
        @NotNull CollisionResult.AxisType axisType,
        int i, int j
    ) {
        AxisMeta m = new AxisMeta();
        m.axis = new Vector3f(axis).normalize();
        m.axisType = axisType;
        m.i = i;
        m.j = j;
        return m;
    }

}
