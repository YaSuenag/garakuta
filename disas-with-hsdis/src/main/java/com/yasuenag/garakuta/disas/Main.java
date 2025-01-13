package com.yasuenag.garakuta.disas;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;

import com.yasuenag.ffmasm.*;
import com.yasuenag.ffmasm.amd64.*;


public class Main{

  public static MethodHandle createDisassembler(){
    var hsdisPath = System.getProperty("hsdis", "hsdis-amd64");
    var sym = SymbolLookup.libraryLookup(hsdisPath, Arena.global());
    var disas = sym.find("decode_instructions_virtual").get();
    var desc = FunctionDescriptor.of(ValueLayout.ADDRESS,   // return value
                                     ValueLayout.ADDRESS,   // start_va
                                     ValueLayout.ADDRESS,   // end_va
                                     ValueLayout.ADDRESS,   // buffer
                                     ValueLayout.JAVA_LONG, // length
                                     ValueLayout.ADDRESS,   // event_callback
                                     ValueLayout.ADDRESS,   // event_stream
                                     ValueLayout.ADDRESS,   // printf_callback
                                     ValueLayout.ADDRESS,   // printf_stream
                                     ValueLayout.ADDRESS,   // options
                                     ValueLayout.JAVA_INT   // newline
                                    );
    return Linker.nativeLinker().downcallHandle(disas, desc);
  }

  public static MemorySegment createRDTSC() throws Exception{
    var seg = new CodeSegment();
    return AMD64AsmBuilder.create(AMD64AsmBuilder.class, seg)
     /* push %rbp      */ .push(Register.RBP)
     /* mov %rsp, %rbp */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
     /* rdtsc          */ .rdtsc()
     /* shl $32, %rdx  */ .shl(Register.RDX, (byte)32, OptionalInt.empty())
     /* or %rdx, %rax  */ .orMR(Register.RDX, Register.RAX, OptionalInt.empty())
     /* leave          */ .leave()
     /* ret            */ .ret()
                          .getMemorySegment();
  }

  public static void main(String[] args) throws Throwable{
    var rdtsc = createRDTSC();
    var decode_instructions_virtual = createDisassembler();

    decode_instructions_virtual.invoke(
      rdtsc,                           // start_va
      rdtsc.asSlice(rdtsc.byteSize()), // end_va
      rdtsc,                           // buffer
      rdtsc.byteSize(),                // length
      MemorySegment.NULL,              // event_callback
      MemorySegment.NULL,              // event_stream
      MemorySegment.NULL,              // printf_callback
      MemorySegment.NULL,              // printf_stream
      Arena.global().allocateFrom(""), // options
      1                                // newline
    );
  }

}
