package com.MinecraftAIPlugin.model;

import java.util.*;

public class ConversationSession {
    private final String npcName;
    private final List<Message> messages = new ArrayList<>();
    private float[] embeddingMemory;  // Mémoire de l'embedding

    public ConversationSession(String npcName) {
        this.npcName = npcName;
    }

    public void addMessage(String role, String content) {
        messages.add(new Message(role, content));
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getNpcName() {
        return npcName;
    }

    // Méthodes pour gérer la mémoire de l'embedding
    public void setEmbeddingMemory(float[] embedding) {
        this.embeddingMemory = embedding;
    }

    public float[] getEmbeddingMemory() {
        return embeddingMemory;
    }

    // Méthode de debug pour afficher les embeddings et les messages
    public void printDebugInfo() {
        System.out.println("---- Debug Info for NPC: " + npcName + " ----");

        // Affichage des messages
        System.out.println("Messages: ");
        for (Message msg : messages) {
            System.out.println("Role: " + msg.role + " | Content: " + msg.content);
        }

        // Affichage de l'embedding actuel (si existe)
        if (embeddingMemory != null) {
            System.out.println("Embedding Memory: ");
            for (float e : embeddingMemory) {
                System.out.print(e + " ");
            }
            System.out.println();
        } else {
            System.out.println("No embedding memory available.");
        }

        System.out.println("-----------------------------------------");
    }

    public static class Message {
        public final String role;
        public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
