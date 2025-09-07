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

@Slf4j
@UtilityClass
public class DebugCollisionVisuals {

    private final double NORMAL_LEN = 0.5;

    public void renderCollision(
        @NotNull World world,
        @NotNull PhysicsShape primary,
        @NotNull CollisionResult result,
        float restitution,
        float tangentialDamp,
        @NotNull InspectVisualHandler h
    ) {
        Vector n = Vector.fromJOML(result.normal());
        if (n.lengthSquared() <= VectorUtils.EPS) return;
        n.normalize();

        var c = result.contactPoint();
        Location anchor = new Location(world, c.x, c.y, c.z);

        var cp = DisplayUtils.spawnDisplay(anchor, DisplayOptions.marker(Material.SHROOMLIGHT));
        addVisual(h, cp);

        var normalLine = LineDisplays.spawnDebugArrow(anchor, n, NORMAL_LEN, Material.ORANGE_CONCRETE_POWDER);
        addVisual(h, normalLine.tip());
        addVisual(h, normalLine.body());
    }

    private void addVisual(@NotNull InspectVisualHandler h, Display d) {
        h.add(InspectVisualHandler.InspectVisualType.COLLISION, d);
    }

}
