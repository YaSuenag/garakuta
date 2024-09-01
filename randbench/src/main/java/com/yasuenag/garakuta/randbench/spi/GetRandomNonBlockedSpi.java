package com.yasuenag.garakuta.randbench.spi;

import java.security.SecureRandomSpi;

import com.yasuenag.garakuta.randbench.internal.FFMHelper;


public class GetRandomNonBlockedSpi extends SecureRandomSpi{

  @Override
  protected byte[] engineGenerateSeed(int numBytes){
    var result = new byte[numBytes];
    GetRandomHelper.getRandomNonBlocked(result);
    return result;
  }

  @Override
  protected void engineNextBytes(byte[] bytes){
    GetRandomHelper.getRandomNonBlocked(bytes);
  }

  @Override
  protected void engineSetSeed(byte[] seed){
    // Do nothing.
  }

}
