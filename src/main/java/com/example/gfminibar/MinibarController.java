package com.example.gfminibar;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;


import java.io.File;
import java.io.IOException;
import java.util.*;

import javafx.stage.Stage;


public class MinibarController {

    // ---- Class Members and UI Controls ----
    // The FXML UI components are annotated with @FXML to link them to the corresponding UI elements
    @FXML
    private VBox tester;
    @FXML
    private VBox translationPane;
    @FXML
    private ComboBox<GrammarFile> grammarDropdown;
    @FXML
    private ComboBox<String> startcatDropdown;
    @FXML
    private  ComboBox<String> fromDropdown;
    @FXML
    private ComboBox<String> toDropdown;
    @FXML
    private TabPane translationTabPane;
    @FXML
    private ScrollPane wordsPane;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ProgressIndicator progressTranslateIndicator;
    @FXML
    private Button exportButton;

    // This ToggleGroup is used for managing radio buttons globally across different panels
    private ToggleGroup globalToggleGroup = new ToggleGroup();

    // A counter used for generating unique radio button IDs
    private int radioButtonCounter = 1;

    // A String to store current sentence
    private String sentence;

    // A List to store current translations
    static ArrayList<String> translations;

    // List to hold global sentence panels for easy management
    public List<HBox> globalSentencePanelList = new ArrayList<>();

    // An instance of a custom predictive word model for sentence suggestions
    PredictiveWordModel predictive = new PredictiveWordModel();

    // An instance of a custom PDF Formatter for exporting translations
    static PdfFormatter pdfFormatter = new PdfFormatter();

    // An instance of a custom class for grammar management
    @FXML
    GrammarManager gm = new GrammarManager();

    // A reference to the currently selected sentence controller
    private SentenceController currentSelectedSentenceController;

    // ---- Initialization Methods ----
    // Methods related to the initialization and setting up of UI components.
    /**
     * Sets the currently selected SentenceController.
     *
     * @param controller The SentenceController to set as the current selection.
     */
    public void setCurrentSelectedSentenceController(SentenceController controller) {
        // Assign the provided SentenceController to the currentSelectedSentenceController field
        this.currentSelectedSentenceController = controller;
    }


    // ---- UI Event Handlers ----
    // Methods that handle user interface actions like button clicks, dropdown selections, etc.

    /**
     * Handles the action when the "Clear" button is clicked.
     * It retains the first sentence panel while removing the rest.
     * Additionally, it resets the relevant UI components.
     */
    @FXML
    private void onClearButtonClick() {
        // ---- Retain the first panel and remove the rest ----
        int numberOfPanels = tester.getChildren().size();
        tester.getChildren().remove(1, numberOfPanels);

        // Reset the counter for the next RadioButton ID
        radioButtonCounter = 1;

        // ---- Manipulate the last remaining panel ----
        HBox lastRemainingPanel = (HBox) tester.getChildren().get(0);

        // Locate the wordBox within the panel
        HBox wordBox = (HBox) lastRemainingPanel.lookup("#wordBox");

        // Check if wordBox is valid and not empty
        if (wordBox != null && !wordBox.getChildren().isEmpty()) {
            clearAndResetWordBox(wordBox);

            // Update the concatenated string to be displayed
            currentSelectedSentenceController.displayConcatenatedText(lastRemainingPanel);
        }
    }

