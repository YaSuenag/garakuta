package com.yasuenag.garakuta.jvmci.jmp;

import com.yasuenag.garakuta.jvmci.jmp.helper.*;


public class CFuncTest{

  public static class GetPID{

    public static int getPid(){
      throw new UnsupportedOperationException("from Java code");
    }

  }

  public static void main(String[] args) throws Exception{
    AsmInjector injector = new AsmInjector();

    FuncLoader.setup(injector);

    byte[] funcname = Util.generateNullTerminatedByteArray("getpid");
    long funcptr = FuncLoader.getFuncAddr(FuncLoader.RTLD_DEFAULT, funcname);
    injector.injectJmpToVoidMethod(GetPID.class.getMethod("getPid"), funcptr);

    int pid = GetPID.getPid();
    System.out.println("PID = " + pid);

    System.out.println("Press any key to exit...");
    System.in.read();
  }

}
