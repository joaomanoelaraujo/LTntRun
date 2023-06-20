package me.d4rkk.aetherplugins.tntrun.game.events;

import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.game.AbstractSkyWars;
import me.d4rkk.aetherplugins.tntrun.game.SkyWarsEvent;

public class EndEvent extends SkyWarsEvent {
  
  @Override
  public void execute(AbstractSkyWars game) {
    game.stop(null);
  }
  
  @Override
  public String getName() {
    return Language.options$events$end;
  }
}
