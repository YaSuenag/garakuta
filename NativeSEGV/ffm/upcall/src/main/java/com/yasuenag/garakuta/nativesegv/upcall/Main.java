package com.yasuenag.garakuta.nativesegv.upcall;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;

import com.yasuenag.ffmasm.*;
import com.yasuenag.ffmasm.amd64.*;

import static java.lang.foreign.MemorySegment.*;
import static java.lang.foreign.ValueLayout.*;

import static com.yasuenag.ffmasm.amd64.Register.*;


public class Main{

  public static void doSEGV(){
    var nullMem = NULL.reinterpret(JAVA_INT.byteSize());
    nullMem.set(JAVA_INT, 0, 1);
  }

  public static void main(String[] args) throws Throwable{
    var mhDoSEGV = MethodHandles.lookup()
                                .findStatic(Main.class, "doSEGV", MethodType.methodType(void.class));
    var doSEGVStub = Linker.nativeLinker()
                           .upcallStub(mhDoSEGV, FunctionDescriptor.ofVoid(), Arena.ofAuto());

    try(var seg = new CodeSegment()){
      var invoker = AMD64AsmBuilder.create(AMD64AsmBuilder.class, seg, FunctionDescriptor.ofVoid())
              /* push %rbp      */ .push(RBP)
              /* mov %rsp, %rbp */ .movRM(RBP, RSP, OptionalInt.empty())
              /* mov addr, %r10 */ .movImm(R10, doSEGVStub.address())
              /* call %r10      */ .call(R10)
              /* leave          */ .leave()
              /* ret            */ .ret()
                                   .build();
      invoker.invokeExact();
    }

  }

}
