package me.d4rkk.aetherplugins.tntrun.hook.hotbar;

import dev.slickcollections.kiwizin.player.Profile;
import dev.slickcollections.kiwizin.player.hotbar.HotbarActionType;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import me.d4rkk.aetherplugins.tntrun.menus.MenuLobbies;
import me.d4rkk.aetherplugins.tntrun.menus.MenuPlay;
import me.d4rkk.aetherplugins.tntrun.menus.MenuShop;
import me.d4rkk.aetherplugins.tntrun.menus.MenuSpectator;
import org.bukkit.entity.Player;


public class SWHotbarActionType extends HotbarActionType {

  @Override
  public void execute(Profile profile, String action, Player target) {
    if (action.equalsIgnoreCase("loja")) {
      new MenuShop(profile);
    } else if (action.equalsIgnoreCase("lobbies")) {
      new MenuLobbies(profile);
    } else if (action.equalsIgnoreCase("espectar")) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game != null) {
        new MenuSpectator(profile.getPlayer(), game);
      }
    } else if (action.equalsIgnoreCase("jogar")) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game != null) {
        new MenuPlay(profile, game.getMode());
      }
    } else if (action.equalsIgnoreCase("sair")) {
      TnTGameAb game = profile.getGame(TnTGameAb.class);
      if (game != null) {
        game.leave(profile, null);
      }
    }
  }
}
