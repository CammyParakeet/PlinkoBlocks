package com.glance.plinko.platform.paper.command.debug;

import com.glance.plinko.platform.paper.command.engine.CommandHandler;
import com.glance.plinko.platform.paper.config.PlinkoObjectConfig;
import com.glance.plinko.platform.paper.display.PlinkoDisplayFactory;
import com.glance.plinko.platform.paper.animation.PlinkoRenderer;
import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;
import com.glance.plinko.platform.paper.game.simulation.PlinkoSimulator;
import com.glance.plinko.platform.paper.game.simulation.factory.PlinkoObjectFactory;
import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
@AutoService(CommandHandler.class)
public class CollisionDebugCommands implements CommandHandler {

    private final PlinkoRenderer renderer;
    private final PlinkoSimulator simulator;
    private final PlinkoObjectFactory objectFactory;
    private final PlinkoDisplayFactory displayFactory;

    @Inject
    public CollisionDebugCommands(
            @NotNull final PlinkoRenderer renderer,
            @NotNull final PlinkoSimulator simulator,
            @NotNull final PlinkoObjectFactory objectFactory,
            @NotNull final PlinkoDisplayFactory displayFactory
    ) {
        this.renderer = renderer;
        this.simulator = simulator;
        this.objectFactory = objectFactory;
        this.displayFactory = displayFactory;
    }

    @Command("debug-collision <offset-x> <offset-z> <height> <material>")
    public void runCollisionDebug(
        final @NotNull Player player,
        final @Argument("offset-x") double offsetX,
        final @Argument("offset-x") double offsetY,
        final @Argument("offset-x") double offsetZ,
        final @Argument("height") double height,
        final @Argument("material") Material material
    ) {
        Location origin = player.getLocation().add(offsetX, height, offsetZ);

        Location pegLoc = origin.clone().subtract(0, height, 0);
        // TODO: peg should itself be a plinko object - or one that extends it, as an infinite mass display

        PlinkoObjectConfig config = PlinkoObjectConfig.defaults(material);

        PlinkoRunContext ctx = new PlinkoRunContext(UUID.randomUUID(), config, origin);

        // TODO: create object and display through factory

        // TODO: run simulation on object

        // TODO: render

        player.sendMessage("Running collision debug!");
    }


}
