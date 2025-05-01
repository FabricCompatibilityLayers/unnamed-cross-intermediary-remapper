package io.github.fabriccompatibilitylayers.crossremapper.helpers;

public class BaseMappingResolver {
    public static String replaceSlashesWithDots(String cname) {
        return cname.replace('/', '.');
    }

    public static String replaceDotsWithSlashes(String cname) {
        return cname.replace('.', '/');
    }
}
