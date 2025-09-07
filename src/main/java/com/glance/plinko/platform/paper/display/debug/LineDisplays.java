package com.glance.plinko.platform.paper.display.debug;

import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.DisplayUtils;
import com.glance.plinko.platform.paper.display.Transformer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Slf4j
@UtilityClass
public class LineDisplays {

    private static final Vector3f WORLD_UP = new Vector3f(0, 1, 0);
    private static final Vector3f ALT_UP   = new Vector3f(1, 0, 0);

    public @NotNull DebugArrow spawnDebugArrow(
        @NotNull Location start,
        @NotNull Vector direction,
        double length
    ) {
        Location end = start.clone().add(direction.clone().normalize().multiply(length));
        return spawnDebugArrow(start, end, 0.025F, Material.WHITE_CONCRETE, Material.YELLOW_CONCRETE);
    }

    public @NotNull DebugArrow spawnDebugArrow(
            @NotNull Location start,
            @NotNull Vector direction,
            double length,
            @NotNull Material mat
    ) {
        Location end = start.clone().add(direction.clone().normalize().multiply(length));
        return spawnDebugArrow(start, end, 0.025F, mat, Material.YELLOW_CONCRETE);
    }

    public @NotNull DebugArrow spawnDebugArrow(
            @NotNull Location start,
            @NotNull Location end,
            float thickness,
            @NotNull Material lineMaterial,
            @Nullable Material tipMaterial
    ) {
        final World world = start.getWorld();
        if (world == null) throw new IllegalStateException("Unloaded world");

        Vector dirBukkit = end.toVector().subtract(start.toVector());
        double length = dirBukkit.length();
        if (length <= 1e-5) throw new IllegalArgumentException("Start and end are too similar");

        DisplayOptions bodyOpts = DisplayOptions.defaultBlock(lineMaterial);
        Display body = DisplayUtils.spawnDisplay(start.clone(), bodyOpts);
        applyLineTransform(body, dirBukkit, thickness, (float) length);

        Material tipMat = tipMaterial != null ? tipMaterial : Material.YELLOW_CONCRETE;
        Display tip = DisplayUtils.spawnDisplay(end.clone(), DisplayOptions.defaultBlock(tipMat));

        float tipScale = thickness * 1.15F;
        float tipLen   = Math.max(thickness * 1.5f, 0.05f);
        applyTipTransform(tip, dirBukkit, tipScale, tipLen);

        return new DebugArrow(body, tip);
    }

    public @NotNull DebugArrow spawnDebugArrow(
            @NotNull Location start,
            @NotNull Location end,
            float thickness,
            @NotNull Material material
    ) {
        return spawnDebugArrow(start, end, thickness, material, null);
    }

    public @NotNull DebugArrow spawnDebugArrow(
        @NotNull Location start,
        @NotNull Vector dir,
        double length,
        float thickness,
        @NotNull Material material
    ) {
        if (dir.lengthSquared() == 0.0) {
            throw new IllegalArgumentException("Must be non zero dir");
        }
        Location end = start.clone().add(dir.clone().normalize().multiply(length));
        return spawnDebugArrow(start, end, thickness, material);
    }

    public void update(
        @NotNull DebugArrow handles,
        @NotNull Location start,
        @NotNull Location end,
        float thickness
    ) {
        Vector dir = end.clone().subtract(start).toVector();
        float length = (float) dir.length();
        if (length <= 1e-5) return;

        handles.body().teleport(start.clone());
        applyLineTransform(handles.body(), dir, thickness, length);

        handles.tip().teleport(end.clone());
        float tipScale = thickness * 1.5F;
        float tipLen = Math.max(thickness * 1.5F, 0.05F);
        applyTipTransform(handles.tip(), dir, tipScale, tipLen);
    }

    public void destroy(@NotNull DebugArrow handles) {
        handles.body().remove();
        handles.tip().remove();
    }

    // Line logic

    private void applyLineTransform(
        @NotNull Display body,
        @NotNull Vector bukkitDir,
        float thickness,
        float length
    ) {
        Vector3f dir = bukkitDir.toVector3f();
        dir.normalize();

        Transformation tf = transformFromMat(
                buildLineMatrix(bukkitDir.toVector3f(), null, length, thickness));

        Transformer.applyTransform(body, tf, false);
    }

    private void applyTipTransform(
        @NotNull Display tip,
        @NotNull Vector bukkitDir,
        float tipScale,
        float tipLength
    ) {
        Vector3f dir = bukkitDir.toVector3f();
        dir.normalize();

        Vector3f up = computeStableUp(dir, null);
        Quaternionf rot = new org.joml.Matrix4f()
                .rotationTowards(dir, up)
                .getNormalizedRotation(new Quaternionf());

        Vector3f localOffset = new Vector3f(-tipScale / 2f, -tipScale / 2f, -tipLength / 2f);
        Vector3f translation = localOffset.rotate(new Quaternionf(rot));

        Vector3f scale = new Vector3f(tipScale, tipScale, tipLength);

        Transformation tf = new Transformation(translation, rot, scale, new Quaternionf());
        Transformer.applyTransform(tip, tf, false);
    }

    private Matrix4f buildLineMatrix(
        @NotNull Vector3f direction,
        @Nullable Vector facing,
        double length,
        float thickness
    ) {
        Vector3f dir = direction.normalize();

        Matrix4f mat = new Matrix4f();
        Vector3f up = computeStableUp(new Vector3f(dir), null);

        mat.rotationTowards(new Vector3f(dir), up);
        mat.translate(-thickness / 2F, -thickness / 2F, 0F);
        mat.scale(thickness, thickness, (float) length);

        return mat;
    }

    private Transformation transformFromMat(@NotNull Matrix4f mat) {
        Vector3f translation = mat.getTranslation(new Vector3f());
        Quaternionf rot = mat.getUnnormalizedRotation(new Quaternionf());
        Vector3f scale = mat.getScale(new Vector3f());

        return new Transformation(translation, rot, scale, new Quaternionf());
    }

    private @NotNull Vector3f computeStableUp(
        @NotNull Vector3f dir,
        @Nullable Vector facing
    ) {
        Vector3f up = facing != null ? facing.toVector3f() : new Vector3f(WORLD_UP);

        // If nearly parallel with dir, swap to a safe alternative
        if (Math.abs(up.dot(dir)) > 0.98f) {
            up.set(ALT_UP);
        }

        // Remove dir component: up = up - (up·dir)*dir
        float d = up.dot(dir);
        up.fma(-d, dir);

        // If too small, pick another axis
        if (up.lengthSquared() < 1e-6f) {
            if (Math.abs(dir.x) < 0.9f) up.set(1, 0, 0);
            else up.set(0, 0, 1);
            d = up.dot(dir);
            up.fma(-d, dir);
        }

        up.normalize();
        return up;
    }

}
