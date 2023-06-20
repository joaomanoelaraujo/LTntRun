package me.d4rkk.aetherplugins.tntrun.menus.cosmetics.animations;

import dev.slickcollections.kiwizin.libraries.menu.PlayerMenu;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.cosmetics.Cosmetic;
import me.d4rkk.aetherplugins.tntrun.cosmetics.types.FallEffect;
import me.d4rkk.aetherplugins.tntrun.menus.MenuShop;
import me.d4rkk.aetherplugins.tntrun.menus.cosmetics.MenuCosmetics;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import dev.slickcollections.kiwizin.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuAnimations extends PlayerMenu {
  
  public MenuAnimations(Profile profile) {
    super(profile.getPlayer(), "Sky Wars - Animações", 4);

    List<FallEffect> falleffects = Cosmetic.listByType(FallEffect.class);
   long max = falleffects.size();
   long owned = falleffects.stream().filter(kprojectileEffect -> kprojectileEffect.has(profile)).count();
   long percentage = max == 0 ? 100 : (owned * 100) / max;
   String color = (owned == max) ? "&a" : (owned > max / 2) ? "&7" : "&c";
    falleffects.clear();
    this.setItem(13, BukkitUtils.deserializeItemStack(
        "DIAMOND_BOOTS : 1 : nome>&aAnimações de Queda : desc>&7Quando você levar dano de queda\n&7irá aparecer partículas.\n \n&fDesbloqueados: " + color + owned + "/" + max + " &8(" + percentage + "%)\n \n&eClique para comprar ou selecionar!"));
    

    this.setItem(31, BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cVoltar : desc>&7Para a loja."));
    
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
            if (evt.getSlot() == 31) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuShop(profile);
            } else if (evt.getSlot() == 13) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuCosmetics<>(profile, "Animações de Queda", FallEffect.class);
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
