package me.d4rkk.aetherplugins.tntrun.game;

import dev.slickcollections.kiwizin.game.GameTeam;

public class TnTGameTeam extends GameTeam {
  
  private final int index;

  
  public TnTGameTeam(TnTGameAb game, String location, int size) {
    super(game, location, size);
    this.index = game.listTeams().size();
  }
  
  @Override
  public void reset() {
    super.reset();
  }
  
  public void startGame() {

    }
}
