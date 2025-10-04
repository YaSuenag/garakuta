package com.yasuenag.trjfr;

import java.nio.file.*;
import java.time.*;


public class Options{

  private LocalDateTime start = LocalDateTime.MIN;

  private LocalDateTime end = LocalDateTime.MAX;

  private Path recordingFile = null;

  private Path clippedFile = null;

  public Options(String[] args){
    if((args == null) || (args.length < 1)){
      throw new IllegalArgumentException("Argument is empty");
    }

    for(int idx = 0; idx < args.length; idx++){
      if(args[idx].equals("-s")){
        if(idx == (args.length - 1)){
          throw new IllegalArgumentException("-s needs start time in ISO 8601");
        }
        start = LocalDateTime.parse(args[++idx]);
      }
      else if(args[idx].equals("-e")){
        if(idx == (args.length - 1)){
          throw new IllegalArgumentException("-e needs end time in ISO 8601");
        }
        end = LocalDateTime.parse(args[++idx]);
      }
      else if(args[idx].startsWith("-")){
        throw new IllegalArgumentException("Unknown option: " + args[idx]);
      }
      else if(recordingFile == null){
        recordingFile = Path.of(args[idx]);
      }
      else if(clippedFile == null){
        clippedFile = Path.of(args[idx]);
      }
      else{
        throw new IllegalArgumentException("Too many arguments");
      }
    }

    if(recordingFile == null){
      throw new IllegalArgumentException("Recording file path is not specified");
    }
    if(clippedFile == null){
      throw new IllegalArgumentException("Clipped file path is not specified");
    }
  }

  public LocalDateTime start(){
    return start;
  }

  public LocalDateTime end(){
    return end;
  }

  public Path recordingFile(){
    return recordingFile;
  }

  public Path clippedFile(){
    return clippedFile;
  }

}
