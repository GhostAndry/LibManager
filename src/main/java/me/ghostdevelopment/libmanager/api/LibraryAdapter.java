package me.ghostdevelopment.libmanager.api;

import java.io.File;
import java.io.InputStream;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Adapter interface to provide platform-specific information to the loader.
 */
public interface LibraryAdapter {
    File getDataFolder();
    Logger getLogger();
    String getPlatform();
    File getPluginFile();
    Function<String, InputStream> getResourceProvider();
}
