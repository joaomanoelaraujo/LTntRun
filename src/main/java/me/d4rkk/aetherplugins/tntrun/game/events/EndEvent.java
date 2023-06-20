package me.d4rkk.aetherplugins.tntrun.game.events;

import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameEvent;

public class EndEvent extends TnTGameEvent {
  
  @Override
  public void execute(TnTGameAb game) {
    game.stop(null);
  }
  
  @Override
  public String getName() {
    return Language.options$events$end;
  }
}
