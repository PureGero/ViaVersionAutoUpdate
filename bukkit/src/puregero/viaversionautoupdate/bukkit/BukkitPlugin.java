package puregero.viaversionautoupdate.bukkit;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import puregero.viaversionautoupdate.IPlugin;
import puregero.viaversionautoupdate.ViaVersionAutoUpdate;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BukkitPlugin extends JavaPlugin implements IPlugin {

    @Override
    public void onEnable() {
        new ViaVersionAutoUpdate(this);
    }

    @Override
    public void broadcastMessage(String message) {
        getServer().broadcastMessage(message);
    }

    @Override
    public void runTaskLaterAsync(Runnable runnable, long seconds) {
        long ticks = seconds*20;
        getServer().getScheduler().runTaskLaterAsynchronously(this, runnable, ticks);
    }

    @Override
    public void restart() {
        try {
            // Use spigot's restart() method if it exists
            if (getServer().getClass().getMethod("spigot") != null) {
                getServer().spigot().restart();
                return;
            }
        } catch (NoSuchMethodException expected) {}

        // Otherwise assume the server knows how to restart itself
        getServer().shutdown();
    }

    @Override
    public File getViaVersionJar() {
        Plugin viaVersion = getServer().getPluginManager().getPlugin("ViaVersion");
        if (viaVersion == null || !(viaVersion instanceof JavaPlugin)) {
            return null;
        } else {
            // getFile() is only in JavaPlugins
            JavaPlugin jViaVersion = (JavaPlugin) viaVersion;
            try {
                // getFile() method is protected in bukkit, force access to it
                Method m = jViaVersion.getClass().getSuperclass().getDeclaredMethod("getFile");
                m.setAccessible(true);
                return (File) m.invoke(jViaVersion);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public File getPluginsDirectory() {
        // Assume the directory that this plugin is in is the plugins directory
        return getFile().getParentFile();
    }
}
