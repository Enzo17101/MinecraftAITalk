package com.MinecraftAIPlugin.model;

public class Message {
    public final String role; // Peut être system, user, assistant ou tool
    public final String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}