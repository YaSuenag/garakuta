package com.yasuenag.garakuta.jvmci.jmp;

import com.yasuenag.garakuta.jvmci.jmp.helper.*;


public class CFuncTest{

  public static class GetPID{

    public static int getPid(){
      throw new UnsupportedOperationException("from Java code");
    }

  }

  public static void callNative(){
    throw new UnsupportedOperationException("from Java code");
  }

  public static void main(String[] args) throws Exception{
    AsmInjector injector = new AsmInjector();

    FuncLoader.setup(injector);

    byte[] funcname = Util.generateNullTerminatedByteArray("getpid");
    long funcptr = FuncLoader.getFuncAddr(FuncLoader.RTLD_DEFAULT, funcname);
    injector.injectJmpToVoidMethod(GetPID.class.getMethod("getPid"), funcptr);

    int pid = GetPID.getPid();
    System.out.println("PID = " + pid);

    String libNameInStr = (args.length == 0) ? "libnative.so"
                                             : args[0];

    byte[] libname = Util.generateNullTerminatedByteArray(libNameInStr);
    byte[] errMsg = new byte[1024];
    long handle = FuncLoader.loadLibrary(libname, errMsg);
    if(handle == 0L){
      System.out.println(Util.generateStringFromNullTerminatedByteArray(errMsg));
    }
    else{
      funcname = Util.generateNullTerminatedByteArray("call_native");
      funcptr = FuncLoader.getFuncAddr(handle, funcname);
      injector.injectJmpToVoidMethod(CFuncTest.class.getMethod("callNative"), funcptr);
      callNative();
    }

    System.out.println("Press any key to exit...");
    System.in.read();
  }

}
