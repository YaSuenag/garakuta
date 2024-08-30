package com.yasuenag.garakuta.nativestackoverflow;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.ref.*;
import java.nio.file.*;
import java.util.*;

import com.yasuenag.ffmasm.*;
import com.yasuenag.ffmasm.amd64.*;


public class Main{

  private static final CodeSegment seg;
  private static final MethodHandle getStackValue;

  static{
    try{
      seg = new CodeSegment();

      var desc = FunctionDescriptor.of(ValueLayout.JAVA_LONG);
      getStackValue = AMD64AsmBuilder.create(AMD64AsmBuilder.class, seg, desc)
                /* mov %rsp, %rax */ .movMR(Register.RSP, Register.RAX, OptionalInt.empty())
                /* ret            */ .ret()
                                     .build();
    }
    catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  private static long guessTailOfGuardPage(long rsp) throws Exception{
    String guessedGuardPage = null;
    try(var reader = Files.newBufferedReader(Path.of("/proc/self/maps"))){
      String line;
      while((line = reader.readLine()) != null){
        var elements = line.split(" ");
        if(elements[1].equals("---p")){
          guessedGuardPage = elements[0];
        }
        else{
          var range = elements[0].split("-");
          long top = Long.parseUnsignedLong(range[0], 16);
          long end = Long.parseUnsignedLong(range[1], 16);
          if((Long.compareUnsigned(rsp, top) >= 0) &&
             (Long.compareUnsigned(rsp, end) < 0)){
            return Long.parseUnsignedLong(guessedGuardPage.split("-")[1], 16);
          }
        }
      }
    }
    return -1;
  }

  public static void dummy(){
    throw new IllegalStateException("Should not reach here!");
  }

  private static MethodHandle createStackOverflowStub(long guardPage) throws Exception{
    // Add some bytes to stack enough to transit to _thread_in_java state
    long newRSP = guardPage + 1024;

    var hndDummy = MethodHandles.lookup()
                                .findStatic(Main.class, "dummy", MethodType.methodType(void.class));
    var dummyStub = Linker.nativeLinker()
                          .upcallStub(hndDummy, FunctionDescriptor.ofVoid(), Arena.ofAuto());

    return AMD64AsmBuilder.create(AMD64AsmBuilder.class, seg, FunctionDescriptor.ofVoid())
     /* push %rbp      */ .push(Register.RBP)
     /* mov %rsp, %rbp */ .movRM(Register.RBP, Register.RSP, OptionalInt.empty())
     /* mov addr, %rsp */ .movImm(Register.RSP, newRSP)
     /* mov addr, %r10 */ .movImm(Register.R10, dummyStub.address())
     /* call %r10      */ .call(Register.R10)
     /* leave          */ .leave()
     /* ret            */ .ret()
                          .build();
  }

  public static void main(String[] args) throws Throwable{
    long rsp = (long)getStackValue.invokeExact();
    System.out.println("rsp: " + Long.toHexString(rsp));

    long guessedGuardPage = guessTailOfGuardPage(rsp);
    System.out.println("guard page: " + Long.toHexString(guessedGuardPage));

    MethodHandle stub = createStackOverflowStub(guessedGuardPage);
    stub.invokeExact();

    throw new IllegalStateException("Should not reach here!");
  }

}
