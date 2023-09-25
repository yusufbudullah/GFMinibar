package com.example.gfminibar;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;


public class PdfFormatter {
    // DateFormat object to format the current date and time in the "yyyy/MM/dd HH:mm:ss" pattern
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    /**
     * Formats and writes the translation data to a PDF file.
     *
     * @param file         The file to write to.
     * @param sentence     The sentence being translated.
     * @param fromLanguage The source language for the translation.
     * @param sortLang     A map of target languages to their translations.
     * @throws IOException If an error occurs while writing to the file.
     */
    public static void formatAndWriteTranslation(File file, String sentence, String fromLanguage, Map<String, List<String>> sortLang) throws IOException {
        // Initialize PDDocument; load existing if the file already exists
        PDDocument document = file.exists() ? PDDocument.load(file) : new PDDocument();

        // Load a Unicode font from resources
        PDFont font = PDType0Font.load(document, new File("src/main/resources/com/example/gfminibar/ArialUnicodeMS.ttf"));

        try {
            // Initialize layout variables
            float margin = 50; // Margin space
            float yStart = 750; // Initial y-position at top of the page
            float pageWidth = 600; // Width of the page
            float yPosition = yStart; // Initialize yPosition to the starting point

            // Initialize a page and contentStream
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Center and write header
            contentStream.setFont(font, 18);
            float titleWidth = font.getStringWidth("Grammatical Framework Minibar") / 1000 * 18;
            float titlePosition = (pageWidth - titleWidth) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(titlePosition, yPosition);
            contentStream.showText("Grammatical Framework Minibar");
            contentStream.endText();

            // Center and write sub-header (Time)
            yPosition -= 30;
            contentStream.setFont(font, 12);
            String timeString = "Time: " + dateFormat.format(new Date());
            float timeWidth = font.getStringWidth(timeString) / 1000 * 12;
            float timePosition = (pageWidth - timeWidth) / 2;
            contentStream.beginText();
            contentStream.newLineAtOffset(timePosition, yPosition);
            contentStream.showText(timeString);
            contentStream.endText();

            // Write "From:" and the sentence
            yPosition -= 30;
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("From: " + fromLanguage);
            contentStream.endText();  // End the text mode for "From:"

            // Move down to write the sentence
            yPosition -= 20;
            contentStream.setFont(font, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition); // Adjust yPosition
            contentStream.showText(sentence);
            contentStream.endText();  // End the text mode for the sentence

            yPosition -= 40; // Move down to start the translations
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition); // Adjust yPosition
            contentStream.showText("To:");
            contentStream.endText();  // End the text mode for the sentence

            yPosition -= 20;

            // Loop through each language and its translations
            for (Map.Entry<String, List<String>> entry : sortLang.entrySet()) {
                String toLanguage = entry.getKey();
                List<String> translations = entry.getValue();

                // Check if we have reached the end of the page, and if so, create a new page
                if (yPosition < margin + margin) {
                    // Close the existing content stream, add new page, create new content stream
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);

                    yPosition = yStart; // Reset to top of page
                }

                // Write the 'To' language in bold
                contentStream.setFont(font, 12); // Set font size to 12 for the language
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition); // Move to correct position
                contentStream.showText(toLanguage); // Write language
                contentStream.endText();
                yPosition -= 20; // Move down for translations

                // Write the translations in regular font
                contentStream.setFont(font, 10); // Set font size to 10 for the translations
                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 20, yPosition); // Indent a bit and move down

                for (String translation : translations) {
                    contentStream.showText(translation); // Write translation
                    contentStream.newLineAtOffset(0, -14.5f); // Move down to next line
                    yPosition -= 14.5f; // Update yPosition
                }
                contentStream.endText(); // End the text writing operation
            }

            // Save the document
            contentStream.close(); // Always close the contentStream
            document.save(file); // Save the PDF to file

        } finally {
            // Close the document, always good to wrap in finally to ensure it gets done
            if (document != null) {
                document.close();
            }
        }
    }



}
