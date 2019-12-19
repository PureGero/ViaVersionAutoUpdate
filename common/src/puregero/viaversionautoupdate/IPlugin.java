package puregero.viaversionautoupdate;

import java.io.File;

public interface IPlugin {

    /**
     * Broadcast a message to all players connected to the server
     *
     * @param message The message to broadcast
     */
    public void broadcastMessage(String message);

    /**
     * Run an asynchronous task at a later point in time
     *
     * @param runnable The task to run
     * @param seconds The number of seconds into the future to run the task
     */
    public void runTaskLaterAsync(Runnable runnable, long seconds);

    /**
     * Restarts the server
     */
    public void restart();

    /**
     * Get the Via Version jar file in the plugins directory
     * @return The File of the ViaVersion.jar or null if it doesn't exist
     */
    public File getViaVersionJar();

    /**
     * Get the plugins directory for installing plugins in
     *
     * @return The File of the plugins directory
     */
    public File getPluginsDirectory();
}
