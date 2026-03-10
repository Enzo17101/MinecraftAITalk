package com.MinecraftAIPlugin.utils;

import com.MinecraftAIPlugin.model.ConversationSession;
import com.MinecraftAIPlugin.model.Message;
import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;


public class OllamaAPI {
    private static final String OLLAMA_URL = "http://localhost:11434";
    private static final String MODEL_NAME = "nous-hermes";

    private static final Gson gson = new GsonBuilder().create();


    /**
     * Envoie une requête POST à /api/generate avec un prompt système unique
     */
    public static String generateResponse(String systemPrompt, Logger logger) {
        if (isOllamaAvailable()) {
            try {
                URL url = new URL(OLLAMA_URL + "/api/generate");
                HttpURLConnection connection = openPostConnection(url);

                Map<String, Object> payload = new HashMap<>();
                payload.put("model", MODEL_NAME);
                payload.put("prompt", systemPrompt);
                //payload.put("format", "json"); // permet un parsing plus propre
                payload.put("stream", false);  // Réponse en une seule fois

                sendPayload(connection, gson.toJson(payload));

                //return readStreamingResponse(connection.getInputStream(), true);
                return readResponse(connection);

            } catch (IOException e) {
                logger.warning("Error while calling Ollama generate endpoint: " + e.getMessage());
                return "I'm sorry, I couldn't respond.";
            }
        }
        else
        {
            logger.warning("Ollama not available.");
            return "I'm sorry, I couldn't respond.";
        }
    }

    /**
     * Envoie une requête POST à /api/chat avec l'historique complet de la session
     */
    public static String getResponseFromOllama(ConversationSession session, Logger logger) {
        if(isOllamaAvailable()) {
            try {
                URL url = new URL(OLLAMA_URL + "/api/chat");
                HttpURLConnection connection = openPostConnection(url);

                Map<String, Object> payload = new HashMap<>();
                payload.put("model", MODEL_NAME);
                payload.put("messages", convertMessages(session.getMessages()));
                //payload.put("format", "json"); // réponse au format JSON uniquement
                payload.put("stream", false);  // Réponse en une seule fois

                sendPayload(connection, gson.toJson(payload));

                //return readStreamingResponse(connection.getInputStream(), true);
                return readResponse(connection);

            } catch (IOException e) {
                logger.warning("Error while calling Ollama chat endpoint: " + e.getMessage());
                return "I'm sorry, I couldn't respond.";
            }
        }
        else
        {
            logger.warning("Ollama not available.");
            return "I'm sorry, I couldn't respond.";
        }
    }

    public static boolean isOllamaAvailable() {
        try {
            URL url = new URL("http://localhost:11434/api/tags");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000); // Timeout court pour ne pas bloquer
            connection.connect();
            int responseCode = connection.getResponseCode();
            return (responseCode == 200);
        } catch (IOException e) {
            return false;
        }
    }

    // Extrait le texte utile de la réponse JSON retournée par Ollama
    private static String extractResponseText(String rawJson, boolean isChat) {
        try {
            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();

            if (isChat && json.has("message")) {
                JsonObject message = json.getAsJsonObject("message");
                if (message.has("content")) {
                    return message.get("content").getAsString().trim();
                }
            } else if (json.has("response")) {
                return json.get("response").getAsString().trim();
            }
        } catch (JsonParseException e) {
            System.err.println("Failed to parse JSON response: " + rawJson);
        }

        return "I'm sorry, I couldn't parse the response.";
    }

    // Lis chaque ligne JSON et concatène les fragments de texte dans le champ "response" ou "message.content"
    private static String readStreamingResponse(InputStream inputStream, boolean isChat) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder fullResponse = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) continue;

            try {
                JsonObject json = JsonParser.parseString(line).getAsJsonObject();

                if (isChat && json.has("message")) {
                    JsonObject message = json.getAsJsonObject("message");
                    if (message.has("content")) {
                        fullResponse.append(message.get("content").getAsString());
                    }
                } else if (json.has("response")) {
                    fullResponse.append(json.get("response").getAsString());
                }

            } catch (JsonParseException e) {
                // On ignore les lignes malformées
                System.err.println("Invalid JSON fragment: " + line);
            }
        }

        return fullResponse.toString().trim();
    }

    // Ouvre une connexion POST avec les bons headers
    private static HttpURLConnection openPostConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    // Envoie un JSON sous forme de string
    private static void sendPayload(HttpURLConnection connection, String json) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }
    }

    private static String readRawResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line.trim());
            }
            return responseBuilder.toString();
        }
    }

    // Lit la réponse du serveur
    private static String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line.trim());
            }

            String rawJson = responseBuilder.toString();

            JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();
            if (json.has("message")) {
                JsonObject message = json.getAsJsonObject("message");
                String role = message.has("role") ? message.get("role").getAsString() : "";
                String content = message.has("content") ? message.get("content").getAsString() : "";

                if (!role.equals("assistant")) {
                    System.err.println("Unexpected role in response: " + role);
                }

                return content;
            }

            return "[Empty or invalid response]";
        } catch (JsonParseException e) {
            System.err.println("Invalid JSON response from Ollama: " + e.getMessage());
            return "[Invalid JSON]";
        }
    }

    // Convertit une liste de Message Java en une liste de Map<String, String> pour l'API
    private static List<Map<String, String>> convertMessages(List<Message> messages) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, String> entry = new HashMap<>();
            entry.put("role", convertRole(msg.role));
            entry.put("content", msg.content);
            result.add(entry);
        }
        return result;
    }

    // Convertit les rôles internes vers ceux attendus par Ollama ("player" → "user", "npc" → "assistant")
    private static String convertRole(String role) {
        return switch (role.toLowerCase()) {
            case "player", "user" -> "user";
            case "npc", "assistant" -> "assistant";
            case "system" -> "system";
            default -> "user";
        };
    }
}
