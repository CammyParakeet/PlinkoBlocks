package com.glance.plinko.platform.paper.inject;

import com.google.inject.AbstractModule;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PlinkoModule extends AbstractModule {

    private final Plugin plugin;

    public PlinkoModule(@NotNull final Plugin plugin) { this.plugin = plugin; }

    @Override
    protected void configure() {
        // bind plugin instance
        bind(Plugin.class).toInstance(plugin);
        bind(JavaPlugin.class).toInstance((JavaPlugin) plugin);
    }
}
