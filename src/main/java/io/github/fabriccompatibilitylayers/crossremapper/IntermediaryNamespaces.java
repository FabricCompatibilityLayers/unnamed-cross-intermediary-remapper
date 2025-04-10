package io.github.fabriccompatibilitylayers.crossremapper;

import fr.catcore.wfvaio.FabricVariants;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum IntermediaryNamespaces {
    SIMPLE(new String[][]{
            new String[]{"intermediary", "%variant%"},
    },
            new Entry(FabricVariants.OFFICIAL),
            new Entry(FabricVariants.LEGACY_FABRIC_V1),
            new Entry(FabricVariants.LEGACY_FABRIC_V2),
            new Entry(FabricVariants.ORNITHE_V1),
            new Entry(FabricVariants.ORNITHE_V2, ">=1.3")
    ),
    MERGED(new String[][]{
            new String[]{FabricLoader.getInstance().getEnvironmentType().name().toLowerCase(Locale.ENGLISH) + "Official", "official"},
            new String[]{"intermediary", "%variant%"},
    },
            new Entry(FabricVariants.BABRIC_NEW_FORMAT),
            new Entry(FabricVariants.ORNITHE_V2, "<1.3")
    );

    private final String[][] namespaces;
    private final Entry[] entries;

    IntermediaryNamespaces(String[][] namespaces, Entry... entries) {
        this.namespaces = namespaces;
        this.entries = entries;
    }

    public static Map<String, String> getNamespaceRenames(FabricVariants variant, Version version) {
        for (IntermediaryNamespaces intermediaryNamespaces : IntermediaryNamespaces.values()) {
            for (Entry entry : intermediaryNamespaces.entries) {
                if (entry.variant == variant) {
                    if (entry.predicate != null) {
                        try {
                            if (!VersionPredicate.parse(entry.predicate).test(version)) {
                                continue;
                            }
                        } catch (VersionParsingException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    Map<String, String> renames = new HashMap<>();

                    for (String[] namespace : intermediaryNamespaces.namespaces) {
                        renames.put(namespace[0], namespace[1].replace("%variant%", variant.name()));
                    }

                    return renames;
                }
            }
        }

        return new HashMap<>();
    }

    public static class Entry {
        public final FabricVariants variant;
        public final @Nullable String predicate;

        public Entry(FabricVariants variant, @Nullable String predicate) {
            this.variant = variant;
            this.predicate = predicate;
        }

        public Entry(FabricVariants variant) {
            this(variant, null);
        }
    }
}
