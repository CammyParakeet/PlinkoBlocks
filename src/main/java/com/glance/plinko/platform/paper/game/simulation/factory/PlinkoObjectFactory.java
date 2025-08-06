package com.glance.plinko.platform.paper.game.simulation.factory;

import com.glance.plinko.platform.paper.game.simulation.PlinkoObject;
import com.glance.plinko.platform.paper.game.simulation.PlinkoRunContext;

// todo figure out with guice?
public interface PlinkoObjectFactory {
    PlinkoObject create(PlinkoRunContext context);
}
