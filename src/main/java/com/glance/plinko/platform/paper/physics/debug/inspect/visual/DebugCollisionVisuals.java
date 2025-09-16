package com.glance.plinko.platform.paper.physics.debug.inspect.visual;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.DisplayUtils;
import com.glance.plinko.platform.paper.display.debug.DebugArrow;
import com.glance.plinko.platform.paper.display.debug.LineDisplays;
import com.glance.plinko.platform.paper.physics.collision.CollisionResponder;
import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
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

    private final double NORMAL_LEN = 0.1;
    private final double MTV_LEN_SCALE = 1.0;
    private final double VEL_PREVIEW_SCALE = 0.4;
    private final float PENETRATION_SLOP = 0.001f;
    private final float PENETRATION_CORRECT_PCT = 0.80f;

    public void renderCollision(
        @NotNull World world,
        @NotNull PhysicsShape primary,
        @NotNull CollisionResult result,
        @NotNull Vector3f orientedNormal,
        float restitution,
        float tangentialDamp,
        @NotNull InspectVisualHandler h
    ) {
//        Vector3f normal = new Vector3f(result.normal());
//        Vector3f normalFlipped = new Vector3f(normal).negate();
//        if (normal.lengthSquared() <= VectorUtils.EPS) return;
//        normal.normalize();

        Vector3f centroidVec = new Vector3f(result.centroid());
        Location centroidLoc = new Location(world, centroidVec.x, centroidVec.y, centroidVec.z);

        var centroidMarker = DisplayUtils.spawnDisplay(centroidLoc,
                DisplayOptions.marker(Material.BLACKSTONE, 0.02));
        add(h, centroidMarker);
        log.warn("Centroid: {}", centroidVec);

        if (primary instanceof OrientedBox obb) {
            var contact = CollisionResponder.surfaceContactPoint(obb, centroidVec, orientedNormal);
            log.warn("Contact Pt: {}", contact);
            Location contactLoc = new Location(world, contact.x, contact.y, contact.z);
            var contactMarker = DisplayUtils.spawnDisplay(contactLoc,
                    DisplayOptions.marker(Material.SHROOMLIGHT, 0.025));
            add(h, contactMarker);
        }

        Vector normalDir = Vector.fromJOML(orientedNormal);
        var normalArrow = LineDisplays.spawnDebugArrow(centroidLoc, normalDir, NORMAL_LEN,
                0.01f, Material.SAND);
        addLine(h, normalArrow);

        // Minimum translation vector
        Vector mtvDir = Vector.fromJOML(orientedNormal).normalize();
        double mtvLen = Math.max(0.0, result.penetrationDepth()) * MTV_LEN_SCALE;
        if (mtvLen > 0.0) {
            Location start = centroidLoc.clone().subtract(mtvDir.clone().multiply(mtvLen));
            var mtvArrow = LineDisplays.spawnDebugArrow(start, mtvDir, mtvLen, 0.015f,
                    Material.ORANGE_CONCRETE);
            addLine(h, mtvArrow);
        }

        var pts = result.contactPoints();
        if (pts != null && !pts.isEmpty()) {
            for (Vector3f p : pts) {
                Location lp = new Location(world, p.x, p.y, p.z);
                var vtx = DisplayUtils.spawnDisplay(lp, DisplayOptions.marker(Material.RED_CONCRETE, 0.05));
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
