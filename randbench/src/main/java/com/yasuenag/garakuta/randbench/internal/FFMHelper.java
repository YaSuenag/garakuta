package com.yasuenag.garakuta.randbench.internal;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.CodeSegment;
import com.yasuenag.ffmasm.PlatformException;
import com.yasuenag.ffmasm.UnsupportedPlatformException;
import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;


public class FFMHelper{

  private static final CodeSegment seg;

  private static final MethodHandle fillWithRDRAND;
  private static final MethodHandle fillWithRDSEED;
  private static final MethodHandle getrandom;

  private static MethodHandle createRDRAND() throws PlatformException, UnsupportedPlatformException{
    var desc = FunctionDescriptor.ofVoid(
                 ValueLayout.ADDRESS, // 1st argument (mem)
                 ValueLayout.JAVA_INT // 2nd argument (length)
               );
    return AMD64AsmBuilder.create(AMD64AsmBuilder.class, seg, desc)
/* .align 16            */ .alignTo16BytesWithNOP()
/* bulk:                */ .label("bulk")
/*   rdrand %rax        */ .rdrand(Register.RAX)
/*   jae bulk           */ .jae("bulk")
/*   mov %rax, (<arg1>) */ .movMR(Register.RAX, Register.RDI, OptionalInt.of(0))
/*   add $8, <arg1>     */ .add(Register.RDI, 8, OptionalInt.empty())
/*   sub $8, <arg2>     */ .sub(Register.RSI, 8, OptionalInt.empty())
/*   jne bulk           */ .jne("bulk")
/*   ret                */ .ret()
                           .build(Linker.Option.critical(true));
  }

  private static MethodHandle createRDSEED() throws PlatformException, UnsupportedPlatformException{
    var desc = FunctionDescriptor.ofVoid(
                 ValueLayout.ADDRESS, // 1st argument (mem)
                 ValueLayout.JAVA_INT // 2nd argument (length)
               );
    return AMD64AsmBuilder.create(AMD64AsmBuilder.class, seg, desc)
/* .align 16            */ .alignTo16BytesWithNOP()
/* bulk:                */ .label("bulk")
/*   rdseed %rax        */ .rdseed(Register.RAX)
/*   jae bulk           */ .jae("bulk")
/*   mov %rax, (<arg1>) */ .movMR(Register.RAX, Register.RDI, OptionalInt.of(0))
/*   add $8, <arg1>     */ .add(Register.RDI, 8, OptionalInt.empty())
/*   sub $8, <arg2>     */ .sub(Register.RSI, 8, OptionalInt.empty())
/*   jne bulk           */ .jne("bulk")
/*   ret                */ .ret()
                           .build(Linker.Option.critical(true));
  }

  private static MethodHandle createGetRandom(){
    var linker = Linker.nativeLinker();
    var size_tLayout = linker.canonicalLayouts().get("size_t");
    var desc = FunctionDescriptor.of(
                 size_tLayout,        // return value (ssize_t)
                 ValueLayout.ADDRESS, // 1st argument (buf)
                 size_tLayout,        // 2nd argument (buflen)
                 ValueLayout.JAVA_INT // 3rd argument (flags)
               );
    return linker.downcallHandle(linker.defaultLookup()
                                       .find("getrandom")
                                       .get(),
                                 desc,
                                 Linker.Option.critical(true));
  }

  static{
    try{
      seg = new CodeSegment();

      fillWithRDRAND = createRDRAND();
      fillWithRDSEED = createRDSEED();
      getrandom = createGetRandom();
    }
    catch(PlatformException | UnsupportedPlatformException e){
      throw new RuntimeException(e);
    }
  }

  public static MethodHandle getFillWithRDRAND(){
    return fillWithRDRAND;
  }

  public static MethodHandle getFillWithRDSEED(){
    return fillWithRDSEED;
  }

  public static MethodHandle getGetRandom(){
    return getrandom;
  }

}
