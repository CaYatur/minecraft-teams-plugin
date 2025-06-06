package com.example.teamsplugin.listeners;

import com.example.teamsplugin.managers.RegionManager;
import com.example.teamsplugin.managers.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {
    private final TeamManager teamManager;
    private final RegionManager regionManager;

    public BlockListener(TeamManager teamManager, RegionManager regionManager) {
        this.teamManager = teamManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!regionManager.canPlaceBlock(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Bu bölgede blok koyamazsınız! Bu bölge başka bir takıma ait.");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!regionManager.canBreakBlock(event.getPlayer(), event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Bu bölgede blok kıramazsınız! Bu bölge başka bir takıma ait.");
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        
        // TNT yakma kontrolü
        if (clickedBlock != null && event.hasItem()) {
            ItemStack item = event.getItem();
            
            // Çakmak ile TNT yakma
            if (item.getType() == Material.FLINT_AND_STEEL) {
                // Tıklanan blok TNT veya yanındaki blok TNT ise
                if (clickedBlock.getType() == Material.TNT || 
                    (clickedBlock.getRelative(0, 1, 0).getType() == Material.TNT)) {
                    
                    // Oyuncu bu bölgede değişiklik yapabilir mi?
                    if (!regionManager.canPlaceBlock(player, clickedBlock.getLocation())) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Bu bölgede TNT patlatamazsınız! Bu bölge başka bir takıma ait.");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        
        // Su/lav koyma kontrolü
        if (!regionManager.canPlaceBlock(player, block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Bu bölgede sıvı koyamazsınız! Bu bölge başka bir takıma ait.");
        }
    }
}