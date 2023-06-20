package me.d4rkk.aetherplugins.tntrun.cosmetics.types.winanimations;

import me.d4rkk.aetherplugins.tntrun.cosmetics.object.AbstractExecutor;
import me.d4rkk.aetherplugins.tntrun.cosmetics.object.winanimations.EnderDragonExecutor;
import me.d4rkk.aetherplugins.tntrun.cosmetics.types.WinAnimation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EnderDragon extends WinAnimation {
  
  public EnderDragon(ConfigurationSection section) {
    super(section.getLong("id"), "ender_dragon", section.getDouble("coins"), section.getString("permission"), section.getString("name"), section.getString("icon"));
  }
  
  public AbstractExecutor execute(Player player) {
    return new EnderDragonExecutor(player);
  }
}
