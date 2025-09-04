package com.glance.plinko.platform.paper.physics.debug.inspect.tool;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class InspectorToolBuilder {

    public ItemStack buildTool(@NotNull InspectorToolType type) {
        Material mat;
        String name;

        switch (type) {
            case MOVE -> {
                mat = Material.STICK;
                name = "<gold>Translate";
            }
            case ROTATE -> {
                mat = Material.COMPASS;
                name = "<red>Rotate";
            }
            case SCALE -> {
                mat = Material.SLIME_BALL;
                name = "<green>Scale";
            }
            case COLLIDE -> {
                mat = Material.TNT;
                name = "<yellow>Collision";
            }
            case TOGGLE_CORNERS -> {
                mat = Material.ENDER_EYE;
                name = "<aqua>Toggle Corners";
            }
            case TOGGLE_KINETICS -> {
                mat = Material.DETECTOR_RAIL;
                name = "<gray>Toggle Kinetics";
            }
            case VELOCITY_DIRECTION -> {
                mat = Material.COMPASS;
                name = "<red> Aim Velocity";
            }
            case VELOCITY_SCALE -> {
                mat = Material.CHAIN;
                name = "<gray> Scale Velocity";
            }
            case ANGULAR_SCALE -> {
                mat = Material.STONECUTTER;
                name = "<gray> Angular Velocity Scale";
            }
            case RESET -> {
                mat = Material.WHITE_CONCRETE;
                name = "Reset?";
            }
            case EXIT -> {
                mat = Material.BARRIER;
                name = "<dark_red>Exit";
            }
            default -> {
                mat = Material.STICK;
                name = "Tool";
            }
        }

        ItemStack item = new ItemStack(mat);
        item.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            type.applyTo(meta);
        });

        return item;
    }

}
