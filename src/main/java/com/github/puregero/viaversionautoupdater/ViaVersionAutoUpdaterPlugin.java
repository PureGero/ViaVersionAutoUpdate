package com.github.puregero.viaversionautoupdater;

import org.bstats.bukkit.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ViaVersionAutoUpdaterPlugin extends JavaPlugin implements Listener {

    private static final int BSTATS_PLUGIN_ID = 26244;

    @Override
    public void onEnable() {
        double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
        this.getLogger().info("[ViaVersionAutoUpdater] Java version: " + javaVersion);

        if (javaVersion < 17) {
            this.getLogger().info("[ViaVersionAutoUpdater] Using ViaVersion-Java8 repository");
            new UpdateCheckerJava8(this);
        } else {
            new UpdateChecker(this);
        }

        try {
            new Metrics(this, BSTATS_PLUGIN_ID);
        } catch (Exception e) {
            this.getLogger().info("");
        }
    }

    public File getPluginsDir() {
        return this.getDataFolder().getParentFile();
    }

}
