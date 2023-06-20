package me.d4rkk.aetherplugins.tntrun.lobby.leaderboards;

import dev.slickcollections.kiwizin.database.Database;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.lobby.Leaderboard;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WinsLeaderboard extends Leaderboard {
  
  public WinsLeaderboard(Location location, String id) {
    super(location, id);
  }
  
  @Override
  public String getType() {
    return "vitorias";
  }
  
  @Override
  public List<String[]> getSplitted() {
    List<String[]> list = Database.getInstance().getLeaderBoard("kCoreSkyWars", (canSeeMonthly() ?
        Collections.singletonList("monthlywins") : Arrays.asList("1v1wins", "2v2wins", "rankedwins")).toArray(new String[0]));
    while (list.size() < 10) {
      list.add(new String[]{Language.lobby$leaderboard$empty, "0"});
    }
    return list;
  }
  
  @Override
  public List<String> getHologramLines() {
    return Language.lobby$leaderboard$wins$hologram;
  }
}
