package com.MinecraftAIPlugin.commands;

import com.MinecraftAIPlugin.NPCPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class StopTalkCommand implements CommandExecutor {

    private final NPCPlugin plugin;

    public StopTalkCommand(NPCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Récupère l'uuid du joueur
        final UUID sessionId = (sender instanceof Player)
                ? ((Player) sender).getUniqueId()
                : UUID.nameUUIDFromBytes("debug-session".getBytes(StandardCharsets.UTF_8));

        if (plugin.getActiveConversations().remove(sessionId) != null) {
            sender.sendMessage(ChatColor.GRAY + "You ended the conversation.");
        } else {
            sender.sendMessage(ChatColor.RED + "You're not talking to any NPC.");
        }

        return true;
    }
}
