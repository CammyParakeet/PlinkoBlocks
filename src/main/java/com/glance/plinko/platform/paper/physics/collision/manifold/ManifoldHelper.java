package com.glance.plinko.platform.paper.physics.collision.manifold;

import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector3f;

@UtilityClass
public class ManifoldHelper {

    private static final class FaceBasis {
        Vector3f center, u, v;  // orthonormal in-plane basis of the face
        float halfU, halfV; // half lengths along u and v
    }

    public Manifold buildFaceFaceManifold(
        @NotNull OrientedBox a,
        @NotNull OrientedBox b,
        @NotNull Vector3f normal,
        boolean refIsA,
        int faceIdx
    ) {
        OrientedBox ref = refIsA ? a : b;
        OrientedBox inc = refIsA ? b : a;

        FaceBasis fb = buildFaceBasis(ref, faceIdx);

        int incFaceIdx = 1;// todo
        FaceBasis ib = buildFaceBasis(inc, incFaceIdx);

        // todo
        return null;
    }

    private FaceBasis buildFaceBasis(@NotNull OrientedBox box, int faceIdx) {
        Matrix3f R = box.rotation();
        Vector3f half = box.halfSize();
        Vector3f scale = box.scale();
        Vector3f c = new Vector3f(box.center());

        Vector3f nx = R.getColumn(faceIdx, new Vector3f());
        int uIdx = (faceIdx + 1) % 3;
        int vIdx = (faceIdx + 2) % 3;
        Vector3f u = R.getColumn(uIdx, new Vector3f());
        Vector3f v = R.getColumn(vIdx, new Vector3f());

        float halfN = half.get(faceIdx) * scale.get(faceIdx);
        float halfU = half.get(uIdx) * scale.get(uIdx);
        float halfV = half.get(vIdx) * scale.get(vIdx);

        // todo choose face center that is on the side closest to the other shape??
        Vector3f center = c;

        FaceBasis fb = new FaceBasis();
        fb.center = new Vector3f(center);
        fb.u = u;
        fb.v = v;
        fb.halfU = halfU;
        fb.halfV = halfV;
        return fb;
    }

}
