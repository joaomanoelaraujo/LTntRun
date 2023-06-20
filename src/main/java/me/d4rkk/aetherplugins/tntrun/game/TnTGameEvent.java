package me.d4rkk.aetherplugins.tntrun.game;

import dev.slickcollections.kiwizin.plugin.logger.KLogger;
import me.d4rkk.aetherplugins.tntrun.Language;
import me.d4rkk.aetherplugins.tntrun.Main;
import me.d4rkk.aetherplugins.tntrun.game.events.AnnounceEvent;
import me.d4rkk.aetherplugins.tntrun.game.events.EndEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public abstract class TnTGameEvent {
  
  public static final Map<Integer, TnTGameEvent> SOLO = new LinkedHashMap<>(), DUPLA = new LinkedHashMap<>(), RANKED = new LinkedHashMap<>();
  public static KLogger LOGGER = ((KLogger) Main.getInstance().getLogger()).getModule("EVENTS");
  private static TnTGameEvent END_EVENT, ANNOUNCE_EVENT;

  public static void setupEvents() {
    END_EVENT = new EndEvent();

    ANNOUNCE_EVENT = new AnnounceEvent();
    for (String evt : Language.options$events$solo$timings) {
      Object[] event = parseEvent(evt);
      if (event == null) {
        LOGGER.log(Level.WARNING, "O evento solo \"" + evt + "\" nao e valido");
        continue;
      }
      
      SOLO.put((int) event[0], (TnTGameEvent) event[1]);
    }
    
    for (String evt : Language.options$events$ranked$timings) {
      Object[] event = parseEvent(evt);
      if (event == null) {
        LOGGER.log(Level.WARNING, "O evento ranked \"" + evt + "\" nao e valido");
        continue;
      }
      
      RANKED.put((int) event[0], (TnTGameEvent) event[1]);
    }
    
    for (String evt : Language.options$events$dupla$timings) {
      Object[] event = parseEvent(evt);
      if (event == null) {
        LOGGER.log(Level.WARNING, "O evento dupla \"" + evt + "\" nao e valido");
        continue;
      }
      
      DUPLA.put((int) event[0], (TnTGameEvent) event[1]);
    }
  }

  private static Object[] parseEvent(String evt) {
    String[] splitter = evt.split(":");
    if (splitter.length <= 1) {
      return null;
    }
    
    int time = 0;
    try {
      if (splitter[1].startsWith("-")) {
        throw new Exception();
      }
      time = Integer.parseInt(splitter[1]);
    } catch (Exception ex) {
      return null;
    }
    
    String eventName = splitter[0];
    if (eventName.equalsIgnoreCase("fim")) {
      return new Object[]{time, END_EVENT};

    } else if (eventName.equalsIgnoreCase("anuncio")) {
      return new Object[]{time, ANNOUNCE_EVENT};
    }
    
    return null;
  }
  
  public abstract void execute(TnTGameAb game);
  
  public abstract String getName();
}
