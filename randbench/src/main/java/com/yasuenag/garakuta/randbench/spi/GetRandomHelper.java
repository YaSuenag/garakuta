package com.yasuenag.garakuta.randbench.spi;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import com.yasuenag.garakuta.randbench.internal.FFMHelper;


public class GetRandomHelper{

  private static final MethodHandle getrandom;

  // from sys/random.h
  private static final int GRND_NONBLOCK = 0x01;
  private static final int GRND_RANDOM   = 0x02;

  static{
    getrandom = FFMHelper.getGetRandom();
  }

  public static void getRandomBlocked(byte[] bytes){
    try{
      var _ = (long)getrandom.invokeExact(MemorySegment.ofArray(bytes), (long)bytes.length, GRND_RANDOM);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public static void getRandomNonBlocked(byte[] bytes){
    try{
      var _ = (long)getrandom.invokeExact(MemorySegment.ofArray(bytes), (long)bytes.length, GRND_NONBLOCK);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

}
