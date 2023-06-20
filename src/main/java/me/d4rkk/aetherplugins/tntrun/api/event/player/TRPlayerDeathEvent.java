package me.d4rkk.aetherplugins.tntrun.api.event.player;


import dev.slickcollections.kiwizin.game.Game;
import dev.slickcollections.kiwizin.game.GameTeam;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.api.TREvent;
import me.d4rkk.aetherplugins.tntrun.game.AbstractSkyWars;
import org.bukkit.event.Cancellable;

public class TRPlayerDeathEvent extends TREvent implements Cancellable {
  private boolean isCancelled;
  private final Game<? extends GameTeam> game;
  private final Profile profile;
  private final Profile killer;

  public TRPlayerDeathEvent(AbstractSkyWars game, Profile profile, Profile killer) {
    this.game = game;
    this.profile = profile;
    this.killer = killer;
  }

  public Game<? extends GameTeam> getGame() {
    return this.game;
  }

  public Profile getProfile() {
    return this.profile;
  }

  public Profile getKiller() {
    return this.killer;
  }

  public boolean hasKiller() {
    return this.killer != null;
  }

  @Override
  public boolean isCancelled() {
    return this.isCancelled;
  }

  @Override
  public void setCancelled(boolean isCancelled) {
    //this.isCancelled = isCancelled;
  }
}
