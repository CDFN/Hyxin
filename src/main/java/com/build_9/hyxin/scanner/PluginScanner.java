package com.build_9.hyxin.scanner;

import com.build_9.hyxin.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A primitive plugin scanner that can be used to find plugins trying to use mixins via hyxin. We avoid using any Hytale
 * code because it has not been loaded yet.
 */
public class PluginScanner {

    private static final Gson GSON = new GsonBuilder().create();
    private final Map<File, PluginManifest> manifests = new HashMap<>();

    /**
     * Scans a directory for JAR files that could be valid plugins with Mixin configs.
     *
     * @param dir The directory to scan.
     */
    public void scanDirectory(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Can not load plugins from '" + dir.getAbsolutePath() + "'. It is not a directory!");
        }
        Constants.log("Scanning for plugins in '" + dir.getAbsolutePath() + "'.");
        for (File candidate : Objects.requireNonNull(dir.listFiles())) {
            if (candidate.isFile() && candidate.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                try {
                    final PluginManifest manifest = readManifest(candidate);
                    if (manifest != null) {
                        manifests.put(candidate, manifest);
                    }
                }
                catch (IOException e) {
                    System.err.println("[Hyxin] Encountered an error when scanning '" + candidate.getPath() + "'.");
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Reads the information we care about from the Hytale plugin manifest file.
     *
     * @param jarFile The file to read information from.
     * @return The manifest data that was read.
     * @throws IOException The file could not be read or did not contain the expected entries.
     */
    public static PluginManifest readManifest(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry("manifest.json");
            if (entry != null) {
                try (Reader reader = new InputStreamReader(jar.getInputStream(entry))) {
                    return GSON.fromJson(reader, PluginManifest.class);
                }
            }
        }
        return null;
    }

    /**
     * Gets an immutable map of plugin files and their manifest data.
     *
     * @return A map of plugin files to their manifest data.
     */
    public Map<File, PluginManifest> entries() {
        return Collections.unmodifiableMap(this.manifests);
    }
}