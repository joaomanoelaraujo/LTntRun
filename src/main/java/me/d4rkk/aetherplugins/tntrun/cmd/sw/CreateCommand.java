package me.d4rkk.aetherplugins.tntrun.cmd.sw;

import dev.slickcollections.kiwizin.player.Profile;
import dev.slickcollections.kiwizin.player.hotbar.Hotbar;
import dev.slickcollections.kiwizin.plugin.config.KConfig;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.cmd.SubCommand;
import me.d4rkk.aetherplugins.tntrun.game.AbstractSkyWars;
import me.d4rkk.aetherplugins.tntrun.game.enums.SkyWarsMode;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import dev.slickcollections.kiwizin.utils.CubeID;
import dev.slickcollections.kiwizin.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateCommand extends SubCommand {

  public static final Map<Player, Object[]> CREATING = new HashMap<>();
  
  public CreateCommand() {
    super("criar", "criar [tntrun/tnttag] [nome]", "Criar uma sala.", true);
  }
  
  public static void handleClick(Profile profile, String display, PlayerInteractEvent evt) {
    Player player = profile.getPlayer();
    switch (display) {
      case "§aSpawn": {
        evt.setCancelled(true);
        Location location = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
        location.setYaw(player.getLocation().getYaw());
        location.setPitch(player.getLocation().getPitch());
        CREATING.get(player)[5] = location;
        player.sendMessage("§aSpawn dos jogadores setado.");
        break;
      }
      case "§aCuboID da Arena": {
        evt.setCancelled(true);
        if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
          CREATING.get(player)[3] = evt.getClickedBlock().getLocation();
          player.sendMessage("§aBorda da Arena 1 setada.");
        } else if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
          CREATING.get(player)[4] = evt.getClickedBlock().getLocation();
          player.sendMessage("§aBorda da Arena 2 setada.");
        } else {
          player.sendMessage("§cClique em um bloco.");
        }
        break;
      }
      case "§aConfirmar": {
        evt.setCancelled(true);
        if (CREATING.get(player)[3] == null) {
          player.sendMessage("§cBorda da Arena 1 não setada.");
          return;
        }
        
        if (CREATING.get(player)[4] == null) {
          player.sendMessage("§cBorda da Arena 2 não setada.");
          return;
        }
        if (CREATING.get(player)[5] == null) {
          player.sendMessage("§cSpawn não setado.");
          return;
        }
        Object[] array = CREATING.get(player);
        World world = player.getWorld();
        KConfig config = Main.getInstance().getConfig("arenas", world.getName());
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        CREATING.remove(player);
        player.sendMessage("§aCriando sala...");

        CubeID cube = new CubeID((Location) array[3], (Location) array[4]);
        config.set("name", array[1]);
        config.set("mode", array[2]);
        config.set("minPlayers", 12);
        config.set("cubeId", cube.toString());
        config.set("spawns", BukkitUtils.serializeLocation((Location) array[5]));
        world.save();
        
        player.sendMessage("§aCriando backup do mapa...");
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
          Main.getInstance().getFileUtils().copyFiles(new File(world.getName()), new File("plugins/kSkyWars/mundos/" + world.getName()), "playerdata", "stats", "uid.dat");
          
          profile.setHotbar(Hotbar.getHotbarById("lobby"));
          profile.refresh();
          AbstractSkyWars.load(config.getFile(), () -> player.sendMessage("§aSala criada com sucesso."));
        }, 60);
        break;
      }
    }
  }
  
  @Override
  public void perform(Player player, String[] args) {
    if (AbstractSkyWars.getByWorldName(player.getWorld().getName()) != null) {
      player.sendMessage("§cJá existe uma sala neste mundo.");
      return;
    }
    
    if (args.length <= 1) {
      player.sendMessage("§cUtilize /sw " + this.getUsage());
      return;
    }
    
    SkyWarsMode mode = SkyWarsMode.fromName(args[0]);
    if (mode == null) {
      player.sendMessage("§cUtilize /sw " + this.getUsage());
      return;
    }
    
    String name = StringUtils.join(args, 1, " ");
    Object[] array = new Object[7];
    array[0] = player.getWorld();
    array[1] = name;
    array[2] = mode.name();
    CREATING.put(player, array);
    
    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    player.getInventory().setItem(2, BukkitUtils.deserializeItemStack("NETHER_STAR : 1 : nome>§aSpawn"));
    player.getInventory().setItem(0, BukkitUtils.deserializeItemStack("BLAZE_ROD : 1 : nome>&aCuboID da Arena"));
    player.getInventory().setItem(1, BukkitUtils.deserializeItemStack("STAINED_CLAY:13 : 1 : nome>&aConfirmar"));
    
    player.updateInventory();
    
    Profile.getProfile(player.getName()).setHotbar(null);
  }
}
