package me.d4rkk.aetherplugins.tntrun.cosmetics.object.winanimations;

import me.d4rkk.aetherplugins.tntrun.cosmetics.object.AbstractExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ThorExecutor extends AbstractExecutor {
  
  public ThorExecutor(Player player) {
    super(player);
  }
  
  @Override
  public void tick() {
    this.player.getWorld().spawnEntity(this.player.getLocation(), EntityType.LIGHTNING);
    this.player.getWorld().spawnEntity(this.player.getLocation(), EntityType.LIGHTNING);
    this.player.getLocation().getWorld().strikeLightning(this.player.getLocation());
    this.player.getLocation().getWorld().strikeLightningEffect(this.player.getLocation());
  }
}