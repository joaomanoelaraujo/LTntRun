package me.d4rkk.aetherplugins.tntrun.lobby.leaderboards;

import dev.slickcollections.kiwizin.database.Database;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.lobby.Leaderboard;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class KillsLeaderboard extends Leaderboard {
  
  public KillsLeaderboard(Location location, String id) {
    super(location, id);
  }
  
  @Override
  public String getType() {
    return "abates";
  }
  
  @Override
  public List<String[]> getSplitted() {
    List<String[]> list = Database.getInstance().getLeaderBoard("kCoreSkyWars", (this.canSeeMonthly() ?
        Collections.singletonList("monthlykills") : Arrays.asList("1v1kills", "2v2kills", "rankedkills")).toArray(new String[0]));
    while (list.size() < 10) {
      list.add(new String[]{Language.lobby$leaderboard$empty, "0"});
    }
    return list;
  }
  
  @Override
  public List<String> getHologramLines() {
    return Language.lobby$leaderboard$kills$hologram;
  }
}
