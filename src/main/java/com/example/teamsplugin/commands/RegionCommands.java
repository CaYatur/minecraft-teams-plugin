package com.example.teamsplugin.commands;

import com.example.teamsplugin.managers.RegionManager;
import com.example.teamsplugin.managers.TeamManager;
import com.example.teamsplugin.models.Region;
import com.example.teamsplugin.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RegionCommands implements CommandExecutor, TabCompleter {
    private RegionManager regionManager;
    private TeamManager teamManager;
    private final List<String> SUBCOMMANDS = Arrays.asList("select", "create", "delete", "info", "list", "transfer", 
                                                      "visualize", "visualizeall", "visualizeteam", "stopvisualize", "help");

    public RegionCommands(RegionManager regionManager, TeamManager teamManager) {
        this.regionManager = regionManager;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyuncular kullanabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "select":
                return handleSelectCommand(player, args);
            case "create":
                return handleCreateCommand(player, args);
            case "delete":
                return handleDeleteCommand(player, args);
            case "info":
                return handleInfoCommand(player, args);
            case "list":
                return handleListCommand(player);
            case "transfer":
                return handleTransferCommand(player, args);
            case "visualize":
                return handleVisualizeCommand(player, args);
            case "visualizeall":
                return handleVisualizeAllCommand(player, args);
            case "visualizeteam":
                return handleVisualizeTeamCommand(player, args);
            case "stopvisualize":
                return handleStopVisualizeCommand(player, args);
            default:
                player.sendMessage(ChatColor.RED + "Bilinmeyen alt komut. /cyregion help yazarak yardım alabilirsiniz.");
                return true;
        }
    }

    private boolean handleSelectCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /cyregion select <1|2>");
            return true;
        }
        
        int pos;
        try {
            pos = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Lütfen 1 veya 2 girin.");
            return true;
        }
        
        if (pos != 1 && pos != 2) {
            player.sendMessage(ChatColor.RED + "Lütfen 1 veya 2 girin.");
            return true;
        }
        
        Location location = player.getLocation();
        regionManager.setPlayerSelection(player, pos, location);
        
        player.sendMessage(ChatColor.GREEN + "Konum " + pos + " seçildi: " + 
                     String.format("X:%d, Y:%d, Z:%d", 
                                  location.getBlockX(), 
                                  location.getBlockY(), 
                                  location.getBlockZ()));
        
        // İkinci seçim de yapıldıysa, toplam alanı göster
        Location[] selections = regionManager.getPlayerSelection(player);
        if (selections != null && selections[0] != null && selections[1] != null) {
            int minX = Math.min(selections[0].getBlockX(), selections[1].getBlockX());
            int minY = Math.min(selections[0].getBlockY(), selections[1].getBlockY());
            int minZ = Math.min(selections[0].getBlockZ(), selections[1].getBlockZ());
            
            int maxX = Math.max(selections[0].getBlockX(), selections[1].getBlockX());
            int maxY = Math.max(selections[0].getBlockY(), selections[1].getBlockY());
            int maxZ = Math.max(selections[0].getBlockZ(), selections[1].getBlockZ());
            
            int width = maxX - minX + 1;  // +1 çünkü seçilen bloklar da dahil
            int height = maxY - minY + 1;
            int depth = maxZ - minZ + 1;
            int volume = width * height * depth;
            
            player.sendMessage(ChatColor.GRAY + "Seçilen alan: " + width + "x" + height + "x" + depth + " = " + volume + " blok");
            
            // 3 blok genişletildiğinde olacak boyut
            int expandedWidth = width + 6;  // Her iki tarafa 3 blok = +6
            int expandedHeight = height + 6;
            int expandedDepth = depth + 6;
            int expandedVolume = expandedWidth * expandedHeight * expandedDepth;
            
            player.sendMessage(ChatColor.GRAY + "3 blok genişletildiğinde: " + expandedWidth + "x" + expandedHeight + "x" + expandedDepth + " = " + expandedVolume + " blok olacak");
            player.sendMessage(ChatColor.GRAY + "Korunan alan: Min=(" + (minX-3) + "," + (minY-3) + "," + (minZ-3) + "), Max=(" + (maxX+3) + "," + (maxY+3) + "," + (maxZ+3) + ")");
        }
        
        return true;
    }

    private boolean handleCreateCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Kullanım: /region create <bölge-adı> <takım-adı>");
            return true;
        }
        
        String regionName = args[1];
        String teamName = args[2];
        
        if (!teamManager.teamExists(teamName)) {
            player.sendMessage(ChatColor.RED + "Takım bulunamadı!");
            return true;
        }
        
        if (regionManager.getRegionByName(regionName) != null) {
            player.sendMessage(ChatColor.RED + "Bu isimde bir bölge zaten var!");
            return true;
        }
        
        Location[] selection = regionManager.getPlayerSelection(player);
        if (selection == null || selection[0] == null || selection[1] == null) {
            player.sendMessage(ChatColor.RED + "Lütfen önce her iki köşeyi de seçin (/region select 1 ve /region select 2)");
            return true;
        }
        
        Region region = regionManager.createRegion(player, regionName, teamName);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Bölge oluşturulurken bir hata oluştu!");
            return true;
        }
        
        player.sendMessage(ChatColor.GREEN + "\"" + regionName + "\" bölgesi " + teamName + " takımı için başarıyla oluşturuldu!");
        player.sendMessage(ChatColor.GRAY + "Hacim: " + region.getVolume() + " blok");
        return true;
    }
    
    private boolean handleDeleteCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /region delete <bölge-adı>");
            return true;
        }
        
        String regionName = args[1];
        Region region = regionManager.getRegionByName(regionName);
        
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Bu isimde bir bölge bulunamadı!");
            return true;
        }
        
        boolean deleted = regionManager.deleteRegion(regionName);
        if (deleted) {
            player.sendMessage(ChatColor.GREEN + "\"" + regionName + "\" bölgesi başarıyla silindi!");
        } else {
            player.sendMessage(ChatColor.RED + "Bölge silinirken bir hata oluştu!");
        }
        
        return true;
    }

    private boolean handleInfoCommand(Player player, String[] args) {
        if (args.length >= 2) {
            String regionName = args[1];
            Region region = regionManager.getRegionByName(regionName);
            
            if (region == null) {
                player.sendMessage(ChatColor.RED + "Bu isimde bir bölge bulunamadı!");
                return true;
            }
            
            sendRegionInfo(player, region);
            return true;
        } else {
            Location location = player.getLocation();
            Region region = regionManager.getRegionAt(location);
            
            if (region == null) {
                player.sendMessage(ChatColor.YELLOW + "Şu anda bir bölgede değilsiniz.");
                player.sendMessage(ChatColor.YELLOW + "Belirli bir bölge hakkında bilgi almak için: /region info <bölge-adı>");
                return true;
            }
            
            sendRegionInfo(player, region);
            return true;
        }
    }

    private boolean handleListCommand(Player player) {
        List<Region> regions = regionManager.getRegions();
        
        if (regions.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Henüz hiç bölge oluşturulmadı.");
            return true;
        }
        
        player.sendMessage(ChatColor.GREEN + "=== Mevcut Bölgeler ===");
        for (Region region : regions) {
            Team owner = region.getOwner();
            ChatColor teamColor = ChatColor.valueOf(owner.getColor());
            player.sendMessage(teamColor + "• " + ChatColor.WHITE + region.getName() + " - " + 
                               teamColor + owner.getName() + ChatColor.GRAY + " (" + region.getVolume() + " blok)");
        }
        return true;
    }
    
    private boolean handleTransferCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Kullanım: /region transfer <bölge-adı> <yeni-takım>");
            return true;
        }
        
        String regionName = args[1];
        String teamName = args[2];
        
        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Bu isimde bir bölge bulunamadı!");
            return true;
        }
        
        if (!teamManager.teamExists(teamName)) {
            player.sendMessage(ChatColor.RED + "Bu isimde bir takım bulunamadı!");
            return true;
        }
        
        boolean transferred = regionManager.transferRegion(regionName, teamName);
        if (transferred) {
            player.sendMessage(ChatColor.GREEN + "\"" + regionName + "\" bölgesi artık " + teamName + " takımına ait!");
        } else {
            player.sendMessage(ChatColor.RED + "Bölge sahipliği değiştirilirken bir hata oluştu!");
        }
        
        return true;
    }
    
    private boolean handleStopVisualizeCommand(Player player, String[] args) {
        if (args.length >= 2) {
            String param = args[1].toLowerCase();
            
            if (param.equals("all")) {
                // Tüm görselleştirmeleri durdur
                regionManager.clearAllVisualizations();
                player.sendMessage(ChatColor.GREEN + "Tüm bölge görselleştirmeleri durduruldu.");
            } else {
                // Belirli bir bölgenin görselleştirmesini durdur
                Region region = regionManager.getRegionByName(param);
                if (region != null) {
                    regionManager.stopGlobalVisualization(param);
                    player.sendMessage(ChatColor.GREEN + "\"" + param + "\" bölgesinin görselleştirmesi durduruldu.");
                } else {
                    player.sendMessage(ChatColor.RED + "Bu isimde bir bölge bulunamadı!");
                }
            }
        } else {
            // Sadece oyuncunun kendi görselleştirmesini durdur
            regionManager.stopVisualization(player);
            player.sendMessage(ChatColor.GREEN + "Bölge görselleştirmesi durduruldu.");
        }
        
        return true;
    }

    private boolean handleVisualizeCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /cyregion visualize <bölge-adı> [all|global] [süre|sonsuz]");
            return true;
        }
        
        String regionName = args[1];
        int duration = 30; // Varsayılan 30 saniye
        boolean global = false; // Varsayılan olarak sadece komutu kullanan oyuncuya göster
        
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("all") || args[2].equalsIgnoreCase("global")) {
                global = true;
                if (args.length >= 4) {
                    if (args[3].equalsIgnoreCase("infinite") || args[3].equalsIgnoreCase("sonsuz")) {
                        duration = -1;
                    } else {
                        try {
                            duration = Integer.parseInt(args[3]);
                            if (duration <= 0 || duration > 3600) {
                                player.sendMessage(ChatColor.RED + "Süre 1-3600 saniye arasında olmalıdır veya 'sonsuz' yazabilirsiniz.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Geçersiz süre. Lütfen saniye cinsinden bir sayı girin veya 'sonsuz' yazın.");
                            return true;
                        }
                    }
                }
            } else if (args[2].equalsIgnoreCase("infinite") || args[2].equalsIgnoreCase("sonsuz")) {
                duration = -1;
            } else {
                try {
                    duration = Integer.parseInt(args[2]);
                    if (duration <= 0 || duration > 3600) {
                        player.sendMessage(ChatColor.RED + "Süre 1-3600 saniye arasında olmalıdır veya 'sonsuz' yazabilirsiniz.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Geçersiz parametre. Kullanım: /cyregion visualize <bölge-adı> [all|global] [süre|sonsuz]");
                    return true;
                }
            }
        }
        
        Region region = regionManager.getRegionByName(regionName);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Bu isimde bir bölge bulunamadı!");
            return true;
        }
        
        if (global) {
            // Tüm oyunculara göster
            regionManager.visualizeRegionForAll(regionName, duration);
            if (duration > 0) {
                player.sendMessage(ChatColor.GREEN + "\"" + regionName + "\" bölgesi " + duration + " saniye boyunca tüm oyunculara görselleştiriliyor...");
            } else {
                player.sendMessage(ChatColor.GREEN + "\"" + regionName + "\" bölgesi sonsuz süre boyunca tüm oyunculara görselleştiriliyor...");
                player.sendMessage(ChatColor.GRAY + "Durdurmak için: /cyregion stopvisualize " + regionName);
            }
        } else {
            // Sadece komutu kullanan oyuncuya göster
            regionManager.visualizeRegion(player, regionName, duration);
            if (duration > 0) {
                player.sendMessage(ChatColor.GREEN + "\"" + regionName + "\" bölgesi " + duration + " saniye boyunca görselleştiriliyor...");
            } else {
                player.sendMessage(ChatColor.GREEN + "\"" + regionName + "\" bölgesi sonsuz süre boyunca görselleştiriliyor...");
                player.sendMessage(ChatColor.GRAY + "Durdurmak için: /cyregion stopvisualize");
            }
        }
        
        return true;
    }

    private boolean handleVisualizeAllCommand(Player player, String[] args) {
        int duration = 30; // Varsayılan 30 saniye
        boolean global = false; // Varsayılan olarak sadece komutu kullanan oyuncuya göster
        
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("infinite") || args[1].equalsIgnoreCase("sonsuz")) {
                duration = -1; // Sonsuz süre
            } else if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("global")) {
                global = true; // Tüm oyunculara göster
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("infinite") || args[2].equalsIgnoreCase("sonsuz")) {
                        duration = -1;
                    } else {
                        try {
                            duration = Integer.parseInt(args[2]);
                            if (duration <= 0 || duration > 3600) {
                                player.sendMessage(ChatColor.RED + "Süre 1-3600 saniye arasında olmalıdır veya 'sonsuz' yazabilirsiniz.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Geçersiz süre. Lütfen saniye cinsinden bir sayı girin veya 'sonsuz' yazın.");
                            return true;
                        }
                    }
                }
            } else {
                try {
                    duration = Integer.parseInt(args[1]);
                    if (duration <= 0 || duration > 3600) {
                        player.sendMessage(ChatColor.RED + "Süre 1-3600 saniye arasında olmalıdır veya 'sonsuz' yazabilirsiniz.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Geçersiz parametre. Kullanım: /cyregion visualizeall [global|all] [süre|sonsuz]");
                    return true;
                }
            }
        }
        
        // Tüm bölgeleri listesini al
        List<Region> regions = regionManager.getRegions();
        
        if (regions.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Henüz hiç bölge oluşturulmadı.");
            return true;
        }
        
        List<String> regionNames = regions.stream().map(Region::getName).collect(Collectors.toList());
        
        if (global) {
            // Tüm oyunculara göster
            regionManager.visualizeRegionsForAll(regionNames, duration);
            if (duration > 0) {
                player.sendMessage(ChatColor.GREEN + "Tüm bölgeler " + duration + " saniye boyunca tüm oyunculara görselleştiriliyor...");
            } else {
                player.sendMessage(ChatColor.GREEN + "Tüm bölgeler sonsuz süre boyunca tüm oyunculara görselleştiriliyor...");
                player.sendMessage(ChatColor.GRAY + "Durdurmak için: /cyregion stopvisualize all");
            }
        } else {
            // Sadece komutu kullanan oyuncuya göster
            regionManager.visualizeRegions(player, regionNames, duration);
            if (duration > 0) {
                player.sendMessage(ChatColor.GREEN + "Tüm bölgeler " + duration + " saniye boyunca görselleştiriliyor...");
            } else {
                player.sendMessage(ChatColor.GREEN + "Tüm bölgeler sonsuz süre boyunca görselleştiriliyor...");
                player.sendMessage(ChatColor.GRAY + "Durdurmak için: /cyregion stopvisualize");
            }
        }
        
        return true;
    }

    private boolean handleVisualizeTeamCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Kullanım: /cyregion visualizeteam <takım-adı> [all|global] [süre|sonsuz]");
            return true;
        }
        
        String teamName = args[1];
        Team team = teamManager.getTeam(teamName);
        if (team == null) {
            player.sendMessage(ChatColor.RED + "Bu isimde bir takım bulunamadı!");
            return true;
        }
        
        // Parametreleri kontrol et
        int duration = 30; // Varsayılan 30 saniye
        boolean global = false; // Varsayılan olarak sadece komutu kullanan oyuncuya göster
        
        if (args.length >= 3) {
            if (args[2].equalsIgnoreCase("all") || args[2].equalsIgnoreCase("global")) {
                global = true;
                if (args.length >= 4) {
                    if (args[3].equalsIgnoreCase("infinite") || args[3].equalsIgnoreCase("sonsuz")) {
                        duration = -1;
                    } else {
                        try {
                            duration = Integer.parseInt(args[3]);
                            if (duration <= 0 || duration > 3600) {
                                player.sendMessage(ChatColor.RED + "Süre 1-3600 saniye arasında olmalıdır veya 'sonsuz' yazabilirsiniz.");
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Geçersiz süre. Lütfen saniye cinsinden bir sayı girin veya 'sonsuz' yazın.");
                            return true;
                        }
                    }
                }
            } else if (args[2].equalsIgnoreCase("infinite") || args[2].equalsIgnoreCase("sonsuz")) {
                duration = -1;
            } else {
                try {
                    duration = Integer.parseInt(args[2]);
                    if (duration <= 0 || duration > 3600) {
                        player.sendMessage(ChatColor.RED + "Süre 1-3600 saniye arasında olmalıdır veya 'sonsuz' yazabilirsiniz.");
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Geçersiz parametre. Kullanım: /cyregion visualizeteam <takım-adı> [all|global] [süre|sonsuz]");
                    return true;
                }
            }
        }
        
        // Takıma ait bölgeleri bul
        List<String> teamRegionNames = new ArrayList<>();
        for (Region region : regionManager.getRegions()) {
            if (region.getOwner().getName().equalsIgnoreCase(teamName)) {
                teamRegionNames.add(region.getName());
            }
        }
        
        if (teamRegionNames.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Bu takıma ait hiç bölge bulunamadı.");
            return true;
        }
        
        if (global) {
            // Tüm oyunculara göster
            regionManager.visualizeRegionsForAll(teamRegionNames, duration);
            if (duration > 0) {
                player.sendMessage(ChatColor.GREEN + teamName + " takımının tüm bölgeleri " + duration + " saniye boyunca tüm oyunculara görselleştiriliyor...");
            } else {
                player.sendMessage(ChatColor.GREEN + teamName + " takımının tüm bölgeleri sonsuz süre boyunca tüm oyunculara görselleştiriliyor...");
                player.sendMessage(ChatColor.GRAY + "Durdurmak için: /cyregion stopvisualize all");
            }
        } else {
            // Sadece komutu kullanan oyuncuya göster
            regionManager.visualizeRegions(player, teamRegionNames, duration);
            if (duration > 0) {
                player.sendMessage(ChatColor.GREEN + teamName + " takımının tüm bölgeleri " + duration + " saniye boyunca görselleştiriliyor...");
            } else {
                player.sendMessage(ChatColor.GREEN + teamName + " takımının tüm bölgeleri sonsuz süre boyunca görselleştiriliyor...");
                player.sendMessage(ChatColor.GRAY + "Durdurmak için: /cyregion stopvisualize");
            }
        }
        
        return true;
    }

    private void sendRegionInfo(Player player, Region region) {
        Team owner = region.getOwner();
        ChatColor teamColor = ChatColor.valueOf(owner.getColor());
        
        player.sendMessage(ChatColor.GREEN + "=== Bölge Bilgisi: " + region.getName() + " ===");
        player.sendMessage(ChatColor.GREEN + "Sahibi: " + teamColor + owner.getName());
        player.sendMessage(ChatColor.GREEN + "Hacim: " + ChatColor.YELLOW + region.getVolume() + " blok");
        player.sendMessage(ChatColor.GREEN + "Minimum köşe: " + ChatColor.YELLOW + formatLocation(region.getMin()));
        player.sendMessage(ChatColor.GREEN + "Maximum köşe: " + ChatColor.YELLOW + formatLocation(region.getMax()));
        player.sendMessage(ChatColor.GRAY + "Bu bölgeyi görselleştirmek için: /region visualize " + region.getName());
    }
    
    private String formatLocation(Location location) {
        return "X:" + location.getBlockX() + ", Y:" + location.getBlockY() + ", Z:" + location.getBlockZ();
    }
    
    private void sendHelpMenu(Player player) {
        player.sendMessage(ChatColor.GREEN + "=== Bölge Komutları Yardım ===");
        player.sendMessage(ChatColor.YELLOW + "/cyregion select <1|2> " + ChatColor.WHITE + "- Bölge köşelerini seçer");
        player.sendMessage(ChatColor.YELLOW + "/cyregion create <bölge-adı> <takım-adı> " + ChatColor.WHITE + "- Seçili köşeler arasında bir takım bölgesi oluşturur");
        player.sendMessage(ChatColor.YELLOW + "/cyregion delete <bölge-adı> " + ChatColor.WHITE + "- Belirtilen bölgeyi siler");
        player.sendMessage(ChatColor.YELLOW + "/cyregion info [bölge-adı] " + ChatColor.WHITE + "- Bölge hakkında bilgi verir");
        player.sendMessage(ChatColor.YELLOW + "/cyregion list " + ChatColor.WHITE + "- Tüm bölgeleri listeler");
        player.sendMessage(ChatColor.YELLOW + "/cyregion transfer <bölge-adı> <takım-adı> " + ChatColor.WHITE + "- Bölgenin sahipliğini değiştirir");
        player.sendMessage(ChatColor.YELLOW + "/cyregion visualize <bölge-adı> [all] [süre|sonsuz] " + ChatColor.WHITE + "- Bölgeyi parçacıklarla görselleştirir");
        player.sendMessage(ChatColor.YELLOW + "/cyregion visualizeall [all] [süre|sonsuz] " + ChatColor.WHITE + "- Tüm bölgeleri görselleştirir");
        player.sendMessage(ChatColor.YELLOW + "/cyregion visualizeteam <takım-adı> [all] [süre|sonsuz] " + ChatColor.WHITE + "- Takımın tüm bölgelerini görselleştirir");
        player.sendMessage(ChatColor.YELLOW + "/cyregion stopvisualize [bölge-adı|all] " + ChatColor.WHITE + "- Bölge görselleştirmesini durdurur");
        player.sendMessage(ChatColor.YELLOW + "/cyregion help " + ChatColor.WHITE + "- Bu yardım menüsünü gösterir");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("select")) {
                return Arrays.asList("1", "2");
            } else if (args[0].equalsIgnoreCase("delete") || 
                       args[0].equalsIgnoreCase("info") || 
                       args[0].equalsIgnoreCase("transfer") ||
                       args[0].equalsIgnoreCase("visualize")) {
                return new ArrayList<>(regionManager.getRegionNames()).stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("transfer")) {
                return teamManager.getTeamNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("visualize")) {
                return Arrays.asList("10", "30", "60", "120");
            }
        }
        return new ArrayList<>();
    }
}