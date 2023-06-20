package me.d4rkk.aetherplugins.tntrun.api;

import java.util.List;

public interface TREventHandler {
  
  void handleEvent(TREvent evt);
  
  List<Class<?>> getEventTypes();
}
