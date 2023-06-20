package me.d4rkk.aetherplugins.tntrun.cosmetics.object.winanimations;

import me.d4rkk.aetherplugins.tntrun.cosmetics.object.AbstractExecutor;
import me.d4rkk.aetherplugins.tntrun.nms.NMS;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

public class EnderDragonExecutor extends AbstractExecutor {
  
  public EnderDragonExecutor(Player player) {
    super(player);
    NMS.createMountableEnderDragon(player);
  }
  
  @Override
  public void tick() {
    player.launchProjectile(Fireball.class);
  }
}