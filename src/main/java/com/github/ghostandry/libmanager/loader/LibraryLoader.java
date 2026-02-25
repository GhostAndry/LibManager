package com.github.ghostandry.libmanager.loader;

import com.github.ghostandry.libmanager.api.LibraryAdapter;
import com.github.ghostandry.libmanager.utils.LibraryBootstrapper;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Universal runtime loader for libraries and core modules.
 */
public class LibraryLoader {

    private final File pluginFolder;
    private final Logger logger;
    private final LibraryBootstrapper bootstrapper;

    public LibraryLoader(File pluginFolder, Logger logger) {
        this.pluginFolder = pluginFolder;
        this.logger = logger;
        this.bootstrapper = new LibraryBootstrapper(logger);
    }

    /**
     * Bootstrap the core module.
     * 
     * @param adapter Platform adapter
     * @param libs List of maven coordinates to download/load
     * @param bootstrapClassName Main class to instantiate from child loader
     * @param parentFirstPrefixes Packages to load from parent loader
     * @param childFirstPrefixes Packages to load from child loader
     * @return The instantiated bootstrap object
     */
    public Object bootstrap(LibraryAdapter adapter, 
                             Collection<String> libs, 
                             String bootstrapClassName,
                             Collection<String> parentFirstPrefixes,
                             Collection<String> childFirstPrefixes) throws Exception {
        
        // 1) Ensure libs are downloaded
        File libDir = new File(pluginFolder, "lib");
        bootstrapper.downloadLibraries(libs, libDir);

        // 2) Collect URLs
        List<URL> urls = new ArrayList<>();
        
        // Include plugin jar if available
        File pluginFile = adapter.getPluginFile();
        if (pluginFile != null && pluginFile.exists()) {
            urls.add(pluginFile.toURI().toURL());
            logger.info("Including plugin jar: " + pluginFile.getName());
        }

        // Include all downloaded libs
        if (libDir.exists()) {
            File[] files = libDir.listFiles((d, n) -> n.endsWith(".jar"));
            if (files != null) {
                for (File f : files) {
                    urls.add(f.toURI().toURL());
                }
            }
        }

        // 3) Create isolated classloader
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) parent = getClass().getClassLoader();
        
        IsolatedClassLoader loader = new IsolatedClassLoader(
            urls.toArray(new URL[0]), 
            parent, 
            parentFirstPrefixes, 
            childFirstPrefixes
        );

        // 4) Instantiate core
        Thread current = Thread.currentThread();
        ClassLoader previous = current.getContextClassLoader();
        try {
            current.setContextClassLoader(loader);
            Class<?> clazz = Class.forName(bootstrapClassName, true, loader);
            
            // Search for suitable constructor
            return instantiate(clazz, adapter, loader);
        } finally {
            current.setContextClassLoader(previous);
        }
    }

    private Object instantiate(Class<?> clazz, LibraryAdapter adapter, ClassLoader loader) throws Exception {
        // Implementation of flexible instantiation logic similar to MinimalLoader
        // ... (can be simplified or kept as is)
        try {
            // Try with (LibraryAdapter, Object)
            Class<?> adapterClass = Class.forName("com.github.ghostandry.libmanager.api.LibraryAdapter", true, loader);
            Constructor<?> ctor = clazz.getConstructor(adapterClass, Object.class);
            return ctor.newInstance(adapter, getPlatformPlugin());
        } catch (NoSuchMethodException e) {
            try {
                // Try with (LibraryAdapter)
                Class<?> adapterClass = Class.forName("com.github.ghostandry.libmanager.api.LibraryAdapter", true, loader);
                Constructor<?> ctor = clazz.getConstructor(adapterClass);
                return ctor.newInstance(adapter);
            } catch (NoSuchMethodException e2) {
                // Try default
                return clazz.getDeclaredConstructor().newInstance();
            }
        }
    }

    /**
     * Override to provide the platform plugin instance (e.g. JavaPlugin).
     */
    protected Object getPlatformPlugin() {
        return null;
    }
}
