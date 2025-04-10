package io.github.fabriccompatibilitylayers.crossremapper;

import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    /* Define ZIP File System Properies in HashMap */
    private static final Map<String, String> ZIP_PROPERTIES = new HashMap<>();

    static {
        /* We want to read an existing ZIP File, so we set this to False */
        ZIP_PROPERTIES.put("create", "false");
        /* Specify the encoding as UTF-8 */
        ZIP_PROPERTIES.put("encoding", "UTF-8");
    }

    @ApiStatus.Internal
    public static FileSystem getJarFileSystem(Path path) throws URISyntaxException, IOException {
        return FileSystems.newFileSystem(URI.create("jar:" + path.toUri()), ZIP_PROPERTIES);
    }
}
