package io.github.fabriccompatibilitylayers.crossremapper;

import com.google.gson.JsonObject;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.ModCandidate;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.ModDiscovererConfig;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class FabricModCandidate implements ModCandidate {
    private final ModContainer modContainer;
    private final String kind, accessWidenerPath;
    private final ModCandidate parent;
    private final JsonObject fabricModJson;
    private Path tempPath = null;
    private ModDiscovererConfig discovererConfig;
    private byte[] accessWidener;
    private Path destination;

    protected FabricModCandidate(ModContainer modContainer, String kind, String accessWidenerPath, ModCandidate parent, JsonObject fabricModJson) {
        this.modContainer = modContainer;
        this.kind = kind;
        this.accessWidenerPath = accessWidenerPath;
        this.parent = parent;
        this.fabricModJson = fabricModJson;
    }

    @Override
    public String getId() {
        return this.modContainer.getMetadata().getId();
    }

    @Override
    public Path getPath() {
        if (this.tempPath == null) {
            return this.getOriginalPath();
        }

        return this.tempPath;
    }

    @Override
    public String getType() {
        return this.kind;
    }

    @Override
    public @Nullable String getAccessWidenerPath() {
        return this.accessWidenerPath;
    }

    @Override
    public @Nullable ModCandidate getParent() {
        return this.parent;
    }

    @Override
    public @Nullable String getVersion() {
        return this.modContainer.getMetadata().getVersion().toString();
    }

    @Override
    public @Nullable String getParentSubPath() {
        return this.modContainer.getOrigin().getKind() == ModOrigin.Kind.NESTED ? this.modContainer.getOrigin().getParentSubLocation() : null;
    }

    @Override
    public String getDestinationName() {
        return "remapped_" + this.getPath().getFileName().toString();
    }

    @Override
    public ModDiscovererConfig getDiscovererConfig() {
        return discovererConfig;
    }

    @Override
    public void setAccessWidener(byte[] data) {
        this.accessWidener = data;
    }

    @Override
    public byte @Nullable [] getAccessWidener() {
        return this.accessWidener;
    }

    @Override
    public void setDestination(Path destination) {
        this.destination = destination;
    }

    @Override
    public @Nullable Path getDestination() {
        return this.destination;
    }

    public ModContainer getModContainer() {
        return modContainer;
    }

    public JsonObject getFabricModJson() {
        return fabricModJson;
    }

    public Path getOriginalPath() {
        return this.modContainer.getOrigin().getKind() != ModOrigin.Kind.NESTED ? this.modContainer.getOrigin().getPaths().get(0) : null;
    }

    @Override
    public void setPath(Path tempPath) {
        this.tempPath = tempPath;
    }

    public void setDiscovererConfig(ModDiscovererConfig discovererConfig) {
        this.discovererConfig = discovererConfig;
    }
}
