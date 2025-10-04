package com.yasuenag.trjfr;

import java.io.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import jdk.jfr.consumer.*;


public class Main{

  private static LocalDateTime start;

  private static LocalDateTime end;

  private static boolean filter(RecordedEvent event){
    var eventStart = LocalDateTime.ofInstant(event.getStartTime(), ZoneId.systemDefault());
    return eventStart.isAfter(start) && eventStart.isBefore(end);
  }

  public static void main(String[] args){
    var options = new Options(args);
    start = options.start();
    end = options.end();

    try(var recording = new RecordingFile(options.recordingFile())){
      recording.write(options.clippedFile(), Main::filter);
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }

}
