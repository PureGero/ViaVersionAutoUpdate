package com.github.puregero.viaversionautoupdater;

import com.github.puregero.multilib.MultiLib;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class UpdateChecker implements Runnable {

    /** The frequency in seconds to check for updates */
    public static final long CHECK_UPDATE_FREQUENCY = 10*60; // 10 minutes

    /** The url to the jenkins last build information */
    private static final String LAST_BUILD_URL = "https://ci.viaversion.com/job/ViaVersion/lastBuild/api/json?random=%f";

    /** The url to download the viaversion jar from */
    private static final String DOWNLOAD_URL = "https://ci.viaversion.com/job/ViaVersion/lastBuild/artifact/%s";

    private final ViaVersionAutoUpdaterPlugin plugin;

    public UpdateChecker(ViaVersionAutoUpdaterPlugin plugin) {
        this.plugin = plugin;

        MultiLib.getAsyncScheduler().runAtFixedRate(this.plugin, task -> this.run(), 200, CHECK_UPDATE_FREQUENCY * 20);
    }

    public String getLastBuildUrl() {
        return LAST_BUILD_URL;
    }

    public String getDownloadUrl() {
        return DOWNLOAD_URL;
    }

    @Override
    public void run() {
        try {
            // Download the json
            String lastBuild = this.downloadLastBuildInfo();

            // Parse the json
            JsonObject json = new JsonParser().parse(lastBuild).getAsJsonObject();

            // Get the file name (json.artifacts[0].filename)
            String fileName = json.getAsJsonArray("artifacts")
                    .get(0).getAsJsonObject()
                    .get("fileName").getAsString();

            String relativePath = json.getAsJsonArray("artifacts")
                    .get(0).getAsJsonObject()
                    .get("relativePath").getAsString();

            int build = json.get("number").getAsInt();

            // Check if it's a new update
            int newestJarBuild = this.getNewestViaVersionJarBuild();
            if (newestJarBuild < build) {

                // New update!
                this.plugin.getLogger().info("[ViaVersionAutoUpdater] Current ViaVersion build: " + newestJarBuild);
                this.plugin.getLogger().info("[ViaVersionAutoUpdater] New ViaVersion build available: " + build + ", installing...");

                String fileNameWithBuildNumber = fileName.replace(".jar", "-" + build + ".jar");

                this.installUpdate(this.getViaVersionJars(), fileNameWithBuildNumber, relativePath);

            }


        } catch (Exception e) {
            this.plugin.getLogger().severe("[ViaVersionAutoUpdater] An error occured while checking for updates");
            e.printStackTrace();
        }
    }

    private int getNewestViaVersionJarBuild() {
        int build = 0;

        for (File jar : this.getViaVersionJars()) {
            String[] parts = jar.getName().split("-");
            try {
                int jarBuild = Integer.parseInt(parts[parts.length - 1].split("\\.")[0]);
                if (jarBuild > build) {
                    build = jarBuild;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return build;
    }

    private File[] getViaVersionJars() {
        return plugin.getPluginsDir().listFiles((dir, name) -> {
            String lowerNameNoHyphens = name.toLowerCase(Locale.ROOT).replace("-", "");
            return lowerNameNoHyphens.startsWith("viaversion") &&
                    lowerNameNoHyphens.endsWith(".jar") &&
                    !lowerNameNoHyphens.contains("autoupdate");
        });
    }

    /**
     * Install an update
     * @param oldJars The old jars to be deleted
     * @param fileName The new file to be downloaded
     */
    private void installUpdate(File[] oldJars, String fileName, String relativePath) throws IOException {
        File newJar = new File(this.plugin.getPluginsDir(), fileName);

        URL url = new URL(String.format(this.getDownloadUrl(), relativePath));
        this.plugin.getLogger().info("[ViaVersionAutoUpdater] Downloading " + url + "...");
        URLConnection connection = url.openConnection();
        // Spoof a user-agent, jenkins doesn't like the java default
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36");
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, newJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            this.plugin.getLogger().info("[ViaVersionAutoUpdater] Downloaded " + newJar + " successfully!");

            // Try to delete old jar now, if not, delete it on exit
            for (File oldJar : oldJars) {
                File deletemeJar = new File(oldJar.getParentFile(), oldJar.getName() + ".deleteme");
                this.plugin.getLogger().info("[ViaVersionAutoUpdater] Moving " + oldJar.getName() + " to " + deletemeJar.getName());
                oldJar.renameTo(deletemeJar);
                this.plugin.getLogger().info("[ViaVersionAutoUpdater] Deleting " + deletemeJar.getName() + " on exit");
                deletemeJar.deleteOnExit();
            }
        }
    }

    /**
     * Download the last build info from LAST_BUILD_URL
     *
     * @return A json string of the last build info
     */
    private String downloadLastBuildInfo() throws IOException {
        URL url = new URL(String.format(this.getLastBuildUrl(), Math.random()));
        URLConnection connection = url.openConnection();
        // Spoof a user-agent, jenkins doesn't like the java default
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36");
        try (InputStream in = connection.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int i;
            while ((i = in.read(b)) >= 0) {
                buffer.write(b, 0, i);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
