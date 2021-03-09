package com.predict.plus.algo.model;

import java.io.*;
import java.nio.file.Files;

/**
 * @author yuangong
 */
public class NativeLoader implements Serializable {
    private String resourcesPath;
    private Boolean extractionDone = false;
    private File tempDir;

    public NativeLoader(String topLevelResourcesPath) throws IOException {
        this.resourcesPath = getResourcesPath(topLevelResourcesPath);
        tempDir = Files.createTempDirectory("mml-natives").toFile();
        tempDir.deleteOnExit();
    }

    public static String getOSPrefix() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("linux") || OS.contains("mac") || OS.contains("darwin")) {
            return "";
        } else if (OS.contains("windows")) {
            return "lib";
        } else {
            throw new UnsatisfiedLinkError(
                    String.format("This component doesn't currently have native support for OS: %s", OS)
            );
        }
    }

    private static String getResourcesPath(String topLevelResourcesPath) {
        String sep = "/";
        String OS = System.getProperty("os.name").toLowerCase();
        String resourcePrefix = topLevelResourcesPath + sep + "%s" + sep;
        if (OS.contains("linux")) {
            return String.format(resourcePrefix, "linux/x86_64");
        } else if (OS.contains("windows")) {
            return String.format(resourcePrefix, "windows/x86_64");
        } else if (OS.contains("mac") || OS.contains("darwin")) {
            return String.format(resourcePrefix, "osx/x86_64");
        } else {
            throw new UnsatisfiedLinkError(
                    String.format("This component doesn't currently have native support for OS: %s", OS)
            );
        }
    }

    /**
     * Loads a named native library from the jar file
     *
     * <p>This method will first try to load the library from java.library.path system property.
     * Only if that fails, the named native library and its dependencies will be extracted to
     * a temporary folder and loaded from there.</p>
     */
    public void loadLibraryByName(String libName) {
        try {
            // First try loading by name
            // It's possible that the native library is already on a path java can discover
            System.loadLibrary(libName);
        } catch (UnsatisfiedLinkError e) {
            try {
                // Get the OS specific library name
                libName = System.mapLibraryName(libName);
                extractNativeLibraries(libName);
                // Try to load library from extracted native resources
                System.load(tempDir.getAbsolutePath() + File.separator + libName);
            } catch (Exception ee) {
                throw new UnsatisfiedLinkError(String.format(
                        "Could not load the native libraries because " +
                                "we encountered the following problems: %s and %s",
                        e.getMessage(), ee.getMessage()));
            }
        }
    }

    private void extractNativeLibraries(String libName) throws IOException {
        if (!extractionDone) {
            extractResourceFromPath(libName, resourcesPath);
        }
        extractionDone = true;
    }

    private void extractResourceFromPath(String libName, String prefix) throws IOException {

        File temp = new File(tempDir.getPath() + File.separator + libName);
        temp.createNewFile();
        temp.deleteOnExit();

        if (!temp.exists()) {
            throw new FileNotFoundException(String.format(
                    "Temporary file %s could not be created. Make sure you can write to this location.",
                    temp.getAbsolutePath())
            );
        }

        String path = prefix + libName;
        InputStream inStream = NativeLoader.class.getResourceAsStream(path);
        if (inStream == null) {
            throw new FileNotFoundException(String.format("Could not find resource %s in jar.", path));
        }

        FileOutputStream outStream = new FileOutputStream(temp);
        byte[] buffer = new byte[1 << 18];
        int bytesRead;

        try {
            while ((bytesRead = inStream.read(buffer)) >= 0) {
                outStream.write(buffer, 0, bytesRead);
            }
        } finally {
            outStream.close();
            inStream.close();
        }
    }
}
