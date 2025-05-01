package io.github.fabriccompatibilitylayers.crossremapper.helpers;

import fr.catcore.wfvaio.FabricVariants;
import io.github.fabriccompatibilitylayers.modremappingapi.api.v2.MappingUtils;
import net.fabricmc.loader.api.MappingResolver;

public class OFFICIALMappingResolver extends BaseMappingResolver {
    private static final String CONTEXT_ID = "cross-intermediary-" + FabricVariants.OFFICIAL;

    public static String mapClassName(MappingResolver resolver, String namespace, String className) {
        return replaceSlashesWithDots(MappingUtils.mapClass(CONTEXT_ID, replaceDotsWithSlashes(className)));
    }

    public static String unmapClassName(MappingResolver resolver, String targetNamespace, String className) {
        return replaceSlashesWithDots(MappingUtils.unmapClass(CONTEXT_ID, replaceDotsWithSlashes(className)));
    }

    public static String mapFieldName(MappingResolver resolver, String namespace, String owner, String name, String descriptor) {
        return MappingUtils.mapField(CONTEXT_ID, replaceDotsWithSlashes(owner), name, descriptor).getName();
    }

    public static String mapMethodName(MappingResolver resolver, String namespace, String owner, String name, String descriptor) {
        return MappingUtils.mapMethod(CONTEXT_ID, replaceDotsWithSlashes(owner), name, descriptor).getName();
    }
}
