package com.example.gfminibar;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MinibarController {

    @FXML
    private VBox tester;

    private ToggleGroup globalToggleGroup = new ToggleGroup();

    private int radioButtonCounter = 1; // Counter for generating unique IDs

    public List<HBox> globalSentencePanelList = new ArrayList<>();

    @FXML
    private ScrollPane wordsPane;

    private SentenceController currentSelectedSentenceController;

    public void setCurrentSelectedSentenceController(SentenceController controller) {
        this.currentSelectedSentenceController = controller;
    }





    @FXML
    private void onClearButtonClick() {
        // Retain the first sentence panel and remove the rest
        int numberOfPanels = tester.getChildren().size();
        if (numberOfPanels > 1) {
            tester.getChildren().remove(1, numberOfPanels);
            // reset the counter for the next radioButton ID
            radioButtonCounter = 1;

            // Get the last remaining panel
            HBox lastRemainingPanel = (HBox) tester.getChildren().get(0);

            // Locate its wordBox
            HBox wordBox = (HBox) lastRemainingPanel.lookup("#wordBox");

            if (wordBox != null && !wordBox.getChildren().isEmpty()) {
                // Store the first TextField
                Node firstTextField = wordBox.getChildren().get(0);

                // Clear all TextFields from the wordBox
                wordBox.getChildren().clear();

                // Add back the first TextField and clear its text
                wordBox.getChildren().add(firstTextField);
                if (firstTextField instanceof TextField) {
                    ((TextField) firstTextField).clear();
                }

                // Update the concatenated string to be displayed
                currentSelectedSentenceController.displayConcatenatedText(lastRemainingPanel);
            }
        }
    }


    @FXML
    private void onAddButtonClick() {
        // Create a new HBox instance from the provided FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sentence.fxml"));
        try {
            HBox sentencePanel = loader.load();
            SentenceController sentenceController = loader.getController();
            sentenceController.setMinibarController(this);
            sentenceController.setGlobalToggleGroup(globalToggleGroup);

            // Get access to the individual elements within the HBox
            RadioButton radioButton = (RadioButton) loader.getNamespace().get("radioButton1");
            radioButton.getStyleClass().add("my-radio-button");
            TextField textField = (TextField) loader.getNamespace().get("textField");
            textField.setId("textField"); // Set its ID
            //Button deleteButton = (Button) loader.getNamespace().get("deleteButton");

            // Set alignment to center the HBox within the tester VBox
            sentencePanel.setAlignment(Pos.CENTER);

            // Set margin to position the HBox within the tester VBox
            tester.setMargin(sentencePanel, new Insets(5, 0, 5, 4));



            // Assign a unique ID to the radio button
            radioButton.setId("radioButton" + radioButtonCounter);

            // Increment the counter for the next ID
            radioButtonCounter++;

            // Assign the radio button to the global ToggleGroup
            radioButton.setToggleGroup(globalToggleGroup);

            // Add the new HBox to the tester VBox
            tester.getChildren().add(sentencePanel);

            // Add the sentence panel to the list
            sentenceController.sentencePanelList.add(sentencePanel);

            // Set mapping between sentence panel and radio button
            sentenceController.setSentencePanelToRadioButtonMapping(sentencePanel, radioButton);

            sentenceController.setSentencePanelList(globalSentencePanelList);
            globalSentencePanelList.add(sentencePanel);

            System.out.println(globalSentencePanelList.size());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void populateWordsPane() {
        // Create a VBox to hold the numbers
        VBox numberContainer = new VBox();
        numberContainer.setSpacing(10); // Set spacing between numbers

        int maxNumbersPerRow = 50; // Maximum number of numbers per row
        int totalNumbers = 800; // Total number of numbers to display
        int currentNumber = 1;

        for (int row = 0; row < totalNumbers / maxNumbersPerRow; row++) {
            // Create an HBox for each row
            HBox rowContainer = new HBox();
            rowContainer.setSpacing(10); // Adjust spacing as needed

            for (int col = 0; col < maxNumbersPerRow; col++) {
                // Create a Label for each number
                Label numberLabel = new Label(Integer.toString(currentNumber));
                // Set the click event handler
                numberLabel.setOnMouseClicked(event -> {
                    System.out.println(numberLabel.getText());
                    if (currentSelectedSentenceController != null) {
                        currentSelectedSentenceController.populateTextField(numberLabel.getText());
                    }
                });
                numberLabel.setStyle("-fx-border-color: black;"); // Add border for better visualization
                rowContainer.getChildren().add(numberLabel);

                currentNumber++;

                if (currentNumber > totalNumbers) {
                    break; // All numbers added
                }
            }

            // Add the row to the VBox
            numberContainer.getChildren().add(rowContainer);
        }

        // Set the content of the wordsPane to the VBox
        wordsPane.setContent(numberContainer);
    }

    @FXML
    private void onUploadButtonClick(){
        // Create a FileChooser object
        FileChooser fileChooser = new FileChooser();

        // Optional: Set extension filters
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog and store user-selected file in a File object
        File file = fileChooser.showOpenDialog(null);

        // Do something with the file (e.g., read it, display it, etc.)
        if(file != null){
            // Logic for handling the selected file
            System.out.println("Selected file: " + file.getAbsolutePath());
        }
    }

    @FXML
    private Label concatLabel; // fx:id="concatLabel" in MiniBar.fxml

    public void setConcatLabel(String text) {
        if (concatLabel != null) {
            concatLabel.setText(text);
        } else {
            System.out.println("concatLabel is null");
        }
    }

    @FXML
    private void onRemoveButtonClick() {
        if (currentSelectedSentenceController != null) {
            // Get the selected HBox from the current SentenceController
            HBox selectedPanel = currentSelectedSentenceController.getSelectedPanel();

            // Remove the associated RadioButton from the ToggleGroup
            RadioButton radioButton = currentSelectedSentenceController.getSentencePanelToRadioButtonMap().get(selectedPanel);
            if (radioButton != null) {
                radioButton.getToggleGroup().getToggles().remove(radioButton);
            }

            // Remove the selected HBox from the VBox
            tester.getChildren().remove(selectedPanel);

            // Remove the mappings and lists entries related to this sentencePanel
            currentSelectedSentenceController.getSentencePanelToRadioButtonMap().remove(selectedPanel);
            currentSelectedSentenceController.getSentencePanelList().remove(selectedPanel);

            // Optionally, reset the currentSelectedSentenceController
            currentSelectedSentenceController = null;
            setConcatLabel("");
        }
    }

}
