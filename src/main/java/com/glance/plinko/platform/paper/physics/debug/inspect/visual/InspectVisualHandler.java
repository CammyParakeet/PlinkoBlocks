package com.glance.plinko.platform.paper.physics.debug.inspect.visual;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.DisplayUtils;
import com.glance.plinko.platform.paper.display.Transformer;
import com.glance.plinko.platform.paper.display.debug.LineDisplays;
import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
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

            Quaternionf rotation = new Quaternionf().setFromNormalized(box.rotation());
            Vector3f center = box.center();
            Vector3f[] axes = box.axes();
            Vector3f halfSize = box.halfSize();
            Vector3f scale = obj.getScale();

            Material mat = (i == 0) ? PRIMARY_CORNERS : SECONDARY_CORNERS;
            DisplayOptions opts = new DisplayOptions(
                    DisplayOptions.Type.ITEM,
                    mat,
                    new Vector(0.1, 0.1, 0.1),
                    false
            );

            for (int x = -1; x <= 1; x += 2) {
                for (int y = -1; y <= 1; y += 2) {
                    for (int z = -1; z <= 1; z += 2) {
                        Vector3f offset = new Vector3f()
                                .fma(x * halfSize.x * scale.x, axes[0])
                                .fma(y * halfSize.y * scale.y, axes[1])
                                .fma(z * halfSize.z * scale.z, axes[2]);

                        Vector3f worldPos = new Vector3f(center).add(offset);
                        Location loc = new Location(world, worldPos.x, worldPos.y, worldPos.z);

                        Display marker = DisplayUtils.spawnDisplay(loc, opts);
                        Transformer.modifyTransform(marker, false, t -> t.getLeftRotation().set(rotation));

                        add(InspectVisualType.SHAPE_CORNERS, marker);
                    }
                }
            }

            Location centerLoc = new Location(world, center.x, center.y, center.z);
            Material centerMat = (i == 0) ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK;
            DisplayOptions centerOpts = new DisplayOptions(
                    DisplayOptions.Type.ITEM,
                    centerMat,
                    new Vector(0.15, 0.15, 0.15),
                    false
            );

            Display centerMarker = DisplayUtils.spawnDisplay(centerLoc, centerOpts);
            Transformer.modifyTransform(centerMarker, false, t -> t.getLeftRotation().set(rotation));
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

        DebugCollisionVisuals.renderCollision(world, box, result, 1.0F, 1.0F, this);
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

            Location start = new Location(world, pos.x, pos.y, pos.z);

            if (velocity.length() > 1e-5) {
                var arrow = LineDisplays.spawnDebugArrow(start, Vector.fromJOML(velocity), velocity.length());
                add(InspectVisualType.KINEMATICS, arrow.body());
                add(InspectVisualType.KINEMATICS, arrow.tip());
            } else {
                log.warn("Toggled kinematics but object had no velocity");
            }

//          TODO later
//            if (angular.length() > 1e-5) {
//                // todo angular arrow
//                add(InspectVisualType.KINEMATICS, );
//            }
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
