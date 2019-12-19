package puregero.viaversionautoupdate;

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

public class UpdateChecker implements Runnable {

    /** The frequency in seconds to check for updates */
    public static final long CHECK_UPDATE_FREQUENCY = 15*60; // 15 minutes

    /** The url to the jenkins last build information */
    public static final String LAST_BUILD_URL = "https://ci.viaversion.com/job/ViaVersion/lastBuild/api/json";

    /** The url to download the viaversion jar from */
    public static final String DOWNLOAD_URL = "https://ci.viaversion.com/job/ViaVersion/lastBuild/artifact/jar/target/%s";

    private ViaVersionAutoUpdate viaVersionAutoUpdate;

    public UpdateChecker(ViaVersionAutoUpdate viaVersionAutoUpdate) {
        this.viaVersionAutoUpdate = viaVersionAutoUpdate;
    }

    @Override
    public void run() {
        try {
            // Download the json
            String lastBuild = downloadLastBuildInfo();

            // Parse the json
            JsonObject json = new JsonParser().parse(lastBuild).getAsJsonObject();

            // Get the file name (json.artifacts[0].filename)
            String fileName = json.getAsJsonArray("artifacts")
                    .get(0).getAsJsonObject()
                    .get("fileName").getAsString();

            // Check if it's a new update
            File oldJar = viaVersionAutoUpdate.getPlugin().getViaVersionJar();
            if (oldJar == null || !oldJar.getName().equalsIgnoreCase(fileName)) {

                // New update!
                installUpdate(oldJar, fileName);

                // Restart the server to apply the update
                viaVersionAutoUpdate.startRestartCountdown();
            }


        } catch (Exception e) {
            System.err.println("[ViaVersionAutoUpdate] An error occured while checking for updates");
            e.printStackTrace();
        }

        viaVersionAutoUpdate.getPlugin().runTaskLaterAsync(this, CHECK_UPDATE_FREQUENCY);
    }

    /**
     * Install an update
     * @param oldJar The old jar to be deleted
     * @param fileName The new file to be downloaded
     */
    private void installUpdate(File oldJar, String fileName) throws IOException {
        File newJar = new File(viaVersionAutoUpdate.getPlugin().getPluginsDirectory(), fileName);

        URL url = new URL(String.format(DOWNLOAD_URL, fileName));
        URLConnection connection = url.openConnection();
        // Spoof a user-agent, jenkins doesn't like the java default
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.79 Safari/537.36");
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, newJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Try to delete old jar now, if not, delete it on exit
            if (!oldJar.delete()) {
                oldJar.deleteOnExit();
            }
        }
    }

    /**
     * Download the last build info from LAST_BUILD_URL
     *
     * @return A json string of the last build info
     */
    private String downloadLastBuildInfo() throws IOException {
        URL url = new URL(LAST_BUILD_URL);
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
