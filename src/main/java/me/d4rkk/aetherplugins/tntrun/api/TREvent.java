package me.d4rkk.aetherplugins.tntrun.api;

import java.util.ArrayList;
import java.util.List;

public class TREvent {

  private static final List<TREventHandler> HANDLERS = new ArrayList<>();

  public static void registerHandler(TREventHandler handler) {
    HANDLERS.add(handler);
  }

  public static void callEvent(TREvent evt) {
    HANDLERS.stream().filter(handler -> handler.getEventTypes().contains(evt.getClass())).forEach(handler -> handler.handleEvent(evt));
  }
}