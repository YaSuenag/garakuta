package com.yasuenag.garakuta.ffm.upcall;

import java.lang.foreign.*;
import java.lang.invoke.*;


public class Main{

  private static final MethodHandle nativeFunc;

  private static final FunctionDescriptor callbackDesc;
  private static final MethodHandle MH_callback;

  private final MethodHandle MH_callbackToUse;
  private final MemorySegment ptrCallback;

  static{
    try{
      System.loadLibrary("native");
      var lookup = SymbolLookup.loaderLookup();
      var desc = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
      var ptrNativeFunc = SymbolLookup.loaderLookup()
                                    .findOrThrow("native");
      nativeFunc = Linker.nativeLinker()
                         .downcallHandle(ptrNativeFunc, desc);

      callbackDesc = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
      MH_callback = MethodHandles.lookup()
                                 .findVirtual(Main.class, "callback", callbackDesc.toMethodType());
    }
    catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  public Main(){
    MH_callbackToUse = MethodHandles.insertArguments(MH_callback, 0, this);
    ptrCallback = Linker.nativeLinker()
                        .upcallStub(MH_callbackToUse, callbackDesc, Arena.global());
  }

  public void callback(int arg){
    System.out.println("from callback: " + arg);
  }

  public static void main(String[] args) throws Throwable{
    var inst = new Main();

    nativeFunc.invoke(100, inst.ptrCallback);
  }

}
