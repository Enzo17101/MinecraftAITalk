package com.MinecraftAIPlugin.commands;

import com.MinecraftAIPlugin.NPCPlugin;
import com.MinecraftAIPlugin.model.ConversationSession;
import com.MinecraftAIPlugin.utils.OllamaAPI;
import org.bukkit.command.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Logger;

import static com.MinecraftAIPlugin.NPCPlugin.getSystemPrompt;

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

        // Message de test
        session.addMessage("system", getSystemPrompt(npcName));

        plugin.getActiveConversations().put(DEBUG_UUID, session);

        String response = OllamaAPI.generateResponse(getSystemPrompt(npcName), plugin.getLogger());
        session.addMessage("npc", response);
        //plugin.getLogger().info("[DEBUG][" + npcName + "] " + response);

        session.printFullConversation(plugin.getLogger());

        return true;
    }
}
