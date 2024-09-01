package com.yasuenag.garakuta.randbench.spi;

import java.security.SecureRandomSpi;

import com.yasuenag.garakuta.randbench.internal.FFMHelper;


public class GetRandomBlockedSpi extends SecureRandomSpi{

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    var result = new byte[numBytes];
    GetRandomHelper.getRandomBlocked(result);
    return result;
  }

  @Override
  protected void engineNextBytes(byte[] bytes){
    GetRandomHelper.getRandomBlocked(bytes);
  }

  @Override
  protected void engineSetSeed(byte[] seed){
    // Do nothing.
  }

}