    /**
     * Handles the action when the "Add" button is clicked.
     * Adds a new sentence panel if all required fields are selected.
     */
    @FXML
    private void onAddButtonClick() {
        // ---- Validate Required Fields ----
        if (!areRequiredFieldsSelected()) {
            displayAlert("Missing Selections", "Please select grammar, category, and 'from' language first.");
            return;
        }

        // ---- Initialize Loader and Controllers ----
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sentence.fxml"));
        try {
            HBox sentencePanel = loader.load();
            SentenceController sentenceController = loader.getController();

            // ---- Setup Controller and Panel ----
            RadioButton radioButton = (RadioButton) loader.getNamespace().get("radioButton1");
            initializeSentenceController(sentenceController, sentencePanel, loader, radioButton);

            // ---- Add Sentence Panel to List ----
            addToGlobalSentencePanelList(sentencePanel, sentenceController, radioButton);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the action when the "Remove" button is clicked.
     * Removes the currently selected sentence panel along with its associated data.
     */
    @FXML
    private void onRemoveButtonClick() {
        // ---- Validate if a Sentence Controller is Selected ----
        if (currentSelectedSentenceController == null) {
            return;
        }

        // ---- Retrieve and Validate the Selected Panel ----
        HBox selectedPanel = currentSelectedSentenceController.getSelectedSentencePanel();
        if (selectedPanel == null) {
            return;
        }

        // ---- Remove RadioButton from Toggle Group ----
        removeRadioButtonFromToggleGroup(selectedPanel);

        // ---- Remove Panel from UI ----
        tester.getChildren().remove(selectedPanel);

        // ---- Update Data Structures ----
        updateDataStructures(selectedPanel);

        // ---- Reset and Translate ----
        currentSelectedSentenceController = null;
        translate("");
    }

    /**
     * Handles the action when the "Upload" button is clicked.
     * Opens a FileChooser dialog for the user to select a grammar file,
     * and then processes the selected file.
     */
    @FXML
    private void onUploadButtonClick() {
        // ---- Initialize FileChooser and Set Extension Filters ----
        FileChooser fileChooser = initializeFileChooser();

        // ---- Open File Dialog and Retrieve Selected File ----
        File selectedFile = fileChooser.showOpenDialog(null);

        // ---- Process Selected File ----
        if (selectedFile != null) {
            processSelectedGrammarFile(selectedFile);
        }
    }

    /**
     * Handles the action when the "Random" button is clicked.
     * Generates random words asynchronously and populates them into the UI.
     */
    @FXML
    public void onRandomButtonClick() {
        // Validate if a Sentence Controller is Selected
        if (currentSelectedSentenceController == null) return;
        HBox selectedSentencePanel = currentSelectedSentenceController.getSelectedSentencePanel();
        if (selectedSentencePanel == null) return;

        // Create and Start Asynchronous Task
        Task<String[]> task = createRandomWordsTask();
        configureTaskEvents(task, selectedSentencePanel);
        new Thread(task).start();
    }

    /**
     * Called when the export button is clicked.
     */
    @FXML
    public void onExportButtonClick(ActionEvent event) {
        // Get the Stage from the ActionEvent
        Stage stage = (Stage) exportButton.getScene().getWindow();

        // Initialize FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Translations");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        // Show the FileChooser dialog and get the user-selected File
        File file = fileChooser.showSaveDialog(stage);

        // Get selected language and translations
        String fromLanguage = fromDropdown.getSelectionModel().getSelectedItem();
        Map<String, List<String>> sortLang = sortLangs(translations);
        String text = sentence;

        // If the user selected a File, try to save the PDF
        if (file != null) {
            try {
                pdfFormatter.formatAndWriteTranslation(file, text, fromLanguage, sortLang);
            } catch (IOException e) {
                // Handle any IOExceptions
                e.printStackTrace();
            }
        }
    }





    /**
     * Handles the selection of a grammar file from the dropdown menu.
     */
    @FXML
    public void onSelectGrammar(ActionEvent actionEvent) {
        GrammarFile selectedGrammar = grammarDropdown.getSelectionModel().getSelectedItem();
        loadSelectedGrammar(selectedGrammar);
    }

    /**
     * Handles the selection of a start category.
     * Clears the interface upon selection.
     */
    @FXML
    public void onSelectStartCat(ActionEvent actionEvent){
        onClearButtonClick();
    }

    /**
     * Handles the selection of the 'from' language.
     * Clears the interface upon selection.
     */
    @FXML
    public void onSelectFrom(ActionEvent actionEvent){
        onClearButtonClick();
    }

    /**
     * Handles the selection of the 'to' language.
     * Updates the display of the concatenated text for the selected panel.
     */
    @FXML
    public void onSelectTo(ActionEvent actionEvent){
        if (currentSelectedSentenceController == null) return;
        HBox selectedPanel = currentSelectedSentenceController.getSelectedSentencePanel();
        if (selectedPanel == null) return;
        currentSelectedSentenceController.displayConcatenatedText(selectedPanel);
    }


    // ---- Helper Methods ----
    // Methods that assist UI Event Handlers, often containing the business logic.

    /**
     * Checks if all the required dropdowns (grammar, start category, and 'from' language)
     * have been selected.
     *
     * @return True if all required fields are selected, otherwise false.
     */
    private boolean areRequiredFieldsSelected() {
        // Check if a grammar is selected from the 'grammarDropdown'
        boolean isGrammarSelected = grammarDropdown.getSelectionModel().getSelectedItem() != null;

        // Check if a start category is selected from the 'startcatDropdown'
        boolean isStartCatSelected = startcatDropdown.getSelectionModel().getSelectedItem() != null;

        // Check if a 'from' language is selected from the 'fromDropdown'
        boolean isFromSelected = fromDropdown.getSelectionModel().getSelectedItem() != null;

        // Return true only if all required fields are selected
        return isGrammarSelected && isStartCatSelected && isFromSelected;
    }


    /**
     * Displays a warning alert dialog box with the given title and message.
     *
     * @param title The title of the alert dialog.
     * @param message The message to display in the alert dialog.
     */
    private void displayAlert(String title, String message) {
        // Create a new Alert of type WARNING
        Alert alert = new Alert(Alert.AlertType.WARNING);

        // Set the title of the Alert dialog
        alert.setTitle(title);

        // Disable the header text to make the dialog simpler
        alert.setHeaderText(null);

        // Set the content message of the Alert dialog
        alert.setContentText(message);

        // Display the Alert dialog and wait for user interaction
        alert.showAndWait();
    }


    /**
     * Generates an array of random words based on the selected 'from' language and start category.
     *
     * @return An array of random words, or null if any of the required selections are missing.
     */
    private String[] generateRandomWords() {
        // Retrieve selected 'from' language and start category
        String selectedFrom = fromDropdown.getSelectionModel().getSelectedItem();
        String selectedCat = startcatDropdown.getSelectionModel().getSelectedItem();

        // Validate that both 'from' language and start category are selected
        if (selectedFrom == null || selectedCat == null) {
            return null;
        }

        // Create a RandomGenerator instance and generate a random string
        RandomGenerator randomizer = new RandomGenerator(gm);
        String randomString = randomizer.generate(selectedFrom, selectedCat);

        // Split the random string into an array of words and return
        return randomString.split("\\s+");
    }


    /**
     * Clears and resets the provided wordBox.
     *
     * @param wordBox The HBox component that contains TextFields.
     */
    private void clearAndResetWordBox(HBox wordBox) {
        // Store the first TextField for later use
        Node firstTextField = wordBox.getChildren().get(0);

        // Clear all TextFields from the wordBox
        wordBox.getChildren().clear();

        // Update the preferred width of the wordBox
        double newWidth = 125;
        wordBox.setPrefWidth(newWidth);

        // Add back the first TextField and clear its text
        wordBox.getChildren().add(firstTextField);
        if (firstTextField instanceof TextField) {
            ((TextField) firstTextField).clear();
        }
    }


    /**
     * Initializes SentenceController and configures its associated UI components.
     *
     * @param sentenceController The SentenceController for the new sentence panel.
     * @param sentencePanel The HBox representing the new sentence panel.
     * @param loader The FXMLLoader used to load the sentence panel.
     * @param radioButton The RadioButton associated with the new sentence panel.
     */
    private void initializeSentenceController(SentenceController sentenceController, HBox sentencePanel, FXMLLoader loader, RadioButton radioButton) {
        sentenceController.setMinibarController(this);
        sentenceController.setGlobalToggleGroup(globalToggleGroup);

        // ---- Customize UI Components ----
        customizeRadioButton(radioButton);

        TextField textField = (TextField) loader.getNamespace().get("textField");
        customizeTextField(textField);

        // ---- Set Layout and Position ----
        sentencePanel.setAlignment(Pos.CENTER);
        tester.setMargin(sentencePanel, new Insets(5, 0, 5, 4));

        // ---- Add to UI ----
        tester.getChildren().add(sentencePanel);
    }


    /**
     * Customizes the RadioButton UI component.
     *
     * @param radioButton The RadioButton to customize.
     */
    private void customizeRadioButton(RadioButton radioButton) {
        radioButton.setId("radioButton" + radioButtonCounter);
        radioButton.getStyleClass().add("my-radio-button");
        radioButton.setToggleGroup(globalToggleGroup);
        radioButtonCounter++;
    }

    /**
     * Customizes the TextField UI component.
     *
     * @param textField The TextField to customize.
     */
    private void customizeTextField(TextField textField) {
        textField.setId("textField");
    }

    /**
     * Adds the sentence panel to the global list and sets its mapping.
     *
     * @param sentencePanel The HBox representing the new sentence panel.
     * @param sentenceController The SentenceController for the new sentence panel.
     * @param radioButton The RadioButton associated with the sentence panel.
     */
    private void addToGlobalSentencePanelList(HBox sentencePanel, SentenceController sentenceController, RadioButton radioButton) {
        sentenceController.sentencePanelList.add(sentencePanel);
        sentenceController.setSentencePanelToRadioButtonMapping(sentencePanel, radioButton);
        sentenceController.setSentencePanelList(globalSentencePanelList);
        globalSentencePanelList.add(sentencePanel);
    }

    /**
     * Removes the RadioButton associated with the provided panel from its ToggleGroup.
     *
     * @param selectedPanel The HBox representing the selected sentence panel.
     */
    private void removeRadioButtonFromToggleGroup(HBox selectedPanel) {
        RadioButton radioButton = currentSelectedSentenceController.getSentencePanelToRadioButtonMap().get(selectedPanel);
        if (radioButton != null) {
            radioButton.getToggleGroup().getToggles().remove(radioButton);
        }
    }

    /**
     * Updates the data structures related to the provided panel.
     *
     * @param selectedPanel The HBox representing the selected sentence panel.
     */
    private void updateDataStructures(HBox selectedPanel) {
        currentSelectedSentenceController.getSentencePanelToRadioButtonMap().remove(selectedPanel);
        currentSelectedSentenceController.getSentencePanelList().remove(selectedPanel);
    }


    /**
     * Initializes the FileChooser and sets its extension filters.
     *
     * @return Initialized FileChooser.
     */
    private FileChooser initializeFileChooser() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text files (*.pgf)", "*.pgf");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

    /**
     * Processes the selected grammar file, updating the grammar dropdown and triggering loading.
     *
     * @param file The selected grammar File object.
     */
    private void processSelectedGrammarFile(File file) {
        // Create and Load New Grammar
        GrammarFile newGrammar = new GrammarFile(file.getName(), file.getAbsolutePath());
        loadSelectedGrammar(newGrammar);

        // Update Grammar Dropdown
        updateGrammarDropdown(newGrammar);
    }

    /**
     * Adds the new grammar to the dropdown and selects it.
     *
     * @param newGrammar The newly added GrammarFile object.
     */
    private void updateGrammarDropdown(GrammarFile newGrammar) {
        grammarDropdown.getItems().add(newGrammar);
        grammarDropdown.getSelectionModel().select(newGrammar);
    }

    /**
     * Configures the events and UI updates for the given task.
     *
     * @param task The task to configure.
     * @param selectedSentencePanel The panel where the words will be populated.
     */
    private void configureTaskEvents(Task<String[]> task, HBox selectedSentencePanel) {
        // Message Handling
        task.messageProperty().addListener((observable, oldValue, newValue) -> {
            // Handle messages (e.g., update a status bar)
        });

        // Progress Indicator Handling
        task.stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                progressTranslateIndicator.setVisible(true);
                progressTranslateIndicator.setProgress(-1);
            } else {
                progressTranslateIndicator.setProgress(0);
                progressTranslateIndicator.setVisible(false);
            }
        });

