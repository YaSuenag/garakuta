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
    return className.equals("sun.tools.attach.VirtualMachineImpl")
        ? Transformer.transform(classfileBuffer) : null;
  }

  public static void premain(String agentArgs, Instrumentation inst){
    inst.addTransformer(new Agent());
  }

}
