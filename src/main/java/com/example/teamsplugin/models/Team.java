package com.example.teamsplugin.models;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Team implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String color;
    private Set<UUID> playerUUIDs; // Sadece UUID'leri saklayacak
    private transient Set<Player> activePlayers; // Online oyuncular için geçici set

    public Team(String name, String color) {
        this.name = name;
        this.color = color;
        this.playerUUIDs = new HashSet<>();
        this.activePlayers = new HashSet<>();
    }
    
    public String getName() {
        return name;
    }
    
    public String getColor() {
        return color;
    }
    
    public boolean addPlayer(Player player) {
        activePlayers.add(player);
        return playerUUIDs.add(player.getUniqueId());
    }
    
    public boolean removePlayer(Player player) {
        activePlayers.remove(player);
        return playerUUIDs.remove(player.getUniqueId());
    }
    
    public boolean hasPlayer(Player player) {
        return playerUUIDs.contains(player.getUniqueId());
    }
    
    public boolean hasPlayerUUID(UUID uuid) {
        return playerUUIDs.contains(uuid);
    }
    
    public Set<UUID> getPlayers() {
        return new HashSet<>(playerUUIDs);
    }
    
    public Set<Player> getActivePlayers() {
        return new HashSet<>(activePlayers);
    }
    
    public void syncActivePlayers() {
        // Aktif oyuncular listesini temizle
        activePlayers.clear();
        
        // Online olan oyuncuları aktif oyuncular listesine ekle
        for (UUID uuid : playerUUIDs) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                activePlayers.add(player);
            }
        }
    }
    
    /**
     * Doğrudan UUID ekler (plugin verileri yüklerken kullanılır)
     */
    public boolean addPlayerUUID(UUID uuid) {
        return playerUUIDs.add(uuid);
    }
}