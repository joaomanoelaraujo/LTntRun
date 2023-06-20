package me.d4rkk.aetherplugins.tntrun.cmd.sw;

import me.d4rkk.aetherplugins.tntrun.cmd.SubCommand;
import org.bukkit.entity.Player;

public class UpdateCommand extends SubCommand {
  
  public UpdateCommand() {
    super("atualizar", "atualizar", "Atualizar o kSkyWars.", true);
  }
  
  @Override
  public void perform(Player player, String[] args) {
     player.sendMessage("§aO plugin já se encontra em sua última versão.");
  }
}
