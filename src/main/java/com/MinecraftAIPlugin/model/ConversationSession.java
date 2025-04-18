package com.MinecraftAIPlugin.model;

import java.util.*;
import java.util.logging.Logger;

public class ConversationSession {
    private final String npcName;
    private final List<Message> messages = new ArrayList<>();

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

    public void printFullConversation(Logger logger) {
        logger.info("---- Full Conversation Log ----");
        for (Message msg : this.getMessages()) {
            logger.info("[" + msg.role.toUpperCase() + "] " + msg.content);
        }
        logger.info("---- End of Conversation ----");
    }
}