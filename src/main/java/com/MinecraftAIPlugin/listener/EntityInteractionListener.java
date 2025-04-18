package com.MinecraftAIPlugin.listener;

import com.MinecraftAIPlugin.NPCPlugin;
import com.MinecraftAIPlugin.model.ConversationSession;
import com.MinecraftAIPlugin.utils.OllamaAPI;
import static com.MinecraftAIPlugin.NPCPlugin.getSystemPrompt;

import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Optional;
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

        // Vérification si l'entité a un nom personnalisé
        if (entity.getCustomName() == null) {
            player.sendMessage(ChatColor.YELLOW + "This entity has no name.");
            return;
        }

        // Récupère le nom du NPC et l'ID du joueur
        String npcName = entity.getCustomName();
        UUID playerId = player.getUniqueId();

        // Récupère ou crée une nouvelle session de conversation
        Optional<ConversationSession> optionalSession = plugin.getConversation(playerId);
        ConversationSession session = optionalSession.orElseGet(() -> {
            ConversationSession newSession = new ConversationSession(npcName);
            newSession.addMessage("system", getSystemPrompt(npcName));
            plugin.newConversation(playerId, newSession);
            return newSession;
        });

        String response;
        boolean newConversation = false;

        // Si c'est un nouveau NPC ou une nouvelle session de conversation
        if (!session.getNpcName().equals(npcName)) {
            // Indiquer que le joueur commence une nouvelle conversation avec un NPC différent
            player.sendMessage(ChatColor.GRAY + "* You switched to talk to " + ChatColor.GOLD + npcName);
            newConversation = true;

            // Génére une nouvelle réponse du NPC
            response = OllamaAPI.generateResponse(getSystemPrompt(npcName), plugin.getLogger());
            session = new ConversationSession(npcName);  // Crée une nouvelle session
            session.addMessage("system", getSystemPrompt(npcName));
            plugin.newConversation(playerId, session);

        } else {
            // Utilise la session existante pour obtenir la réponse du même NPC
            response = OllamaAPI.getResponseFromOllama(session, plugin.getLogger());
        }

        // Ajoute la réponse du NPC à la session de conversation
        session.addMessage("npc", response);

        // Affiche la réponse au joueur
        player.sendMessage(ChatColor.GOLD + npcName + ChatColor.WHITE + ": " + response);

        // Affiche toute la conversation pour le débogage
        session.printFullConversation(plugin.getLogger());
    }
}
