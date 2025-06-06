package com.example.teamsplugin.managers;

import com.example.teamsplugin.models.Region;
import com.example.teamsplugin.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class RegionManager {
    private Map<String, Region> regions;
    private Map<String, Location[]> playerSelections;
    private TeamManager teamManager;
    private Plugin plugin;
    private Map<UUID, BukkitTask> visualTasks;

    // Sınıf değişkenlerine yeni görselleştirme izleyicisi ekle
    private Map<String, BukkitTask> globalVisualizationTasks;

    public RegionManager(TeamManager teamManager) {
        this.regions = new HashMap<>();
        this.playerSelections = new HashMap<>();
        this.teamManager = teamManager;
        this.visualTasks = new HashMap<>();
        this.globalVisualizationTasks = new HashMap<>();
        this.plugin = null; // Plugin eklenecek
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isInRegion(Location location) {
        for (Region region : regions.values()) {
            if (region.contains(location)) {
                return true;
            }
        }
        return false;
    }

    public Region getRegionAt(Location location) {
        for (Region region : regions.values()) {
            if (region.contains(location)) {
                return region;
            }
        }
        return null;
    }
    
    public Region getRegionByName(String name) {
        return regions.get(name.toLowerCase());
    }

    public boolean canPlaceBlock(Player player, Location location) {
        Region region = getRegionAt(location);
        if (region == null) {
            return true; // No region, can place
        }
        
        Team playerTeam = teamManager.getPlayerTeam(player);
        return playerTeam != null && playerTeam.equals(region.getOwner());
    }

    public boolean canBreakBlock(Player player, Location location) {
        Region region = getRegionAt(location);
        if (region == null) {
            return true; // No region, can break
        }
        
        Team playerTeam = teamManager.getPlayerTeam(player);
        return playerTeam != null && playerTeam.equals(region.getOwner());
    }

    /**
     * Oyuncunun belirttiği pozisyonda bir bölge köşesi seçer.
     * Seçilen konum tam olarak hedeflenen bloğu içerir.
     */
    public void setPlayerSelection(Player player, int position, Location location) {
        String playerName = player.getName();
        if (!playerSelections.containsKey(playerName)) {
            playerSelections.put(playerName, new Location[2]);
        }
        
        // Ayağın durduğu bloğun tam koordinatlarını al
        Location blockLocation = location.clone();
        
        // BlockX, BlockY, BlockZ tam olarak üzerinde durduğu bloğu verir
        // Bunu koruyoruz, değiştirmiyoruz
        
        // Debug mesajları
        player.sendMessage(ChatColor.GRAY + "Seçim " + position + ": " + 
                          String.format("X:%d, Y:%d, Z:%d", 
                                       blockLocation.getBlockX(), 
                                       blockLocation.getBlockY(), 
                                       blockLocation.getBlockZ()));
        
        // Seçim noktasını kaydet
        playerSelections.get(playerName)[position - 1] = blockLocation;
    }

    public Location[] getPlayerSelection(Player player) {
        return playerSelections.get(player.getName());
    }

    /**
     * Seçilen noktalar arasında yeni bir bölge oluşturur ve belirtilen miktarda genişletir.
     * @param player Bölgeyi oluşturan oyuncu
     * @param regionName Bölge adı
     * @param teamName Bölgenin sahibi olacak takım adı
     * @return Oluşturulan bölge, başarısız olursa null
     */
    /**
     * Seçilen noktalar arasında yeni bir bölge oluşturur ve belirtilen miktarda genişletir.
     * Seçilen bloklar tam olarak dahil edilir ve her yönde TAM OLARAK 3 blok genişletilir.
     */
    public Region createRegion(Player player, String regionName, String teamName) {
        Location[] selection = getPlayerSelection(player);
        if (selection == null || selection[0] == null || selection[1] == null) {
            return null;
        }
        
        if (regions.containsKey(regionName.toLowerCase())) {
            return null; // Bu isimde bölge zaten var
        }
        
        Team team = teamManager.getTeam(teamName);
        if (team == null) {
            return null;
        }
        
        // Orijinal seçimi göster
        player.sendMessage(ChatColor.GRAY + "Bölge oluşturuluyor...");
        player.sendMessage(ChatColor.GRAY + "Seçim 1: " + 
                        String.format("X:%d, Y:%d, Z:%d", 
                                    selection[0].getBlockX(), 
                                    selection[0].getBlockY(), 
                                    selection[0].getBlockZ()));
        player.sendMessage(ChatColor.GRAY + "Seçim 2: " + 
                        String.format("X:%d, Y:%d, Z:%d", 
                                    selection[1].getBlockX(), 
                                    selection[1].getBlockY(), 
                                    selection[1].getBlockZ()));
        
        // Bölgeyi oluştur - seçilen bloklar dahil
        Region region = new Region(regionName, selection[0], selection[1], team);
        
        // Genişletme öncesi bilgileri göster
        player.sendMessage(ChatColor.GRAY + "Orijinal bölge: " + region.getDimensions() + " = " + region.getVolume() + " blok");
        player.sendMessage(ChatColor.GRAY + "Min: (" + region.getMin().getBlockX() + "," + 
                        region.getMin().getBlockY() + "," + region.getMin().getBlockZ() + ")");
        player.sendMessage(ChatColor.GRAY + "Max: (" + region.getMax().getBlockX() + "," + 
                        region.getMax().getBlockY() + "," + region.getMax().getBlockZ() + ")");
        
        // Bölgeyi tam olarak 3 blok genişlet (her yönde)
        region.expand(3);
        
        // Genişletme sonrası bilgileri göster
        player.sendMessage(ChatColor.GRAY + "Genişletilmiş bölge: " + region.getDimensions() + " = " + region.getVolume() + " blok");
        player.sendMessage(ChatColor.GRAY + "Min: (" + region.getMin().getBlockX() + "," + 
                        region.getMin().getBlockY() + "," + region.getMin().getBlockZ() + ")");
        player.sendMessage(ChatColor.GRAY + "Max: (" + region.getMax().getBlockX() + "," + 
                        region.getMax().getBlockY() + "," + region.getMax().getBlockZ() + ")");
        
        // Test için kritik mesafe bloklarını kontrol et
        int minX = region.getMin().getBlockX();
        int minY = region.getMin().getBlockY();
        int minZ = region.getMin().getBlockZ();
        
        int maxX = region.getMax().getBlockX();
        int maxY = region.getMax().getBlockY();
        int maxZ = region.getMax().getBlockZ();
        
        // İçeride bir blok (kesin korunmalı)
        Location insideBlock = new Location(selection[0].getWorld(), minX + 1, minY + 1, minZ + 1);
        
        // Sınırda bir blok (korunmalı)
        Location borderBlock = new Location(selection[0].getWorld(), minX, minY, minZ);
        
        // Hemen dışarıda bir blok (4. blokta - korunmasız)
        Location outsideBlock = new Location(selection[0].getWorld(), minX - 1, minY, minZ);
        
        player.sendMessage(ChatColor.GRAY + "İçeride bir blok: " + 
                        String.format("(%d,%d,%d) - Korunuyor: %b", 
                                    insideBlock.getBlockX(), insideBlock.getBlockY(), insideBlock.getBlockZ(), 
                                    region.contains(insideBlock)));
        
        player.sendMessage(ChatColor.GRAY + "Sınırda bir blok: " + 
                        String.format("(%d,%d,%d) - Korunuyor: %b", 
                                    borderBlock.getBlockX(), borderBlock.getBlockY(), borderBlock.getBlockZ(), 
                                    region.contains(borderBlock)));
        
        player.sendMessage(ChatColor.GRAY + "Dışarıda bir blok (4. blok): " + 
                        String.format("(%d,%d,%d) - Korunuyor: %b", 
                                    outsideBlock.getBlockX(), outsideBlock.getBlockY(), outsideBlock.getBlockZ(), 
                                    region.contains(outsideBlock)));
        
        regions.put(regionName.toLowerCase(), region);
        return region;
    }
    
    public boolean deleteRegion(String regionName) {
        if (regions.containsKey(regionName.toLowerCase())) {
            regions.remove(regionName.toLowerCase());
            return true;
        }
        return false;
    }
    
    public boolean transferRegion(String regionName, String teamName) {
        Region region = getRegionByName(regionName);
        Team team = teamManager.getTeam(teamName);
        
        if (region != null && team != null) {
            region.setOwner(team);
            return true;
        }
        return false;
    }

    public List<Region> getRegions() {
        return new ArrayList<>(regions.values());
    }
    
    public Set<String> getRegionNames() {
        return regions.keySet();
    }
    
    // Görselleştirme metodlarını ekle/düzelt:

    public void visualizeRegion(Player player, String regionName, int durationSeconds) {
        Region region = getRegionByName(regionName);
        if (region == null || plugin == null) {
            return;
        }
        
        // Mevcut görselleştirme görevini iptal et
        stopVisualization(player);
        
        // Yeni görselleştirme görevi başlat
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            drawRegionOutline(player, region);
        }, 0L, 10L); // Her yarım saniyede bir çalıştır
        
        visualTasks.put(player.getUniqueId(), task);
        
        // Süre pozitifse, görselleştirmeyi bitirmek için zamanlayıcı ayarla
        if (durationSeconds > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                stopVisualization(player);
                player.sendMessage(ChatColor.YELLOW + "Bölge görselleştirmesi sona erdi.");
            }, durationSeconds * 20L);
        }
    }

    public void stopVisualization(Player player) {
        BukkitTask task = visualTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    // Tüm görselleştirmeleri temizle metodu
    public void clearAllVisualizations() {
        // Oyuncu-spesifik görselleştirmeleri iptal et
        for (BukkitTask task : visualTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        visualTasks.clear();
        
        // Global görselleştirmeleri iptal et
        for (BukkitTask task : globalVisualizationTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        globalVisualizationTasks.clear();
    }

    // Birden fazla bölgeyi görselleştirmek için yeni metod
    public void visualizeRegions(Player player, List<String> regionNames, int durationSeconds) {
        List<Region> regionsToVisualize = new ArrayList<>();
        
        for (String regionName : regionNames) {
            Region region = getRegionByName(regionName);
            if (region != null) {
                regionsToVisualize.add(region);
            }
        }
        
        if (regionsToVisualize.isEmpty() || plugin == null) {
            return;
        }
        
        // Mevcut görselleştirme görevini iptal et
        stopVisualization(player);
        
        // Yeni görselleştirme görevi başlat
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Region region : regionsToVisualize) {
                drawRegionOutline(player, region);
            }
        }, 0L, 10L);
        
        visualTasks.put(player.getUniqueId(), task);
        
        // Süre pozitifse, görselleştirmeyi bitirmek için zamanlayıcı ayarla
        if (durationSeconds > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                stopVisualization(player);
                player.sendMessage(ChatColor.YELLOW + "Bölge görselleştirmesi sona erdi.");
            }, durationSeconds * 20L);
        }
    }

    // Tüm oyunculara bölge görselleştirme
    public void visualizeRegionForAll(String regionName, int durationSeconds) {
        Region region = getRegionByName(regionName);
        if (region == null || plugin == null) {
            return;
        }
        
        // Bu bölge için mevcut görev varsa iptal et
        stopGlobalVisualization(regionName);
        
        // Yeni görselleştirme görevi başlat
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                drawRegionOutline(player, region);
            }
        }, 0L, 10L);
        
        globalVisualizationTasks.put(regionName.toLowerCase(), task);
        
        // Süre pozitifse, görselleştirmeyi bitirmek için zamanlayıcı ayarla
        if (durationSeconds > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                stopGlobalVisualization(regionName);
                Bukkit.broadcastMessage(ChatColor.YELLOW + "\"" + regionName + "\" bölgesinin görselleştirmesi sona erdi.");
            }, durationSeconds * 20L);
        }
    }

    // Birden fazla bölgeyi tüm oyunculara görselleştirme
    public void visualizeRegionsForAll(List<String> regionNames, int durationSeconds) {
        List<Region> regionsToVisualize = new ArrayList<>();
        List<String> validRegionNames = new ArrayList<>();
        
        for (String regionName : regionNames) {
            Region region = getRegionByName(regionName);
            if (region != null) {
                regionsToVisualize.add(region);
                validRegionNames.add(regionName);
                // Eğer bu bölge için mevcut bir global görselleştirme görevi varsa iptal et
                stopGlobalVisualization(regionName);
            }
        }
        
        if (regionsToVisualize.isEmpty() || plugin == null) {
            return;
        }
        
        // Tüm bölgeler için tek bir görev oluştur
        String taskId = "multiple_" + System.currentTimeMillis();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (Region region : regionsToVisualize) {
                    drawRegionOutline(player, region);
                }
            }
        }, 0L, 10L);
        
        globalVisualizationTasks.put(taskId, task);
        
        // Görselleştirilen bölge isimlerini sakla
        for (String regionName : validRegionNames) {
            globalVisualizationTasks.put(regionName.toLowerCase() + "_ref", null); // Reference için
        }
        
        // Süre pozitifse, görselleştirmeyi bitirmek için zamanlayıcı ayarla
        if (durationSeconds > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                globalVisualizationTasks.remove(taskId).cancel();
                // Referansları temizle
                for (String regionName : validRegionNames) {
                    globalVisualizationTasks.remove(regionName.toLowerCase() + "_ref");
                }
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Bölge görselleştirmesi sona erdi.");
            }, durationSeconds * 20L);
        }
    }

    // Tüm bölgeleri görselleştir
    public void visualizeAllRegionsForAll(int durationSeconds) {
        List<String> allRegionNames = new ArrayList<>(regions.keySet());
        visualizeRegionsForAll(allRegionNames, durationSeconds);
    }

    // Global görselleştirmeyi durdur
    public void stopGlobalVisualization(String regionName) {
        BukkitTask task = globalVisualizationTasks.remove(regionName.toLowerCase());
        if (task != null) {
            task.cancel();
        }
        // Referansları da temizle
        globalVisualizationTasks.remove(regionName.toLowerCase() + "_ref");
    }

    private void drawRegionOutline(Player player, Region region) {
        // Koordinatları al
        Location min = region.getMin();
        Location max = region.getMax();
        World world = min.getWorld();
        
        // Takım rengine göre parçacık rengini belirle
        Team team = region.getOwner();
        Particle.DustOptions dustOptions;
        
        // Takım rengini RGB'ye dönüştür
        switch (team.getColor()) {
            case "RED":
                dustOptions = new Particle.DustOptions(org.bukkit.Color.RED, 1.0f);
                break;
            case "BLUE":
                dustOptions = new Particle.DustOptions(org.bukkit.Color.BLUE, 1.0f);
                break;
            case "GREEN":
                dustOptions = new Particle.DustOptions(org.bukkit.Color.GREEN, 1.0f);
                break;
            case "YELLOW":
                dustOptions = new Particle.DustOptions(org.bukkit.Color.YELLOW, 1.0f);
                break;
            case "PURPLE":
                dustOptions = new Particle.DustOptions(org.bukkit.Color.PURPLE, 1.0f);
                break;
            case "AQUA":
                dustOptions = new Particle.DustOptions(org.bukkit.Color.AQUA, 1.0f);
                break;
            default:
                dustOptions = new Particle.DustOptions(org.bukkit.Color.WHITE, 1.0f);
        }
        
        // Kenarları çiz (sadece çerçeve, dolu değil)
        // Alt dikdörtgen
        for (int x = min.getBlockX(); x <= max.getBlockX(); x += 2) {
            world.spawnParticle(Particle.REDSTONE, new Location(world, x, min.getY(), min.getZ()), 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(Particle.REDSTONE, new Location(world, x, min.getY(), max.getZ()), 1, 0, 0, 0, 0, dustOptions);
        }
        
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z += 2) {
            world.spawnParticle(Particle.REDSTONE, new Location(world, min.getX(), min.getY(), z), 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(Particle.REDSTONE, new Location(world, max.getX(), min.getY(), z), 1, 0, 0, 0, 0, dustOptions);
        }
        
        // Üst dikdörtgen
        for (int x = min.getBlockX(); x <= max.getBlockX(); x += 2) {
            world.spawnParticle(Particle.REDSTONE, new Location(world, x, max.getY(), min.getZ()), 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(Particle.REDSTONE, new Location(world, x, max.getY(), max.getZ()), 1, 0, 0, 0, 0, dustOptions);
        }
        
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z += 2) {
            world.spawnParticle(Particle.REDSTONE, new Location(world, min.getX(), max.getY(), z), 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(Particle.REDSTONE, new Location(world, max.getX(), max.getY(), z), 1, 0, 0, 0, 0, dustOptions);
        }
        
        // Dikey kenarlar
        for (int y = min.getBlockY(); y <= max.getBlockY(); y += 2) {
            world.spawnParticle(Particle.REDSTONE, new Location(world, min.getX(), y, min.getZ()), 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(Particle.REDSTONE, new Location(world, min.getX(), y, max.getZ()), 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(Particle.REDSTONE, new Location(world, max.getX(), y, min.getZ()), 1, 0, 0, 0, 0, dustOptions);
            world.spawnParticle(Particle.REDSTONE, new Location(world, max.getX(), y, max.getZ()), 1, 0, 0, 0, 0, dustOptions);
        }
    }

    // RegionManager.java dosyasına kaydetme/yükleme metodları ekleyelim:

    // saveData ve loadData metodlarını ekleyin:

    public void saveData() {
        if (plugin == null) {
            return;
        }
        
        try {
            // Regions klasörünü oluştur
            File regionsFolder = new File(plugin.getDataFolder(), "regions");
            if (!regionsFolder.exists()) {
                regionsFolder.mkdirs();
            }
            
            // Tüm bölgeleri kaydet
            for (Region region : regions.values()) {
                File regionFile = new File(regionsFolder, region.getName() + ".yml");
                FileConfiguration config = new YamlConfiguration();
                
                Map<String, Object> data = region.serialize();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    config.set(entry.getKey(), entry.getValue());
                }
                
                config.save(regionFile);
            }
            
            // Bölge listesi dosyasını kaydet
            File regionListFile = new File(plugin.getDataFolder(), "regionList.yml");
            FileConfiguration config = new YamlConfiguration();
            config.set("regions", new ArrayList<>(regions.keySet()));
            config.save(regionListFile);
            
            plugin.getLogger().info("Bölge verileri başarıyla kaydedildi.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Bölge verileri kaydedilirken bir hata oluştu", e);
        }
    }

    public void loadData() {
        if (plugin == null) {
            return;
        }
        
        try {
            regions.clear();
            
            // Bölge listesini yükle
            File regionListFile = new File(plugin.getDataFolder(), "regionList.yml");
            if (!regionListFile.exists()) {
                return;
            }
            
            FileConfiguration listConfig = YamlConfiguration.loadConfiguration(regionListFile);
            List<String> regionNames = listConfig.getStringList("regions");
            
            // Regions klasörünü kontrol et
            File regionsFolder = new File(plugin.getDataFolder(), "regions");
            if (!regionsFolder.exists()) {
                return;
            }
            
            // Her bölge için verileri yükle
            for (String regionName : regionNames) {
                File regionFile = new File(regionsFolder, regionName + ".yml");
                if (!regionFile.exists()) {
                    continue;
                }
                
                FileConfiguration config = YamlConfiguration.loadConfiguration(regionFile);
                String name = config.getString("name");
                String worldName = config.getString("worldName");
                int minX = config.getInt("minX");
                int minY = config.getInt("minY");
                int minZ = config.getInt("minZ");
                int maxX = config.getInt("maxX");
                int maxY = config.getInt("maxY");
                int maxZ = config.getInt("maxZ");
                String ownerTeamName = config.getString("ownerTeam");
                
                // Dünya ve takım kontrolü
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Bölge yüklenirken dünya bulunamadı: " + worldName);
                    continue;
                }
                
                Team ownerTeam = teamManager.getTeam(ownerTeamName);
                if (ownerTeam == null) {
                    plugin.getLogger().warning("Bölge yüklenirken takım bulunamadı: " + ownerTeamName);
                    continue;
                }
                
                // Bölgeyi oluşturmak için tam koordinat noktaları oluştur
                Location minLocation = new Location(world, minX, minY, minZ);
                Location maxLocation = new Location(world, maxX, maxY, maxZ);
                
                // Region sınıfını çağırırken koordinatların doğru sırayla kullanıldığından emin ol
                Region region = new Region(name, minLocation, maxLocation, ownerTeam);
                
                // Bölgenin koordinatlarını doğrudan ayarlamak için getter ve setter'lar eklemek daha güvenli
                // ancak bu örnekte Region sınıfı zaten doğru şekilde yapılandırılmış
            
                regions.put(name.toLowerCase(), region);
            }
            
            plugin.getLogger().info("Bölge verileri başarıyla yüklendi: " + regions.size() + " bölge");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Bölge verileri yüklenirken bir hata oluştu", e);
        }
    }
    
    // onDisable için eklenen temizleme metodu
    public void cleanup() {
        clearAllVisualizations();
    }
}