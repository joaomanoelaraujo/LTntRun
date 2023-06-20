package me.d4rkk.aetherplugins.tntrun.cosmetics;


public enum CosmeticType {



  FALL_EFFECT("Efeitos de Queda"),
  DEATH_CRY("Gritos de Morte"),
  DEATH_MESSAGE("Mensagens de Morte"),
  WIN_ANIMATION("Comemorações de Vitória");
  
  private final String[] names;
  
  CosmeticType(String... names) {
    this.names = names;
  }
  
  public String getName(long index) {
    return this.names[(int) (index - 1)];
  }
}
