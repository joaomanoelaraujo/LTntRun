package me.d4rkk.aetherplugins.tntrun.listeners.server;

import dev.slickcollections.kiwizin.game.GameState;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class ServerListener implements Listener {
  
  @EventHandler
  public void onBlockIgnite(BlockIgniteEvent evt) {
    TnTGameAb game = TnTGameAb.getByWorldName(evt.getBlock().getWorld().getName());
    if (game == null) {
      evt.setCancelled(true);
    } else if (game.getState() != GameState.EMJOGO) {
      evt.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onBlockBurn(BlockBurnEvent evt) {
    TnTGameAb game = TnTGameAb.getByWorldName(evt.getBlock().getWorld().getName());
    if (game == null) {
      evt.setCancelled(true);
    } else if (game.getState() != GameState.EMJOGO) {
      evt.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onLeavesDecay(LeavesDecayEvent evt) {
    evt.setCancelled(true);
  }
  
  @EventHandler
  public void onEntityExplode(EntityExplodeEvent evt) {
    TnTGameAb game = TnTGameAb.getByWorldName(evt.getEntity().getWorld().getName());
    if (game == null) {
      evt.setCancelled(true);
    } else if (game.getState() != GameState.EMJOGO) {
      evt.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onWeatherChange(WeatherChangeEvent evt) {
    evt.setCancelled(evt.toWeatherState());
  }
}