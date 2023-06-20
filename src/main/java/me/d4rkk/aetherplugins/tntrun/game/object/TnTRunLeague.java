package me.d4rkk.aetherplugins.tntrun.game.object;

import dev.slickcollections.kiwizin.player.Profile;
import dev.slickcollections.kiwizin.plugin.config.KConfig;
import me.d4rkk.aetherplugins.tntrun.Main;
import dev.slickcollections.kiwizin.utils.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class TnTRunLeague {
  
  private static final List<TnTRunLeague> LEAGUES = new ArrayList<>();
  private static final KConfig CONFIG = Main.getInstance().getConfig("leagues");
  protected String name;
  protected String tag;
  protected String symbol;
  protected long points;
  protected String key;
  
  public TnTRunLeague(String key, String name, String symbol, String tag, long points) {
    this.name = StringUtils.formatColors(name);
    this.points = points;
    this.symbol = symbol;
    this.tag = StringUtils.formatColors(tag.replace("{symbol}", symbol).replace("{name}", name));
    this.key = key;
  }
  
  public static long getPoints(Profile profile) {
    return profile.getDataContainer("kCoreSkyWars", "rankedpoints").getAsLong();
  }
  
  public static TnTRunLeague fromKey(String key) {
    return listLeagues().stream().filter(k -> k.getKey().equals(key)).findFirst().orElse(null);
  }
  
  public static TnTRunLeague fromPoints(long compare) {
    return listLeagues().stream().filter(a -> compare >= Long.parseLong(String.valueOf(a.getPoints()))).findFirst()
        .orElse(listLeagues().get(listLeagues().size() - 1));
  }
  
  public static TnTRunLeague getLeague(Profile profile) {
    return listLeagues().stream().
        filter(f -> getPoints(profile) >= f.getPoints()).findFirst().orElse(listLeagues().get(listLeagues().size() - 1));
  }
  
  public static void setupLeagues() {
    ConfigurationSection section = CONFIG.getSection("leagues");
    section.getKeys(false).forEach(key -> LEAGUES
        .add(new TnTRunLeague(key, section.getString(key + ".name"), section.getString(key + ".symbol"),
            section.getString(key + ".tag"), section.getLong(key + ".points"))));
    LEAGUES.sort((l1, l2) -> Long.compare(l2.getPoints(), l1.getPoints()));
  }
  
  public static List<TnTRunLeague> listLeagues() {
    return LEAGUES;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getTag() {
    return this.tag;
  }
  
  public String getSymbol() {
    return this.symbol;
  }
  
  public String getKey() {
    return this.key;
  }
  
  public long getPoints() {
    return this.points;
  }
}
