package org.embeding;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenerateEmbeddings {
    private static final int MAX_SIZE = 5000; // Maximum size to take from text

    /**
     * Generate embeddings for text by only using the first MAX_SIZE characters
     *
     * @param model The embedding model name to use
     * @param text The text to embed
     * @return List of embeddings
     */
    public static List<List<Double>> getEmbeddings(String model, String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        // Take just the first chunk of text
        String chunk = getFirstChunk(text, MAX_SIZE);

        // Create API instance
        OllamaAPI ollamaAPI = new OllamaAPI();

        try {
            // Get embeddings for just this chunk
            OllamaEmbedResponseModel response = ollamaAPI.embed(model, Arrays.asList(chunk));
            if (response != null) {
                return response.getEmbeddings();
            }
        } catch (OllamaBaseException | IOException | InterruptedException e) {
            System.err.println("Error generating embedding: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * Get first chunk of text up to maxSize, respecting word boundaries
     */
    private static String getFirstChunk(String text, int maxSize) {
        if (text.length() <= maxSize) {
            return text;
        }

        // Find last space within maxSize to avoid cutting words
        int end = maxSize;
        if (!Character.isWhitespace(text.charAt(end))) {
            int lastSpace = text.lastIndexOf(' ', end);
            if (lastSpace > 0) {
                end = lastSpace;
            }
        }

        return text.substring(0, end).trim();
    }
}