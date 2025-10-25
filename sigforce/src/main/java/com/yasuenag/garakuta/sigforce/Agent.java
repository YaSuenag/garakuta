package com.yasuenag.garakuta.sigforce;

import java.lang.instrument.*;
import java.security.*;


public class Agent implements ClassFileTransformer{

  @Override
  public byte[] transform(ClassLoader loader,
                          String className,
                          Class<?> classBeingRedefined,
                          ProtectionDomain protectionDomain,
                          byte[] classfileBuffer)
                              throws IllegalClassFormatException{
    if(className.equals("sun/tools/attach/VirtualMachineImpl")){
      System.out.println("sigforce: register transformer for " + className);
      return Transformer.transform(classfileBuffer);
    }

    return null;
  }

  public static void premain(String agentArgs, Instrumentation inst){
    System.out.println("sigforce: loaded");
    inst.addTransformer(new Agent());
  }

}
