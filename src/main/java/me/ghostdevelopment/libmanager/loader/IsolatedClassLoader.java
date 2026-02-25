package me.ghostdevelopment.libmanager.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom classloader with selective child-first behavior.
 */
public final class IsolatedClassLoader extends URLClassLoader {

    private final Set<String> parentFirstPrefixes = new HashSet<>();
    private final Set<String> childFirstPrefixes = new HashSet<>();

    public IsolatedClassLoader(URL[] urls, ClassLoader parent, 
                               Collection<String> parentFirst, 
                               Collection<String> childFirst) {
        super(urls, parent);
        if (parentFirst != null) this.parentFirstPrefixes.addAll(parentFirst);
        if (childFirst != null) this.childFirstPrefixes.addAll(childFirst);
        
        // Always parent-first for java and platform basics
        this.parentFirstPrefixes.add("java.");
        this.parentFirstPrefixes.add("javax.");
        this.parentFirstPrefixes.add("sun.");
        this.parentFirstPrefixes.add("jdk.");
    }

    private boolean startsWithAny(String name, Collection<String> prefixes) {
        for (String p : prefixes) {
            if (name.startsWith(p)) return true;
        }
        return false;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Check parent-first prefixes
        if (startsWithAny(name, parentFirstPrefixes)) {
            return super.loadClass(name, resolve);
        }

        // Try child-first for selected prefixes
        if (startsWithAny(name, childFirstPrefixes)) {
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    try {
                        c = findClass(name);
                    } catch (ClassNotFoundException ignored) {
                        // Fallback to parent
                        c = getParent().loadClass(name);
                    }
                }
                if (resolve) resolveClass(c);
                return c;
            }
        }

        // Default behavior: parent-first
        return super.loadClass(name, resolve);
    }

    @Override
    public URL getResource(String name) {
        if (isChildFirstResource(name)) {
            URL url = findResource(name);
            if (url != null) return url;
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (isChildFirstResource(name)) {
            List<URL> all = new ArrayList<>();
            Enumeration<URL> child = findResources(name);
            while (child.hasMoreElements()) all.add(child.nextElement());
            Enumeration<URL> parent = getParent().getResources(name);
            while (parent.hasMoreElements()) all.add(parent.nextElement());
            return Collections.enumeration(all.stream().distinct().collect(Collectors.toList()));
        }
        return super.getResources(name);
    }

    private boolean isChildFirstResource(String name) {
        String normalized = name.replace('/', '.');
        return startsWithAny(normalized, childFirstPrefixes);
    }
}
