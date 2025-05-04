package io.github.fabriccompatibilitylayers.crossremapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.catcore.wfvaio.FabricVariants;
import fr.catcore.wfvaio.WhichFabricVariantAmIOn;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class FabricModRemapper implements ModRemapper {
    private final JsonParser parser = new JsonParser();
    private CacheHandler cacheHandler;

    @Override
    public String getContextId() {
        return "cross-intermediary-discovery";
    }

    @Override
    public void init(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    @Override
    public List<ModDiscovererConfig> getModDiscoverers() {
        return Arrays.asList(
            ModDiscovererConfig.builder("mods")
                    .candidateCollector(this::collectCandidates)
                    .fileNameMatcher(".jaaaarrr")
                    .build()
        );
    }

    private static final List<String> excludedIds = Arrays.asList(
            "minecraft",
            "java",
            "fabricloader",
            "mixinextras",
            "unnamed-cross-intermediary-remapper",
            "mod-remapping-api"
    );

    @Override
    public List<ModRemapper> collectSubRemappers(List<ModCandidate> discoveredMods) {
        List<ModRemapper> remappers = new ArrayList<>();

        discoveredMods.clear();

        Map<String, ModContainer> modMap = FabricLoader.getInstance().getAllMods()
                .stream().collect(Collectors.toMap(container -> container.getMetadata().getId(), container -> container));

        Map<String, ModCandidate> candidateMap = new HashMap<>();

        for (ModContainer modContainer : modMap.values()) {
            if (excludedIds.contains(modContainer.getMetadata().getId())) {
                continue;
            }

            try {
                containerToCandidate(modContainer, modMap, candidateMap);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (ModCandidate mod : candidateMap.values()) {
            if (excludedIds.contains(mod.getId())) {
                continue;
            }

            try {
                extractModCandidate((FabricModCandidate) mod, modMap);
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        Map<FabricVariants, List<ModCandidate>> variantToCandidateMap = candidateMap.values()
                .stream()
                .filter(modCandidate -> !excludedIds.contains(modCandidate.getId()))
                .collect(Collectors.groupingBy(candidate -> FabricVariants.valueOf(candidate.getType())));

        for (FabricVariants variant : FabricVariants.values()) {
//            if (variant == FabricVariants.UNKNOWN) remappers.add(new DedicatedModRemapper(variant, variantToCandidateMap.getOrDefault(variant, new ArrayList<>())));

            if (variant == FabricVariants.UNKNOWN || !IntermediaryCacheHandler.hasIntermediaryFor(variant) || variant == WhichFabricVariantAmIOn.getVariant() || variant == FabricVariants.BABRIC) continue;

            List<ModCandidate> candidates = variantToCandidateMap.getOrDefault(variant, new ArrayList<>());
            if (variantToCandidateMap.containsKey(FabricVariants.UNKNOWN)) candidates.addAll(variantToCandidateMap.get(FabricVariants.UNKNOWN));

            remappers.add(new DedicatedModRemapper(variant, candidates));
        }

        return remappers;
    }

    @Override
    public MappingsConfig getMappingsConfig() {
        return MappingsConfig.defaultConfig();
    }

    @Override
    public List<RemappingFlags> getRemappingFlags() {
        return new ArrayList<>();
    }

    @Override
    public void afterRemapping() {

    }

    @Override
    public void afterAllRemappings() {

    }

    @Override
    public void addRemappingLibraries(List<RemapLibrary> libraries, EnvType environment) {

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

    private void extractModCandidate(FabricModCandidate modCandidate, Map<String, ModContainer> modMap) throws IOException, URISyntaxException {
        Path tempDir = this.cacheHandler.resolveTemp(modCandidate.getType());

        if (!Files.exists(tempDir)) {
            Files.createDirectory(tempDir);
        }

        Path output;

        if (modCandidate.getModContainer().getOrigin().getKind() == ModOrigin.Kind.NESTED) {
            String[] parts = modCandidate.getParentSubPath().split("/");

            output = tempDir.resolve(parts[parts.length - 1]);

            if (Files.exists(output)) {
                Files.delete(output);
            }

            Files.copy(
                    modMap.get(modCandidate.getModContainer().getOrigin().getParentModId()).findPath(modCandidate.getParentSubPath()).get(),
                    output
            );
        } else {
            output = tempDir.resolve(modCandidate.getPath().getFileName().toString());

            if (Files.exists(output)) {
                Files.delete(output);
            }

            Files.copy(modCandidate.getPath(), output);
        }

        if (Files.isDirectory(output)) return;

        modCandidate.setPath(output);

        JsonObject fabricModJson = modCandidate.getFabricModJson();

        String originalName = fabricModJson.has("name") ? fabricModJson.get("name").getAsString() : fabricModJson.get("id").getAsString();
        fabricModJson.addProperty("name", "(Remapped) " + originalName);

        List<String> toRemove = new ArrayList<>();

        if (fabricModJson.has("jars")) {
            JsonArray jars = fabricModJson.get("jars").getAsJsonArray();

            for (JsonElement jar : jars) {
                JsonObject jarObject = jar.getAsJsonObject();
                toRemove.add(jarObject.get("file").getAsString());
            }

            fabricModJson.remove("jars");
        }

        try (FileSystem fs = Utils.getJarFileSystem(output)) {
            Path fmj = fs.getPath("/fabric.mod.json");
            Files.delete(fmj);
            Files.write(fmj, fabricModJson.toString().getBytes());

            for (String subPath : toRemove) {
                Files.delete(fs.getPath("/" + subPath));
            }
        }
    }

    private void containerToCandidate(ModContainer container, Map<String, ModContainer> modMap, Map<String, ModCandidate> candidateMap) throws IOException {
        Path manifestPath = container.findPath("META-INF/MANIFEST.MF").orElse(null);

        FabricVariants variant = FabricVariants.UNKNOWN;
        boolean preRemapped = false;

        try {
            Manifest manifest = new Manifest(manifestPath.toUri().toURL().openStream());
            Attributes attributesObject = manifest.getMainAttributes();
            Map<String, Object> attributes = attributesObject.entrySet()
                    .stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));

            variant = WhichFabricVariantAmIOn.identifyVariantFromManifest(attributes);

            if (attributes.containsKey(FabricConstants.MANIFEST_ENTRY)) {
                variant = FabricVariants.valueOf((String) attributes.get(FabricConstants.MANIFEST_ENTRY));
                preRemapped = true;
            }
        } catch (Throwable ignored) {
        }

        if (!preRemapped && container.getMetadata().containsCustomValue("fabric-loom:generated") &&
                container.getMetadata().getCustomValue("fabric-loom:generated").getAsBoolean()) {
            variant = FabricVariants.UNKNOWN;
        }

        if (variant == WhichFabricVariantAmIOn.getVariant()) return;

        String accessWidenerPath = null;
        JsonObject fabricModJson = null;

        try {
            Path jsonPath = container.findPath("fabric.mod.json").orElse(null);

            fabricModJson = parser.parse(new String(Files.readAllBytes(jsonPath), StandardCharsets.UTF_8)).getAsJsonObject();

            if (fabricModJson.has("schemaVersion") && fabricModJson.get("schemaVersion").getAsInt() == 1) {
                if (fabricModJson.has("accessWidener")) {
                    accessWidenerPath = fabricModJson.get("accessWidener").getAsString();
                }
            }
        } catch (Throwable t) {
        }

        ModCandidate parent = null;

        if (container.getOrigin().getKind() == ModOrigin.Kind.NESTED) {
            if (!candidateMap.containsKey(container.getOrigin().getParentModId())) {
                containerToCandidate(modMap.get(container.getOrigin().getParentModId()), modMap, candidateMap);
            }

            parent = candidateMap.get(container.getOrigin().getParentModId());
        } else {
            if (Files.isDirectory(container.getOrigin().getPaths().get(0))) return;
        }

        candidateMap.put(container.getMetadata().getId(), new FabricModCandidate(
                container, variant.toString(), accessWidenerPath, parent, fabricModJson
        ));
    }

    public List<ModCandidate> collectCandidates(ModDiscovererConfig config, Path modPath, List<String> fileList) {
        return new ArrayList<>();
    }
}
