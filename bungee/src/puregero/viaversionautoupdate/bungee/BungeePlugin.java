package puregero.viaversionautoupdate.bungee;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import puregero.viaversionautoupdate.IPlugin;
import puregero.viaversionautoupdate.ViaVersionAutoUpdate;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class BungeePlugin extends Plugin implements IPlugin {

    @Override
    public void onEnable() {
        new ViaVersionAutoUpdate(this);
    }

    @Override
    public void broadcastMessage(String message) {
        getProxy().broadcast(new TextComponent(message));
    }

    @Override
    public void runTaskLaterAsync(Runnable runnable, long seconds) {
        getProxy().getScheduler().schedule(this, runnable, seconds, TimeUnit.SECONDS);
    }

    @Override
    public void restart() {
        getProxy().stop();
    }

    @Override
    public File getViaVersionJar() {
        Plugin viaVersion = getProxy().getPluginManager().getPlugin("ViaVersion");
        if (viaVersion == null) {
            return null;
        } else {
            return viaVersion.getFile();
        }
    }

    @Override
    public File getPluginsDirectory() {
        return getProxy().getPluginsFolder();
    }
}
