package com.glance.plinko.platform.paper.physics.debug.inspect;

import com.glance.plinko.platform.paper.display.Transformer;
import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.utils.lifecycle.Manager;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Singleton
public class InspectorManager implements Manager {

    private final Map<UUID, InspectSession> sessions = new HashMap<>();

    public InspectSession getOrCreate(@NotNull Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), uuid -> new InspectSession());
    }

    public void clear(@NotNull Player player) {
        InspectSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.clearVisuals();
        }
    }

    @Override
    public void onDisable() {
        log.warn("Disabling Inspector Manager");
        this.sessions.forEach((id, s) -> {
            log.warn("Disabling session for {}", id);
            @Nullable Player p = Bukkit.getPlayer(id);
            if (p != null) {
                log.warn("Clearing player inv? {}", p.getName());
                // todo restore
                p.getInventory().clear();
            }
            s.clearVisuals();
        });
        this.sessions.clear();
    }

    public void moveObject(
        @NotNull Player player,
        int slot,
        @NotNull Axis axis,
        double amount
    ) {
        InspectSession session = getOrCreate(player);
        PlinkoObject obj = session.getObject(slot);
        Display display = session.getDisplay(slot);

        if (obj == null || display == null) {
            player.sendMessage("Your collision inspection environment is not setup");
            return;
        }

        Vector3f pos = obj.getPosition();
        switch (axis) {
            case X -> pos.x += (float) amount;
            case Y -> pos.y += (float) amount;
            case Z -> pos.z += (float) amount;
            default -> {
                player.sendMessage("Invalid axis for translation");
                return;
            }
        }

        Transformer.renderAt(display, Vector.fromJOML(pos));
        session.getVisualHandler().update(player, session);
    }

    public void rotateObject(
        @NotNull Player player,
        int slot,
        @NotNull Axis axis,
        double degrees
    ) {
        InspectSession session = getOrCreate(player);
        PlinkoObject obj = session.getObject(slot);
        Display display = session.getDisplay(slot);

        if (obj == null || display == null) {
            player.sendMessage("Your collision inspection environment is not setup");
            return;
        }

        Quaternionf rotation = new Quaternionf();
        float radians = (float) Math.toRadians(degrees);
        switch (axis) {
            case X -> rotation.rotateX(radians);
            case Y -> rotation.rotateY(radians);
            case Z -> rotation.rotateZ(radians);
            default -> {
                player.sendMessage("Invalid axis for translation");
                return;
            }
        }

        obj.getRotation().mul(rotation);
        Transformer.modifyTransform(display, true, t -> t.getLeftRotation().set(obj.getRotation()));
        session.getVisualHandler().update(player, session);
    }

    public void scaleObject(
        @NotNull Player player,
        int slot,
        float x,
        float y,
        float z
    ) {
        InspectSession session = getOrCreate(player);
        PlinkoObject obj = session.getObject(slot);
        Display display = session.getDisplay(slot);

        if (obj == null || display == null) {
            player.sendMessage(Component.text("No object found in slot " + slot));
            return;
        }

        obj.setScale(new Vector3f(x, y, z).mul(obj.getScale()));
        Transformer.modifyTransform(display, true, t -> t.getScale().mul(x, y, z));
        session.getVisualHandler().update(player, session);
    }

}
