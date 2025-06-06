package com.example.teamsplugin.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Region implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String worldName; // Dünya adını saklayacağız
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;
    private Team owner;

    /**
     * İki nokta arasında bir bölge oluşturur.
     * Nokta 1 ve Nokta 2 tam olarak dahil edilir.
     */
    public Region(String name, Location point1, Location point2, Team owner) {
        this.name = name;
        this.worldName = point1.getWorld().getName();
        
        // Tam blok koordinatlarını kullan
        this.minX = Math.min(point1.getBlockX(), point2.getBlockX());
        this.minY = Math.min(point1.getBlockY(), point2.getBlockY());
        this.minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());
        
        this.maxX = Math.max(point1.getBlockX(), point2.getBlockX());
        this.maxY = Math.max(point1.getBlockY(), point2.getBlockY());
        this.maxZ = Math.max(point1.getBlockZ(), point2.getBlockZ());
        
        this.owner = owner;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Bir lokasyonun bu bölge içinde olup olmadığını kontrol eder.
     */
    public boolean contains(Location location) {
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    public Team getOwner() {
        return owner;
    }
    
    public void setOwner(Team owner) {
        this.owner = owner;
    }
    
    public Location getMin() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("Dünya bulunamadı: " + worldName);
        }
        return new Location(world, minX, minY, minZ);
    }
    
    public Location getMax() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("Dünya bulunamadı: " + worldName);
        }
        return new Location(world, maxX, maxY, maxZ);
    }
    
    /**
     * Bölgeyi her yönde tam olarak belirtilen miktarda blok genişletir.
     * Bloklar dahil olacak şekilde hesaplanır.
     */
    public void expand(int amount) {
        minX -= amount;
        minY -= amount;
        minZ -= amount;
        
        maxX += amount;
        maxY += amount;
        maxZ += amount;
    }
    
    /**
     * Bölgenin hacmini blok sayısı olarak hesaplar.
     */
    public int getVolume() {
        int width = maxX - minX + 1;  // +1 çünkü başlangıç bloğu da dahil
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        return width * height * depth;
    }
    
    /**
     * Bölgenin boyutlarını string olarak döndürür.
     */
    public String getDimensions() {
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        return width + "x" + height + "x" + depth;
    }
    
    /**
     * Bölgenin temel bilgilerini döndürür.
     */
    public String getInfo() {
        return String.format("Bölge: %s, Dünya: %s, Min: (%d, %d, %d), Max: (%d, %d, %d), Boyut: %s, Hacim: %d blok",
                name, worldName, minX, minY, minZ, maxX, maxY, maxZ, getDimensions(), getVolume());
    }
    
    /**
     * Serileştirme için yardımcı metodlar
     */
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("worldName", worldName);
        data.put("minX", minX);
        data.put("minY", minY);
        data.put("minZ", minZ);
        data.put("maxX", maxX);
        data.put("maxY", maxY);
        data.put("maxZ", maxZ);
        data.put("ownerTeam", owner.getName());
        return data;
    }
}