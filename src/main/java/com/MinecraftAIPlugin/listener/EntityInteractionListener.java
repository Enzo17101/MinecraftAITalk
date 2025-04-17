package com.MinecraftAIPlugin.listener;

import com.MinecraftAIPlugin.NPCPlugin;
import com.MinecraftAIPlugin.model.ConversationSession;
import com.MinecraftAIPlugin.utils.OllamaAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.UUID;

public class EntityInteractionListener implements Listener {

    private final NPCPlugin plugin;

    public EntityInteractionListener(NPCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity.getCustomName() == null) {
            player.sendMessage(ChatColor.YELLOW + "This entity has no name.");
            return;
        }

        String npcName = entity.getCustomName();
        UUID playerId = player.getUniqueId();
        var conversations = plugin.getActiveConversations();

        ConversationSession session;

        if (conversations.containsKey(playerId)) {
            session = conversations.get(playerId);
            if (!session.getNpcName().equals(npcName)) {
                player.sendMessage(ChatColor.GRAY + "* You switched to talk to " + ChatColor.GOLD + npcName);
                session = new ConversationSession(npcName);
                session.addMessage("system", getSystemPrompt(npcName));
                conversations.put(playerId, session);
            } else {
                player.sendMessage(ChatColor.GRAY + "* You are already talking to " + ChatColor.GOLD + npcName);
            }
        } else {
            player.sendMessage(ChatColor.GRAY + "* You start talking to " + ChatColor.GOLD + npcName);
            session = new ConversationSession(npcName);
            session.addMessage("system", getSystemPrompt(npcName));
            conversations.put(playerId, session);
        }

        // Génère la réponse du NPC
        String response = OllamaAPI.getResponseFromOllama(session, plugin.getLogger());
        session.addMessage("npc", response);

        // Affiche la réponse au joueur
        player.sendMessage(ChatColor.GOLD + npcName + ChatColor.WHITE + ": " + response);

        // Après coup : met à jour la mémoire par embeddings
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            float[] embedding = OllamaAPI.getEmbedding(response, plugin.getLogger());
            if (embedding != null) {
                session.setEmbeddingMemory(embedding);

                // Mode debug : affiche la mémoire actuelle et le log de la session
                if (plugin.getConfig().getBoolean("debug", false)) {
                    plugin.getLogger().info("[DEBUG] NPC: " + npcName + " - updated embedding memory.");
                    session.printDebugInfo();
                }
            }
        });
    }

    private String getSystemPrompt(String npcName) {
        return "You are a medieval NPC named " + npcName + ". The player wants to talk to you. Introduce yourself and ask a question.";
    }
}
