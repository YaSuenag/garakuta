package com.yasuenag.trjfr;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import jdk.jfr.consumer.*;


public class Main{

  private static int totalEvents;

  private static void printEntry(Map.Entry<RecordedMethod, AtomicInteger> entry){
    var numEvents = entry.getValue().get();
    var method = entry.getKey();
    System.out.format("%s.%s%s, %d, %.02f%%\n",
                      method.getType().getName(),
                      method.getName(),
                      method.getDescriptor(),
                      numEvents,
                      (double)numEvents / (double)totalEvents * 100.0);
  }

  public static void main(String[] args){
    var options = new Options(args);

    Map<RecordedMethod, AtomicInteger>  map = new HashMap<>();
    totalEvents = 0;

    try(var recording = new RecordingFile(options.recordingFile())){
      while(recording.hasMoreEvents()){
        var event = recording.readEvent();
        if(event.getEventType().getName().equals("jdk.ExecutionSample")){
          var eventStart = LocalDateTime.ofInstant(event.getStartTime(), ZoneId.systemDefault());
          if(eventStart.isAfter(options.start()) && eventStart.isBefore(options.end())){
            var topStack = event.getStackTrace().getFrames().get(0).getMethod();
            var counter = map.computeIfAbsent(topStack, k -> new AtomicInteger());
            counter.incrementAndGet();
            totalEvents++;
          }
        }
      }
    }
    catch(IOException e){
      e.printStackTrace();
    }

    map.entrySet()
       .stream()
       .sorted(Comparator.comparingInt(e -> ((Map.Entry<String, AtomicInteger>)e).getValue().get()).reversed())
       .forEach(Main::printEntry);
  }

}
