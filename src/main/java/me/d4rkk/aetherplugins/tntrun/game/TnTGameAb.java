package me.d4rkk.aetherplugins.tntrun.game;

import dev.slickcollections.kiwizin.Manager;
import dev.slickcollections.kiwizin.bukkit.BukkitParty;
import dev.slickcollections.kiwizin.bukkit.BukkitPartyManager;
import dev.slickcollections.kiwizin.game.FakeGame;
import dev.slickcollections.kiwizin.game.Game;
import dev.slickcollections.kiwizin.game.GameState;
import dev.slickcollections.kiwizin.game.GameTeam;
import dev.slickcollections.kiwizin.nms.NMS;
import dev.slickcollections.kiwizin.party.PartyPlayer;
import dev.slickcollections.kiwizin.player.Profile;
import dev.slickcollections.kiwizin.player.hotbar.Hotbar;
import dev.slickcollections.kiwizin.player.role.Role;
import dev.slickcollections.kiwizin.plugin.config.KConfig;
import dev.slickcollections.kiwizin.plugin.logger.KLogger;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.api.TREvent;
import me.d4rkk.aetherplugins.tntrun.api.event.game.TRGameStartEvent;
import me.d4rkk.aetherplugins.tntrun.api.event.player.TRPlayerDeathEvent;
import me.d4rkk.aetherplugins.tntrun.container.SelectedContainer;
import me.d4rkk.aetherplugins.tntrun.cosmetics.CosmeticType;
import me.d4rkk.aetherplugins.tntrun.cosmetics.types.*;
import me.d4rkk.aetherplugins.tntrun.game.enums.TnTGameMode;
import me.d4rkk.aetherplugins.tntrun.game.events.AnnounceEvent;
import me.d4rkk.aetherplugins.tntrun.game.interfaces.LoadCallback;
import me.d4rkk.aetherplugins.tntrun.game.object.TnTGameBlock;
import me.d4rkk.aetherplugins.tntrun.game.object.TnTGameConfig;
import me.d4rkk.aetherplugins.tntrun.game.object.TnTGameTask;
import me.d4rkk.aetherplugins.tntrun.tagger.TagUtils;
import dev.slickcollections.kiwizin.utils.BukkitUtils;
import dev.slickcollections.kiwizin.utils.CubeID;
import dev.slickcollections.kiwizin.utils.StringUtils;
import dev.slickcollections.kiwizin.utils.enums.EnumSound;
import me.d4rkk.aetherplugins.tntrun.hook.TNTCoreHook;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class TnTGameAb implements Game<TnTGameTeam> {
  
  public static final KLogger LOGGER = ((KLogger) Main.getInstance().getLogger()).getModule("GAME");
  public static final List<TnTGameAb> QUEUE = new ArrayList<>();
  private static final SimpleDateFormat SDF = new SimpleDateFormat("mm:ss");
  private static final Map<String, TnTGameAb> GAMES = new HashMap<>();
  private String name;
  private TnTGameConfig config;
  private int timer;
  private GameState state;
  private TnTGameTask task;
  private List<UUID> players;
  private List<UUID> spectators;
  private Map<String, Integer> points;
  private Map<String, Integer> kills;
  private final Map<String, TnTGameBlock> blocks = new HashMap<>();
  private List<Map.Entry<String, Integer>> topKills = new ArrayList<>();
  private final Map<String, Object[]> streak = new HashMap<>();
  private Map.Entry<Integer, TnTGameEvent> event;
  private Map.Entry<Integer, TnTGameEvent> nextEvent;
  
  public TnTGameAb(String name, LoadCallback callback) {
    this.name = name;
    this.timer = Language.options$start$waiting + 1;
    this.task = new TnTGameTask(this);
    this.config = new TnTGameConfig(this);
    this.config.setupSpawns();
    this.state = GameState.AGUARDANDO;
    this.players = new ArrayList<>();
    this.spectators = new ArrayList<>();
    this.kills = new HashMap<>();
    this.task.reset();
    
    if (!Language.options$regen$world_reload) {
      KConfig config = Main.getInstance().getConfig("blocos", name);
      if (config.contains("dataBlocks")) {
        for (String blockdata : config.getStringList("dataBlocks")) {
          blocks.put(blockdata.split(" : ")[0],
              new TnTGameBlock(Material.matchMaterial(blockdata.split(" : ")[1].split(", ")[0]), Byte.parseByte(blockdata.split(" : ")[1].split(", ")[1])));
        }
      } else {
        this.state = GameState.ENCERRADO;
        ArenaRollbackerTask.scan(this, config, callback);
      }
    } else if (callback != null) {
      callback.finish();
    }
  }
  
  public static void addToQueue(TnTGameAb game) {
    if (QUEUE.contains(game)) {
      return;
    }
    
    QUEUE.add(game);
  }
  
  public static void setupGames() {
    TnTGameEvent.setupEvents();
    new ArenaRollbackerTask().runTaskTimer(Main.getInstance(), 0, Language.options$regen$world_reload ? 100 : 1);
    
    File ymlFolder = new File("plugins/kSkyWars/arenas");
    File mapFolder = new File("plugins/kSkyWars/mundos");
    
    if (!ymlFolder.exists() || !mapFolder.exists()) {
      if (!ymlFolder.exists()) {
        ymlFolder.mkdirs();
      }
      if (!mapFolder.exists()) {
        mapFolder.mkdirs();
      }
    }
    
    for (File file : ymlFolder.listFiles()) {
      load(file, null);
    }
    
    LOGGER.info(GAMES.size() + " salas carregadas.");
  }
  
  public static void load(File yamlFile, LoadCallback callback) {
    String arenaName = yamlFile.getName().split("\\.")[0];
    
    try {
      File backup = new File("plugins/kSkyWars/mundos", arenaName);
      if (!backup.exists() || !backup.isDirectory()) {
        throw new IllegalArgumentException("Backup do mapa nao encontrado para a arena \"" + yamlFile.getName() + "\"");
      }
      
      TnTGameMode mode = TnTGameMode.fromName(Main.getInstance().getConfig("arenas", arenaName).getString("mode"));
      if (mode == null) {
        throw new IllegalArgumentException("Modo do mapa \"" + yamlFile.getName() + "\" nao e valido");
      }
      
      GAMES.put(arenaName, mode.buildGame(arenaName, callback));
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "load(\"" + yamlFile.getName() + "\"): ", ex);
    }
  }
  
  public static TnTGameAb getByWorldName(String worldName) {
    return GAMES.get(worldName);
  }
  
  public static int getWaiting(TnTGameMode mode) {
    int waiting = 0;
    List<TnTGameAb> games = listByMode(mode);
    for (TnTGameAb game : games) {
      if (game.getState() != GameState.EMJOGO) {
        waiting += game.getOnline();
      }
    }
    
    return waiting;
  }
  
  public static int getPlaying(TnTGameMode mode) {
    int playing = 0;
    List<TnTGameAb> games = listByMode(mode);
    for (TnTGameAb game : games) {
      if (game.getState() == GameState.EMJOGO) {
        playing += game.getOnline();
      }
    }
    
    return playing;
  }
  
  public static TnTGameAb findRandom(TnTGameMode mode) {
    List<TnTGameAb> games = GAMES.values().stream().filter(game -> game.getMode().equals(mode)
        && game.getState().canJoin() && game.getOnline() < game.getMaxPlayers())
        .sorted((g1, g2) -> Integer.compare(g2.getOnline(), g1.getOnline())).collect(Collectors.toList());
    TnTGameAb game = games.stream().findFirst().orElse(null);
    if (game != null && game.getOnline() == 0) {
      game = games.get(ThreadLocalRandom.current().nextInt(games.size()));
    }
    
    return game;
  }
  
  public static Map<String, List<TnTGameAb>> getAsMap(TnTGameMode mode) {
    Map<String, List<TnTGameAb>> result = new HashMap<>();
    GAMES.values().stream().filter(game -> game.getMode().equals(mode) && game.getState().canJoin()
        && game.getOnline() < game.getMaxPlayers()).forEach(game -> {
      List<TnTGameAb> list = result.computeIfAbsent(game.getMapName(), k -> new ArrayList<>());
      
      if (game.getState().canJoin() && game.getOnline() < game.getMaxPlayers()) {
        list.add(game);
      }
    });
    
    return result;
  }
  
  public static List<TnTGameAb> listByMode(TnTGameMode mode) {
    return GAMES.values().stream().filter(sw -> sw.getMode().equals(mode))
        .collect(Collectors.toList());
  }
  
  public void destroy() {
    this.name = null;
    this.config.destroy();
    this.config = null;
    this.timer = 0;
    this.state = null;
    this.task.cancel();
    this.task = null;
    this.players.clear();
    this.players = null;
    this.spectators.clear();
    this.spectators = null;
    this.kills.clear();
    this.kills = null;
    this.topKills.clear();
    this.topKills = null;
  }
  
  @Override
  public void broadcastMessage(String message) {
    this.broadcastMessage(message, true);
  }
  
  @Override
  public void broadcastMessage(String message, boolean spectators) {
    this.listPlayers().forEach(player -> player.sendMessage(message));
  }
  
  public void spectate(Player player, Player target) {
    if (this.getState() == GameState.AGUARDANDO) {
      player.sendMessage("§cA partida ainda não começou.");
      return;
    }
    
    Profile profile = Profile.getProfile(player.getName());
    if (profile.playingGame()) {
      if (profile.getGame().equals(this)) {
        return;
      }
      
      profile.getGame().leave(profile, this);
    }
    
    profile.setGame(this);
    spectators.add(player.getUniqueId());
    
    player.teleport(target.getLocation());
    TNTCoreHook.reloadScoreboard(profile);
    for (Player players : Bukkit.getOnlinePlayers()) {
      if (!players.getWorld().equals(player.getWorld())) {
        player.hidePlayer(players);
        players.hidePlayer(player);
        continue;
      }
      
      if (isSpectator(players)) {
        players.showPlayer(player);
      } else {
        players.hidePlayer(player);
      }
      player.showPlayer(players);
    }
    
    profile.setHotbar(Hotbar.getHotbarById("spectator"));
    profile.refresh();
    player.setGameMode(GameMode.ADVENTURE);
    player.spigot().setCollidesWithEntities(false);
    player.setAllowFlight(true);
    player.setFlying(true);
    this.updateTags();
  }
  
  private void joinParty(Profile profile, boolean ignoreLeader) {
    Player player = profile.getPlayer();
    if (player == null || !this.state.canJoin() || this.players.size() >= this.getMaxPlayers()) {
      return;
    }
    
    if (profile.getGame() != null && profile.getGame().equals(this)) {
      return;
    }
    
    TnTGameTeam team = null;
    boolean fullSize = false;
    BukkitParty party = BukkitPartyManager.getMemberParty(player.getName());
    if (party != null) {
      if (!ignoreLeader) {
        if (!party.isLeader(player.getName())) {
          player.sendMessage("§cApenas o líder da Party pode buscar por partidas.");
          return;
        }
        
        if (party.onlineCount() + players.size() > getMaxPlayers()) {
          return;
        }
        
        fullSize = true;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(),
            () -> party.listMembers().stream().filter(PartyPlayer::isOnline).map(pp -> Profile.getProfile(pp.getName()))
                .filter(pp -> pp != null && pp.getGame(FakeGame.class) == null).forEach(pp -> joinParty(pp, true)), 5);
      } else {
        team =
            listTeams().stream().filter(st -> st.canJoin() && (party.listMembers().stream().anyMatch(pp -> pp.isOnline() && st.hasMember((Player) Manager.getPlayer(pp.getName())))))
                .findAny().orElse(null);
      }
    }

    team = team == null ? getAvailableTeam(fullSize ? this.getMode().getSize() : 1) : team;
    if (team == null) {
      return;
    }

    team.addMember(player);

    if (profile.getGame() != null) {
      profile.getGame().leave(profile, profile.getGame());
    }


    this.players.add(player.getUniqueId());
    profile.setGame(this);


    player.teleport(team.getLocation());
    TNTCoreHook.reloadScoreboard(profile);
    
    profile.setHotbar(Hotbar.getHotbarById("waiting"));
    profile.refresh();
    for (Player players : Bukkit.getOnlinePlayers()) {
      if (!players.getWorld().equals(player.getWorld())) {
        player.hidePlayer(players);
        players.hidePlayer(player);
        continue;
      }
      
      if (isSpectator(players)) {
        player.hidePlayer(players);
      } else {
        player.showPlayer(players);
      }
      players.showPlayer(player);
    }
    
    this.broadcastMessage(Language.ingame$broadcast$join.replace("{player}", Role.getColored(player.getName())).replace("{players}", String.valueOf(this.getOnline()))
        .replace("{max_players}", String.valueOf(this.getMaxPlayers())));
    if (this.getOnline() == this.getMaxPlayers() && this.timer > Language.options$start$full) {
      this.timer = Language.options$start$full;
    }
  }
  
  boolean able(String pp) {
    List<Player> playerStream = listTeams().stream().findAny().orElse(null).listPlayers().stream().filter(pr -> pp.equals(pr.getName())).collect(Collectors.toList());
    return !playerStream.isEmpty();
  }
  
  @Override
  public void join(Profile profile) {
    this.joinParty(profile, false);
  }
  
  @Override
  public void leave(Profile profile, Game<?> game) {
    Player player = profile.getPlayer();
    if (player == null || profile.getGame() != this) {
      return;
    }
    
    TnTGameTeam team = this.getTeam(player);
    
    boolean alive = this.players.contains(player.getUniqueId());
    this.players.remove(player.getUniqueId());
    this.spectators.remove(player.getUniqueId());
    
    if (game != null) {
      if (alive && this.state == GameState.EMJOGO) {
        List<Profile> hitters = profile.getLastHitters();
        Profile killer = hitters.size() > 0 ? hitters.get(0) : null;
        killLeave(profile, killer);
        this.listPlayers().forEach(players -> NMS.sendActionBar(players, Language.ingame$actionbar$killed.replace("{alive}", StringUtils.formatNumber(this.getOnline()))));
        for (Profile hitter : hitters) {
          if (!hitter.equals(killer) && hitter.playingGame() && hitter.getGame().equals(this) && !this.isSpectator(hitter.getPlayer())) {
            hitter.addStats("kCoreSkyWars", this.getMode().getStats() + "assists");
            // Mensal.
            hitter.addStats("kCoreSkyWars", "monthlyassists");
          }
        }
        hitters.clear();
      }
      
      if (team != null) {
        team.removeMember(player);
        if (this.state == GameState.AGUARDANDO && !team.isAlive()) {

        }
      }
      if (Profile.isOnline(player.getName())) {
        profile.setGame(null);
        TagUtils.setTag(player);
      }
      if (this.state == GameState.AGUARDANDO) {
        this.broadcastMessage(Language.ingame$broadcast$leave.replace("{player}", Role.getColored(player.getName())).replace("{players}", String.valueOf(this.getOnline()))
            .replace("{max_players}", String.valueOf(this.getMaxPlayers())));
      }
      this.check();
      return;
    }
    
    if (alive && this.state == GameState.EMJOGO) {
      List<Profile> hitters = profile.getLastHitters();
      Profile killer = hitters.size() > 0 ? hitters.get(0) : null;
      killLeave(profile, killer);
      this.listPlayers().forEach(players -> NMS.sendActionBar(players, Language.ingame$actionbar$killed.replace("{alive}", StringUtils.formatNumber(this.getOnline()))));
      for (Profile hitter : hitters) {
        if (!hitter.equals(killer) && hitter.playingGame() && hitter.getGame().equals(this) && !this.isSpectator(hitter.getPlayer())) {
          hitter.addStats("kCoreSkyWars", this.getMode().getStats() + "assists");
          // Mensal
          hitter.addStats("kCoreSkyWars", "monthlyassists");
        }
      }
      hitters.clear();
    }
    
    if (team != null) {
      team.removeMember(player);
      if (this.state == GameState.AGUARDANDO && !team.isAlive()) {

      }
    }
    profile.setGame(null);
    TagUtils.setTag(player);
    TNTCoreHook.reloadScoreboard(profile);
    profile.setHotbar(Hotbar.getHotbarById("lobby"));
    profile.refresh();
    if (this.state == GameState.AGUARDANDO) {
      this.broadcastMessage(Language.ingame$broadcast$leave.replace("{player}", Role.getColored(player.getName())).replace("{players}", String.valueOf(this.getOnline()))
          .replace("{max_players}", String.valueOf(this.getMaxPlayers())));
    }
    this.check();
  }
  
  @Override
  public void start() {
    this.state = GameState.EMJOGO;
    this.task.swap(null);

    this.listTeams().forEach(TnTGameTeam::startGame);

    for (Player player : this.listPlayers(false)) {

      Profile profile = Profile.getProfile(player.getName());
      TNTCoreHook.reloadScoreboard(profile);
      profile.setHotbar(null);
      profile.addStats("kCoreSkyWars", this.getMode().getStats() + "games");
      if (Main.kClans) {
       // if (ClanAPI.getClanByPlayerName(profile.getName()) != null) {
         // ClanAPI.addCoins(ClanAPI.getClanByPlayerName(profile.getName()), Language.options$coins$clan$play);
       // }
      }

      profile.refresh();
      player.getInventory().clear();
      player.getInventory().setArmorContents(null);
      

      
      player.updateInventory();
      player.setGameMode(GameMode.SURVIVAL);
      player.setNoDamageTicks(80);
    }
    
    TRGameStartEvent evt = new TRGameStartEvent(this);
    TREvent.callEvent(evt);
    
    this.updateTags();
    this.check();
  }

  public void kill(Profile profile, Profile killer) {
    Player player = profile.getPlayer();
    this.killLeave(profile, killer);
    TRPlayerDeathEvent evt = new TRPlayerDeathEvent(this, profile, killer);
    TREvent.callEvent(evt);
    if (evt.isCancelled()) {
      // TODO: Reviver
      return;
    }

    TnTGameTeam team = this.getTeam(player);
    if (team != null) {
      team.removeMember(player);
    }
    this.players.remove(player.getUniqueId());
    this.spectators.add(player.getUniqueId());
    profile.setHotbar(Hotbar.getHotbarById("spectator"));
    for (Player players : this.listPlayers()) {
      if (isSpectator(players)) {
        player.showPlayer(players);
      } else {
        players.hidePlayer(player);
      }
    }
    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
      if (player.isOnline()) {
        profile.refresh();
        player.setGameMode(GameMode.ADVENTURE);
        player.spigot().setCollidesWithEntities(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        if (killer != null) {
          player.setVelocity(player.getLocation().getDirection().multiply(-1.6));
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
          if (player.isOnline()) {
            int coinsKill = (int) profile.calculateWM(this.getKills(player) * Language.options$coins$kills);
            int pointsKill = this.getKills(player) * Language.options$points$kills;

            if (coinsKill > 0) {
              player.sendMessage(Language.ingame$messages$coins$base.replace("{points}", StringUtils.formatNumber(pointsKill)).replace("{coins}", StringUtils.formatNumber(coinsKill)).replace("{points_win}", "").replace("{coins_win}", "").replace("{coins_kills}",
                      Language.ingame$messages$coins$kills.replace("{coins}", StringUtils.formatNumber(coinsKill)).replace("{kills}", StringUtils.formatNumber(this.getKills(player)))
                              .replace("{s}", this.getKills(player) > 1 ? "s" : "")).replace("{points_kills}", pointsKill < 1 ? "" : Language.ingame$messages$points$kills.replace("{s}", this.getKills(player) > 1 ? "s" : "").replace("{points}", StringUtils.formatNumber(pointsKill))));
            }

            NMS.sendTitle(player, Language.ingame$titles$death$header, Language.ingame$titles$death$footer, 0, 60, 0);
          }
        }, 27);
      }
    }, 3);
    this.updateTags();
    this.check();
  }


  @Override
  public void killLeave(Profile profile, Profile killer) {
    Player player = profile.getPlayer();
    
    Player pk = killer != null ? killer.getPlayer() : null;
    if (player.equals(pk)) {
      pk = null;
    }
    
    profile.addStats("kCoreSkyWars", this.getMode().getStats() + "deaths");
    profile.addStats("kCoreSkyWars", "monthlydeaths");
      this.broadcastMessage(Language.ingame$broadcast$suicide.replace("{name}", Role.getColored(player.getName())));
      String suffix = this.addKills(pk);
      EnumSound.ORB_PICKUP.play(pk, 1.0F, 1.0F);
      if (this.getMode() == TnTGameMode.RANKED) {
        killer.addStats("kCoreSkyWars", Language.options$points$kills, "rankedpoints");
      }
      killer.addCoinsWM("kCoreSkyWars", Language.options$coins$kills);
      killer.addStats("kCoreSkyWars", this.getMode().getStats() + "kills");
      killer.addStats("kCoreSkyWars", "monthlykills");
      if (Main.kClans) {
      //  if (ClanAPI.getClanByPlayerName(killer.getName()) != null) {
       //   ClanAPI.addCoins(ClanAPI.getClanByPlayerName(killer.getName()), Language.options$coins$clan$kills);
      //  }
      }
      
      DeathMessage dm = killer.getAbstractContainer("kCoreSkyWars", "selected", SelectedContainer.class).getSelected(CosmeticType.DEATH_MESSAGE, DeathMessage.class);
      if (dm != null) {
        this.broadcastMessage(dm.getRandomMessage().replace("{name}", Role.getColored(player.getName())).replace("{killer}", Role.getColored(pk.getName())) + suffix);
      } else {
        this.broadcastMessage(Language.ingame$broadcast$default_killed_message.replace("{name}", Role.getColored(player.getName())).replace("{killer}", Role.getColored(pk.getName())) + suffix);
      }
    }
    


  private void check() {
    if (this.state != GameState.EMJOGO) {
      return;
    }
    
    List<TnTGameTeam> teams = this.listTeams().stream().filter(GameTeam::isAlive).collect(Collectors.toList());
    if (teams.size() <= 1) {
      this.stop(teams.isEmpty() ? null : teams.get(0));
    }
    
    teams.clear();
  }
  
  @Override
  public void stop(TnTGameTeam winners) {
    this.state = GameState.ENCERRADO;
    
    StringBuilder name = new StringBuilder();
    List<Player> players = winners != null ? winners.listPlayers() : null;
    if (players != null) {
      for (Player player : players) {
        if (!name.toString().isEmpty()) {
          name.append(" §ae ").append(Role.getColored(player.getName()));
        } else {
          name = new StringBuilder(Role.getColored(player.getName()));
        }
      }
      
      players.clear();
    }
    if (name.toString().isEmpty()) {
      this.broadcastMessage(Language.ingame$broadcast$end);
    } else {
      this.broadcastMessage((this.getMode() == TnTGameMode.SOLO || this.getMode() == TnTGameMode.RANKED ? Language.ingame$broadcast$win$solo : Language.ingame$broadcast$win$dupla).replace("{name}", name.toString()));
    }
    for (Player player : this.listPlayers(false)) {
      Profile profile = Profile.getProfile(player.getName());

      profile.update();
      TnTGameTeam team = this.getTeam(player);
      if (team != null) {
        int coinsWin = (int) (team.equals(winners) ? profile.calculateWM(Language.options$coins$wins) : 0);
        int coinsKill = (int) profile.calculateWM(this.getKills(player) * Language.options$coins$kills);
        int pointsKill = this.getKills(player) * Language.options$points$kills;
        int pointsWin = (team.equals(winners) ? Language.options$points$wins : 0);
        int totalPoints = pointsKill + pointsWin;
        int totalCoins = coinsWin + coinsKill;
        
        if (totalCoins > 0 || totalPoints > 0) {
          Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            if (totalCoins > 0) {
              player.sendMessage(
                  Language.ingame$messages$coins$base.replace("{points}", StringUtils.formatNumber(totalPoints)).replace("{coins}", StringUtils.formatNumber(totalCoins))
                      .replace("{points_kills}", pointsKill < 1 ? "" : Language.ingame$messages$points$kills.replace("{s}", this.getKills(player) > 1 ? "s" : "").replace("{points}", StringUtils.formatNumber(pointsKill))).replace("{points_win}", pointsWin > 0 ? Language.ingame$messages$points$win.replace("{points}", StringUtils.formatNumber(pointsWin)) : "").replace("{coins_win}", coinsWin > 0 ? Language.ingame$messages$coins$win.replace("{coins}", StringUtils.formatNumber(coinsWin)) : "").replace("{coins_kills}",
                      coinsKill > 0 ?
                          Language.ingame$messages$coins$kills.replace("{coins}", StringUtils.formatNumber(coinsKill)).replace("{kills}", StringUtils.formatNumber(this.getKills(player)))
                              .replace("{s}", this.getKills(player) > 1 ? "s" : "") :
                          ""));
            }
            if (totalPoints > 0 && this.getMode().equals(TnTGameMode.RANKED)) {
              player.sendMessage(
                  Language.ingame$messages$points$base
                      .replace("{points}", StringUtils.formatNumber(totalPoints))
                      .replace("{points_kills}", pointsKill < 1 ? "" : Language.ingame$messages$points$kills
                          .replace("{kills}", StringUtils.formatNumber(this.getKills(player)))
                          .replace("{s}", this.getKills(player) > 1 ? "s" : "")
                          .replace("{points}", StringUtils.formatNumber(pointsKill)))
                      .replace("{points_win}", pointsWin > 0 ? Language.ingame$messages$points$win.
                          replace("{points}", StringUtils.formatNumber(pointsWin)) : "")
                      .replace("{coins_win}", coinsWin > 0 ? Language.ingame$messages$coins$win
                          .replace("{coins}", StringUtils.formatNumber(coinsWin)) : "")
                      .replace("{coins_kills}",
                          coinsKill > 0 ?
                              Language.ingame$messages$coins$kills.replace("{coins}",
                                  StringUtils.formatNumber(coinsKill)).replace("{kills}", StringUtils.formatNumber(this.getKills(player)))
                                  .replace("{s}", this.getKills(player) > 1 ? "s" : "") :
                              ""));
            }
          }, 30);
        }
      }
      
      if (winners != null && winners.hasMember(player)) {
        profile.addCoinsWM("kCoreSkyWars", Language.options$coins$wins);
        if (this.getMode().equals(TnTGameMode.RANKED)) {
          profile.addStats("kCoreSkyWars", Language.options$points$wins, "rankedpoints");
        }
        if (Main.kClans) {
       //   if (ClanAPI.getClanByPlayerName(profile.getName()) != null) {
        //    ClanAPI.addCoins(ClanAPI.getClanByPlayerName(profile.getName()), Language.options$coins$clan$wins);
        //  }
        }
        profile.addStats("kCoreSkyWars", this.getMode().getStats() + "wins");
        profile.addStats("kCoreSkyWars", "monthlywins");
        NMS.sendTitle(player, Language.ingame$titles$win$header, Language.ingame$titles$win$footer, 10, 80, 10);
      } else {
        NMS.sendTitle(player, Language.ingame$titles$lose$header, Language.ingame$titles$lose$footer, 10, 80, 10);
      }
      
      this.spectators.add(player.getUniqueId());
      profile.setHotbar(Hotbar.getHotbarById("spectator"));
      profile.refresh();
      player.setGameMode(GameMode.ADVENTURE);
      player.setAllowFlight(true);
      player.setFlying(true);
    }
    
    this.updateTags();
    this.task.swap(winners);
  }
  
  @Override
  public void reset() {
    this.event = null;
    this.nextEvent = null;
    this.topKills.clear();
    this.kills.clear();
    this.streak.clear();
    this.players.clear();
    this.spectators.clear();
    this.task.cancel();
    this.listTeams().forEach(TnTGameTeam::reset);
    addToQueue(this);
  }
  
  private void updateTags() {
    for (Player player : this.listPlayers()) {
      Scoreboard scoreboard = player.getScoreboard();
      
      for (Player players : this.listPlayers()) {
        TnTGameTeam gt;
        
        if (this.isSpectator(players)) {
          Team team = scoreboard.getEntryTeam(players.getName());
          if (team != null && !team.getName().equals("spectators")) {
            if (team.getSize() == 1) {
              team.unregister();
            } else {
              team.removeEntry(players.getName());
            }
            team = null;
          }
          
          if (team == null) {
            team = scoreboard.getTeam("spectators");
            if (team == null) {
              team = scoreboard.registerNewTeam("spectators");
              team.setPrefix("§8");
              team.setCanSeeFriendlyInvisibles(true);
            }
            
            if (!team.hasEntry(players.getName())) {
              team.addEntry(players.getName());
            }
          }
        } else if ((gt = this.getTeam(players)) != null) {
          Team team = scoreboard.getTeam(gt.getName());
          if (team == null) {
            team = scoreboard.registerNewTeam(gt.getName());
            team.setPrefix(gt.hasMember(player) ? "§a" : "§c");
          }
          
          if (!team.hasEntry(players.getName())) {
            team.addEntry(players.getName());
          }
        }
      }
    }
  }
  
  @Override
  public String getGameName() {
    return this.name;
  }
  
  public int getTimer() {
    return this.timer;
  }
  
  public void setTimer(int timer) {
    this.timer = timer;
  }
  
  public TnTGameConfig getConfig() {
    return this.config;
  }
  
  public World getWorld() {
    return this.config.getWorld();
  }
  
  public TnTGameTask getTask() {
    return this.task;
  }
  
  public CubeID getCubeId() {
    return this.config.getCubeId();
  }
  
  public String getMapName() {
    return this.config.getMapName();
  }

  public TnTGameMode getMode() {
    return this.config.getMode();
  }
  
  @Override
  public GameState getState() {
    return this.state;
  }
  
  public void setState(GameState state) {
    this.state = state;
  }
  
  @Override
  public boolean isSpectator(Player player) {
    return this.spectators.contains(player.getUniqueId());
  }

  public String addKills(Player player) {
    this.kills.put(player.getName(), this.getKills(player) + 1);
    this.topKills = this.kills.entrySet().stream().sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue())).collect(Collectors.toList());
    Object[] lastKill = this.streak.get(player.getName());
    if (lastKill != null) {
      if ((long) lastKill[0] + 6000 > System.currentTimeMillis()) {
        long streak = (long) lastKill[1];
        this.streak.get(player.getName())[0] = System.currentTimeMillis();
        this.streak.get(player.getName())[1] = streak + 1L;
      }
    }

    lastKill = new Object[2];
    lastKill[0] = System.currentTimeMillis();
    lastKill[1] = 2L;
    this.streak.put(player.getName(), lastKill);
    return "";
  }

  public int getKills(Player player) {
    return this.kills.get(player.getName()) != null ? kills.get(player.getName()) : 0;
  }

  public String getTopKill(int ranking) {
    Map.Entry<String, Integer> entry = this.topKills.size() < ranking ? null : this.topKills.get(ranking - 1);
    if (entry == null) {
      return Language.scoreboards$ranking$empty;
    }

    return Language.scoreboards$ranking$format.replace("{name}", Role.getColored(entry.getKey())).replace("{kills}", StringUtils.formatNumber(entry.getValue()));
  }

  @Override
  public int getOnline() {
    return this.players.size();
  }

  @Override
  public int getMaxPlayers() {
    return this.listTeams().size() * this.getMode().getSize();
  }

  public TnTGameTeam getAvailableTeam(int teamSize) {
    return this.listTeams().stream().filter(team -> team.canJoin(teamSize)).findAny().orElse(null);
  }

  @Override
  public TnTGameTeam getTeam(Player player) {
    return this.listTeams().stream().filter(team -> team.hasMember(player)).findAny().orElse(null);
  }

  public void resetBlock(Block block) {
    TnTGameBlock sb = this.blocks.get(BukkitUtils.serializeLocation(block.getLocation()));
    
    if (sb != null) {
      block.setType(sb.getMaterial());
      BlockState state = block.getState();
      state.getData().setData(sb.getData());
      state.update(true);
    } else {
      block.setType(Material.AIR);
    }
  }

  public Map<String, TnTGameBlock> getBlocks() {
    return this.blocks;
  }
  
  public void generateEvent() {
    this.event =
        this.listEvents().entrySet().stream().filter(e -> !(e.getValue() instanceof AnnounceEvent) && this.getTimer() < e.getKey()).min(Comparator.comparingInt(Map.Entry::getKey))
            .orElse(null);
    this.nextEvent = this.listEvents().entrySet().stream().filter(e -> this.getTimer() < e.getKey()).min(Comparator.comparingInt(Map.Entry::getKey)).orElse(null);
  }
  
  public String getEvent() {
    if (this.event == null) {
      return Language.options$events$end;
    }
    
    return this.event.getValue().getName() + " " + SDF.format((this.event.getKey() - this.getTimer()) * 1000);
  }
  
  public Map.Entry<Integer, TnTGameEvent> getNextEvent() {
    return this.nextEvent;
  }
  
  public int getTimeUntilEvent() {
    return (this.event == null ? this.getTimer() : this.event.getKey()) - this.getTimer();
  }
  
  @Override
  public List<TnTGameTeam> listTeams() {
    return this.config.listTeams();
  }
  

  
  public Map<Integer, TnTGameEvent> listEvents() {
    return this.getMode() == TnTGameMode.SOLO ? TnTGameEvent.SOLO : this.getMode() == TnTGameMode.RANKED ? TnTGameEvent.RANKED : TnTGameEvent.DUPLA;
  }

  @Override
  public List<Player> listPlayers() {
    return this.listPlayers(true);
  }

  public void getPlayer(Player player) {
    player.getPlayer();
  }

  @Override
  public List<Player> listPlayers(boolean spectators) {
    List<Player> players = new ArrayList<>(spectators ? this.spectators.size() + this.players.size() : this.players.size());
    this.players.forEach(id -> players.add(Bukkit.getPlayer(id)));
    if (spectators) {
      this.spectators.stream().filter(id -> !this.players.contains(id)).forEach(id -> players.add(Bukkit.getPlayer(id)));
    }

    return players.stream().filter(Objects::nonNull).collect(Collectors.toList());
  }
  }
