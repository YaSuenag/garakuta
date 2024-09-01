package com.yasuenag.garakuta.randbench.spi;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.security.SecureRandomSpi;

import com.yasuenag.garakuta.randbench.internal.FFMHelper;


public class RdSeedSpi extends SecureRandomSpi{

  private static final MethodHandle fillWithRDSEED;

  static{
    fillWithRDSEED = FFMHelper.getFillWithRDSEED();
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    try{
      var result = new byte[numBytes];
      fillWithRDSEED.invokeExact(MemorySegment.ofArray(result), numBytes);
      return result;
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  @Override
  protected void engineNextBytes(byte[] bytes){
    try{
      fillWithRDSEED.invokeExact(MemorySegment.ofArray(bytes), bytes.length);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  @Override
  protected void engineSetSeed(byte[] seed){
    // Do nothing.
  }

}
