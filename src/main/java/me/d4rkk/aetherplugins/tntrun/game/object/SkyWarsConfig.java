package me.d4rkk.aetherplugins.tntrun.game.object;

import dev.slickcollections.kiwizin.plugin.config.KConfig;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.game.AbstractSkyWars;
import me.d4rkk.aetherplugins.tntrun.game.SkyWarsTeam;
import me.d4rkk.aetherplugins.tntrun.game.enums.SkyWarsMode;
import dev.slickcollections.kiwizin.utils.CubeID;
import me.d4rkk.aetherplugins.tntrun.utils.VoidChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SkyWarsConfig {
  
  private AbstractSkyWars game;
  private KConfig config;
  
  private String yaml;
  private World world;
  private String name;
  private SkyWarsMode mode;
  private List<SkyWarsTeam> teams;
  private CubeID cubeId;
  private final int minPlayers;
  public SkyWarsConfig(AbstractSkyWars game) {
    this.game = game;
    this.yaml = game.getGameName();
    this.config = Main.getInstance().getConfig("arenas", this.yaml);
    this.name = this.config.getString("name");
    this.mode = SkyWarsMode.fromName(this.config.getString("mode"));
    this.minPlayers = config.getInt("minPlayers");
    this.cubeId = new CubeID(config.getString("cubeId"));
    this.teams = new ArrayList<>();
    this.reload(null);
  }

  public void setupSpawns() {
    this.config.getStringList("spawns").forEach(spawns -> this.teams.add(new SkyWarsTeam(this.game, spawns, this.mode.getSize())));
  //  this.teams.add(new SkyWarsTeam(game, this.config.getString("spawns"), this.mode.getSize()));
   // this.teams.add(new SkyWarsTeam(game, this.config.getString("spawn"), this.mode.getSize()));
  }
  
  public void destroy() {
    if ((this.world = Bukkit.getWorld(this.yaml)) != null) {
      Bukkit.unloadWorld(this.world, false);
    }
    
    Main.getInstance().getFileUtils().deleteFile(new File(this.yaml));
    this.game = null;
    this.yaml = null;
    this.name = null;
    this.mode = null;
    this.teams.clear();
    this.teams = null;
    this.cubeId = null;
    this.world = null;
    this.config = null;
  }
  
  public void reload(final Runnable async) {
    File file = new File("plugins/kSkyWars/mundos/" + this.yaml);
    if (Bukkit.getWorld(file.getName()) != null) {
      Bukkit.unloadWorld(file.getName(), false);
    }
    
    Runnable delete = () -> {
      Main.getInstance().getFileUtils().deleteFile(new File(file.getName()));
      Main.getInstance().getFileUtils().copyFiles(file, new File(file.getName()));
      
      Runnable newWorld = () -> {
        WorldCreator wc = WorldCreator.name(file.getName());
        wc.generator(VoidChunkGenerator.VOID_CHUNK_GENERATOR);
        wc.generateStructures(false);
        this.world = wc.createWorld();
        this.world.setTime(0L);
        this.world.setStorm(false);
        this.world.setThundering(false);
        this.world.setAutoSave(false);
        this.world.setAnimalSpawnLimit(0);
        this.world.setWaterAnimalSpawnLimit(0);
        this.world.setKeepSpawnInMemory(false);
        this.world.setGameRuleValue("doMobSpawning", "false");
        this.world.setGameRuleValue("doDaylightCycle", "false");
        this.world.setGameRuleValue("mobGriefing", "false");
        this.world.getEntities().stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
        if (async != null) {
          async.run();
        }
      };
      
      if (async == null) {
        newWorld.run();
        return;
      }
      
      Bukkit.getScheduler().runTask(Main.getInstance(), newWorld);
    };
    
    if (async == null) {
      delete.run();
      return;
    }
    
    Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), delete);
  }
  

  public World getWorld() {
    return this.world;
  }
  
  public KConfig getConfig() {
    return this.config;
  }
  
  public String getMapName() {
    return this.name;
  }
  
  public SkyWarsMode getMode() {
    return this.mode;
  }
  
  public List<SkyWarsTeam> listTeams() {
    return this.teams;
  }

  public CubeID getCubeId() {
    return this.cubeId;
  }
  
  public int getMinPlayers() {
    return minPlayers;
  }
}
