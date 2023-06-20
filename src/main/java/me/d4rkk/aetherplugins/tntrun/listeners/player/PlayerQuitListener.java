package me.d4rkk.aetherplugins.tntrun.listeners.player;

import me.d4rkk.aetherplugins.tntrun.cmd.sw.BuildCommand;
import me.d4rkk.aetherplugins.tntrun.cmd.sw.CreateCommand;
import me.d4rkk.aetherplugins.tntrun.tagger.TagUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent evt) {
    evt.setQuitMessage(null);
    BuildCommand.remove(evt.getPlayer());
    TagUtils.reset(evt.getPlayer().getName());
    CreateCommand.CREATING.remove(evt.getPlayer());

  }
}
