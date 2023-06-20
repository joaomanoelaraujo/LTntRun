package me.d4rkk.aetherplugins.tntrun.listeners.player;

import dev.slickcollections.kiwizin.game.GameState;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.cmd.tntgame.BuildCommand;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import me.d4rkk.aetherplugins.tntrun.game.enums.TnTGameMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerRestListener implements Listener {
  
  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game == null) {
        evt.setCancelled(true);
      } else {
        evt.setCancelled(game.getState() != GameState.EMJOGO || game.isSpectator(evt.getPlayer()));
      }
    } else {
      evt.setCancelled(evt.getItemDrop().getType().equals(Material.GOLD_PLATE));
    }
  }


      @EventHandler
      public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block blockAbove = player.getLocation().getBlock();
        Block blockBelow = blockAbove.getLocation().subtract(0, 1, 0).getBlock();
        Profile profile = Profile.getProfile(player.getName());
        TnTGameAb game = profile.getGame(TnTGameAb.class);
        if (game != null && game.getState() == GameState.EMJOGO && game.getMode() == TnTGameMode.SOLO) {
          if (isRemovableBlock(blockAbove.getType()) || isRemovableBlock(blockBelow.getType())) {

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), new BukkitRunnable() {
              @Override
              public void run() {
                if (isRemovableBlock(blockAbove.getType())) {
                  blockAbove.setType(Material.AIR);
                }
                if (isRemovableBlock(blockBelow.getType())) {
                  blockBelow.setType(Material.AIR);
                }
              }
            }, 12L); // 20 ticks = 1 segundo
          }
        }
      }
      private boolean isRemovableBlock(Material material) {
        return material == Material.SAND || material == Material.GRAVEL || material == Material.TNT;
      }
  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game == null) {
        evt.setCancelled(true);
      } else {
        evt.setCancelled(game.getState() != GameState.EMJOGO || game.isSpectator(evt.getPlayer()));
      }
    }
  }
  
  @EventHandler
  public void onBlockBreak(BlockBreakEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game == null) {
        evt.setCancelled(!BuildCommand.hasBuilder(evt.getPlayer()));
      } else {
        if (evt.getBlock().getType() == Material.SLIME_BLOCK) {
          evt.setCancelled(true);
          return;
        }
        evt.setCancelled(game.getState() != GameState.EMJOGO || game.isSpectator(evt.getPlayer()) || !game.getCubeId().contains(evt.getBlock().getLocation()));
      }
    }
  }
  
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent evt) {
    Profile profile = Profile.getProfile(evt.getPlayer().getName());
    if (profile != null) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game == null) {
        evt.setCancelled(!BuildCommand.hasBuilder(evt.getPlayer()));
      } else {
        evt.setCancelled(game.getState() != GameState.EMJOGO || game.isSpectator(evt.getPlayer()) || !game.getCubeId().contains(evt.getBlock().getLocation()));
      }
    }
  }
}
