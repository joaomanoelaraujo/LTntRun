package me.d4rkk.aetherplugins.tntrun.cosmetics.object.winanimations;

import me.d4rkk.aetherplugins.tntrun.cosmetics.object.AbstractExecutor;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

public class TntExecutor extends AbstractExecutor {
  
  public TntExecutor(Player player) {
    super(player);
  }
  
  @Override
  public void tick() {
    this.player.getWorld().spawn(player.getLocation().clone().add(Math.floor(Math.random() * 3.0D), 5, Math.floor(Math.random() * 3.0D)), TNTPrimed.class);
  }
}
