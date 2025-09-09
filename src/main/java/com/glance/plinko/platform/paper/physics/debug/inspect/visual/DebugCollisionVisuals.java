package com.glance.plinko.platform.paper.physics.debug.inspect.visual;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.DisplayUtils;
import com.glance.plinko.platform.paper.display.debug.DebugArrow;
import com.glance.plinko.platform.paper.display.debug.LineDisplays;
import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.shape.PhysicsShape;
import com.glance.plinko.platform.paper.utils.math.VectorUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@Slf4j
@UtilityClass
public class DebugCollisionVisuals {

    private final double NORMAL_LEN = 0.25;
    private final double MTV_LEN_SCALE = 1.0;
    private final double VEL_PREVIEW_SCALE = 0.4;
    private final float PENETRATION_SLOP = 0.001f;
    private final float PENETRATION_CORRECT_PCT = 0.80f;

    public void renderCollision(
        @NotNull World world,
        @NotNull PhysicsShape primary,
        @NotNull CollisionResult result,
        float restitution,
        float tangentialDamp,
        @NotNull InspectVisualHandler h
    ) {
        Vector3f normal = new Vector3f(result.normal());
        Vector3f normalFlipped = new Vector3f(normal).negate();
        if (normal.lengthSquared() <= VectorUtils.EPS) return;
        normal.normalize();

        Vector3f c3 = new Vector3f(result.centroid());
        Location centroid = new Location(world, c3.x, c3.y, c3.z);

        var centroidMarker = DisplayUtils.spawnDisplay(centroid, DisplayOptions.marker(Material.SHROOMLIGHT));
        add(h, centroidMarker);

        Vector normalDir = Vector.fromJOML(normalFlipped);
        var normalArrow = LineDisplays.spawnDebugArrow(centroid, normalDir, NORMAL_LEN, 0.03f, Material.ORANGE_CONCRETE_POWDER);
        addLine(h, normalArrow);

        // Minimum translation vector
        Vector mtvDir = Vector.fromJOML(normalFlipped).normalize();
        double mtvLen = Math.max(0.0, result.penetrationDepth()) * MTV_LEN_SCALE;
        if (mtvLen > 0.0) {
            Location start = centroid.clone().subtract(mtvDir.clone().multiply(mtvLen));
            var mtvArrow = LineDisplays.spawnDebugArrow(start, mtvDir, mtvLen, 0.02f, Material.RED_CONCRETE);
            addLine(h, mtvArrow);
        }

        var pts = result.contactPoints();
        if (pts != null && !pts.isEmpty()) {
            for (Vector3f p : pts) {
                Location lp = new Location(world, p.x, p.y, p.z);
                var vtx = DisplayUtils.spawnDisplay(lp, DisplayOptions.marker(Material.RED_CONCRETE, 0.04));
                add(h, vtx);
            }

            if (pts.size() >= 2) {
                for (int i = 0; i < pts.size(); i++) {
                    Vector3f a = pts.get(i);
                    Vector3f b = pts.get((i + 1) % pts.size());
                    spawnSegment(h, world, a, b);
                }
            }
        }
    }

    private void spawnSegment(
        @NotNull InspectVisualHandler h,
        @NotNull World world,
        @NotNull Vector3f a,
        @NotNull Vector3f b
    ) {
        Vector3f dir = new Vector3f(b).sub(a);
        double len = Math.sqrt(dir.lengthSquared());
        if (len <= VectorUtils.EPS) return;

        Location start = new Location(world, a.x, a.y, a.z);
        var seg = LineDisplays.spawnDebugArrow(start,
                Vector.fromJOML(dir.normalize()), len, 0.01f, Material.GREEN_CONCRETE);
        addLine(h, seg);
    }

    public Vector3f computeCorrectionVector(@NotNull CollisionResult result) {
        float pen = Math.max(result.penetrationDepth() - PENETRATION_SLOP, 0F) * PENETRATION_CORRECT_PCT;
        return new Vector3f(result.normal()).negate().mul(pen);
    }

    private void addLine(@NotNull InspectVisualHandler h, @NotNull DebugArrow debugArrow) {
        add(h, debugArrow.body());
        add(h, debugArrow.tip());
    }

    private void add(@NotNull InspectVisualHandler h, Display d) {
        h.add(InspectVisualHandler.InspectVisualType.COLLISION, d);
    }

}
