package io.github.fabriccompatibilitylayers.crossremapper;

import fr.catcore.wfvaio.FabricVariants;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.CacheHandler;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingsConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class DedicatedMappingsConfig implements MappingsConfig {
    private final FabricVariants variant;
    private final CacheHandler cacheHandler;

    public DedicatedMappingsConfig(FabricVariants variant, CacheHandler cacheHandler) {
        this.variant = variant;
        this.cacheHandler = cacheHandler;
    }

    @Override
    public String getSourceNamespace() {
        return variant.name();
    }

    @Override
    public Supplier<String> getExtraMappings() {
        return () -> IntermediaryCacheHandler.getIntermediary(this.variant, this.cacheHandler);
    }

    @Override
    public Map<String, String> getRenamingMap() {
        return IntermediaryNamespaces.getNamespaceRenames(variant, FabricConstants.MC_VERSION);
    }

    @Override
    public @Nullable String getDefaultPackage() {
        return null;
    }
}
