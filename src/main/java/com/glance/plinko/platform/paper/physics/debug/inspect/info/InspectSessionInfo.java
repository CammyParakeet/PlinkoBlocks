package com.glance.plinko.platform.paper.physics.debug.inspect.info;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.debug.inspect.InspectSession;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.time.Instant;
import java.util.Locale;

@UtilityClass
public class InspectSessionInfo {

    public String debugDump(InspectSession session) {
        final String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder(2048);

        sb.append(nl);
        sb.append("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓").append(nl);
        sb.append("┃ InspectSession Dump @ ").append(Instant.now()).append(" ┃").append(nl);
        sb.append("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛").append(nl);

        // debug per object
        for (int i = 0; i < 2; i++) {
            sb.append("Slot #").append(i).append(nl);

            PlinkoObject obj = session.getObject(i);

            if (obj == null) {
                sb.append("  (no object)").append(nl);
            } else {
                // Kinematics
                Vector3f pos = safe(obj.getPosition());
                Quaternionf rot = obj.getRotation() == null ? new Quaternionf() : obj.getRotation();
                Vector3f scl = safe(obj.getScale());
                Vector3f vel = safe(obj.getVelocity());
                Vector3f ang = safe(obj.getAngularVelocity());

                sb.append("  Object:").append(nl);
                sb.append("    pos: ").append(fmt(pos)).append(nl);
                sb.append("    rot(q): ").append(fmt(rot)).append("  eulerXYZ°: ").append(fmtDeg(eulerXYZ(rot))).append(nl);
                sb.append("    scale: ").append(fmt(scl)).append(nl);
                if (obj.isImmovable()) {
                    sb.append("    obj is immovable.").append(nl);;
                } else {
                    sb.append("    v: ").append(fmt(vel)).append(" |v|=").append(fmt(len(vel))).append(nl);
                    sb.append("    w: ").append(fmt(ang)).append(" |w|=").append(fmt(len(ang))).append(nl);
                }

                try {
                    if (!(obj.currentShape() instanceof OrientedBox box)) continue;

                    Vector3f c = box.center();
                    Vector3f h = box.halfSize();
                    Matrix3f R = box.rotation();
                    Vector3f ax = R.getColumn(0, new Vector3f());
                    Vector3f ay = R.getColumn(1, new Vector3f());
                    Vector3f az = R.getColumn(2, new Vector3f());

                    sb.append("    shape: OrientedBox").append(nl);
                    sb.append("      center: ").append(fmt(c)).append(nl);
                    sb.append("      halfSize: ").append(fmt(h)).append(nl);
                    sb.append("      axes: X=").append(fmt(ax)).append(" Y=").append(fmt(ay)).append(" Z=").append(fmt(az)).append(nl);

                    Vector3f[] corners = computeObbCorners(c, h, ax, ay, az);
                    for (int k = 0; k < corners.length; k++) {
                        sb.append("      corner[").append(k).append("]: ").append(fmt(corners[k])).append(nl);
                    }
                } catch (Throwable t) {
                    sb.append("    shape: <error reading> ").append(t.getClass().getSimpleName()).append(": ")
                            .append(t.getMessage()).append(nl);
                }
            }

            if (i == 0) sb.append("  ───────────────────────────────────────────────────────────").append(nl);
        }

        if (session.getLastResult() == null) {
            sb.append("No Recent Collision").append(nl);
        } else {
            sb.append("CollisionResult:").append(nl);
            CollisionResult cr = session.getLastResult();

            sb.append("  contact point: ").append(fmt(cr.centroid())).append(nl);
            try {
                Vector3f n = safe(cr.normal());
                float depth = cr.penetrationDepth();
                sb.append("  normal: ").append(fmtNorm(n)).append("  depth: ").append(fmt(depth)).append(nl);
            } catch (Throwable t) {
                sb.append("  normal/depth: <error> ").append(t.getMessage()).append(nl);
            }
        }

        return sb.toString();
    }


    /* ---- Utils ---- */

    private Vector3f[] computeObbCorners(Vector3f c, Vector3f h, Vector3f ax, Vector3f ay, Vector3f az) {
        Vector3f[] out = new Vector3f[8];
        int idx = 0;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    Vector3f corner = new Vector3f(c)
                        .add(new Vector3f(ax).mul(sx * h.x))
                        .add(new Vector3f(ay).mul(sy * h.y))
                        .add(new Vector3f(az).mul(sz * h.z));
                    out[idx++] = corner;
                }
            }
        }
        return out;
    }

    private Vector3f safe(@Nullable Vector3f v) {
        return v != null ? new Vector3f(v) : new Vector3f();
    }

    private String fmt(Vector3f v) {
        return String.format(Locale.ROOT, "(%.3f, %.3f, %.3f)", v.x, v.y, v.z);
    }

    private String fmt(Quaternionf q) {
        return String.format(Locale.ROOT, "(x=%.4f, y=%.4f, z=%.4f, w=%.4f)", q.x, q.y, q.z, q.w);
    }

    private String fmt(float f) {
        return String.format(Locale.ROOT, "%.4f", f);
    }

    private static String fmtNorm(Vector3f v) {
        Vector3f n = new Vector3f(v);
        if (n.lengthSquared() > 0f) n.normalize();
        return fmt(n);
    }

    private float len(Vector3f v) { return (float) Math.sqrt(v.lengthSquared()); }

    /** Euler XYZ (radians) -> degrees; JOML returns XYZ order for getEulerAnglesXYZ */
    private static Vector3f eulerXYZ(Quaternionf q) {
        Vector3f rads = new Vector3f();
        q.getEulerAnglesXYZ(rads); // X then Y then Z (in radians)
        return new Vector3f((float) Math.toDegrees(rads.x), (float) Math.toDegrees(rads.y), (float) Math.toDegrees(rads.z));
    }
    private static String fmtDeg(Vector3f deg) {
        return String.format(Locale.ROOT, "(x=%.2f°, y=%.2f°, z=%.2f°)", deg.x, deg.y, deg.z);
    }

}
