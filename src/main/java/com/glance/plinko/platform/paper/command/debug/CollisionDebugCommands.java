package com.glance.plinko.platform.paper.command.debug;

import com.glance.plinko.platform.paper.animation.ObjectAnimator;
import com.glance.plinko.platform.paper.animation.PlinkoAnimation;
import com.glance.plinko.platform.paper.command.engine.CommandHandler;
import com.glance.plinko.platform.paper.config.PlinkoObjectConfig;
import com.glance.plinko.platform.paper.display.DisplayOptions;
import com.glance.plinko.platform.paper.display.PlinkoDisplayFactory;
import com.glance.plinko.platform.paper.animation.PlinkoRenderer;
import com.glance.plinko.platform.paper.display.Transformer;
import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;
import com.glance.plinko.platform.paper.game.simulation.PlinkoSimulator;
import com.glance.plinko.platform.paper.game.simulation.factory.PlinkoObjectFactory;
import com.glance.plinko.platform.paper.game.simulation.obstacle.PegFactory;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@Slf4j
@Singleton
@AutoService(CommandHandler.class)
public class CollisionDebugCommands implements CommandHandler {

    private final Plugin plugin;
    private final PlinkoRenderer renderer;
    private final PlinkoSimulator simulator;
    private final PlinkoObjectFactory objectFactory;
    private final PlinkoDisplayFactory displayFactory;

    @Inject
    public CollisionDebugCommands(
            @NotNull final Plugin plugin,
            @NotNull final PlinkoRenderer renderer,
            @NotNull final PlinkoSimulator simulator,
            @NotNull final PlinkoObjectFactory objectFactory,
            @NotNull final PlinkoDisplayFactory displayFactory
    ) {
        this.plugin = plugin;
        this.renderer = renderer;
        this.simulator = simulator;
        this.objectFactory = objectFactory;
        this.displayFactory = displayFactory;
    }

    @Command("debug-collision <offset-x> <offset-z> <height> <material>")
    public void runCollisionDebug(
        final @NotNull Player player,
        final @Argument("offset-x") double offsetX,
        final @Argument("offset-z") double offsetZ,
        final @Argument("height") double height,
        final @Argument("material") Material material
    ) {
        Location origin = player.getLocation().add(offsetX, height, offsetZ);
        origin.setYaw(0F);
        origin.setPitch(0F);

        Location pegLoc = origin.clone().subtract(0, height, 0);
        // TODO: peg should itself be a plinko object - or one that extends it, as an infinite mass display

        PlinkoObjectConfig config = PlinkoObjectConfig.defaults(material);

        PlinkoRunContext ctx = new PlinkoRunContext(UUID.randomUUID(), config, origin, false);

        PlinkoObject object = objectFactory.create(ctx);
        PlinkoObject peg = PegFactory.createPeg(pegLoc, 0.75F);

        PlinkoAnimation animation = simulator.simulate(ctx, object, List.of(peg.currentShape()));
        Display display = displayFactory.createDisplay(ctx);
        Display pegDisplay = displayFactory.createPegDisplay(
                pegLoc, DisplayOptions.defaultItem(Material.GOLD_BLOCK));
        log.warn("Created a display: {}", display);

        ObjectAnimator animator = new ObjectAnimator(animation, object, display);
        log.warn("Have animator: {}", animator);

        renderer.add(animator);
        log.warn("Rendering: {}", renderer.activeAnimations());

        player.sendMessage("Running collision debug!");
    }

//    private void createTest(Location loc) {
//        Location bLoc = loc.clone().add(2.0, 0.0, 0.0);
//        Display d = bLoc.getWorld().spawn(bLoc, BlockDisplay.class, b -> {
//            b.setPersistent(false);
//            b.setBlock(Material.GOLD_BLOCK.createBlockData());
//            b.setInterpolationDelay(0);
//            b.setInterpolationDuration(1);
//        });
//
//        int maxT = 200;
//
//        new BukkitRunnable() {
//            int t = 0;
//
//            @Override
//            public void run() {
//                if (t++ >= maxT) {
//                    this.cancel();
//                    return;
//                }
//
//                Transformer.modifyTransform(d, trans -> {
//                    trans.getTranslation().sub(0.0F, t * 0.03F, 0.0F);
//                });
//            }
//        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
//    }


}
