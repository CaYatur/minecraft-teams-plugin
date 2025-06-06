package com.example.teamsplugin.managers;

import com.example.teamsplugin.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class TeamManager {
    private Map<String, Team> teams;
    private Plugin plugin;

    public TeamManager() {
        this.teams = new HashMap<>();
    }
    
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public Team createTeam(String name, String color) {
        Team team = new Team(name, color);
        teams.put(name.toLowerCase(), team);
        return team;
    }

    public boolean addPlayerToTeam(String teamName, Player player) {
        Team team = getTeam(teamName);
        if (team != null) {
            team.addPlayer(player);
            return true;
        }
        return false;
    }

    public boolean removePlayerFromTeam(String teamName, Player player) {
        Team team = getTeam(teamName);
        if (team != null) {
            team.removePlayer(player);
            return true;
        }
        return false;
    }

    public Team getPlayerTeam(Player player) {
        for (Team team : teams.values()) {
            if (team.hasPlayer(player)) {
                return team;
            }
        }
        return null;
    }

    public Team getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    public boolean teamExists(String name) {
        return teams.containsKey(name.toLowerCase());
    }

    public Set<String> getTeamNames() {
        return teams.keySet();
    }

    public List<Team> getAllTeams() {
        return new ArrayList<>(teams.values());
    }

    // Veri yükleme/kaydetme metodları
    public void saveData() {
        if (plugin == null) {
            return;
        }
        
        try {
            // Teams klasörünü oluştur
            File teamsFolder = new File(plugin.getDataFolder(), "teams");
            if (!teamsFolder.exists()) {
                teamsFolder.mkdirs();
            }
            
            // Tüm takımları kaydet
            for (Team team : teams.values()) {
                File teamFile = new File(teamsFolder, team.getName() + ".yml");
                FileConfiguration config = new YamlConfiguration();
                
                config.set("name", team.getName());
                config.set("color", team.getColor());
                
                List<String> playerUUIDs = new ArrayList<>();
                for (UUID uuid : team.getPlayers()) {
                    playerUUIDs.add(uuid.toString());
                }
                config.set("players", playerUUIDs);
                
                config.save(teamFile);
            }
            
            // Takım listesi dosyasını kaydet
            File teamListFile = new File(plugin.getDataFolder(), "teamList.yml");
            FileConfiguration config = new YamlConfiguration();
            config.set("teams", new ArrayList<>(teams.keySet()));
            config.save(teamListFile);
            
            plugin.getLogger().info("Takım verileri başarıyla kaydedildi.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Takım verileri kaydedilirken bir hata oluştu", e);
        }
    }
    
    public void loadData() {
        if (plugin == null) {
            return;
        }
        
        try {
            teams.clear();
            
            // Takım listesini yükle
            File teamListFile = new File(plugin.getDataFolder(), "teamList.yml");
            if (!teamListFile.exists()) {
                return;
            }
            
            FileConfiguration listConfig = YamlConfiguration.loadConfiguration(teamListFile);
            List<String> teamNames = listConfig.getStringList("teams");
            
            // Teams klasörünü kontrol et
            File teamsFolder = new File(plugin.getDataFolder(), "teams");
            if (!teamsFolder.exists()) {
                return;
            }
            
            // Her takım için verileri yükle
            for (String teamName : teamNames) {
                File teamFile = new File(teamsFolder, teamName + ".yml");
                if (!teamFile.exists()) {
                    continue;
                }
                
                FileConfiguration config = YamlConfiguration.loadConfiguration(teamFile);
                String name = config.getString("name");
                String color = config.getString("color");
                
                Team team = new Team(name, color);
                
                // Oyuncuların UUID'lerini ekle - artık online olup olmadığına bakmıyoruz
                List<String> playerUUIDs = config.getStringList("players");
                for (String uuidStr : playerUUIDs) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        // BU SATIR HATALI: team.getPlayers().add(uuid);
                        // getPlayers() metodu kopya döndürüyor, değişiklikler kaydedilmiyor
                        
                        // Bunun yerine doğrudan ekleme yapacak metodu kullanalım:
                        team.addPlayerUUID(uuid);
                        
                        // Eğer oyuncu online ise aktif oyunculara ekle
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null && player.isOnline()) {
                            team.getActivePlayers().add(player);
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Geçersiz UUID: " + uuidStr);
                    }
                }
                
                // Aktif oyuncuları senkronize et
                team.syncActivePlayers();
                
                teams.put(name.toLowerCase(), team);
            }
            
            plugin.getLogger().info("Takım verileri başarıyla yüklendi: " + teams.size() + " takım");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Takım verileri yüklenirken bir hata oluştu", e);
        }
    }
}