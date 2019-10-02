package com.yasuenag.garakuta.jvmci.jmp.helper;


public class Util{

  public static byte[] generateNullTerminatedByteArray(String str){
    byte[] inBytes = str.getBytes();
    byte[] inBytes_arg = new byte[inBytes.length + 1]; // add null char
    System.arraycopy(inBytes, 0, inBytes_arg, 0, inBytes.length);
    inBytes_arg[inBytes.length] = (byte)0;

    return inBytes_arg;
  }

  public static String generateStringFromNullTerminatedByteArray(byte[] str){
    int nulCharIdx = 0;
    for(nulCharIdx = 0; (nulCharIdx < str.length) && (str[nulCharIdx] != (byte)0); nulCharIdx++){
      // Do nothing.
    }

    return new String(str, 0, nulCharIdx);
  }

}
