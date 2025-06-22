package com.github.puregero.viaversionautoupdate;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ViaVersionAutoUpdatePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        double javaVersion = Double.parseDouble(System.getProperty("java.specification.version"));
        this.getLogger().info("[ViaVersionAutoUpdate] Java version: " + javaVersion);

        if (javaVersion < 17) {
            this.getLogger().info("[ViaVersionAutoUpdate] Using ViaVersion-Java8 repository");
            new UpdateCheckerJava8(this);
        } else {
            new UpdateChecker(this);
        }
    }

    public File getPluginsDir() {
        return this.getDataFolder().getParentFile();
    }

}
