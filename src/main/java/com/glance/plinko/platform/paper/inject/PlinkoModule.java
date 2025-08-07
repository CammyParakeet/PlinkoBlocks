package com.glance.plinko.platform.paper.inject;

import com.glance.plinko.platform.paper.display.PlinkoDisplayFactory;
import com.glance.plinko.platform.paper.game.simulation.factory.PlinkoObjectFactory;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlinkoModule extends AbstractModule {

    private final Plugin plugin;

    public PlinkoModule(@NotNull final Plugin plugin) { this.plugin = plugin; }

    @Override
    protected void configure() {
        // bind plugin instance
        this.bind(Plugin.class).toInstance(plugin);
        this.bind(JavaPlugin.class).toInstance((JavaPlugin) plugin);

        this.install(new FactoryModuleBuilder().build(PlinkoObjectFactory.class));
        this.install(new FactoryModuleBuilder().build(PlinkoDisplayFactory.class));
    }
}
