package me.d4rkk.aetherplugins.tntrun.menus;

import dev.slickcollections.kiwizin.libraries.menu.UpdatablePlayerMenu;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.game.AbstractSkyWars;
import me.d4rkk.aetherplugins.tntrun.game.enums.SkyWarsMode;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import dev.slickcollections.kiwizin.utils.StringUtils;
import dev.slickcollections.kiwizin.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MenuPlay extends UpdatablePlayerMenu {
  
  private final SkyWarsMode mode;
  
  public MenuPlay(Profile profile, SkyWarsMode mode) {
    super(profile.getPlayer(), "TnT " + mode.getName(), !"hentai".equals("hentai") ? 4 : 3);
    this.mode = mode;
    
    this.update();
    this.register(Main.getInstance(), 20);
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
              EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
              AbstractSkyWars game = AbstractSkyWars.findRandom(this.mode);
              if (game != null) {
                this.player.sendMessage(Language.lobby$npc$play$connect);
                game.join(profile);
              }
            }
          }
        }
      }
    }
  }
  
  @Override
  public void update() {

    int waiting = AbstractSkyWars.getWaiting(this.mode);
    int playing = AbstractSkyWars.getPlaying(this.mode);
    
    this.setItem(13, BukkitUtils.deserializeItemStack("TNT : 1 : nome>&aTnt : desc>\n&fEm espera: &7" + StringUtils.formatNumber(waiting) + "\n&fJogando: &7" + StringUtils.formatNumber(playing) + "\n \n&eClique para jogar!"));

  }
  
  public void cancel() {
    super.cancel();
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
