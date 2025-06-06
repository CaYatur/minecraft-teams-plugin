package com.example.teamsplugin.listeners;

import com.example.teamsplugin.managers.TeamManager;
import com.example.teamsplugin.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {
    private TeamManager teamManager;

    public PlayerListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        
        // Hoş geldin mesajı
        player.sendMessage(ChatColor.GREEN + "Takım Savaşları Eklentisi yüklü!");
        player.sendMessage(ChatColor.YELLOW + "Komutlar için /cyteam help veya /cyregion help yazabilirsiniz.");
        
        // Oyuncunun takımını kontrol et ve aktif oyunculara ekle
        boolean foundTeam = false;
        for (Team team : teamManager.getAllTeams()) {
            if (team.hasPlayerUUID(playerUUID)) {
                team.getActivePlayers().add(player);
                player.sendMessage(ChatColor.GREEN + "Takımınız: " + 
                    ChatColor.valueOf(team.getColor()) + team.getName());
                foundTeam = true;
                break;
            }
        }
        
        if (!foundTeam) {
            player.sendMessage(ChatColor.YELLOW + "Herhangi bir takımda değilsiniz. " + 
                "Bir takıma katılmak için /cyteam join <takım-adı> komutunu kullanabilirsiniz.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Oyuncuyu aktif oyunculardan çıkar ama UUID'sini silme
        Team playerTeam = teamManager.getPlayerTeam(player);
        if (playerTeam != null) {
            playerTeam.getActivePlayers().remove(player);
        }
    }
}