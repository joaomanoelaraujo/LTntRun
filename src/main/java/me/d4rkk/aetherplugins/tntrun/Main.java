package me.d4rkk.aetherplugins.tntrun;

import dev.slickcollections.kiwizin.Core;
import dev.slickcollections.kiwizin.libraries.MinecraftVersion;
import dev.slickcollections.kiwizin.plugin.KPlugin;
import me.d4rkk.aetherplugins.tntrun.cmd.Commands;
import me.d4rkk.aetherplugins.tntrun.cosmetics.Cosmetic;
import me.d4rkk.aetherplugins.tntrun.game.AbstractSkyWars;
import me.d4rkk.aetherplugins.tntrun.game.object.SkyWarsLeague;
import me.d4rkk.aetherplugins.tntrun.hook.SWCoreHook;
import me.d4rkk.aetherplugins.tntrun.listeners.Listeners;
import me.d4rkk.aetherplugins.tntrun.tagger.TagUtils;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import me.d4rkk.aetherplugins.tntrun.lobby.*;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;

public class Main extends KPlugin {
  
  public static boolean kMysteryBox, kCosmetics, kClans;
  public static String currentServerName;
  private static Main instance;
  private static boolean validInit;
  
  public static Main getInstance() {
    return instance;
  }
  
  @Override
  public void start() {
    instance = this;
  }
  
  @Override
  public void load() {}
  
  @Override
  public void enable() {
    if (MinecraftVersion.getCurrentVersion().getCompareId() != 183) {
      this.setEnabled(false);
      this.getLogger().warning("O plugin apenas funciona na versao 1_8_R3 (Atual: " + MinecraftVersion.getCurrentVersion().getVersion() + ")");
      return;
    }
    
    saveDefaultConfig();
    currentServerName = getConfig().getString("lobby");
    kMysteryBox = Bukkit.getPluginManager().getPlugin("kMysteryBox") != null;
    kCosmetics = Bukkit.getPluginManager().getPlugin("kCosmetics") != null;
    kClans = Bukkit.getPluginManager().getPlugin("kClans") != null;
    if (getConfig().getString("spawn") != null) {
      Core.setLobby(BukkitUtils.deserializeLocation(getConfig().getString("spawn")));
    }
    
    AbstractSkyWars.setupGames();
    Language.setupLanguage();
    SWCoreHook.setupHook();
    Listeners.setupListeners();
    SkyWarsLeague.setupLeagues();
    
    Commands.setupCommands();
    
    Leaderboard.setupLeaderboards();
    
    DeliveryNPC.setupNPCs();
    Cosmetic.setupCosmetics();
    PlayNPC.setupNPCs();
    StatsNPC.setupNPCs();
    Lobby.setupLobbies();

    validInit = true;
    this.getLogger().info("O plugin foi ativado.");
  }
  
  @Override
  public void disable() {
    if (validInit) {
      DeliveryNPC.listNPCs().forEach(DeliveryNPC::destroy);
      PlayNPC.listNPCs().forEach(PlayNPC::destroy);
      StatsNPC.listNPCs().forEach(StatsNPC::destroy);
      Leaderboard.listLeaderboards().forEach(Leaderboard::destroy);
      TagUtils.reset();
    }
    
    File update = new File("plugins/kSkyWars/update", "kSkyWars.jar");
    if (update.exists()) {
      try {
        this.getFileUtils().deleteFile(new File("plugins/" + update.getName()));
        this.getFileUtils().copyFile(new FileInputStream(update), new File("plugins/" + update.getName()));
        this.getFileUtils().deleteFile(update.getParentFile());
        this.getLogger().info("Update aplicada.");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    this.getLogger().info("O plugin foi desativado.");
  }
  
  private static class Checker {
    public static void check() {}
  }
}