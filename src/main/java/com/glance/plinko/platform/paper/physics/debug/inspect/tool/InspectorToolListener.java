package com.glance.plinko.platform.paper.physics.debug.inspect.tool;

import com.glance.plinko.platform.paper.physics.debug.inspect.InspectorManager;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Axis;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@AutoService(Listener.class)
public class InspectorToolListener implements Listener {

    private final InspectorManager manager;

    @Inject
    public InspectorToolListener(
        @NotNull InspectorManager manager
    ) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        Optional<InspectorToolType> toolOpt = InspectorToolType.fromItem(item);
        if (toolOpt.isEmpty()) return;

        event.setCancelled(true);

        InspectorToolType tool = toolOpt.get();

        if (player.isSneaking() && (tool == InspectorToolType.MOVE || tool == InspectorToolType.ROTATE)) {
            InspectorToolType.cycleAxis(item);
            player.sendActionBar(MiniMessage.miniMessage()
                    .deserialize("<yellow>Switched to axis " + InspectorToolType.getAxis(item)));
            return;
        }

        Axis axis = InspectorToolType.getAxis(item);
        switch (tool) {
            case MOVE -> {
                float delta = event.getAction().isLeftClick() ? 0.1F : -0.1F;
                manager.moveObject(player, 0, axis, delta);
            }
            case ROTATE -> {
                float angle = event.getAction().isLeftClick() ? 10F : -10F;
                manager.rotateObject(player, 0, axis, angle);
            }
            case SCALE -> {
                float scale = event.getAction().isLeftClick() ? 1.1F : 0.9F;
                manager.scaleObject(player, 0, scale, scale, scale);
            }
            case COLLIDE -> player.performCommand("inspect-collision collide");
            case TOGGLE_CORNERS -> player.performCommand("inspect-collision graphics toggle-corners");
            case TOGGLE_KINETICS -> player.performCommand("inspect-collision graphics toggle-kinematics");
            case TOGGLE_COLLISION -> player.performCommand("inspect-collision graphics toggle-collision");
            case VELOCITY_DIRECTION -> player.performCommand("inspect-collision set-velocity-dir");
            case VELOCITY_SCALE -> {
                float scale = event.getAction().isLeftClick() ? 1.1F : 0.9F;
                player.performCommand("inspect-collision scale-velocity " + scale);
            }
            case ANGULAR_SCALE -> {
                float scale = event.getAction().isLeftClick() ? 1.1F : 0.9F;
                //  todo
            }
            case RESET -> {
                manager.clear(player);
                player.performCommand("inspect-collision setup");
            }
            case EXIT -> {
                manager.clear(player);
                player.getInventory().clear();
                player.sendMessage("Existed Inspector Mode");
            }
        }
    }

}
