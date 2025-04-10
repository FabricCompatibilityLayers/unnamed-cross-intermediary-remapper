package io.github.fabriccompatibilitylayers.crossremapper;

import fr.catcore.wfvaio.FabricVariants;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.CacheHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class IntermediaryCacheHandler {
    public static boolean hasIntermediaryFor(FabricVariants variant) {
        return IntermediarySources.getIntermediary(variant, FabricConstants.RAW_VERSION, FabricConstants.MC_VERSION) != null;
    }

    public static String getIntermediary(FabricVariants variant, CacheHandler cacheHandler) {
        Library library = IntermediarySources.getIntermediary(variant, FabricConstants.RAW_VERSION, FabricConstants.MC_VERSION);

        if (library == null) throw new IllegalStateException("Unable to find intermediary for variant " + variant + " compatible with version " + FabricConstants.RAW_VERSION);

        Path libraryPath = cacheHandler.resolveCache(library.getPath());

        try {
            if (!Files.exists(libraryPath)) {
                Path parent = libraryPath.getParent();

                if (!Files.exists(parent)) {
                    Files.createDirectories(parent);
                }

                URL url = new URL(library.getURL());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try (InputStream inputStream = connection.getInputStream()) {
                    Files.copy(inputStream, libraryPath);
                }

                connection.disconnect();
            }

            return getMappingsInIntermediaryFile(libraryPath);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getMappingsInIntermediaryFile(Path path) throws URISyntaxException, IOException {
        try (FileSystem fs = Utils.getJarFileSystem(path)) {
            Path mappingsPath = fs.getPath("/mappings/mappings.tiny");

            return new String(Files.readAllBytes(mappingsPath));
        }
    }
}
