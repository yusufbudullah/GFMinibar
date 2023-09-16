package com.example.gfminibar;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;


public class TranslationController {
    @FXML
    private HBox translationPanel;
    @FXML
    private Label langLabel;

    public Label getLangLabel() {
        return langLabel;
    }

}
