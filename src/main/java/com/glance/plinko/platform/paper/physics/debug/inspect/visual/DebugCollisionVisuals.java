package com.glance.plinko.platform.paper.physics.debug.inspect.visual;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.DisplayUtils;
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

    private final double NORMAL_LEN = 0.5;
    private final double MTV_LEN_SCALE = 1.0;
    private final double VEL_PREVIEW_SCALE = 0.4;

    public void renderCollision(
        @NotNull World world,
        @NotNull PhysicsShape primary,
        @NotNull CollisionResult result,
        float restitution,
        float tangentialDamp,
        @NotNull InspectVisualHandler h
    ) {
        Vector3f n3 = new Vector3f(result.normal());
        if (n3.lengthSquared() <= VectorUtils.EPS) return;
        n3.normalize();

        Vector3f c3 = new Vector3f(result.centroid());
        Location centroid = new Location(world, c3.x, c3.y, c3.z);

        var centroidMarker = DisplayUtils.spawnDisplay(centroid, DisplayOptions.marker(Material.SHROOMLIGHT));
        add(h, centroidMarker);

        Vector nB = Vector.fromJOML(n3);
        var normalArrow = LineDisplays.spawnDebugArrow(centroid, nB, NORMAL_LEN, Material.ORANGE_CONCRETE_POWDER);
        add(h, normalArrow.body());
        add(h, normalArrow.tip());

        double mtvLen = Math.max(0.0, result.penetrationDepth()) * MTV_LEN_SCALE;
        if (mtvLen > 0.0) {
            var mtvArrow = LineDisplays.spawnDebugArrow(centroid, nB, mtvLen, Material.RED_CONCRETE);
            add(h, mtvArrow.body());
            add(h, mtvArrow.tip());
        }

        var pts = result.contactPoints();
        if (pts != null && !pts.isEmpty()) {
            for (Vector3f p : pts) {
                Location lp = new Location(world, p.x, p.y, p.z);
                var vtx = DisplayUtils.spawnDisplay(lp, DisplayOptions.marker(Material.BLACKSTONE, 0.02));
                add(h, vtx);
            }

//            if (pts.size() >= 2) {
//                for (int i = 0; i < pts.size(); i++) {
//                    Vector3f a = pts.get(i);
//                }
//            }
        }
    }

    private void add(@NotNull InspectVisualHandler h, Display d) {
        h.add(InspectVisualHandler.InspectVisualType.COLLISION, d);
    }

}
