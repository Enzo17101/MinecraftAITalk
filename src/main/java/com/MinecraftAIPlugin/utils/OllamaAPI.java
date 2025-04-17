package com.MinecraftAIPlugin.utils;

import com.MinecraftAIPlugin.model.ConversationSession;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public class OllamaAPI {

    // Nouvelle méthode pour obtenir l'embedding
    public static float[] getEmbedding(String text) {
        try {
            URL url = new URL("http://localhost:11434/api/embeddings");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonInput = String.format("{\"model\": \"nous-hermes\", \"text\": \"%s\"}", text.replace("\"", "\\\""));
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                // Extrait les données d'embedding depuis la réponse
                String response = result.toString();
                int index = response.indexOf("\"embedding\":[");
                if (index >= 0) {
                    response = response.substring(index + 12);
                    int end = response.indexOf("]");
                    if (end > 0) {
                        String embeddingStr = response.substring(0, end);
                        String[] embeddingArray = embeddingStr.split(",");
                        float[] embedding = new float[embeddingArray.length];
                        for (int i = 0; i < embeddingArray.length; i++) {
                            embedding[i] = Float.parseFloat(embeddingArray[i]);
                        }
                        return embedding;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new float[0];  // Renvoie un tableau vide en cas d'erreur
    }

    public static String getResponseFromOllama(ConversationSession session, Logger logger) {
        try {
            // Ajout de l'embedding de la conversation
            float[] memoryEmbedding = session.getEmbeddingMemory();
            StringBuilder messagesJson = new StringBuilder("[");
            for (var msg : session.getMessages()) {
                messagesJson.append(String.format(
                        "{\"role\":\"%s\",\"content\":\"%s\"},",
                        msg.role,
                        msg.content.replace("\"", "\\\"")
                ));
            }
            if (messagesJson.charAt(messagesJson.length() - 1) == ',') {
                messagesJson.deleteCharAt(messagesJson.length() - 1);
            }
            messagesJson.append("]");

            String jsonInput = String.format(
                    "{\"model\": \"nous-hermes\", \"messages\": %s, \"embedding\": %s, \"stream\": false}",
                    messagesJson, Arrays.toString(memoryEmbedding)
            );
            logger.info("Input : " + jsonInput);

            // Exécution de la requête HTTP à Ollama pour obtenir une réponse
            URL url = new URL("http://localhost:11434/api/chat");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

                // Extrait le champ "content"
                String response = result.toString();
                int index = response.indexOf("\"content\":\"");
                if (index >= 0) {
                    response = response.substring(index + 10);
                    int end = response.indexOf("\"");
                    if (end > 0) {
                        response = response.substring(0, end).replace("\\n", " ").replace("\\\"", "\"");
                    }
                }

                // Met à jour l'embedding de la session
                session.setEmbeddingMemory(getEmbedding(response));

                return response;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Sorry, I couldn't get a response from the NPC right now.";
        }
    }
}
