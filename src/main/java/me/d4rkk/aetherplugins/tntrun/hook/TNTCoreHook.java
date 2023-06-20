package me.d4rkk.aetherplugins.tntrun.hook;

import com.comphenix.protocol.ProtocolLibrary;
import dev.slickcollections.kiwizin.Core;
import dev.slickcollections.kiwizin.achievements.Achievement;
import dev.slickcollections.kiwizin.achievements.types.SkyWarsAchievement;
import dev.slickcollections.kiwizin.game.GameState;
import dev.slickcollections.kiwizin.player.Profile;
import dev.slickcollections.kiwizin.player.hotbar.Hotbar;
import dev.slickcollections.kiwizin.player.hotbar.HotbarAction;
import dev.slickcollections.kiwizin.player.hotbar.HotbarActionType;
import dev.slickcollections.kiwizin.player.hotbar.HotbarButton;
import dev.slickcollections.kiwizin.player.scoreboard.KScoreboard;
import dev.slickcollections.kiwizin.player.scoreboard.scroller.ScoreboardScroller;
import dev.slickcollections.kiwizin.plugin.config.KConfig;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameTeam;
import me.d4rkk.aetherplugins.tntrun.game.enums.TnTGameMode;
import me.d4rkk.aetherplugins.tntrun.game.object.TnTRunLeague;
import me.d4rkk.aetherplugins.tntrun.hook.hotbar.SWHotbarActionType;
import me.d4rkk.aetherplugins.tntrun.hook.protocollib.HologramAdapter;
import dev.slickcollections.kiwizin.utils.StringUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

public class TNTCoreHook {
  
  public static void setupHook() {
    Core.minigame = "Sky Wars";
    
    setupHotbars();
    new BukkitRunnable() {
      @Override
      public void run() {
        Profile.listProfiles().forEach(profile -> {
          if (profile.getScoreboard() != null) {
            profile.getScoreboard().scroll();
          }
        });
      }
    }.runTaskTimerAsynchronously(Main.getInstance(), 0, Language.scoreboards$scroller$every_tick);
    
    new BukkitRunnable() {
      @Override
      public void run() {
        Profile.listProfiles().forEach(profile -> {
          if (!profile.playingGame() && profile.getScoreboard() != null) {
            profile.update();
          }
        });
      }
    }.runTaskTimerAsynchronously(Main.getInstance(), 0, 20);
    
    ProtocolLibrary.getProtocolManager().addPacketListener(new HologramAdapter());
  }
  
  public static void checkAchievements(Profile profile) {
    Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
      Achievement.listAchievements(SkyWarsAchievement.class).stream().filter(swa -> swa.canComplete(profile)).forEach(swa -> {
        swa.complete(profile);
        profile.getPlayer().sendMessage(Language.lobby$achievement.replace("{name}", swa.getName()));
      });
    });
  }
  
  public static void reloadScoreboard(Profile profile) {
    if (!profile.playingGame()) {
      checkAchievements(profile);
    }
    Player player = profile.getPlayer();
    TnTGameAb game = profile.getGame(TnTGameAb.class);
    List<String> lines = game == null ?
        Language.scoreboards$lobby :
        game.getState() == GameState.AGUARDANDO ?
            Language.scoreboards$waiting :
            (game.getMode() == TnTGameMode.SOLO || game.getMode() == TnTGameMode.RANKED ? Language.scoreboards$ingame$solo : Language.scoreboards$ingame$dupla);
    
    profile.setScoreboard(new KScoreboard() {
      @Override
      public void update() {
        for (int index = 0; index < Math.min(lines.size(), 15); index++) {
          String line = lines.get(index);
          if (game != null) {

            line =
                line.replace("{map}", game.getMapName())
                    .replace("{server}", game.getGameName())
                    .replace("{mode}", game.getMode().getName())
                    .replace("{next_event}", game.getEvent())
                    .replace("{players}", StringUtils.formatNumber(game.getOnline()))
                    .replace("{teams}", StringUtils.formatNumber(game.listTeams().stream().filter(TnTGameTeam::isAlive).count()))
                    .replace("{max_players}", StringUtils.formatNumber(game.getMaxPlayers()))
                    .replace("{time}", game.getTimer() == 46 ? Language.scoreboards$time$waiting : Language.scoreboards$time$starting.replace("{time}", StringUtils.formatNumber(game.getTimer())))
                    .replace("{kills}", StringUtils.formatNumber(game.getKills(player)))
                    .replace("{ranking_1}", game.getTopKill(1))
                    .replace("{ranking_2}", game.getTopKill(2))
                    .replace("{date}", new SimpleDateFormat("dd/MM/YY").format(System.currentTimeMillis()))

                    .replace("{ranking_3}", game.getTopKill(3));
          } else {
            line = PlaceholderAPI.setPlaceholders(player, line);
            line = line.replace("%kCore_SkyWars_league%", TnTRunLeague.getLeague(profile).getTag());
          }
          
          this.add(15 - index, line);
        }
      }
    }.scroller(new ScoreboardScroller(Language.scoreboards$scroller$titles)).to(player).build());
    if (game != null && game.getState() != GameState.AGUARDANDO) {
      profile.getScoreboard().health().updateHealth();
    }
    profile.update();
    profile.getScoreboard().scroll();
  }
  
  private static void setupHotbars() {
    HotbarActionType.addActionType("skywars", new SWHotbarActionType());
    
    KConfig config = Main.getInstance().getConfig("hotbar");
    for (String id : new String[]{"lobby", "waiting", "spectator"}) {
      Hotbar hotbar = new Hotbar(id);
      
      ConfigurationSection hb = config.getSection(id);
      for (String button : hb.getKeys(false)) {
        try {
          hotbar.getButtons().add(new HotbarButton(hb.getInt(button + ".slot"), new HotbarAction(hb.getString(button + ".execute")), hb.getString(button + ".icon")));
        } catch (Exception ex) {
          Main.getInstance().getLogger().log(Level.WARNING, "Falha ao carregar o botao \"" + button + "\" da hotbar \"" + id + "\": ", ex);
        }
      }
      
      Hotbar.addHotbar(hotbar);
    }
  }
}
