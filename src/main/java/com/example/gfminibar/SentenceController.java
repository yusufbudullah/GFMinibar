package com.example.gfminibar;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SentenceController {

    @FXML
    // List to store references to dynamically created TextFields
    private List<TextField> textFieldList = new ArrayList<>();
    // List to store references to dynamically created sentence panels
    public List<HBox> sentencePanelList = new ArrayList<>();
    // Map to link sentence panels to radio buttons
    private Map<HBox, RadioButton> sentencePanelToRadioButtonMap = new HashMap<>();

    private MinibarController minibarController;

    private ToggleGroup globalToggleGroup;

    private HBox selectedPanel;


    public void setMinibarController(MinibarController minibarController) {
        this.minibarController = minibarController;
    }

    public void setGlobalToggleGroup(ToggleGroup globalToggleGroup) {
        this.globalToggleGroup = globalToggleGroup;
    }

    public HBox getSelectedPanel() {
        return selectedPanel;
    }

    public Map<HBox, RadioButton> getSentencePanelToRadioButtonMap() {
        return sentencePanelToRadioButtonMap;
    }

    public List<HBox> getSentencePanelList() {
        return sentencePanelList;
    }




    private String concatenateTextFields(HBox sentencePanel) {
        HBox wordBox = (HBox) sentencePanel.lookup("#wordBox");
        StringBuilder concatenatedText = new StringBuilder();

        for (Node node : wordBox.getChildren()) {
            if (node instanceof TextField) {
                TextField textField = (TextField) node;
                concatenatedText.append(textField.getText()).append(" ");
            }
        }

        return concatenatedText.toString().trim();
    }




    @FXML
    private void onEnterKeyPressed(KeyEvent event) {
        if (event.getCode().getName().equals("Enter")) {
            addNewTextFieldToWordBox(event);
        }
    }

    private void addNewTextFieldToWordBox(KeyEvent event) {
        TextField sourceTextField = (TextField) event.getSource();

        // Create a new TextField instance
        TextField newTextField = new TextField();
        newTextField.setId("textField");
        newTextField.setPrefHeight(sourceTextField.getPrefHeight());
        newTextField.setPrefWidth(63); // Set the width to 20
        newTextField.setOnKeyPressed(this::onEnterKeyPressed);

        // Get the parent sentencePanel
        HBox sentencePanel = (HBox) sourceTextField.getParent();

        // Get the wordBox within the sentencePanel
        HBox wordBox = (HBox) sentencePanel.lookup("#wordBox");

        // Set margin to 5 on the left
        HBox.setMargin(newTextField, new Insets(0, 0, 0, 5));

        // Add the new TextField to the wordBox
        wordBox.getChildren().add(newTextField);

        // Request focus on the new TextField
        newTextField.requestFocus();

        textFieldList.add(newTextField);
        displayConcatenatedText(sentencePanel);
    }

    private void addNewTextFieldToWordBox() {
        if (selectedPanel == null) {
            return;
        }

        // Create a new TextField instance
        TextField newTextField = new TextField();
        newTextField.setId("textField");
        newTextField.setPrefWidth(63); // Set the width to 20
        newTextField.setStyle("-fx-margin: 0 0 0 15;"); // Set padding on the left to 5

        // Setting the height based on an existing TextField in the wordBox, or default to some value
        HBox wordBox = (HBox) selectedPanel.lookup("#wordBox");
        if (!wordBox.getChildren().isEmpty()) {
            TextField existingTextField = (TextField) wordBox.getChildren().get(0); // Assuming the first child is a TextField
            newTextField.setPrefHeight(existingTextField.getPrefHeight());
        } else {
            newTextField.setPrefHeight(30); // Set a default value
        }

        // Add the new TextField to the wordBox
        wordBox.getChildren().add(newTextField);

        // Request focus on the new TextField
        newTextField.requestFocus();

        displayConcatenatedText(selectedPanel);
    }



    @FXML
    private void onSentencePanelClick(MouseEvent event) {
        selectedPanel = (HBox) event.getSource();
        RadioButton radioButton = sentencePanelToRadioButtonMap.get(selectedPanel);

        // Reset all panels to their default style
        for (HBox panel : sentencePanelList) {
            panel.getStyleClass().remove("selected-panel");
        }

        // Highlight this panel
        selectedPanel.getStyleClass().add("selected-panel");
        // Request focus on the new TextField
        selectedPanel.requestFocus();
        displayConcatenatedText(selectedPanel);

        if (radioButton != null) {
            radioButton.setSelected(true);
        }

        // Set this controller as the currently selected one
        if (minibarController != null) {
            minibarController.setCurrentSelectedSentenceController(this);
        }
    }


    public void setSentencePanelToRadioButtonMapping(HBox sentencePanel, RadioButton radioButton) {
        sentencePanelToRadioButtonMap.put(sentencePanel, radioButton);
    }

    public void setSentencePanelList(List<HBox> list) {
        this.sentencePanelList = list;
    }


    @FXML
    private void onDeleteKeyPressed(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (keyCode == KeyCode.DELETE || keyCode == KeyCode.BACK_SPACE) {
            Toggle selectedToggle = globalToggleGroup.getSelectedToggle();
            if (selectedToggle != null) {
                RadioButton selectedRadioButton = (RadioButton) selectedToggle;
                HBox sentencePanel = findSentencePanelByRadioButton(selectedRadioButton);
                if (sentencePanel != null) {
                    removeLastTextField(sentencePanel);
                    displayConcatenatedText(sentencePanel);
                }
            }
        }
    }

    private HBox findSentencePanelByRadioButton(RadioButton radioButton) {
        return sentencePanelToRadioButtonMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(radioButton))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void removeLastTextField(HBox sentencePanel) {
        HBox wordBox = (HBox) sentencePanel.lookup("#wordBox");
        int childrenCount = wordBox.getChildren().size();

        if (childrenCount > 1) {
            TextField lastTextField = (TextField) wordBox.getChildren().get(childrenCount - 1);
            wordBox.getChildren().remove(lastTextField);
        } else if (childrenCount == 1) {
            // If only one TextField remains, clear its text instead of removing it.
            TextField remainingTextField = (TextField) wordBox.getChildren().get(0);
            remainingTextField.clear();
        }
    }


    public void displayConcatenatedText(HBox sentencePanel) {
        String text = concatenateTextFields(sentencePanel);
        System.out.println("Selected panel text: " + text);
        if (minibarController != null) {
            minibarController.setConcatLabel(text);
        }


    }

    public void populateTextField(String text) {
        // Assuming the currently selected sentencePanel is stored in a variable called "selectedPanel"
        if (selectedPanel != null) {
            HBox wordBox = (HBox) selectedPanel.lookup("#wordBox");
            if (wordBox != null && !wordBox.getChildren().isEmpty()) {
                // Find the last TextField within the wordBox
                TextField lastTextField = null;
                for (Node node : wordBox.getChildren()) {
                    if (node instanceof TextField) {
                        lastTextField = (TextField) node;
                    }
                }

                // Populate the last TextField if found
                if (lastTextField != null) {
                    lastTextField.setText(text);
                    addNewTextFieldToWordBox();
                }
            }
        }
    }





}






