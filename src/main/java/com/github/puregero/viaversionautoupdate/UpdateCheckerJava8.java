package com.github.puregero.viaversionautoupdate;

public class UpdateCheckerJava8 extends UpdateChecker {

    /** The url to the jenkins last build information */
    private static final String LAST_BUILD_URL = "https://ci.viaversion.com/job/ViaVersion-Java8/lastBuild/api/json?random=%f";

    /** The url to download the viaversion jar from */
    private static final String DOWNLOAD_URL = "https://ci.viaversion.com/job/ViaVersion-Java8/lastBuild/artifact/%s";

    public UpdateCheckerJava8(ViaVersionAutoUpdatePlugin plugin) {
        super(plugin);
    }

    @Override
    public String getLastBuildUrl() {
        return LAST_BUILD_URL;
    }

    @Override
    public String getDownloadUrl() {
        return DOWNLOAD_URL;
    }
}
