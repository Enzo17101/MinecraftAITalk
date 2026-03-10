package com.MinecraftAIPlugin.commands;

import com.MinecraftAIPlugin.NPCPlugin;
import com.MinecraftAIPlugin.model.ConversationSession;
import com.MinecraftAIPlugin.utils.OllamaAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TalkCommand implements CommandExecutor {

    private final NPCPlugin plugin;

    public TalkCommand(NPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Récupère l'uuid du joueur
        final UUID sessionId = (sender instanceof Player)
                ? ((Player) sender).getUniqueId()
                : UUID.nameUUIDFromBytes("debug-session".getBytes(StandardCharsets.UTF_8));

        // Vérifie si le joueur à une conversation en cours ou non
        var conversations = plugin.getActiveConversations();
        if (!conversations.containsKey(sessionId)) {
            sender.sendMessage(ChatColor.RED + "You're not talking to any NPC right now. Right-click an NPC to start.");
            return true;
        }

        // Vérifie le format de la commande
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /talk <message>");
            return true;
        }

        // Parse le message et l'ajoute à la session
        String userMessage = String.join(" ", args);
        ConversationSession session = conversations.get(sessionId);
        session.addMessage("player", userMessage);

        // Pareil avec la réponse du npc
        String response = OllamaAPI.getResponseFromOllama(session, plugin.getLogger());
        session.addMessage("npc", response);

        // Affiche le résultat du message
        //TODO : à formater
        if (sender instanceof Player player) {
            player.sendMessage(ChatColor.GOLD + session.getNpcName() + ChatColor.WHITE + ": " + response);
        } else {
            // S'affiche dans la console si en mode débug
            //plugin.getLogger().info("[DEBUG][" + session.getNpcName() + "] " + response);
        }
        session.printFullConversation(plugin.getLogger());

        return true;
    }
}
