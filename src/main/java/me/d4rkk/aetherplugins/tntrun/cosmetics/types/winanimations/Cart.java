package me.d4rkk.aetherplugins.tntrun.cosmetics.types.winanimations;

import me.d4rkk.aetherplugins.tntrun.cosmetics.object.AbstractExecutor;
import me.d4rkk.aetherplugins.tntrun.cosmetics.object.winanimations.CartExecutor;
import me.d4rkk.aetherplugins.tntrun.cosmetics.types.WinAnimation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Cart extends WinAnimation {
  
  public Cart(ConfigurationSection section) {
    super(section.getLong("id"), "cowboy", section.getDouble("coins"), section.getString("permission"), section.getString("name"), section.getString("icon"));
  }
  
  public AbstractExecutor execute(Player player) {
    return new CartExecutor(player);
  }
}
