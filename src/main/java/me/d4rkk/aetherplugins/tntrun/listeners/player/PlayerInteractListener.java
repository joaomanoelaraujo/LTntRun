package me.d4rkk.aetherplugins.tntrun.listeners.player;

import dev.slickcollections.kiwizin.Core;
import dev.slickcollections.kiwizin.game.GameState;
import dev.slickcollections.kiwizin.libraries.npclib.api.event.NPCLeftClickEvent;
import dev.slickcollections.kiwizin.libraries.npclib.api.event.NPCRightClickEvent;
import dev.slickcollections.kiwizin.libraries.npclib.api.npc.NPC;
import dev.slickcollections.kiwizin.menus.MenuDeliveries;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.cmd.tntgame.BuildCommand;
import me.d4rkk.aetherplugins.tntrun.cmd.tntgame.CreateCommand;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import me.d4rkk.aetherplugins.tntrun.game.enums.TnTGameMode;
import me.d4rkk.aetherplugins.tntrun.menus.MenuPlay;
import me.d4rkk.aetherplugins.tntrun.menus.MenuStatsNPC;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import net.minecraft.server.v1_8_R3.DamageSource;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {
  
  @EventHandler
  public void onNPCLeftClick(NPCLeftClickEvent evt) {
    Player player = evt.getPlayer();
    Profile profile = Profile.getProfile(player.getName());
    
    if (profile != null) {
      NPC npc = evt.getNPC();
      if (npc.data().has("play-npc")) {
        new MenuPlay(profile, TnTGameMode.fromName(npc.data().get("play-npc")));
      }
    }
  }
  
  @EventHandler
  public void onNPCRightClick(NPCRightClickEvent evt) {
    Player player = evt.getPlayer();
    Profile profile = Profile.getProfile(player.getName());
    
    if (profile != null) {
      NPC npc = evt.getNPC();
      if (npc.data().has("play-npc")) {
        new MenuPlay(profile, TnTGameMode.fromName(npc.data().get("play-npc")));
      } else if (npc.data().has("delivery-npc")) {
        new MenuDeliveries(profile);
      } else if (npc.data().has("stats-npc")) {
        new MenuStatsNPC(profile);
      }
    }
  }
  
  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerInteract(PlayerInteractEvent evt) {
    Player player = evt.getPlayer();
    Profile profile = Profile.getProfile(player.getName());
    
    if (profile != null) {
      if (CreateCommand.CREATING.containsKey(player) && CreateCommand.CREATING.get(player)[0].equals(player.getWorld())) {
        ItemStack item = player.getItemInHand();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
          CreateCommand.handleClick(profile, item.getItemMeta().getDisplayName(), evt);
        }
      }
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game == null && !BuildCommand.hasBuilder(player)) {
        evt.setCancelled(true);
      } else if (game != null && (game.getState() != GameState.EMJOGO || game.isSpectator(player))) {
        if (!(game.getState() == GameState.AGUARDANDO)) {
          player.updateInventory();
          evt.setCancelled(true);
        } else if (game.getState() == GameState.AGUARDANDO && evt.getClickedBlock() != null && evt.getClickedBlock().getType() == Material.GOLD_PLATE) {
          evt.setCancelled(false);
          InventoryHolder ih = ((InventoryHolder) evt.getClickedBlock().getLocation().clone().subtract(0, 0.5, 0).getBlock().getState());
          // Limpar e adicionar os foguetes novamente.
          ih.getInventory().clear();
          for (int i = 0; i < 10; i++) {
            ih.getInventory().addItem(BukkitUtils.deserializeItemStack("FIREWORK : 64"));
          }
        } else if (game.getState() == GameState.AGUARDANDO && evt.getClickedBlock() != null && evt.getClickedBlock().getType() != Material.GOLD_PLATE) {
          evt.setCancelled(true);
        }
          }
      }
  }
  
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent evt) {
    if (evt.getTo().getBlockY() != evt.getFrom().getBlockY() && evt.getTo().getBlockY() < 0) {
      Player player = evt.getPlayer();
      Profile profile = Profile.getProfile(player.getName());
      
      if (profile != null) {
        TnTGameAb game = profile.getGame(TnTGameAb.class);
        if (game == null) {
          player.teleport(Core.getLobby());
        } else {
          if (game.getState() != GameState.EMJOGO || game.isSpectator(player)) {
            player.teleport(game.getCubeId().getCenterLocation());
          } else {
            ((CraftPlayer) player).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, (float) player.getMaxHealth());
          }
        }
      }
    }
  }
}
