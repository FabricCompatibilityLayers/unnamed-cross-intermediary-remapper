package io.github.fabriccompatibilitylayers.crossremapper;

import fr.catcore.wfvaio.FabricVariants;
import fr.catcore.wfvaio.WhichFabricVariantAmIOn;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class DedicatedModRemapper implements ModRemapper {
    private final FabricVariants variant;
    private final List<ModCandidate> candidates;
    private final ModDiscovererConfig discovererConfig = ModDiscovererConfig.builder("mods")
            .candidateCollector(this::collectCandidates)
            .fileNameMatcher(".jaaaarrr")
            .exportToOriginalFolder(true)
            .build();
    private CacheHandler cacheHandler;

    public DedicatedModRemapper(FabricVariants variant, List<ModCandidate> candidates) {
        this.variant = variant;
        this.candidates = candidates;
    }

    @Override
    public String getContextId() {
        return "cross-intermediary-" + this.variant.toString();
    }

    @Override
    public void init(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    @Override
    public List<ModDiscovererConfig> getModDiscoverers() {
        return Collections.singletonList(
                discovererConfig
        );
    }

    @Override
    public List<ModRemapper> collectSubRemappers(List<ModCandidate> discoveredMods) {
        this.candidates.forEach(candidate -> {
            ((FabricModCandidate) candidate).setDiscovererConfig(discovererConfig);
        });

        discoveredMods.addAll(this.candidates);

        return new ArrayList<>();
    }

    @Override
    public MappingsConfig getMappingsConfig() {
        return new DedicatedMappingsConfig(this.variant, this.cacheHandler);
    }

    @Override
    public List<RemappingFlags> getRemappingFlags() {
        return Arrays.asList(RemappingFlags.MIXIN, RemappingFlags.ACCESS_WIDENER);
    }

    @Override
    public void afterRemapping() {
        for (ModCandidate modCandidate : this.candidates) {
            if (modCandidate.getParentSubPath() == null) {
                Path path = ((FabricModCandidate) modCandidate).getOriginalPath();

                try {
                    Files.move(path, FabricLoader.getInstance().getGameDir().resolve("mods").resolve(path.getFileName().toString() + ".disabled"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try (FileSystem fs = Utils.getJarFileSystem(modCandidate.getDestination())) {
                Path manifestPath = fs.getPath("/META-INF/MANIFEST.MF");

                Manifest manifest = new Manifest(manifestPath.toUri().toURL().openStream());
                Attributes attributesObject = manifest.getMainAttributes();
                attributesObject.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
                attributesObject.put(new Attributes.Name(FabricConstants.MANIFEST_ENTRY), WhichFabricVariantAmIOn.getVariant().toString());

                Files.delete(manifestPath);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                manifest.write(out);
                Files.write(manifestPath, out.toByteArray());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void afterAllRemappings() {

    }

    @Override
    public void addRemappingLibraries(List<RemapLibrary> libraries, EnvType environment) {

    }

    public List<ModCandidate> collectCandidates(ModDiscovererConfig config, Path modPath, List<String> fileList) {
        return new ArrayList<>();
    }

    @Override
    public void registerAdditionalMappings(MappingBuilder mappingBuilder) {

    }

    @Override
    public void registerPreVisitors(VisitorInfos visitorInfos) {

    }

    @Override
    public void registerPostVisitors(VisitorInfos visitorInfos) {

    }
}
