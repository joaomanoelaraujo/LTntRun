package me.d4rkk.aetherplugins.tntrun.game.enums;

import dev.slickcollections.kiwizin.reflection.Accessors;
import me.d4rkk.aetherplugins.tntrun.game.TnTGameAb;
import me.d4rkk.aetherplugins.tntrun.game.interfaces.LoadCallback;
import me.d4rkk.aetherplugins.tntrun.game.types.NormalTnTGame;

public enum TnTGameMode {
  RANKED("Ranked", "ranked", 1, NormalTnTGame.class, 1),
  SOLO("Solo", "1v1", 1, NormalTnTGame.class, 1),
  DUPLA("Duplas", "2v2", 2, NormalTnTGame.class, 1);

  private static final TnTGameMode[] VALUES = values();
  private final int size;
  private final String stats;
  private final String name;
  private final Class<? extends TnTGameAb> gameClass;
  private final int cosmeticIndex;

  TnTGameMode(String name, String stats, int size, Class<? extends TnTGameAb> gameClass, int cosmeticIndex) {
    this.name = name;
    this.stats = stats;
    this.size = size;
    this.gameClass = gameClass;
    this.cosmeticIndex = cosmeticIndex;
  }

  public static TnTGameMode fromName(String name) {
    for (TnTGameMode mode : VALUES) {
      if (name.equalsIgnoreCase(mode.name())) {
        return mode;
      }
    }

    return null;
  }

  public TnTGameAb buildGame(String name, LoadCallback callback) {
    return Accessors.getConstructor(this.gameClass, String.class, LoadCallback.class).newInstance(name, callback);
  }

  public int getSize() {
    return this.size;
  }

  public String getStats() {
    return this.stats;
  }

  public String getName() {
    return this.name;
  }

  public int getCosmeticIndex() {
    return cosmeticIndex;
  }
}