        // Success Handling
        task.setOnSucceeded(event -> {
            String[] words = task.getValue();
            if (words != null) {
                populateTextFields(words, selectedSentencePanel);
                currentSelectedSentenceController.displayConcatenatedText(selectedSentencePanel);
            }
        });

        // Failure Handling
        task.setOnFailed(event -> handleTaskFailure(task));
    }

    /**
     * Handles the failure of a task by logging or displaying an error message.
     *
     * @param task The task that has failed.
     */
    private void handleTaskFailure(Task<String[]> task) {
        Throwable e = task.getException();
        if (e != null) {
            e.printStackTrace(); // Or show a dialog, or both
        }
    }

    /**
     * Finds the appropriate VBox to add the translation panel to, based on the language name.
     *
     * @param langName The name of the language.
     * @return The target VBox for adding the translation panel, or null if not found.
     */
    private VBox findTargetVBox(String langName) {
        String selectedItem = toDropdown.getSelectionModel().getSelectedItem();
        String tabName = "All".equals(selectedItem) ? langName : "Default";

        // Find the target tab with the specified name
        Tab targetTab = translationTabPane.getTabs().stream()
                .filter(tab -> tabName.equals(tab.getText()))
                .findFirst()
                .orElse(null);

        // Retrieve the target VBox from the tab, if found
        if (targetTab != null) {
            ScrollPane scrollPane = (ScrollPane) targetTab.getContent();
            return (VBox) scrollPane.getContent();
        }

        return null;  // Tab not found
    }

    /**
     * Creates a Task to populate words asynchronously.
     *
     * @param text The text based on which the pane will be populated
     * @return Configured Task object for the operation
     */
    private Task<List<String>> createPopulateWordsTask(String text) {
        return new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                String selectedFrom = fromDropdown.getSelectionModel().getSelectedItem();
                String selectedCat = startcatDropdown.getSelectionModel().getSelectedItem();
                return gm.loadWords(selectedFrom, selectedCat, text);
            }
        };
    }

    /**
     * Attaches listeners to handle Task state changes, such as starting, running, and ending.
     *
     * @param task The Task whose state needs to be monitored
     */
    private void attachTaskStateListeners(Task<?> task) {
        task.stateProperty().addListener((observable, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                //Indicate progress while task is running
                progressTranslateIndicator.setVisible(true);
                progressTranslateIndicator.setProgress(-1);  // Indeterminate progress
            } else if (newState == Worker.State.SUCCEEDED || newState == Worker.State.FAILED) {
                //Hide progress indicator when task completed
                progressTranslateIndicator.setProgress(0);
            }
        });
    }

    /**
     * Attaches handlers that execute upon the Task's successful completion or failure.
     *
     * @param task The Task whose completion needs to be handled
     * @param text The text based on which the pane will be populated
     */
    private void attachPopulateWordsCompletionHandlers(Task<List<String>> task, String text) {
        task.setOnSucceeded(e -> handleSuccessfulWordPopulation(task.getValue(), text));
        task.setOnFailed(e -> handleFailedWordPopulation(e.getSource().getException()));
    }


    /**
     * Handles the UI updates when the word population fails.
     *
     * @param exception The exception thrown during the operation
     */
    private void handleFailedWordPopulation(Throwable exception) {
        // Log the exception for debugging
        exception.printStackTrace();

        // Display an error message or some other UI indication
        // For example, you can show an alert dialog
        displayAlert("Error", "Failed to populate words");
    }

    /**
     * Creates a Task object for asynchronous translation.
     *
     * @param text Text to translate
     * @return Task configured for translation
     */
    private Task<ArrayList<String>> createTranslationTask(String text) {
        return new Task<ArrayList<String>>() {
            @Override
            protected ArrayList<String> call() throws Exception {
                String selectedFrom = fromDropdown.getSelectionModel().getSelectedItem();
                String selectedTo = toDropdown.getSelectionModel().getSelectedItem();
                translations = (ArrayList<String>) gm.getTranslation(text, selectedFrom, selectedTo);
                return translations;
            }
        };
    }

    /**
     * Attach handlers to manage UI and data post successful or failed translation.
     *
     * @param task Translation task
     * @param text Original text that was translated
     */
    private void attachTranslationCompletionHandlers(Task<ArrayList<String>> task, String text) {
        task.setOnSucceeded(e -> handleSuccessfulTranslation(task.getValue(), text));
        task.setOnFailed(e -> handleFailedTranslation(e.getSource().getException()));
    }

    /**
     * Handles the UI updates when the translation task fails.
     *
     * @param exception The exception that occurred during the translation
     */
    private void handleFailedTranslation(Throwable exception) {
        // Log the exception for debugging purposes
        exception.printStackTrace();

        // Update the UI to reflect the failure
        Platform.runLater(() -> addTranslationPanel("Translation Not Available", "Default"));

        // Hide progress indicator
        progressTranslateIndicator.setVisible(false);
    }


    // ---- Asynchronous Methods ----
    // Methods that run asynchronous tasks, often for loading data or performing heavy computations.

    /**
     * Loads the selected grammar in a separate thread to keep the UI responsive.
     *
     * @param grammar The selected grammar to load.
     */
    public void loadSelectedGrammar(GrammarFile grammar) {
        // Run the loading process in a separate thread
        new Thread(() -> {
            // Show and set the ProgressIndicator to an indeterminate state on the JavaFX Application Thread
            Platform.runLater(() -> {
                progressIndicator.setVisible(true);  // Show ProgressIndicator
                progressIndicator.setProgress(-1);  // Set to indeterminate state
            });

            if (grammar != null) {
                String fileName = grammar.getName();
                String filePath = grammar.getPath();

                // Load the grammar using GrammarManager
                gm.loadGrammar(fileName, filePath);
            }

            // Hide the ProgressIndicator once the loading is done, on the JavaFX Application Thread
            Platform.runLater(() -> {
                // Populate UI components based on the loaded grammar
                populateStartCat(gm);
                populateLanguages(gm);

                // Hide the ProgressIndicator
                progressIndicator.setVisible(false);
            });
        }).start();
    }


    /**
     * Asynchronously populates the words pane based on the selected categories and suggestions.
     *
     * @param text The text based on which the pane is populated
     */
    public void populateWordsPane(String text) {
        // Create and configure the task for populating words
        Task<List<String>> task = createPopulateWordsTask(text);

        // Attach state listeners to manage UI during the task execution
        attachTaskStateListeners(task);

        // Attach post-execution behavior for task
        attachPopulateWordsCompletionHandlers(task, text);

        // Start the task in a new thread
        new Thread(task).start();
    }

    /**
     * Asynchronously translates a given text and updates the UI.
     *
     * @param text Text to be translated
     */
    public void translate(String text) {
        sentence = text;
        // Create and configure the task for translation
        Task<ArrayList<String>> task = createTranslationTask(text);

        // Attach state listeners to manage UI during the task execution
        attachTaskStateListeners(task);

        // Attach post-execution behavior for the task
        attachTranslationCompletionHandlers(task, text);

        // Start the task in a new thread
        new Thread(task).start();
    }

    /**
     * Creates a task for generating random words.
     *
     * @return Task that generates random words.
     */
    private Task<String[]> createRandomWordsTask() {
        return new Task<String[]>() {
            @Override
            protected String[] call() {
                updateMessage("Generating random words...");
                String[] words = generateRandomWords(); // Blocking method
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    return null;
                }
                updateMessage("Completed");
                return words;
            }
        };
    }


    // ---- UI Update Methods ----
    // Methods specifically related to updating UI components based on changes to data or state.

    /**
     * Adds a list of words as labels to a given VBox, wrapped into rows based on a maximum width.
     *
     * @param wordList List of words to add.
     * @param style CSS style for each label.
     * @param wordContainer The VBox to which the words should be added.
     * @param maxWidth Maximum width for each row of words.
     */
    private void addWordsToContainer(List<String> wordList, String style, VBox wordContainer, double maxWidth) {
        // Initialize variables
        double currentWidth = 0;
        HBox rowContainer = createNewRowContainer();

        for (String word : wordList) {
            // Create and style a new label for the word
            Label wordLabel = createStyledLabel(word, style);

            // Update current row width
            double labelWidth = word.length() * 10;  // Approximate width calculation
            currentWidth += labelWidth + 10;

            // Check if adding this word would exceed the max row width
            if (currentWidth > maxWidth - 10) {
                // Add the current row to the VBox and create a new row
                wordContainer.getChildren().add(rowContainer);
                rowContainer = createNewRowContainer();
                currentWidth = labelWidth + 10;
            }

            // Add the word label to the current row
            rowContainer.getChildren().add(wordLabel);
        }

        // Add the final row to the VBox if it contains any labels
        if (!rowContainer.getChildren().isEmpty()) {
            wordContainer.getChildren().add(rowContainer);
        }
    }

    /**
     * Creates a new styled label for a given word.
     *
     * @param word The word to display.
     * @param style The CSS style for the label.
     * @return A new styled Label object.
     */
    private Label createStyledLabel(String word, String style) {
        Label label = new Label(word);
        label.setStyle(style);
        label.setPadding(new Insets(5, 5, 5, 5));
        label.setOnMouseClicked(event -> {
            if (currentSelectedSentenceController != null) {
                currentSelectedSentenceController.populateTextField(label.getText());
            }
        });
        return label;
    }

    /**
     * Creates a new row container (HBox) for holding word labels.
     *
     * @return A new HBox object with predefined settings.
     */
    private HBox createNewRowContainer() {
        HBox hbox = new HBox(10);
        hbox.getStyleClass().add("word-row-container");
        return hbox;
    }


    /**
     * Populates the 'startcatDropdown' with categories retrieved from the given GrammarManager.
     * Automatically selects the first category in the list.
     *
     * @param gm The GrammarManager object containing the categories.
     */
    public void populateStartCat(GrammarManager gm) {
        // Retrieve the list of categories from the GrammarManager
        List<String> categories = gm.getCategories();

        // Populate the 'startcatDropdown' with the retrieved categories
        startcatDropdown.setItems(FXCollections.observableArrayList(categories));

        // Automatically select the first category in the dropdown
        startcatDropdown.getSelectionModel().selectFirst();
    }


    /**
     * Populates the 'fromDropdown' and 'toDropdown' with languages retrieved from
     * the given GrammarManager. Automatically selects the first language in both dropdowns.
     *
     * @param gm The GrammarManager object containing the languages.
     */
    public void populateLanguages(GrammarManager gm) {
        // Retrieve the list of languages from the GrammarManager
        List<String> langs = gm.getLanguages();

        // Populate the 'fromDropdown' with the retrieved languages
        fromDropdown.setItems(FXCollections.observableArrayList(langs));

        // Create a new list for 'toDropdown' and add "All" as the first item
        List<String> toLangs = new ArrayList<>(langs);
        toLangs.add(0, "All");

        // Populate the 'toDropdown' with the new list
        toDropdown.setItems(FXCollections.observableArrayList(toLangs));

        // Automatically select the first item in both dropdowns
        fromDropdown.getSelectionModel().selectFirst();
        toDropdown.getSelectionModel().selectFirst();
    }


    /**
     * Populates the 'wordBox' HBox within the selected sentence panel with text fields,
     * each containing one of the given words.
     *
     * @param words An array of words to populate the text fields with.
     * @param selectedSentencePanel The HBox containing the 'wordBox' to populate.
     */
    private void populateTextFields(String[] words, HBox selectedSentencePanel) {
        // Look for the 'wordBox' HBox within the selected sentence panel
        HBox wordBox = (HBox) selectedSentencePanel.lookup("#wordBox");

        // Clear any existing children from the 'wordBox'
        wordBox.getChildren().clear();

        // Populate the 'wordBox' with text fields, each containing one of the given words
        for (String word : words) {
            wordBox.getChildren().add(createWordTextField(word));
        }
    }


    /**
     * Adds a translation panel to the appropriate tab in the translationTabPane.
     * The panel is populated with translations for the given language text and name.
     *
     * @param langText The language text to display.
     * @param langName The name of the language.
     */
    public void addTranslationPanel(String langText, String langName) {
        try {
            // Load the translation panel from the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("translation.fxml"));
            HBox translationPanel = loader.load();

            // Retrieve the controller and set the language labels
            TranslationController translationController = loader.getController();
            translationController.getLangLabel().setText(langText);
            translationController.getLangNameLabel().setText(langName);

            // Center-align the language label within the panel
            Label langLabel = translationController.getLangLabel();
            Pane pane = translationController.getTranslationPanel();
            langLabel.layoutXProperty().bind(pane.widthProperty().subtract(langLabel.widthProperty()).divide(2));

            // Determine the target VBox for adding the translation panel
            VBox targetVBox = findTargetVBox(langName);
            if (targetVBox == null) {
                return;  // Target tab not found
            }

            // Position and add the translation panel to the target VBox
            targetVBox.setMargin(translationPanel, new Insets(5, 0, 5, 4));
            targetVBox.getChildren().add(translationPanel);

        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception (e.g., display an error dialog)
        }
    }

    /**
     * Adds a new Tab to the 'translationTabPane' with the given language name.
     * The Tab contains a VBox wrapped in a ScrollPane.
     *
     * @param langName The name of the language for the new Tab.
     */
    public void addTranslationTabPane(String langName) {
        // Create a new Tab with the given language name
        Tab tab = new Tab(langName);

        // Create a ScrollPane and set its style
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: #819fa7;");

        // Create a VBox and set it as the content of the ScrollPane
        VBox vbox = new VBox();
        scrollPane.setContent(vbox);

        // Set the ScrollPane as the content of the Tab
        tab.setContent(scrollPane);

        // Add the new Tab to the 'translationTabPane'
        translationTabPane.getTabs().add(tab);
    }


    /**
     * Creates a new TextField with the given word as its content and applies
     * standard styling and margins to it.
     *
     * @param word The word to set as the TextField's content.
     * @return A styled TextField containing the given word.
     */
    private TextField createWordTextField(String word) {
        // Create a new TextField and set its content to the given word
        TextField newTextField = new TextField(word);

        // Set the dimensions for the TextField
        final double FIXED_WIDTH = 63;
        newTextField.setId("textField");
        newTextField.setPrefWidth(FIXED_WIDTH);
        newTextField.setMinWidth(FIXED_WIDTH);
        newTextField.setMaxWidth(FIXED_WIDTH);

        // Set the margin for the new TextField within its container
        HBox.setMargin(newTextField, new Insets(0, 0, 0, 5));

        return newTextField;
    }

    /**
     * Handles the UI updates when the word population is successful.
     *
     * @param words The list of words
     * @param text The text based on which the pane was populated
     */
    private void handleSuccessfulWordPopulation(List<String> words, String text) {
        // Get predictive words and handle possible null value
        List<String> suggestedWords = predictive.predictWordsForIncompleteSentence(text);
        Set<String> suggestedWordsSet = suggestedWords != null ? new HashSet<>(suggestedWords) : Collections.emptySet();

        // Partition words into final suggested words and other words
        List<String> finalSuggestedWords = new ArrayList<>();
        List<String> otherWords = new ArrayList<>();
        for (String word : words) {
            if (suggestedWordsSet.contains(word)) {
                finalSuggestedWords.add(word);
            } else {
                otherWords.add(word);
            }
        }

        // Create containers for the words
        double maxWidth = wordsPane.getWidth();
        VBox wordContainer = new VBox(10);
        wordContainer.getStyleClass().add("word-row-container");

        // Add suggested words first, if any
        if (!finalSuggestedWords.isEmpty()) {
            addWordsToContainer(finalSuggestedWords, "-fx-border-color: black; -fx-background-color: #b7d7e7; -fx-text-fill: black;", wordContainer, maxWidth);
        }

        // Add other words
        addWordsToContainer(otherWords, "-fx-border-color: black; -fx-background-color: #f2efea; -fx-text-fill: black;", wordContainer, maxWidth);

        // Set the content of the wordsPane to the VBox
        wordsPane.setContent(wordContainer);
    }

    /**
     * Handles the UI updates when the translation is successful.
     *
     * @param translations The list of translations
     * @param originalText The original text that was to be translated
     */
    private void handleSuccessfulTranslation(ArrayList<String> translations, String originalText) {
        // Set progress indicator to indeterminate state while updating the UI
        Platform.runLater(() -> {
            progressTranslateIndicator.setVisible(true);
            progressTranslateIndicator.setProgress(-1);
        });

        // Clear any existing translation panels
        translationTabPane.getTabs().clear();

        // If translations are not null, proceed to display them
        if (translations != null) {
            predictive.addSentenceToCorpus(originalText);
            String selectedTo = toDropdown.getSelectionModel().getSelectedItem();

            // Check if translations for all languages are to be displayed
            if ("All".equals(selectedTo)) {
                Map<String, List<String>> sortedLangs = sortLangs(translations);

                for (String key : sortedLangs.keySet()) {
                    List<String> items = getItemsForKey(key, sortedLangs);
                    addTranslationTabPane(key);
                    for (String item : items) {
                        addTranslationPanel(item, key);
                    }
                }
            } else {
                addTranslationTabPane("Default");
                for (String item : translations) {
                    int index = item.indexOf("=");
                    if (index != -1 && index < item.length() - 1) {
                        String textAfter = item.substring(index + 1).trim();
                        addTranslationPanel(textAfter, "");
                    }
                }
            }
        } else {
            addTranslationTabPane("Default");
            Platform.runLater(() -> addTranslationPanel("Translation Not Available", "Default"));
        }

        // Once all UI updates are done, set progress indicator to complete
        Platform.runLater(() -> {
            progressTranslateIndicator.setProgress(1);
            progressTranslateIndicator.setVisible(false);
        });
    }




    // ---- Static Utility Methods ----
    // Static methods that perform general utilities not tied to instance state.

    /**
     * Sorts a list of strings into a map based on the prefix before the " = " delimiter.
     *
     * @param originalList The original list of strings to be sorted.
     * @return A map where each key is a prefix, and the corresponding value is a list of strings sharing that prefix.
     */
    public static Map<String, List<String>> sortLangs(List<String> originalList) {
        // Initialize a map to hold the grouped strings
        Map<String, List<String>> groupedMap = new HashMap<>();

        // Iterate through the original list of strings
        for (String item : originalList) {
            // Find the position of the " = " delimiter
            int equalPos = item.indexOf(" = ");

            // If the delimiter exists, proceed with grouping
            if (equalPos != -1) {
                // Extract the prefix and the value based on the position of " = "
                String prefix = item.substring(0, equalPos);
                String value = item.substring(equalPos + 3);

                // Add the value to the appropriate group in the map
                groupedMap.computeIfAbsent(prefix, k -> new ArrayList<>()).add(value);
            }
        }

        return groupedMap;
    }


    /**
     * Retrieves a list of items for a given key from a map. If the key does not exist,
     * an empty list is returned.
     *
     * @param key The key to look up in the map.
     * @param map The map containing lists of items keyed by strings.
     * @return A list of items for the given key, or an empty list if the key doesn't exist.
     */
    public static List<String> getItemsForKey(String key, Map<String, List<String>> map) {
        // Retrieve the list of items for the given key, or return an empty list if the key doesn't exist
        return map.getOrDefault(key, Collections.emptyList());
    }

}
