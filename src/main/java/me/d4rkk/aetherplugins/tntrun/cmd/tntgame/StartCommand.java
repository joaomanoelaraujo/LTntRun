package me.d4rkk.aetherplugins.tntrun.cmd.tntgame;

import dev.slickcollections.kiwizin.game.GameState;
import dev.slickcollections.kiwizin.player.Profile;
import me.d4rkk.aetherplugins.tntrun.cmd.SubCommand;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import org.bukkit.entity.Player;

public class StartCommand extends SubCommand {
  
  public StartCommand() {
    super("iniciar", "iniciar", "Iniciar a partida.", true);
  }
  
  @Override
  public void perform(Player player, String[] args) {
    Profile profile = Profile.getProfile(player.getName());
    if (profile != null) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game != null) {
        if (game.getState() == GameState.AGUARDANDO) {
          game.start();
          player.sendMessage("§aVocê iniciou a partida!");
        } else {
          player.sendMessage("§cA partida já está em andamento.");
        }
      }
    }
  }
}
