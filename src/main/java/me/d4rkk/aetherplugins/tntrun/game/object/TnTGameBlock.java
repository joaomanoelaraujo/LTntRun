package me.d4rkk.aetherplugins.tntrun.game.object;

import org.bukkit.Material;

public class TnTGameBlock {
  
  private final Material material;
  private final byte data;
  
  public TnTGameBlock(Material material, byte data) {
    this.material = material;
    this.data = data;
  }
  
  public Material getMaterial() {
    return this.material;
  }
  
  public byte getData() {
    return this.data;
  }
  
  @Override
  public String toString() {
    return "SkyWarsBlock{material=" + material + ", data=" + data + "}";
  }
}
