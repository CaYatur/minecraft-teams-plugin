package com.example.teamsplugin;

import com.example.teamsplugin.commands.HelpCommand;
import com.example.teamsplugin.commands.RegionCommands;
import com.example.teamsplugin.commands.TeamCommands;
import com.example.teamsplugin.listeners.BlockListener;
import com.example.teamsplugin.listeners.PlayerListener;
import com.example.teamsplugin.managers.RegionManager;
import com.example.teamsplugin.managers.TeamManager;
import com.example.teamsplugin.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class TeamsPlugin extends JavaPlugin {
    private TeamManager teamManager;
    private RegionManager regionManager;

    @Override
    public void onEnable() {
        // Veri klasörünü oluştur
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Managers başlat
        teamManager = new TeamManager();
        teamManager.setPlugin(this);
        
        regionManager = new RegionManager(teamManager);
        regionManager.setPlugin(this);

        // Verileri yükle
        teamManager.loadData();
        regionManager.loadData();
        
        // Komutları kaydet
        registerCommands();

        // Listener'ları kaydet
        getServer().getPluginManager().registerEvents(new BlockListener(teamManager, regionManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(teamManager), this);

        // Online oyuncuları takımlarıyla senkronize et
        syncOnlinePlayers();
        
        // Başlangıç mesajı
        getLogger().info(ChatColor.GREEN + "Takım Savaşları Eklentisi aktif edildi!");
        
        // Eğer hiç takım yoksa varsayılan takımları oluştur
        if (teamManager.getTeamNames().isEmpty()) {
            teamManager.createTeam("Kırmızı", "RED");
            teamManager.createTeam("Mavi", "BLUE");
        }
    }

    /**
     * Sunucu başlangıcında online oyuncuları takımlarıyla senkronize eder
     */
    private void syncOnlinePlayers() {
        getLogger().info("Online oyuncular takımlarıyla senkronize ediliyor...");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();
            boolean foundTeam = false;
            
            for (Team team : teamManager.getAllTeams()) {
                if (team.hasPlayerUUID(playerUUID)) {
                    team.getActivePlayers().add(player);
                    player.sendMessage(ChatColor.GREEN + "Takımınız senkronize edildi: " + 
                        ChatColor.valueOf(team.getColor()) + team.getName());
                    foundTeam = true;
                    break;
                }
            }
            
            if (foundTeam) {
                getLogger().info(player.getName() + " takımıyla senkronize edildi.");
            } else {
                getLogger().info(player.getName() + " herhangi bir takımda değil.");
            }
        }
    }

    private void registerCommands() {
        // Team komutu
        TeamCommands teamCommands = new TeamCommands(teamManager);
        PluginCommand cyteamCommand = getCommand("cyteam");
        if (cyteamCommand != null) {
            cyteamCommand.setExecutor(teamCommands);
            cyteamCommand.setTabCompleter(teamCommands);
        }

        // Region komutu
        RegionCommands regionCommands = new RegionCommands(regionManager, teamManager);
        PluginCommand cyregionCommand = getCommand("cyregion");
        if (cyregionCommand != null) {
            cyregionCommand.setExecutor(regionCommands);
            cyregionCommand.setTabCompleter(regionCommands);
        }
        
        // Help komutu
        PluginCommand cyhelpCommand = getCommand("cyhelp");
        if (cyhelpCommand != null) {
            cyhelpCommand.setExecutor(new HelpCommand());
        }
    }

    @Override
    public void onDisable() {
        // Görselleştirme görevlerini temizle
        regionManager.cleanup();
        
        // Verileri kaydet
        teamManager.saveData();
        regionManager.saveData();
        
        getLogger().info(ChatColor.RED + "Takım Savaşları Eklentisi devre dışı bırakıldı!");
    }
}