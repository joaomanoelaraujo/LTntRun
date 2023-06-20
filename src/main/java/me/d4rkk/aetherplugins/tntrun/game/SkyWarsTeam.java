package me.d4rkk.aetherplugins.tntrun.game;

import dev.slickcollections.kiwizin.game.GameTeam;

public class SkyWarsTeam extends GameTeam {
  
  private final int index;

  
  public SkyWarsTeam(AbstractSkyWars game, String location, int size) {
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
