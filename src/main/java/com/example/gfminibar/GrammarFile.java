package com.example.gfminibar;

/**
 * Represents a grammar file with its associated name and path.
 */
public class GrammarFile {

    /** The name of the grammar file. */
    private String name;

    /** The path to the grammar file. */
    private String path;

    /**
     * Constructs a new GrammarFile with the given name and path.
     *
     * @param name The name of the grammar file.
     * @param path The path to the grammar file.
     */
    public GrammarFile(String name, String path) {
        this.name = name;
        this.path = path;
    }

    /**
     * Returns the name of the grammar file.
     *
     * @return The name of the grammar file.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the path to the grammar file.
     *
     * @return The path to the grammar file.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the string representation of the grammar file.
     * This representation is the name of the file, which will be displayed in the ComboBox.
     *
     * @return The name of the grammar file.
     */
    @Override
    public String toString() {
        return name;
    }
}

