package io.github.fabriccompatibilitylayers.crossremapper.helpers;

import fr.catcore.wfvaio.FabricVariants;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class ORNITHE_V2MappingResolver extends BaseMappingResolver {
    private static final String CONTEXT_ID = "cross-intermediary-" + FabricVariants.ORNITHE_V2;

    public static String mapClassName(MappingResolver resolver, String namespace, String className) {
        if (namespace.equals("official")) {
            return FabricLoader.getInstance().getMappingResolver().mapClassName(namespace, className);
        }

        return replaceSlashesWithDots(MappingUtils.mapClass(CONTEXT_ID, replaceDotsWithSlashes(className)));
    }

    public static String unmapClassName(MappingResolver resolver, String namespace, String className) {
        if (namespace.equals("official")) {
            return FabricLoader.getInstance().getMappingResolver().unmapClassName(namespace, className);
        }

        return replaceSlashesWithDots(MappingUtils.unmapClass(CONTEXT_ID, replaceDotsWithSlashes(className)));
    }

    public static String mapFieldName(MappingResolver resolver, String namespace, String owner, String name, String descriptor) {
        if (namespace.equals("official")) {
            return FabricLoader.getInstance().getMappingResolver().mapFieldName(namespace, owner, name, descriptor);
        }

        return MappingUtils.mapField(CONTEXT_ID, replaceDotsWithSlashes(owner), name, descriptor).getName();
    }

    public static String mapMethodName(MappingResolver resolver, String namespace, String owner, String name, String descriptor) {
        if (namespace.equals("official")) {
            return FabricLoader.getInstance().getMappingResolver().mapMethodName(namespace, owner, name, descriptor);
        }

        return MappingUtils.mapMethod(CONTEXT_ID, replaceDotsWithSlashes(owner), name, descriptor).getName();
    }
}
