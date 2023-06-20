package me.d4rkk.aetherplugins.tntrun.api.event.game;

import dev.slickcollections.kiwizin.game.Game;
import dev.slickcollections.kiwizin.game.GameTeam;
import me.d4rkk.aetherplugins.tntrun.api.TREvent;

public class TRGameStartEvent extends TREvent {
  
  private final Game<? extends GameTeam> game;
  
  public TRGameStartEvent(Game<? extends GameTeam> game) {
    this.game = game;
  }
  
  public Game<? extends GameTeam> getGame() {
    return this.game;
  }
}
