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

        Vector3f nRef = refIsA ? new Vector3f(normal) : new Vector3f(normal).negate();

        // Reference face sits on +n side
        FaceBasis fb = buildFaceBasis(ref, faceIdx, nRef);

        // Incident face sits on -n side
        int incFaceIdx = mostAntiParallelFace(inc, nRef);
        FaceBasis ib = buildFaceBasis(inc, incFaceIdx, new Vector3f(nRef).negate());

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

//        Vector3f nRefLocal = ref.rotation().getColumn(faceIdx, new Vector3f());
//        assert nRef.dot(nRefLocal) > 0f : "Reference face not aligned with nRef";
//        Vector3f nIncLocal = inc.rotation().getColumn(incFaceIdx, new Vector3f());
//        assert nRef.dot(nIncLocal) < 0f : "Incident face not anti-parallel to nRef";

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

    /* ----- Edge-Edge support ----- */

    static final class EdgeSeg {
        Vector3f a; // endpoint 1
        Vector3f b; // endpoint 2
    }

    public Manifold buildEdgeEdgeManifold(
        @NotNull OrientedBox a,
        @NotNull OrientedBox b,
        int edgeIdxA,
        int edgeIdxB,
        @NotNull Vector3f normal
    ) {
        List<EdgeSeg> edgesA = edgeForAxis(a, edgeIdxA);
        List<EdgeSeg> edgesB = edgeForAxis(b, edgeIdxB);

        float bestDist2 = Float.POSITIVE_INFINITY;
        Vector3f bestP = null, bestQ = null;

        Vector3f p = new Vector3f();
        Vector3f q = new Vector3f();

        for (EdgeSeg eA : edgesA) {
            for (EdgeSeg eB : edgesB) {
                closestPointsOnSegments(eA, eB, p, q);
                float d2 = p.distanceSquared(q);
                if (d2 < bestDist2) {
                    bestDist2 = d2;
                    bestP = new Vector3f(p);
                    bestQ = new Vector3f(q);
                }
            }
        }

        Manifold m = new Manifold();
        if (bestP != null) {
            Vector3f mid = new Vector3f(bestP).add(bestQ).mul(0.5F);
            m.points.add(mid);
            m.centroid.set(mid);
        }
        return m;
    }

    private List<EdgeSeg> edgeForAxis(@NotNull OrientedBox box, int axisIdx) {
        Matrix3f R = box.rotation();
        Vector3f half = box.halfSize();
        Vector3f scale = box.scale();
        Vector3f c = new Vector3f(box.center());

        Vector3f dir = R.getColumn(axisIdx, new Vector3f());
        float halfLen = half.get(axisIdx) * scale.get(axisIdx);

        int uIdx = (axisIdx + 1) % 3;
        int vIdx = (axisIdx + 2) % 3;

        Vector3f u = R.getColumn(uIdx, new Vector3f());
        Vector3f v = R.getColumn(vIdx, new Vector3f());
        float hU = half.get(uIdx) * scale.get(uIdx);
        float hV = half.get(vIdx) * scale.get(vIdx);

        List<EdgeSeg> list = new ArrayList<>(4);
        for (int sU = -1; sU <= 1; sU += 2) {
            for (int sV = -1; sV <= 1; sV += 2) {
                Vector3f base = new Vector3f(c).fma(sU * hU, u).fma(sV * hV, v);
                EdgeSeg e = new EdgeSeg();
                e.a = new Vector3f(base).fma(-halfLen, dir);
                e.b = new Vector3f(base).fma(+halfLen, dir);
                list.add(e);
            }
        }
        return list;
    }

    /**
     * Closest points between two 3D segments e1(a1,b1) and e2(a2,b2)
     * <p>
     * Outputs points on each segment into out1 and out2
     */
    private void closestPointsOnSegments(
        @NotNull EdgeSeg e1,
        @NotNull EdgeSeg e2,
        @NotNull Vector3f out1,
        @NotNull Vector3f out2
    ) {
        Vector3f p1 = e1.a;
        Vector3f q1 = e1.b;
        Vector3f p2 = e2.a;
        Vector3f q2 = e2.b;

        Vector3f d1 = new Vector3f(q1).sub(p1);
        Vector3f d2 = new Vector3f(q2).sub(p2);
        Vector3f r  = new Vector3f(p1).sub(p2);

        float a = d1.dot(d1); // squared length of S1
        float e = d2.dot(d2); // squared length of S2
        float f = d2.dot(r);

        float s, t;

        if (a <= VectorUtils.EPS) {
            s = 0f;
            t = clamp(f / e, 0f, 1f);
        } else {
            float c = d1.dot(r);
            if (e <= VectorUtils.EPS) {
                t = 0f;
                s = clamp(-c / a, 0f, 1f);
            } else {
                float b = d1.dot(d2);
                float denom = a*e - b*b;

                if (denom != 0f) s = clamp((b*f - c*e) / denom, 0f, 1f);
                else s = 0f;

                t = (b*s + f) / e;
                if (t < 0f) {
                    t = 0f;
                    s = clamp(-c / a, 0f, 1f);
                } else if (t > 1f) {
                    t = 1f;
                    s = clamp((b - c) / a, 0f, 1f);
                }
            }
        }

        out1.set(new Vector3f(p1).fma(s, d1));
        out2.set(new Vector3f(p2).fma(t, d2));
    }

}
