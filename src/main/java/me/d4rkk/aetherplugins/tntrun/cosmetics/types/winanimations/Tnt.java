package me.d4rkk.aetherplugins.tntrun.cosmetics.types.winanimations;

import me.d4rkk.aetherplugins.tntrun.cosmetics.object.AbstractExecutor;
import me.d4rkk.aetherplugins.tntrun.cosmetics.object.winanimations.TntExecutor;
import me.d4rkk.aetherplugins.tntrun.cosmetics.types.WinAnimation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Tnt extends WinAnimation {
  
  public Tnt(ConfigurationSection section) {
    super(section.getLong("id"), "tnt", section.getDouble("coins"), section.getString("permission"), section.getString("name"), section.getString("icon"));
  }
  
  public AbstractExecutor execute(Player player) {
    return new TntExecutor(player);
  }
}
