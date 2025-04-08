package org.embeding;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.io.InputStream;

public class GenerateEmbeddings {
    private static final int MAX_SIZE = 5000; // Maximum size to take from text
    private static String OLLAMA_HOST = "http://localhost:11434"; // Default value
    private static OllamaAPI ollamaAPI;

    static {
        // Load configuration for Ollama endpoint from config.properties
        try {
            Properties props = new Properties();
            InputStream input = GenerateEmbeddings.class.getClassLoader()
                    .getResourceAsStream("config.properties");

            if (input != null) {
                props.load(input);
                String configuredHost = props.getProperty("ollama_host");
                if (configuredHost != null && !configuredHost.isEmpty()) {
                    OLLAMA_HOST = configuredHost;
                }
                input.close();
            }

            System.out.println("Connecting to Ollama at: " + OLLAMA_HOST);
            ollamaAPI = new OllamaAPI(OLLAMA_HOST);
        } catch (Exception e) {
            System.err.println("Error loading Ollama configuration: " + e.getMessage());
            ollamaAPI = new OllamaAPI(OLLAMA_HOST); // Fallback to default
        }
    }

    /**
     * Generate embeddings for text by only using the first MAX_SIZE characters
     *
     * @param model The embedding model name to use
     * @param text The text to embed
     * @return First embedding vector as a flat List<Double>
     */
    public static List<Double> getEmbeddings(String model, String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            // Limit text size to prevent tokens overflow
            String chunk = text.length() > MAX_SIZE ? text.substring(0, MAX_SIZE) : text;

            // Check if Ollama is reachable
            if (!checkOllamaAvailability()) {
                System.err.println("Ollama service is not reachable at " + OLLAMA_HOST);
                return Collections.emptyList();
            }

            OllamaEmbedResponseModel response = ollamaAPI.embed(model, Arrays.asList(chunk));
            if (response != null && !response.getEmbeddings().isEmpty()) {
                // Return just the first embedding vector (we only send one chunk)
                return response.getEmbeddings().get(0);
            }

            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error generating embeddings: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Check if the Ollama service is available
     *
     * @return true if Ollama is reachable, false otherwise
     */
    private static boolean checkOllamaAvailability() {
        try {
            return ollamaAPI.ping();
        } catch (Exception e) {
            System.err.println("Ollama health check failed: " + e.getMessage());
            return false;
        }
    }
}