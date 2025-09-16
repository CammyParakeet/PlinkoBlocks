package com.glance.plinko.platform.paper.physics.debug.inspect.tool;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Axis;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public enum InspectorToolType {
    MOVE,
    ROTATE,
    SCALE,
    COLLIDE,
    TOGGLE_SHAPE,
    TOGGLE_CORNERS,
    TOGGLE_KINETICS,
    TOGGLE_COLLISION,
    VELOCITY_DIRECTION,
    VELOCITY_SCALE,
    ANGULAR_DIRECTION,
    ANGULAR_SCALE,
    ANIMATE_CURRENT_SPIN,
    ANIMATE_COLLISION_SPIN,
    RESET,
    EXIT;

    private static final NamespacedKey KEY = new NamespacedKey("plinko", "inspect_tool");
    private static final NamespacedKey AXIS_KEY = new NamespacedKey("plinko", "inspect_axis");

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Axis getAxis(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Axis.X;

        String stored = meta.getPersistentDataContainer().get(AXIS_KEY, PersistentDataType.STRING);
        if (stored == null) return Axis.X;

        try {
            return Axis.valueOf(stored.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Axis.X;
        }
    }

    public static void cycleAxis(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Axis current = getAxis(item);
        Axis next = switch (current) {
            case X -> Axis.Y;
            case Y -> Axis.Z;
            case Z -> Axis.X;
        };

        meta.getPersistentDataContainer().set(AXIS_KEY, PersistentDataType.STRING, next.name());
        item.setItemMeta(meta);
        updateDisplayName(item);
    }

    public static Optional<InspectorToolType> fromItem(@NotNull ItemStack item) {
        if (item.isEmpty()) return Optional.empty();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String value = pdc.get(KEY, PersistentDataType.STRING);
        if (value == null) return Optional.empty();

        try {
            return Optional.of(InspectorToolType.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static void updateDisplayName(@NotNull ItemStack item) {
        Optional<InspectorToolType> typeOpt = fromItem(item);
        if (typeOpt.isEmpty()) return;

        InspectorToolType type = typeOpt.get();
        Axis axis = getAxis(item);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String label = switch (type) {
            case MOVE -> "<gold>Translate";
            case ROTATE -> "<red>Rotate";
            default -> "Non Axis'd Tool";
        };

        if (type == MOVE || type == ROTATE) {
            label += "(<white>" + axis.name() + "</white>)";
        }

        meta.displayName(MiniMessage.miniMessage().deserialize(label));
        item.setItemMeta(meta);
    }

    public void applyTo(@NotNull ItemMeta meta) {
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, id());
    }

}
