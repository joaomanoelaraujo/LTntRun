package me.d4rkk.aetherplugins.tntrun.listeners.player;

import dev.slickcollections.kiwizin.Core;
import dev.slickcollections.kiwizin.nms.NMS;
import dev.slickcollections.kiwizin.player.Profile;
import dev.slickcollections.kiwizin.player.hotbar.Hotbar;
import dev.slickcollections.kiwizin.player.role.Role;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.hook.TNTCoreHook;
import me.d4rkk.aetherplugins.tntrun.tagger.TagUtils;
import dev.slickcollections.kiwizin.titles.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent evt) {
    evt.setJoinMessage(null);

    Player player = evt.getPlayer();
    TagUtils.sendTeams(player);

    Profile profile = Profile.getProfile(player.getName());
    TNTCoreHook.reloadScoreboard(profile);
    profile.setHotbar(Hotbar.getHotbarById("lobby"));
    profile.refresh();

    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
      TagUtils.setTag(evt.getPlayer());

      if (Role.getPlayerRole(player).isBroadcast()) {
        String broadcast = Language.lobby$broadcast
                .replace("{player}", Role.getPrefixed(player.getName()));
        Profile.listProfiles().forEach(pf -> {
          if (!pf.playingGame()) {
            Player players = pf.getPlayer();
            if (players != null) {
              players.sendMessage(broadcast);
            }
          }
        });
      }
    }, 5);

    Bukkit.getScheduler().runTaskLaterAsynchronously(Core.getInstance(), () -> {
      TitleManager.joinLobby(profile);
    }, 10);

    NMS.sendTitle(player, "", "", 0, 1, 0);
    if (Language.lobby$tab$enabled) {
      NMS.sendTabHeaderFooter(player, Language.lobby$tab$header, Language.lobby$tab$footer);
    }
  }
}
