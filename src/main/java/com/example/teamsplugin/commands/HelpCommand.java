package com.example.teamsplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "=== Takım Savaşları Plugin Yardım ===");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam help" + ChatColor.WHITE + " - Takım komutları yardım menüsünü gösterir");
        sender.sendMessage(ChatColor.YELLOW + "/cyregion help" + ChatColor.WHITE + " - Bölge komutları yardım menüsünü gösterir");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "Ana Komutlar:");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam" + ChatColor.WHITE + " - Takımları yönetmek için komutlar");
        sender.sendMessage(ChatColor.YELLOW + "/cyregion" + ChatColor.WHITE + " - Bölgeleri yönetmek için komutlar");
        return true;
    }
}