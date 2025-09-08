package com.glance.plinko.platform.paper.physics.collision.manifold;

import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import com.glance.plinko.platform.paper.utils.math.VectorUtils;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ManifoldHelper {

    private static final class FaceBasis {
        Vector3f center, u, v;  // orthonormal in-plane basis of the face
        float halfU, halfV; // half lengths along u and v

        Vector3f[] rectangleCorners() {
            return new Vector3f[] {
                new Vector3f(this.center).fma(+this.halfU, this.u).fma(+this.halfV, this.v),
                new Vector3f(this.center).fma(+this.halfU, this.u).fma(-this.halfV, this.v),
                new Vector3f(this.center).fma(-this.halfU, this.u).fma(-this.halfV, this.v),
                new Vector3f(this.center).fma(-this.halfU, this.u).fma(+this.halfV, this.v),
            };
        }

        Vector2f toUV(Vector3f world) {
            Vector3f rel = new Vector3f(world).sub(this.center);
            return new Vector2f(rel.dot(this.u), rel.dot(this.v));
        }

        Vector3f fromUV(Vector2f uv) {
            return new Vector3f(this.center).fma(uv.x, this.u).fma(uv.y, this.v);
        }

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

        // Reference face sits on +n side
        FaceBasis fb = buildFaceBasis(ref, faceIdx, normal);

        // Incident face sits on -n side
        int incFaceIdx = mostAntiParallelFace(inc, normal);
        FaceBasis ib = buildFaceBasis(inc, incFaceIdx, new Vector3f(normal).negate());

        // Incident 3D quad
        Vector3f[] incCorners = ib.rectangleCorners();
        List<Vector2f> polyUV = new ArrayList<>(4);
        for (Vector3f c : incCorners) polyUV.add(fb.toUV(c));

        // Clip polygon to the reference rectangle
        List<Vector2f> clipped = clipToRectangle(polyUV, fb.halfU, fb.halfV);

        Manifold m = new Manifold();
        if (!clipped.isEmpty()) {
            Vector3f sum = new Vector3f();
            for (Vector2f uv : clipped) {
                Vector3f pt = fb.fromUV(uv);
                m.points.add(pt);
                sum.add(pt);
            }
            m.centroid.set(sum.div((float) clipped.size()));
            return m;
        }

        // Fallback from rare degeneracy
        Vector3f rel = new Vector3f(ib.center).sub(fb.center);
        float u = clamp(rel.dot(fb.u), -fb.halfU, fb.halfU);
        float v = clamp(rel.dot(fb.v), -fb.halfV, fb.halfV);
        Vector3f pt = fb.fromUV(new Vector2f(u, v));
        m.points.add(pt);
        m.centroid.set(pt);
        return m;
    }

    /**
     * Build a face basis for a specific face index, center placed on the correct side
     * of the box along that faces `normal` depending on the world space dir `toward`
     * <p>
     * If dot(toward, nx) >= 0, place the face at +halfN along nx; else at -halfN
     */
    private FaceBasis buildFaceBasis(
        @NotNull OrientedBox box,
        int faceIdx,
        @NotNull Vector3f toward
    ) {
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

        float sign = (toward.dot(nx) >= 0f) ? +1f : -1f;
        Vector3f center = new Vector3f(c).fma(sign * halfN, nx);

        FaceBasis fb = new FaceBasis();
        fb.center = new Vector3f(center);
        fb.u = u;
        fb.v = v;
        fb.halfU = halfU;
        fb.halfV = halfV;
        return fb;
    }

    private int mostAntiParallelFace(@NotNull OrientedBox box, @NotNull Vector3f n) {
        Vector3f[] axes = box.axes();
        int idx = 0;
        float best = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < 3; i++) {
            float score = -n.dot(axes[i]); // larger = more anti-parallel
            if (score > best) { best = score; idx = i; }
        }
        return idx;
    }

    // Hodgman polygon clipping formula
    private List<Vector2f> clipToRectangle(
        @NotNull List<Vector2f> poly,
        float hU, float hV
    ) {
        if (poly.isEmpty()) return poly;
        List<Vector2f> out = poly;
        out = clipHalfSpace(out, p -> p.x <= hU, (a, b) -> intersectAtX(a, b, hU));
        out = clipHalfSpace(out, p -> p.x >= -hU, (a,b) -> intersectAtX(a,b,-hU));
        out = clipHalfSpace(out, p -> p.y <=  hV, (a,b) -> intersectAtY(a,b, hV));
        out = clipHalfSpace(out, p -> p.y >= -hV, (a,b) -> intersectAtY(a,b,-hV));

        return out;
    }

    private interface InsideTest { boolean test(Vector2f p); }
    private interface Intersect { Vector2f apply(Vector2f a, Vector2f b); }

    private List<Vector2f> clipHalfSpace(
        @NotNull List<Vector2f> poly,
        InsideTest inside,
        Intersect intersect
    ) {
        if (poly.isEmpty()) return poly;
        List<Vector2f> out = new ArrayList<>(poly.size() + 4);
        Vector2f prev = poly.getLast();
        boolean prevIn = inside.test(prev);

        for (Vector2f current : poly) {
            boolean currentIn = inside.test(current);
            if (currentIn) {
                if (!prevIn) out.add(intersect.apply(prev, current));
                out.add(current);
            } else if (prevIn) {
                out.add(intersect.apply(prev, current));
            }
            prev = current;
            prevIn = currentIn;
        }
        return out;
    }

    private Vector2f intersectAtX(Vector2f a, Vector2f b, float xBound) {
        float dx = b.x - a.x;
        if (Math.abs(dx) < VectorUtils.EPS) return new Vector2f(xBound, a.y); // degenerate vertical
        float t = (xBound - a.x) / dx;
        return new Vector2f(xBound, a.y + t * (b.y - a.y));
    }

    private Vector2f intersectAtY(Vector2f a, Vector2f b, float yBound) {
        float dy = b.y - a.y;
        if (Math.abs(dy) < VectorUtils.EPS) return new Vector2f(a.x, yBound); // degenerate horizontal
        float t = (yBound - a.y) / dy;
        return new Vector2f(a.x + t * (b.x - a.x), yBound);
    }

    private float clamp(float x, float lo, float hi) {
        return Math.max(lo, Math.min(hi, x));
    }

}
