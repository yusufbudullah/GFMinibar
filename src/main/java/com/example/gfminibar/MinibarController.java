package com.example.gfminibar;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

import org.grammaticalframework.pgf.*;


public class MinibarController {

    @FXML
    private VBox tester;
    @FXML
    private VBox translationPane;
    @FXML
    private ComboBox<GrammarFile> grammarDropdown;
    @FXML
    private ComboBox<String> startcatDropdown;
    @FXML
    private ComboBox<String> fromDropdown;
    @FXML
    private ComboBox<String> toDropdown;

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
    public void addTranslationPanel(String langText) {
        try {
            // Load the Translation panel
            FXMLLoader loader = new FXMLLoader(getClass().getResource("translation.fxml"));
            HBox translationPanel = loader.load();

            // Get the controller and set data
            TranslationController translationController = loader.getController();
            translationController.getLangLabel().setText(langText);

            // Set alignment to center the HBox within the tester VBox
            translationPanel.setAlignment(Pos.CENTER);

            // Set margin to position the HBox within the tester VBox
            translationPane.setMargin(translationPanel, new Insets(5, 0, 5, 4));

            // Add the panel to the translationPane VBox
            translationPane.getChildren().add(translationPanel);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., show an error dialog)
        }
    }

    @FXML
    public void clearTranslationPanels() {
        translationPane.getChildren().clear();
    }

    @FXML
    GrammarManager gm = new GrammarManager();



    @FXML
    void populateWordsPane(String text) {
        // Create a VBox to hold the words
        VBox wordContainer = new VBox();
        wordContainer.setSpacing(10); // Set spacing between rows
        String selectedFrom = fromDropdown.getSelectionModel().getSelectedItem();
        String selectedCat = startcatDropdown.getSelectionModel().getSelectedItem();



        List<String> words = gm.loadWords(selectedFrom, selectedCat, text); //list of words

        double maxWidth = wordsPane.getWidth(); // Get the width of the wordsPane
        double currentWidth = 0; // Track the current width of the row

        // Create an HBox for each row
        HBox rowContainer = new HBox();
        rowContainer.setSpacing(10); // Adjust spacing as needed
        rowContainer.getStyleClass().add("word-row-container");

        for (String word : words) {
            Label wordLabel = new Label(word);

            wordLabel.setOnMouseClicked(event -> {
                System.out.println(wordLabel.getText());
                if (currentSelectedSentenceController != null) {
                    currentSelectedSentenceController.populateTextField(wordLabel.getText());
                }
            });

            wordLabel.setStyle("-fx-border-color: black;"); // Add border for better visualization
            wordLabel.setStyle("-fx-background-color:  #f2efea");
            wordLabel.setPadding(new Insets(5, 5, 5, 5));

            // Estimate the width that this label will take (this is a rough estimate)
            double labelWidth = word.length() * 10;  // Assuming each character is around 7 pixels wide

            if (currentWidth + labelWidth > maxWidth-10) {
                // This label won't fit, need a new row
                wordContainer.getChildren().add(rowContainer);
                rowContainer = new HBox();
                rowContainer.setSpacing(10);
                currentWidth = 0;
            }

            rowContainer.getChildren().add(wordLabel);
            currentWidth += labelWidth + 10; // Adding 10 for spacing
        }

        // Add any remaining words in the last row
        if (!rowContainer.getChildren().isEmpty()) {
            wordContainer.getChildren().add(rowContainer);
        }

        // Set the content of the wordsPane to the VBox
        wordsPane.setContent(wordContainer);
    }





    @FXML
    private void onUploadButtonClick(){
        // Create a FileChooser object
        FileChooser fileChooser = new FileChooser();

        // Optional: Set extension filters
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files (*.pgf)", "*.pgf");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog and store user-selected file in a File object
        File file = fileChooser.showOpenDialog(null);

        // Do something with the file (e.g., read it, display it, etc.)
        if(file != null){
            // Logic for handling the selected file
            ObservableList<GrammarFile> grammars = FXCollections.observableArrayList();
            System.out.println("Selected file: " + file.getAbsolutePath());
            System.out.println("Selected file: " + file.getName());
            GrammarFile newGrammar = new GrammarFile(file.getName(), file.getAbsolutePath());
            grammarDropdown.getItems().add(newGrammar);
            //grammarDropdown.getSelectionModel().select(newGrammar);  // Automatically select the new item
        }
    }

    public void onSelectGrammar(ActionEvent actionEvent) {
        //GrammarManager gm = new GrammarManager();
        GrammarFile selected = grammarDropdown.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String fileName = selected.getName();
            String filePath = selected.getPath();
            gm.loadGrammar(fileName, filePath);
            //System.out.println(gm.getCategories());
            populateStartCat(gm);
            populateLanguages(gm);

        }
    }

    public void onSelectFrom(ActionEvent actionEvent){
        onClearButtonClick();
    }

    public void onSelectTo(ActionEvent actionEvent){
        HBox selectedPanel = currentSelectedSentenceController.getSelectedSentencePanel();
        //Update the concatenated string to be displayed
        currentSelectedSentenceController.displayConcatenatedText(selectedPanel);

    }


    public void populateStartCat(GrammarManager gm){
        List<String> categories = gm.getCategories();
        startcatDropdown.setItems(FXCollections.observableArrayList(categories));
    }

    public void populateLanguages(GrammarManager gm){
        //populate from dropdown
        System.out.println(gm.getLanguages());
        List<String> langs = gm.getLanguages();
        fromDropdown.setItems(FXCollections.observableArrayList(langs));
        //populate to dropdown
        toDropdown.setItems(FXCollections.observableArrayList(langs));

    }



    @FXML
    private Label concatLabel; // fx:id="concatLabel" in MiniBar.fxml

    public void translate(String text) {
        String selectedFrom = fromDropdown.getSelectionModel().getSelectedItem();
        String selectedTo = toDropdown.getSelectionModel().getSelectedItem();
        //System.out.println(selectedFrom+":"+selectedTo);
        //String text2 = "he can buy water at a pub";
        //System.out.println(text2);
        try{
            ArrayList<String> translations = (ArrayList<String>) gm.getTranslation(text,selectedFrom, selectedTo);
            clearTranslationPanels();
            for (String item : translations) {
                int index = item.indexOf("=");  // Find the index of '='

                if (index != -1 && index < item.length() - 1) {
                    String textAfter = item.substring(index + 1).trim();
                    System.out.println(textAfter);
                    addTranslationPanel(textAfter);
                }
            }
            //System.out.println(sentence);
        }catch(Exception e){
            e.printStackTrace();
        }
        /*if (concatLabel != null) {
            concatLabel.setText(sentence.toString());
        } else {
            System.out.println("concatLabel is null");
        }*/
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
            translate("");
        }
    }

    public void onRandomButtonClick() {
        // Create a new RandomGenerator instance (you might need to pass necessary arguments to its constructor)
        RandomGenerator randomizer = new RandomGenerator(gm);

        // Generate a random string using the RandomGenerator
        String selectedFrom = fromDropdown.getSelectionModel().getSelectedItem();
        String selectedCat = startcatDropdown.getSelectionModel().getSelectedItem();
        String randomString = randomizer.generate(selectedFrom, selectedCat);

        // Split the random string into individual words
        String[] words = randomString.split("\\s+");

        if (currentSelectedSentenceController == null) {
            return;
        }

        // Get the selected sentencePanel from the current SentenceController
        HBox selectedSentencePanel = currentSelectedSentenceController.getSelectedSentencePanel();
        if (selectedSentencePanel == null) {
            return;
        }

        // Clear existing TextFields
        HBox wordBox = (HBox) selectedSentencePanel.lookup("#wordBox");
        wordBox.getChildren().clear();

        // Populate TextFields in the selected sentencePanel
        for (String word : words) {
            TextField newTextField = new TextField(word);
            newTextField.setId("textField");
            newTextField.setPrefWidth(63);
            // Add your necessary configurations like setting up KeyListeners, etc.

            HBox.setMargin(newTextField, new Insets(0, 0, 0, 5));
            wordBox.getChildren().add(newTextField);
        }

        // Update concatenated text, translation, or any other UI elements
        currentSelectedSentenceController.displayConcatenatedText(selectedSentencePanel);
    }



}
