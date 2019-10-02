package com.yasuenag.garakuta.jvmci.jmp.helper;


public class FuncLoader{

  // from /usr/include/dlfcn.h
  public static final long RTLD_DEFAULT = 0L;

  public static long loadLibrary(byte[] libnameInBytes, byte[] errMsgInBytes){
    throw new UnsupportedOperationException("from Java code");
  }

  public static long getFuncAddr(long handle, byte[] symbolInBytes){
    throw new UnsupportedOperationException("from Java code");
  }

  public static void setup(AsmInjector injector) throws Exception{
    injector.injectLoadFunc(
           FuncLoader.class.getMethod("loadLibrary", byte[].class, byte[].class));
    injector.injectLookupFunc(
           FuncLoader.class.getMethod("getFuncAddr", long.class, byte[].class));
  }

}

