package com.glance.plinko.platform.paper;

import com.glance.plinko.platform.paper.inject.PaperComponentScanner;
import com.glance.plinko.platform.paper.inject.PlinkoModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PlinkoBlocks extends JavaPlugin {

    @Getter
    private Injector injector;

    @Override
    public void onLoad() {
        this.injector = Guice.createInjector(new PlinkoModule(this));
        getLogger().setLevel(Level.FINE);
    }

    @Override
    public void onEnable() {
        PaperComponentScanner.scanAndInitialize(this, this.injector);
    }

    @Override
    public void onDisable() {
        PaperComponentScanner.scanAndCleanup(this, this.injector);
    }

}
