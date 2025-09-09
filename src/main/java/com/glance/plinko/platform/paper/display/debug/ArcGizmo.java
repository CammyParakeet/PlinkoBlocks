package com.glance.plinko.platform.paper.display.debug;

import com.glance.plinko.platform.paper.utils.math.VectorUtils;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ArcGizmo {

    @Value
    public class ArcHandles {
        List<Display> segments;
        @Nullable DebugArrow tip;
    }

    public @NotNull ArcHandles renderAngularArc(
            @NotNull World world,
            @NotNull Vector3f center,
            @NotNull Vector3f angularVelocity,
            double radius,
            double angleScale,
            double maxAngle,
            int segments,
            float thickness,
            @NotNull Material material
    ) {
        final float wLen = angularVelocity.length();
        if (wLen < VectorUtils.EPS || radius <= VectorUtils.EPS || segments <= 0) {
            return new ArcHandles(List.of(), null);
        }

        // Spin axis
        final Vector3f axis = new Vector3f(angularVelocity).normalize();
        // Stable start dir
        final Vector3f startDir = stablePerp(axis).normalize().mul((float) radius);

        // Sweep angle
        final double theta = Math.min(wLen * angleScale, maxAngle);
        if (theta <= VectorUtils.EPS) {
            return new ArcHandles(List.of(), null);
        }

        final List<Vector3f> pts = new ArrayList<>(segments + 1);
        for (int i = 0; i <= segments; i++) {
            final double t = (double) i / (double) segments;
            final float angle = (float) (t * theta);
            final Quaternionf q = new Quaternionf().fromAxisAngleRad(axis.x, axis.y, axis.z, angle);
            final Vector3f p = new Vector3f(startDir).rotate(q).add(center);
            pts.add(p);
        }

        final List<Display> bodies = new ArrayList<>(segments);
        for (int i = 0; i < segments; i++) {
            final Location a = toLoc(world, pts.get(i));
            final Location b = toLoc(world, pts.get(i + 1));

            DebugArrow seg = LineDisplays.spawnDebugArrow(a, b, thickness, material);
            seg.tip().remove();
            bodies.add(seg.body());
        }

        DebugArrow tipArrow;
        {
            final Vector3f end  = pts.getLast();
            final Vector3f prev = pts.get(pts.size() - 2);
            final Vector3f tangent = new Vector3f(end).sub(prev).normalize();

            final Location tipStart = toLoc(world, end);
            final double tipLen = Math.max(thickness * 1.5, 0.08);
            tipArrow = LineDisplays
                    .spawnDebugArrow(tipStart, Vector.fromJOML(tangent), tipLen, thickness, material);
        }

        return new ArcHandles(bodies, tipArrow);
    }

    /**
     * Destroy all spawned displays for this arc
     */
    public static void destroy(@NotNull ArcHandles handles) {
        for (Display d : handles.getSegments()) {
            d.remove();
        }
        if (handles.getTip() != null) {
            LineDisplays.destroy(handles.getTip());
        }
    }

    /* Stable perpendicular vector to axis */
    private @NotNull Vector3f stablePerp(@NotNull Vector3f axis) {
        Vector3f up = new Vector3f(0, 1, 0);
        if (Math.abs(up.dot(axis)) > 0.98f) up.set(1, 0, 0);

        float d = up.dot(axis);
        up.fma(-d, axis).normalize();
        return up;
    }

    private @NotNull Location toLoc(@NotNull World w, @NotNull Vector3f v) {
        return new Location(w, v.x, v.y, v.z);
    }

}