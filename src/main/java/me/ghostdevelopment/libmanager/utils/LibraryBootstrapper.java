package me.ghostdevelopment.libmanager.utils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Downloads libraries from Maven repositories.
 */
public class LibraryBootstrapper {

    private final String repositoryUrl;
    private final Logger logger;

    public LibraryBootstrapper(String repositoryUrl, Logger logger) {
        this.repositoryUrl = repositoryUrl.endsWith("/") ? repositoryUrl : repositoryUrl + "/";
        this.logger = logger;
    }

    public LibraryBootstrapper(Logger logger) {
        this("https://repo1.maven.org/maven2/", logger);
    }

    /**
     * Downloads a collection of maven coordinates to the specified folder.
     * @param coords Maven coordinates (group:artifact:version)
     * @param libDir Destination directory
     * @return true if any library was downloaded
     */
    public boolean downloadLibraries(Collection<String> coords, File libDir) {
        boolean downloadedAny = false;

        if (!libDir.exists() && !libDir.mkdirs()) {
            logger.severe("Could not create library directory: " + libDir.getAbsolutePath());
            return false;
        }

        for (String coord : coords) {
            try {
                String[] parts = coord.split(":");
                if (parts.length != 3) {
                    logger.warning("Invalid maven coord: " + coord);
                    continue;
                }
                String group = parts[0].replace('.', '/');
                String artifact = parts[1];
                String version = parts[2];
                String jarName = artifact + "-" + version + ".jar";
                File out = new File(libDir, jarName);
                
                if (out.exists()) {
                    continue;
                }

                String url = repositoryUrl + group + "/" + artifact + "/" + version + "/" + jarName;
                logger.info("Downloading library: " + coord);
                
                downloadFile(url, out);
                downloadedAny = true;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to download lib " + coord + ": " + e.getMessage());
            }
        }

        return downloadedAny;
    }

    private void downloadFile(String urlStr, File destination) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(20_000);
        conn.setRequestProperty("User-Agent", "LibManager/1.0");
        conn.connect();
        
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IllegalStateException("HTTP " + conn.getResponseCode() + " for " + urlStr);
        }
        
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
