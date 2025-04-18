package com.MinecraftAIPlugin;

import com.MinecraftAIPlugin.commands.StartDebugCommand;
import com.MinecraftAIPlugin.commands.StopTalkCommand;
import com.MinecraftAIPlugin.commands.TalkCommand;
import com.MinecraftAIPlugin.listener.EntityInteractionListener;
import com.MinecraftAIPlugin.model.ConversationSession;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class NPCPlugin extends JavaPlugin {

    // Dictionnaire des conversations actives (UUID du joueur ou "debug-session" → session)
    private final Map<UUID, ConversationSession> activeConversations = new HashMap<>();

    @Override
    public void onEnable() {
        // Commandes
        this.getCommand("talk").setExecutor(new TalkCommand(this));
        this.getCommand("stoptalk").setExecutor(new StopTalkCommand(this));
        this.getCommand("startdebug").setExecutor(new StartDebugCommand(this));

        // Listener pour les interactions avec les entitées
        getServer().getPluginManager().registerEvents(new EntityInteractionListener(this), this);

        getLogger().info("NPCDialogue plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("NPCDialogue plugin disabled!");
    }


    public Map<UUID, ConversationSession> getActiveConversations() {
        return activeConversations;
    }

    // Récupère une session de conversation existante pour un joueur donné
    public Optional<ConversationSession> getConversation(UUID playerId) {
        if (activeConversations.containsKey(playerId)) {
            return Optional.of(activeConversations.get(playerId));
        }
        return Optional.empty();
    }

    // Crée une nouvelle session de conversation pour un joueur donné, si elle n'existe pas déjà
    public void newConversation(UUID playerId, ConversationSession session) {
        activeConversations.put(playerId, session);
    }



    // TEMPORAIRE
    // Pour le débug

    private boolean debugMode = false;

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }


    public static String getSystemPrompt(String npcName) {
        return "You are a medieval NPC named " + npcName + " selling potions. The player wants to talk to you. Introduce yourself and ask a question.";
    }
}
