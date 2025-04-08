package org.outils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ElasticsearchHealthCheck {

    private static final String HOST = "localhost";
    private static final int PORT = 9200;

    public static void elasticsearchHealthCheck() {
        if (!isPortOpen()) restartElasticsearch();
    }

    public static boolean isPortOpen() {
        try (Socket socket = new Socket(HOST, PORT)) {
            return true;
        } catch (IOException e) {
            System.out.println("❌ Port " + PORT + " is closed or Elasticsearch is down.");
            return false;
        }
    }
    /* this is a simple way to restart Elasticsearch,
     * Option 1: Run docker-compose
     * Option 2: Run a shell script
     */
    public static void restartElasticsearch() {
        try {
            System.out.println("🔄 Restarting Elasticsearch...");
            InputStream inputStream = ElasticsearchHealthCheck.class.getResourceAsStream("/start.sh");
            File tempScript = File.createTempFile("start", ".sh");
            Files.copy(inputStream, tempScript.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempScript.setExecutable(true);
            ProcessBuilder processBuilder = new ProcessBuilder(tempScript.getAbsolutePath());
            Process process = processBuilder.start();
            process.waitFor();
            tempScript.delete();
            System.out.println("✅ Elasticsearch restarted.");
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error restarting Elasticsearch: " + e.getMessage());
        }
    }
}