package com.example.gfminibar;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


public class TranslationController {
    // ---- Class Members and UI Controls ----
    @FXML
    private HBox translationPanel;
    @FXML
    private Label langLabel;
    @FXML
    private Label langNameLabel;
    @FXML
    private Pane transPanel;

    // ---- Accessor Methods ----

    /**
     * Retrieves the translation panel (transPanel).
     *
     * @return The Pane representing the translation panel.
     */
    public Pane getTranslationPanel() {
        return transPanel;
    }

    /**
     * Retrieves the language label (langLabel).
     *
     * @return The Label representing the language label.
     */
    public Label getLangLabel() {
        return langLabel;
    }

    /**
     * Retrieves the language name label (langNameLabel).
     *
     * @return The Label representing the language name label.
     */
    public Label getLangNameLabel() {
        return langNameLabel;
    }


}
