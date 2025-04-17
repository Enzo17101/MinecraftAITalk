package com.MinecraftAIPlugin.commands;

import com.MinecraftAIPlugin.NPCPlugin;
import com.MinecraftAIPlugin.model.ConversationSession;
import com.MinecraftAIPlugin.utils.OllamaAPI;
import org.bukkit.command.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class StartDebugCommand implements CommandExecutor {

    private final NPCPlugin plugin;

    public StartDebugCommand(NPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.setDebugMode(true);
        sender.sendMessage("Debug mode is now active.");

        final UUID DEBUG_UUID = UUID.nameUUIDFromBytes("debug-session".getBytes(StandardCharsets.UTF_8));
        String npcName = "Eldon";

        ConversationSession session = new ConversationSession(npcName);
        session.addMessage("system", "You are a medieval NPC named " + npcName + ". The player wants to talk to you. Introduce yourself and ask a question.");

        plugin.getActiveConversations().put(DEBUG_UUID, session);

        String response = OllamaAPI.getResponseFromOllama(session, plugin.getLogger());
        session.addMessage("npc", response);
        plugin.getLogger().info("[DEBUG][" + npcName + "] " + response);

        return true;
    }
}
