package me.d4rkk.aetherplugins.tntrun.menus;

import dev.slickcollections.kiwizin.libraries.menu.PlayerMenu;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.cosmetics.Cosmetic;

import me.d4rkk.aetherplugins.tntrun.menus.cosmetics.MenuCosmetics;

import dev.slickcollections.kiwizin.utils.BukkitUtils;
import dev.slickcollections.kiwizin.utils.enums.EnumSound;
import me.d4rkk.aetherplugins.tntrun.cosmetics.types.*;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuShop extends PlayerMenu {
  
  public MenuShop(Profile profile) {
    super(profile.getPlayer(), "Loja - Tnt Run", 3);

    
    List<WinAnimation> animations = Cosmetic.listByType(WinAnimation.class);
    long max = animations.size();
    long owned = animations.stream().filter(animation -> animation.has(profile)).count();
    long percentage = max == 0 ? 100 : (owned * 100) / max;
    String color = (owned == max) ? "&a" : (owned > max / 2) ? "&7" : "&c";
    animations.clear();
    this.setItem(13, BukkitUtils.deserializeItemStack(
        "DRAGON_EGG : 1 : nome>&aComemorações de Vitória : desc>&7Esbanje estilo nas suas vitórias\n&7com comemorações exclusivas.\n \n&fDesbloqueados: " + color + owned + "/" + max + " &8(" + percentage + "%)\n \n&eClique para comprar ou selecionar!"));
    

    this.register(Main.getInstance());
    this.open();
  }
  
  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getInventory())) {
      evt.setCancelled(true);
      
      if (evt.getWhoClicked().equals(this.player)) {
        Profile profile = Profile.getProfile(this.player.getName());
        if (profile == null) {
          this.player.closeInventory();
          return;
        }
        
        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getInventory())) {
          ItemStack item = evt.getCurrentItem();
          if (item != null && item.getType() != Material.AIR) {
            if (evt.getSlot() == 13) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuCosmetics<>(profile, "Comemorações", WinAnimation.class);
            }
          }
        }
      }
    }
  }
  
  public void cancel() {
    HandlerList.unregisterAll(this);
  }
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    if (evt.getPlayer().equals(this.player)) {
      this.cancel();
    }
  }
  
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent evt) {
    if (evt.getPlayer().equals(this.player) && evt.getInventory().equals(this.getInventory())) {
      this.cancel();
    }
  }
}
