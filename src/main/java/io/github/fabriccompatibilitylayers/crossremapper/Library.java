package io.github.fabriccompatibilitylayers.crossremapper;

import java.io.File;

public class Library {
    public final String name;
    public final String url;

    public Library(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getURL() {
        String path;
        String[] parts = this.name.split(":", 3);
        path = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] + ".jar";

        return url + path;
    }

    public String getPath() {
        String[] parts = this.name.split(":", 3);
        String path = parts[0].replace(".", File.separator) + File.separator + parts[1] + File.separator + parts[2] + File.separator + parts[1] + "-" + parts[2] + ".jar";
        return path.replaceAll(" ", "_");
    }
}
