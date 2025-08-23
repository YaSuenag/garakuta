package com.yasuenag.garakuta.sigforce;

import java.lang.classfile.*;
import java.lang.constant.*;


public class Transformer{

  private static void replaceBytecodes(MethodBuilder builder, MethodElement element){
    ClassDesc owner = ClassDesc.of("sun.tools.attach.VirtualMachineImpl");
    MethodTypeDesc typeDesc = MethodTypeDesc.of(ClassDesc.ofDescriptor("V"),
                                                ClassDesc.ofDescriptor("I"));
    builder.withCode(c -> c.iload(0)
                           .invokestatic(owner, "sendQuitTo", typeDesc)
                           .iconst_1()
                           .ireturn());
  }

  public static byte[] transform(byte[] classBytes){
    ClassFile cf = ClassFile.of();
    ClassModel cm = cf.parse(classBytes);
    ClassTransform ct = ClassTransform.transformingMethods(mm -> mm.methodName().equalsString("checkCatchesAndSendQuitTo"),
                                                           Transformer::replaceBytecodes);
    return cf.transformClass(cm, ct);
  }

}
