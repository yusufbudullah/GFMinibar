package com.example.gfminibar;

public class GrammarFile {
    private String name;
    private String path;

    public GrammarFile(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return name; // This will be displayed in the ComboBox
    }
}
