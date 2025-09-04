package com.glance.plinko.platform.paper.command.debug;

import com.glance.plinko.platform.paper.command.engine.CommandHandler;
import com.glance.plinko.platform.paper.config.PlinkoObjectConfig;
import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.PlinkoDisplayFactory;
import com.glance.plinko.platform.paper.display.Transformer;
import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;
import com.glance.plinko.platform.paper.game.simulation.factory.PlinkoObjectFactory;
import com.glance.plinko.platform.paper.physics.collision.CollisionResult;
import com.glance.plinko.platform.paper.physics.debug.inspect.InspectSession;
import com.glance.plinko.platform.paper.physics.debug.inspect.InspectVisualHandler;
import com.glance.plinko.platform.paper.physics.debug.inspect.InspectorManager;
import com.glance.plinko.platform.paper.physics.debug.inspect.tool.InspectorToolBuilder;
import com.glance.plinko.platform.paper.physics.debug.inspect.tool.InspectorToolType;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

@Slf4j
@Singleton
@AutoService(CommandHandler.class)
public class InspectCollisionCommands implements CommandHandler {

    private final InspectorManager inspectorManager;
    private final PlinkoObjectFactory objectFactory;
    private final PlinkoDisplayFactory displayFactory;

    @Inject
    public InspectCollisionCommands(
            @NotNull final InspectorManager inspectorManager,
            @NotNull final PlinkoObjectFactory objectFactory,
            @NotNull final PlinkoDisplayFactory displayFactory
    ) {
        this.inspectorManager = inspectorManager;
        this.displayFactory = displayFactory;
        this.objectFactory = objectFactory;
    }

    @Command("inspect-collision setup [mass0] [mass1]")
    public void setup(
        @NotNull Player player,
        @Nullable @Argument("mass0") Float mass0,
        @Nullable @Argument("mass1") Float mass1
    ) {
        Location base = player.getLocation();
        base.setYaw(0F);
        base.setPitch(0F);
        Location loc0 = base.clone().add(0, 2, 0);

        // Object 0 - Primary (dynamic)
        float massFor0 = (mass0 != null) ? mass0 : 1.0F;
        PlinkoObjectConfig config0 = PlinkoObjectConfig
                .builder()
                .mass(massFor0)
                .display(DisplayOptions.defaultItem(Material.BLUE_STAINED_GLASS))
                .build();
        PlinkoRunContext ctx0 = new PlinkoRunContext(UUID.randomUUID(), config0, loc0, true);
        PlinkoObject obj0 = objectFactory.create(ctx0);
        Display disp0 = displayFactory.createDisplay(ctx0);

        // Object 1 - Secondary (static)
        Location loc1 = base.clone();
        float massFor1 = (mass1 != null) ? mass1 : Float.POSITIVE_INFINITY;
        PlinkoObjectConfig config1 = PlinkoObjectConfig
                .builder()
                .mass(massFor1)
                .display(DisplayOptions.defaultItem(Material.YELLOW_STAINED_GLASS))
                .build();
        PlinkoRunContext ctx1 = new PlinkoRunContext(UUID.randomUUID(), config1, loc1, true);
        PlinkoObject obj1 = objectFactory.create(ctx1);
        Display disp1 = displayFactory.createDisplay(ctx1);

        InspectSession session = inspectorManager.getOrCreate(player);
        session.set(0, obj0, disp0);
        session.set(1, obj1, disp1);

        player.getInventory().clear();
        giveInspectHotbar(player.getInventory());

        player.sendMessage("Inspector Setup Complete");
    }

    private void giveInspectHotbar(@NotNull PlayerInventory inv) {
        inv.setItem(0, InspectorToolBuilder.buildTool(InspectorToolType.MOVE));
        inv.setItem(1, InspectorToolBuilder.buildTool(InspectorToolType.ROTATE));
        inv.setItem(2, InspectorToolBuilder.buildTool(InspectorToolType.SCALE));
        inv.setItem(3, InspectorToolBuilder.buildTool(InspectorToolType.COLLIDE));
        inv.setItem(4, InspectorToolBuilder.buildTool(InspectorToolType.TOGGLE_CORNERS));
        inv.setItem(5, InspectorToolBuilder.buildTool(InspectorToolType.TOGGLE_KINETICS));
        inv.setItem(6, InspectorToolBuilder.buildTool(InspectorToolType.VELOCITY_DIRECTION));
        inv.setItem(26, InspectorToolBuilder.buildTool(InspectorToolType.VELOCITY_SCALE));
        inv.setItem(7, InspectorToolBuilder.buildTool(InspectorToolType.RESET));
        inv.setItem(8, InspectorToolBuilder.buildTool(InspectorToolType.EXIT));
    }

    // todo realtime collision result setup

    @Command("inspect-collision collide")
    public void collide(@NotNull Player player) {
        InspectSession session = inspectorManager.getOrCreate(player);
        PlinkoObject a = session.getObject(0);
        PlinkoObject b = session.getObject(1);

        if (a == null || b == null) {
            player.sendMessage("Your collision inspection environment is not setup");
            return;
        }

        CollisionResult result = a.currentShape().collide(b.currentShape());

        if (result == null) {
            player.sendMessage("No collision detected");
            return;
        }

        session.setLastResult(result);

        player.sendMessage("Collision detected: " + result);
    }

    @Command("inspect-collision move <slot> <axis> <amount>")
    public void move(
        @NotNull Player player,
        @Argument int slot,
        @Argument Axis axis,
        @Argument double amount
    ) {
        inspectorManager.moveObject(player, slot, axis, amount);
    }

    @Command("inspect-collision rotate <slot> <axis> <degrees>")
    public void rotate(
            @NotNull Player player,
            @Argument int slot,
            @Argument Axis axis,
            @Argument double degrees
    ) {
        inspectorManager.rotateObject(player, slot, axis, degrees);
    }

    @Command("inspect-collision scale <slot> <x> <y> <z>")
    public void scale(
            @NotNull Player player,
            int slot,
            float x,
            float y,
            float z
    ) {
        inspectorManager.scaleObject(player, slot, x, y, z);
    }

    @Command("inspect-collision toggle-corners")
    public void toggleCorners(@NotNull Player player) {
        InspectSession session = inspectorManager.getOrCreate(player);
        session.getVisualHandler().toggle(
                player, InspectVisualHandler.InspectVisualType.SHAPE_CORNERS, session);
    }

    @Command("inspect-collision toggle-kinematics")
    public void toggleKinematics(@NotNull Player player) {
        @NotNull InspectSession session = inspectorManager.getOrCreate(player);
        session.getVisualHandler().toggle(
                player, InspectVisualHandler.InspectVisualType.KINEMATICS, session);
    }

    @Command("inspect-collision set-velocity-dir")
    public void setVelocityDir(@NotNull Player player) {
        @NotNull InspectSession session = inspectorManager.getOrCreate(player);

        Vector look = player.getEyeLocation().getDirection();
        var current = session.linearState(0);

        current.setDirection(look.toVector3f());
        if (current.getMagnitude() <= 0F) current.setMagnitude(0.25F);

        session.applyLinearToObject(0);
        session.updateVisuals(player);
    }

    @Command("inspect-collision scale-velocity <amount>")
    public void scaleVelocity(@NotNull Player player, @Argument("amount") float amount) {
        @NotNull InspectSession session = inspectorManager.getOrCreate(player);

        var current = session.linearState(0);
        current.scaleMagnitude(amount);

        session.applyLinearToObject(0);
        session.updateVisuals(player);
    }

}
