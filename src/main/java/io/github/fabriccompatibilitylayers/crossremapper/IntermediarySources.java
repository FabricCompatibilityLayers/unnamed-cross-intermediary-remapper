package io.github.fabriccompatibilitylayers.crossremapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.catcore.wfvaio.FabricVariants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public enum IntermediarySources {
    OFFICIAL_LF_V2(new FabricVariants[] {
            FabricVariants.OFFICIAL,
            FabricVariants.LEGACY_FABRIC_V2
    }, new Source[] {
            new Source(
                    null,
                    "https://meta.fabricmc.net/v2/versions/intermediary",
                    "https://maven.fabricmc.net/",
                    null
            ),
            new Source(
                    ">=1.8.2 <1.14-alpha.18.43.b",
                    null,
                    "https://maven.legacyfabric.net/",
                    "net.legacyfabric.v2:intermediary:%version%"
            )
    }),
    LF_V1(new FabricVariants[]{
            FabricVariants.LEGACY_FABRIC_V1
    }, new Source[] {
            new Source(
                    null,
                    "https://meta.legacyfabric.net/v2/versions/intermediary",
                    "https://maven.legacyfabric.net/",
                    null
            )
    }),
    BABRIC(new FabricVariants[]{
            FabricVariants.BABRIC,
            FabricVariants.BABRIC_NEW_FORMAT
    }, new Source[] {
            new Source(
                    null,
                    "https://meta.babric.glass-launcher.net/v2/versions/intermediary",
                    "https://maven.glass-launcher.net/babric/",
                    null
            )
    }),
    ORNITHE_V1(
            new FabricVariants[]{
                    FabricVariants.ORNITHE_V1
            },
            new Source[] {
                    new Source(
                            null,
                            "https://meta.ornithemc.net/v3/versions/gen1/intermediary",
                            "https://maven.ornithemc.net/releases/",
                            null
                    )
            }
    ),
    ORNITHE_V2(
            new FabricVariants[]{
                    FabricVariants.ORNITHE_V1
            },
            new Source[] {
                    new Source(
                            null,
                            "https://meta.ornithemc.net/v3/versions/gen2/intermediary",
                            "https://maven.ornithemc.net/releases/",
                            null
                    )
            }
    );

    private static final JsonParser jsonParser = new JsonParser();

    static {
        for (IntermediarySources source : values()) {
            source.populateVersions();
        }
    }

    private final FabricVariants[] fabricVariants;
    private final Source[] sources;

    private final Map<String, Library> versions = new HashMap<>();

    IntermediarySources(FabricVariants[] fabricVariants, Source[] sources) {
        this.fabricVariants = fabricVariants;
        this.sources = sources;
    }

    private void populateVersions() {
        for (Source source : sources) {
            if (source.meta != null) {
                try {
                    URL url = new URL(source.meta);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    try (Reader reader = new InputStreamReader(connection.getInputStream())) {
                        JsonElement element = jsonParser.parse(reader);

                        if (element.isJsonArray()) {
                            JsonArray array = element.getAsJsonArray();

                            for (JsonElement versionElement : array) {
                                if (versionElement.isJsonObject()) {
                                    JsonObject obj = versionElement.getAsJsonObject();

                                    String maven = obj.get("maven").getAsString();
                                    String version = obj.get("version").getAsString();

                                    Library library = new Library(maven, source.maven);

                                    if (obj.has("versionNoSide")) {
                                        String versionNoSide = obj.get("versionNoSide").getAsString();

                                        if (Objects.equals(version, versionNoSide)) {
                                            versions.put(version, library);
                                        } else if (Objects.equals(version, versionNoSide + "-" + FabricLoader.getInstance().getEnvironmentType().name().toLowerCase(Locale.ENGLISH))) {
                                            versions.put(versionNoSide, library);
                                        }
                                    } else {
                                        versions.put(version, library);
                                    }
                                }
                            }
                        }
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static Library getIntermediary(FabricVariants variant, String originalVersion, Version formattedVersion) {
        for (IntermediarySources source : values()) {
            if (Arrays.stream(source.fabricVariants).anyMatch(v -> v == variant)) {
                if (source.versions.containsKey(originalVersion)) {
                    return source.versions.get(originalVersion);
                }

                for (Source source1 : source.sources) {
                    if (source1.predicate != null) {
                        try {
                            if (VersionPredicate.parse(source1.predicate).test(formattedVersion)) {
                                return new Library(
                                        source1.artifactPattern.replace("%version%", originalVersion),
                                        source1.maven
                                );
                            }
                        } catch (VersionParsingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        return null;
    }

    public static class Source {
        private final @Nullable String predicate;
        private final @Nullable String meta;
        private final String maven;
        private final @Nullable String artifactPattern;

        public Source(@Nullable String predicate, @Nullable String meta, String maven, @Nullable String artifactPattern) {
            this.predicate = predicate;
            this.meta = meta;
            this.maven = maven;
            this.artifactPattern = artifactPattern;
        }
    }

}
