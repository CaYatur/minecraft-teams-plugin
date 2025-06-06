package com.example.teamsplugin.commands;

import com.example.teamsplugin.managers.TeamManager;
import com.example.teamsplugin.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamCommands implements CommandExecutor, TabCompleter {
    private TeamManager teamManager;
    private final List<String> SUBCOMMANDS = Arrays.asList("create", "join", "leave", "info", "list", "help");
    private final List<String> COLORS = Arrays.asList("RED", "BLUE", "GREEN", "YELLOW", "PURPLE", "AQUA", "GOLD", "GRAY");

    public TeamCommands(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMenu(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreateCommand(sender, args);
            case "join":
                return handleJoinCommand(sender, args);
            case "leave":
                return handleLeaveCommand(sender, args);
            case "info":
                return handleInfoCommand(sender, args);
            case "list":
                return handleListCommand(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Bilinmeyen alt komut. /team help yazarak yardım alabilirsiniz.");
                return true;
        }
    }

    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Kullanım: /team create <takım-adı> <renk>");
            return true;
        }
            
        String teamName = args[1];
        String teamColor = args[2].toUpperCase();
            
        if (teamManager.teamExists(teamName)) {
            sender.sendMessage(ChatColor.RED + "Bu takım zaten mevcut!");
            return true;
        }
            
        if (!COLORS.contains(teamColor)) {
            sender.sendMessage(ChatColor.RED + "Geçersiz renk! Kullanılabilir renkler: " + String.join(", ", COLORS));
            return true;
        }
            
        teamManager.createTeam(teamName, teamColor);
        sender.sendMessage(ChatColor.GREEN + "Takım " + teamName + " başarıyla oluşturuldu!");
        return true;
    }

    private boolean handleJoinCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Kullanım: /team join <takım-adı> [oyuncu]");
            return true;
        }
            
        String teamName = args[1];
        Player player;
            
        if (args.length >= 3) {
            player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Oyuncu bulunamadı!");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Konsol bir oyuncu belirtmelidir!");
                return true;
            }
            player = (Player) sender;
        }
            
        if (!teamManager.teamExists(teamName)) {
            sender.sendMessage(ChatColor.RED + "Takım bulunamadı!");
            return true;
        }
            
        Team currentTeam = teamManager.getPlayerTeam(player);
        if (currentTeam != null) {
            sender.sendMessage(ChatColor.RED + "Oyuncu zaten " + currentTeam.getName() + " takımında!");
            return true;
        }
            
        teamManager.addPlayerToTeam(teamName, player);
        sender.sendMessage(ChatColor.GREEN + player.getName() + " " + teamName + " takımına katıldı!");
        if (sender != player) {
            player.sendMessage(ChatColor.GREEN + teamName + " takımına eklendiniz!");
        }
        
        // Takım verilerini hemen kaydet
        teamManager.saveData();
        
        return true;
    }

    private boolean handleLeaveCommand(CommandSender sender, String[] args) {
        Player player;
        if (args.length >= 2) {
            player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Oyuncu bulunamadı!");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Konsol bir oyuncu belirtmelidir!");
                return true;
            }
            player = (Player) sender;
        }
            
        Team team = teamManager.getPlayerTeam(player);
        if (team == null) {
            sender.sendMessage(ChatColor.RED + "Oyuncu herhangi bir takımda değil!");
            return true;
        }
            
        teamManager.removePlayerFromTeam(team.getName(), player);
        sender.sendMessage(ChatColor.GREEN + player.getName() + " " + team.getName() + " takımından ayrıldı!");
        if (sender != player) {
            player.sendMessage(ChatColor.GREEN + team.getName() + " takımından çıkarıldınız!");
        }
        
        // Takım verilerini hemen kaydet
        teamManager.saveData();
        
        return true;
    }

    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            String teamName = args[1];
            Team team = teamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage(ChatColor.RED + "Takım bulunamadı!");
                return true;
            }
                
            sendTeamInfo(sender, team);
            return true;
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            Team team = teamManager.getPlayerTeam(player);
            if (team == null) {
                sender.sendMessage(ChatColor.RED + "Herhangi bir takımda değilsiniz!");
                return true;
            }
                
            sendTeamInfo(sender, team);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Kullanım: /team info <takım-adı>");
            return true;
        }
    }
    
    private boolean handleListCommand(CommandSender sender) {
        if (teamManager.getTeamNames().isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Henüz hiç takım oluşturulmadı.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GREEN + "=== Mevcut Takımlar ===");
        for (String teamName : teamManager.getTeamNames()) {
            Team team = teamManager.getTeam(teamName);
            int playerCount = team.getPlayers().size();
            sender.sendMessage(ChatColor.valueOf(team.getColor()) + team.getName() + ChatColor.WHITE + " - Oyuncu sayısı: " + playerCount);
        }
        return true;
    }
    
    private void sendTeamInfo(CommandSender sender, Team team) {
        sender.sendMessage(ChatColor.GREEN + "=== " + team.getName() + " Takım Bilgisi ===");
        sender.sendMessage(ChatColor.GREEN + "Renk: " + team.getColor());
        
        List<String> playerNames = new ArrayList<>();
        for (UUID uuid : team.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                playerNames.add(player.getName());
            }
        }
        
        if (playerNames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Bu takımda henüz oyuncu yok.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Oyuncular: " + String.join(", ", playerNames));
        }
    }
    
    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Takım Komutları Yardım ===");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam create <takım-adı> <renk> " + ChatColor.WHITE + "- Yeni bir takım oluşturur");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam join <takım-adı> [oyuncu] " + ChatColor.WHITE + "- Bir takıma katılır veya bir oyuncuyu ekler");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam leave [oyuncu] " + ChatColor.WHITE + "- Takımdan ayrılır veya bir oyuncuyu çıkarır");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam info [takım-adı] " + ChatColor.WHITE + "- Takım bilgilerini gösterir");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam list " + ChatColor.WHITE + "- Tüm takımları listeler");
        sender.sendMessage(ChatColor.YELLOW + "/cyteam help " + ChatColor.WHITE + "- Bu yardım menüsünü gösterir");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("info")) {
                return teamManager.getTeamNames().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("create")) {
                return Arrays.asList("KırmızıTakım", "MaviTakım", "YeşilTakım");
            } else if (args[0].equalsIgnoreCase("leave")) {
                return null; // Tüm online oyuncuları önerir
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                return COLORS.stream()
                        .filter(s -> s.startsWith(args[2].toUpperCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("join")) {
                return null; // Tüm online oyuncuları önerir
            }
        }
        return new ArrayList<>();
    }
}