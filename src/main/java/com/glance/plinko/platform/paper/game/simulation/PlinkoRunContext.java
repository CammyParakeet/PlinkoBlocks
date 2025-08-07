package com.glance.plinko.platform.paper.game.simulation;

import com.glance.plinko.platform.paper.config.PlinkoObjectConfig;
import org.bukkit.Location;

import java.util.UUID;

public record PlinkoRunContext(
   UUID runId,
   PlinkoObjectConfig config,
   Location origin
) {}
