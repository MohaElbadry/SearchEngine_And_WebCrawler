package org.db;

import org.elbadry.SiteData;
import org.embeding.GenerateEmbeddings;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmbeddingStorageTest {

    public static void main(String[] args) throws IOException {
        EmbeddingStorageTest testRunner = new EmbeddingStorageTest();
//        testRunner.testEmbeddingsGenerationAndStorage();
        ElasticsearchService service = new ElasticsearchService();
        service.deleteData("my_index","#" );
        service.deleteData("my_index","https://geeksblabla.community/#projects" );
        service.deleteData("my_index","https://geeksblabla.community#footer" );
        service.deleteData("my_index","https://geeksblabla.community/about#team" );
    }

    void testEmbeddingsGenerationAndStorage() throws IOException {
        ElasticsearchService service = new ElasticsearchService();
        String testIndex = "test_embedding_storage_";
        String testId = "test_" + UUID.randomUUID().toString().substring(0, 8);

        try {
            String sampleText = "Testing embeddings storage.";
            List<List<Double>> embeddings = GenerateEmbeddings.getEmbeddings("nomic-embed-text", sampleText);
            SiteData siteData = new SiteData(testId, "Test Document", sampleText, embeddings);
            Map<String, Object> document = org.outils.ObjectToMapConverter.convertToMap(siteData);
            System.out.println("INT THE TRY");
            service.storeData(testId, testIndex, document);
            List<String> storedIds = service.getIDs(testIndex);
            System.out.println(storedIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            service.deleteListe(testIndex, "test_");
//        }
    }
}