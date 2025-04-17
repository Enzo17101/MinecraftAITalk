package com.MinecraftAIPlugin;

import com.MinecraftAIPlugin.commands.StartDebugCommand;
import com.MinecraftAIPlugin.commands.StopTalkCommand;
import com.MinecraftAIPlugin.commands.TalkCommand;
import com.MinecraftAIPlugin.listener.EntityInteractionListener;
import com.MinecraftAIPlugin.model.ConversationSession;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class NPCPlugin extends JavaPlugin {

    // Dictionnaire des conversations actives (UUID du joueur ou "debug-session" → session)
    private boolean debugMode = false;
    private Logger logger;
    private final Map<UUID, ConversationSession> activeConversations = new HashMap<>();

    @Override
    public void onEnable() {
        // Enregistrement des commandes
        this.getCommand("talk").setExecutor(new TalkCommand(this));
        this.getCommand("stoptalk").setExecutor(new StopTalkCommand(this));
        this.getCommand("startdebug").setExecutor(new StartDebugCommand(this));

        // Enregistrement de l'écouteur d'interactions avec les entités
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

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}
