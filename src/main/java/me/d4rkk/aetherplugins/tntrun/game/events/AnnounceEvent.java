package me.d4rkk.aetherplugins.tntrun.game.events;

import dev.slickcollections.kiwizin.nms.NMS;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameEvent;
import dev.slickcollections.kiwizin.utils.StringUtils;
import dev.slickcollections.kiwizin.utils.enums.EnumSound;

public class AnnounceEvent extends TnTGameEvent {
  
  @Override
  public void execute(TnTGameAb game) {
    int minutes = game.getTimeUntilEvent() / 60;
    
    game.listPlayers(false).forEach(player -> {
      EnumSound.CLICK.play(player, 0.5F, 2.0F);
      NMS.sendTitle(player, Language.ingame$titles$end$header,
          Language.ingame$titles$end$footer.replace("{time}", StringUtils.formatNumber(minutes)).replace("{s}", minutes > 1 ? "s" : ""), 20, 60, 20);
    });
  }
  
  @Override
  public String getName() {
    return "";
  }
}