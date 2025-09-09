package com.glance.plinko.platform.paper.physics.debug.inspect.visual;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.DisplayUtils;
import com.glance.plinko.platform.paper.display.Transformer;
import com.glance.plinko.platform.paper.display.debug.ArcGizmo;
import com.glance.plinko.platform.paper.display.debug.LineDisplays;
import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.physics.collision.CollisionResponder;
import com.glance.plinko.platform.paper.physics.collision.CollisionResponse;
import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.debug.inspect.InspectSession;
import com.glance.plinko.platform.paper.physics.shape.OrientedBox;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Slf4j
public class InspectVisualHandler {

    private final EnumMap<InspectVisualType, Boolean> visibilityFlags = new EnumMap<>(InspectVisualType.class);
    private final EnumMap<InspectVisualType, List<Display>> visuals = new EnumMap<>(InspectVisualType.class);

    private static final Material PRIMARY_CORNERS = Material.WHITE_CONCRETE;
    private static final Material SECONDARY_CORNERS = Material.BLACK_CONCRETE;

    public enum InspectVisualType {
        SHAPE_CORNERS,
        COLLISION,
        KINEMATICS
    }

    public InspectVisualHandler() {
        for (InspectVisualType type : InspectVisualType.values()) {
            visibilityFlags.put(type, false);
            visuals.put(type, new ArrayList<>());
        }
    }

    public void add(
            @NotNull InspectVisualType type,
            @NotNull Display display
    ) {
        visuals.get(type).add(display);
    }

    public void toggle(
        @NotNull Player player,
        @NotNull InspectVisualType type,
        @NotNull InspectSession session
    ) {
        boolean nowEnabled = !visibilityFlags.getOrDefault(type, false);
        visibilityFlags.put(type, nowEnabled);

        if (nowEnabled) {
            render(player, type, session);
        } else {
            clear(type);
        }
    }

    public void update(
        @NotNull Player player,
        @NotNull InspectSession session
    ) {
        for (InspectVisualType type : InspectVisualType.values()) {
            if (isEnabled(type)) {
                render(player, type, session);
            }
        }
    }

    public void render(
        @NotNull Player player,
        @NotNull InspectVisualType type,
        @NotNull InspectSession session
    ) {
        switch (type) {
            case SHAPE_CORNERS -> renderCorners(player.getWorld(), session);
            case COLLISION -> renderCollision(player.getWorld(), session);
            case KINEMATICS -> renderKinematics(player.getWorld(), session);
        }
    }

    private void renderCorners(
        @NotNull World world,
        @NotNull InspectSession session
    ) {
        clear(InspectVisualType.SHAPE_CORNERS);

        for (int i = 0; i < 2; i++) {
            PlinkoObject obj = session.getObject(i);
            if (obj == null) continue;

            if (!(obj.currentShape() instanceof OrientedBox box)) continue;
            Vector3f center = box.center();

            Material mat = (i == 0) ? PRIMARY_CORNERS : SECONDARY_CORNERS;
            var opts = DisplayOptions.marker(mat, 0.05);
            Quaternionf rotation = new Quaternionf().setFromNormalized(box.rotation());

            for (var corner : box.corners()) {
                Vector3f worldPos = new Vector3f(corner);
                Location loc = new Location(world, worldPos.x, worldPos.y, worldPos.z);

                Display marker = DisplayUtils.spawnDisplay(loc, opts);
                Transformer.modifyTransform(marker, false, t -> t.getLeftRotation().set(rotation));

                add(InspectVisualType.SHAPE_CORNERS, marker);
            }

            Location centerLoc = new Location(world, center.x, center.y, center.z);
            Material centerMat = (i == 0) ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;
            DisplayOptions centerOpts = new DisplayOptions(
                    DisplayOptions.Type.ITEM,
                    centerMat,
                    new Vector(0.1, 0.1, 0.1),
                    new Quaternionf(rotation),
                    false
            );

            Display centerMarker = DisplayUtils.spawnDisplay(centerLoc, centerOpts);

            add(InspectVisualType.SHAPE_CORNERS, centerMarker);
        }
    }

