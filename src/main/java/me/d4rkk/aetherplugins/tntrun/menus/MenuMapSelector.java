package me.d4rkk.aetherplugins.tntrun.menus;

import dev.slickcollections.kiwizin.libraries.menu.UpdatablePlayerPagedMenu;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.game.AbstractSkyWars;
import me.d4rkk.aetherplugins.tntrun.game.enums.SkyWarsMode;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import dev.slickcollections.kiwizin.utils.TimeUtils;
import dev.slickcollections.kiwizin.utils.enums.EnumSound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuMapSelector extends UpdatablePlayerPagedMenu {
  
  private Profile profile;
  private SkyWarsMode mode;
  private boolean can = true;
  private Map<ItemStack, String> maps = new HashMap<>();
  private Map<String, List<AbstractSkyWars>> games = new HashMap<>();
  
  public MenuMapSelector(Profile profile, SkyWarsMode mode) {
    super(profile.getPlayer(), "Mapas - SkyWars " + mode.getName(), 6);
    this.profile = profile;
    this.mode = mode;
    this.previousPage = 19;
    this.nextPage = 26;
    this.onlySlots(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
    
    this.removeSlotsWith(BukkitUtils.deserializeItemStack("INK_SACK:1 : 1 : nome>&cVoltar"), 49);
    
    this.update();
    this.register(Main.getInstance(), 20);
    this.open();
  }
  
  @EventHandler
  public void onInventoryClick(InventoryClickEvent evt) {
    if (evt.getInventory().equals(this.getCurrentInventory())) {
      evt.setCancelled(true);
      
      if (evt.getWhoClicked().equals(this.player)) {
        if (evt.getClickedInventory() != null && evt.getClickedInventory().equals(this.getCurrentInventory())) {
          ItemStack item = evt.getCurrentItem();
          
          if (item != null && item.getType() != Material.AIR) {
            if (evt.getSlot() == this.previousPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openPrevious();
            } else if (evt.getSlot() == this.nextPage) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              this.openNext();
            } else if (evt.getSlot() == 30) {
              EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
            } else if (evt.getSlot() == 49) {
              EnumSound.CLICK.play(this.player, 0.5F, 2.0F);
              new MenuPlay(this.profile, this.mode);
            } else {
              String mapName = this.maps.get(item);
              if (mapName != null && this.can) {
                EnumSound.ITEM_PICKUP.play(this.player, 0.5F, 2.0F);
                for (AbstractSkyWars game : this.games.get(mapName)) {
                  if (game.getState().canJoin() && game.getOnline() < game.getMaxPlayers()) {
                    this.player.sendMessage(Language.lobby$npc$play$connect);
                    this.profile.setStats("kCoreSkyWars", System.currentTimeMillis() + TimeUtils.getExpireIn(1), "lastmap");
                    game.join(this.profile);
                    break;
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  @Override
  public void update() {
    if (!this.player.hasPermission("kskywars.menu.selector") && profile.getStats("kCoreSkyWars", "lastmap") >= System.currentTimeMillis()) {
      this.can = false;
    }
    
    List<ItemStack> items = new ArrayList<>();
    this.games = AbstractSkyWars.getAsMap(this.mode);
    for (Map.Entry<String, List<AbstractSkyWars>> entry : this.games.entrySet()) {
      List<AbstractSkyWars> games = entry.getValue();
      ItemStack item = BukkitUtils.deserializeItemStack("EMPTY_MAP : 1 : nome>&b" + entry.getKey() + " : desc>&8Modo " + this.mode.getName() + "\n \n&7Salas disponíveis: &a" + games
          .size() + "\n \n&eClique com o botão esquerdo para jogar!");
      items.add(item);
      this.maps.put(item, entry.getKey());
    }
    
    if (this.lastListSize != -1 && this.lastListSize != items.size()) {
      items.clear();
      new MenuMapSelector(this.profile, this.mode);
      return;
    }
    
    this.removeSlotsWith(BukkitUtils.deserializeItemStack(Language.lobby$npc$play$menu$info$item.replace("{desc}", this.player.hasPermission("mskywars.menu.selector") ?
        Language.lobby$npc$play$menu$info$desc_not_limit :
        Language.lobby$npc$play$menu$info$desc_limit.replace("{limit}", this.can ? "0/1" : "1/1"))), 48);
    this.setItems(items);
  }
  
  public void cancel() {
    super.cancel();
    HandlerList.unregisterAll(this);
    this.profile = null;
    this.mode = null;
    this.maps.clear();
    this.maps = null;
    this.games.values().forEach(List::clear);
    this.games.clear();
    this.games = null;
  }
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    if (evt.getPlayer().equals(this.player)) {
      this.cancel();
    }
  }
  
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent evt) {
    if (evt.getPlayer().equals(this.player) && evt.getInventory().equals(this.getCurrentInventory())) {
      this.cancel();
    }
  }
}
