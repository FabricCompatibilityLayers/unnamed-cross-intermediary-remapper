package io.github.fabriccompatibilitylayers.crossremapper;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

public class FabricConstants {
    public static final String MANIFEST_ENTRY = "Fabric-Intermediary";
    public static final Version MC_VERSION = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion();
    public static String RAW_VERSION = McVersionGetter.getMcVersion();
}