    private void renderCollision(
        @NotNull World world,
        @NotNull InspectSession session
    ) {
        clear(InspectVisualType.COLLISION);
        CollisionResult result = session.getLastResult();
        if (result == null) return;

        PlinkoObject main = session.getObject(0);
        if (main == null || main.isImmovable()) return;
        if (!(main.currentShape() instanceof OrientedBox box)) return;

        DebugCollisionVisuals
            .renderCollision(world, box, result, 1.0F, 1.0F, this);

        CollisionResponse response = CollisionResponder.planResponseLite(main, result);

        Vector3f pos = box.center();
        Location start = new Location(world, pos.x, pos.y, pos.z);
        log.warn("Linear Delta: {}", response.linearVelocityDelta());
        var velocity = new Vector3f(main.getVelocity()).add(response.linearVelocityDelta());
        if (velocity.length() > 1e-5) {
            var arrow = LineDisplays.spawnDebugArrow(start, Vector.fromJOML(velocity), velocity.length(),
                    0.01f, Material.RED_CONCRETE);
            add(InspectVisualType.COLLISION, arrow.body());
            add(InspectVisualType.COLLISION, arrow.tip());
        } else {
            log.warn("Not doing linear as it's {}", velocity);
        }

        log.warn("Angular Delta: {}", response.angularVelocityDelta());
        var angularVel = new Vector3f(main.getAngularVelocity()).add(response.angularVelocityDelta());
        if (angularVel.length() > 1e-5) {
            var arc = ArcGizmo.renderAngularArc(
                    world,
                    new Vector3f(pos),
                    new Vector3f(angularVel),
                    0.25,
                    0.35,
                    Math.PI * 1.75,
                    16,
                    0.0145F,
                    Material.RED_CONCRETE
            );

            if (arc.getTip() != null) {
                add(InspectVisualType.COLLISION, arc.getTip().tip());
                add(InspectVisualType.COLLISION, arc.getTip().body());
            }

            arc.getSegments().forEach(d -> add(InspectVisualType.COLLISION, d));
        } else {
            log.warn("Not doing angular as it's {}", angularVel);
        }

        //Vector3f correction = DebugCollisionVisuals.computeCorrectionVector(result);
        Vector3f corrected = new Vector3f(main.getPosition()).add(response.correction());
        Location ghostLoc = new Location(world, corrected.x, corrected.y, corrected.z);

        var ghostOpts = DisplayOptions.fromObject(Material.LIGHT_BLUE_STAINED_GLASS, main);
        Display ghost = DisplayUtils.spawnDisplay(ghostLoc, ghostOpts);

        add(InspectVisualType.COLLISION, ghost);
    }

    private void renderKinematics(
        @NotNull World world,
        @NotNull InspectSession session
    ) {
        clear(InspectVisualType.KINEMATICS);

        for (int i = 0; i < 2; i++) {
            PlinkoObject obj = session.getObject(i);
            if (obj == null || obj.isImmovable()) continue;

            if (!(obj.currentShape() instanceof OrientedBox box)) continue;

            Vector3f pos = box.center();
            Vector3f velocity = obj.getVelocity();
            Vector3f angular = obj.getAngularVelocity();
            Vector3f angularVel = new Vector3f(2.25f, 3.5f, -1.1f);
            obj.setAngularVelocity(angularVel);

            Location start = new Location(world, pos.x, pos.y, pos.z);

            if (velocity.length() > 1e-5) {
                var arrow = LineDisplays.spawnDebugArrow(start, Vector.fromJOML(velocity), velocity.length());
                add(InspectVisualType.KINEMATICS, arrow.body());
                add(InspectVisualType.KINEMATICS, arrow.tip());
            } else {
                log.warn("Toggled kinematics but object had no velocity");
            }

            if (angularVel.length() > 1e-5) {
                var arc = ArcGizmo.renderAngularArc(
                    world,
                    new Vector3f(pos),
                    new Vector3f(angularVel),
                    0.3,
                    0.35,
                    Math.PI * 1.75,
                    16,
                    0.02F,
                        Material.LIME_CONCRETE
                );

                if (arc.getTip() != null) {
                    add(InspectVisualType.KINEMATICS, arc.getTip().tip());
                    add(InspectVisualType.KINEMATICS, arc.getTip().body());
                }

                arc.getSegments().forEach(d -> add(InspectVisualType.KINEMATICS, d));
            }
        }
    }

    public boolean isEnabled(@NotNull InspectVisualType type) {
        return visibilityFlags.getOrDefault(type, false);
    }

    public void clear(@NotNull InspectVisualType type) {
        List<Display> list = visuals.get(type);
        list.forEach(Display::remove);
        list.clear();
    }

    public void clearAll() {
        for (InspectVisualType type : InspectVisualType.values()) {
            clear(type);
        }
    }

}
