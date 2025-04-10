package io.github.fabriccompatibilitylayers.crossremapper;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;

public class McVersionGetter {
    public static String getMcVersion() {
        try {
            return ((FabricLoaderImpl) FabricLoader.getInstance()).getGameProvider().getRawGameVersion().replace("Beta ", "b");
        } catch (Throwable e) {
            return FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString();
        }
    }
}
