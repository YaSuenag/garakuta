package com.yasuenag.garakuta.jvmci.jmp.helper;

import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

import jdk.vm.ci.amd64.*;
import jdk.vm.ci.code.*;
import jdk.vm.ci.code.site.*;
import jdk.vm.ci.hotspot.*;
import jdk.vm.ci.meta.*;
import jdk.vm.ci.runtime.*;

import sun.misc.*;


public class AsmInjector{

  private static final MetaAccessProvider metaAccess;

  private static final CodeCacheProvider codeCache;

  private static final long DLL_LOAD_ADDR;

  private static final long DLL_LOOKUP_ADDR;

  private static final int ARRAY_LENGTH_OFFSET;

  private final ByteBuffer machineCode;

  static{
    JVMCIBackend backend = JVMCI.getRuntime().getHostJVMCIBackend();
    metaAccess = backend.getMetaAccess();
    codeCache = backend.getCodeCache();
    HotSpotVMConfigAccess config = new HotSpotVMConfigAccess(HotSpotJVMCIRuntime.runtime()
                                                                                .getConfigStore());

    DLL_LOAD_ADDR = config.getAddress("os::dll_load", null);
    DLL_LOOKUP_ADDR = config.getAddress("os::dll_lookup", null);

    // Offset of arrayOop length should be calculated dynamically.
    // See arrayOopDesc::length_offset_in_bytes() in arrayOop.hpp
    boolean useClassCOOP = config.getFlag("UseCompressedClassPointers", Boolean.class);
    int klassGapOffset = config.getFieldOffset("oopDesc::_metadata._klass", Integer.class, "Klass*");
    int narrowKlassSize = config.getFieldValue("CompilerToVM::Data::sizeof_narrowKlass", Integer.class, "int");
    int arrayOopDescSize = config.getFieldValue("CompilerToVM::Data::sizeof_arrayOopDesc", Integer.class, "int");
    ARRAY_LENGTH_OFFSET = useClassCOOP ? klassGapOffset + narrowKlassSize
                                       : arrayOopDescSize;
  }

  public AsmInjector(){
    machineCode = ByteBuffer.allocate(64).order(ByteOrder.nativeOrder());
  }

  private void emitREXWOp(Register dest){
    byte rexw = (byte)(0b01001000 | (dest.encoding >> 3));
    machineCode.put(rexw);
  }

  private void emitMovRegReg(Register dest, Register src){
    // MOV dest, src (REG-REG)
    emitREXWOp(dest);
    machineCode.put((byte)0x8B); // MOV
    machineCode.put((byte)(                  0b11 << 6  | // REG-REG
                           ((dest.encoding & 0x7) << 3) |
                            (src.encoding & 0x7)));
  }

  private void emitMemOp(byte op, Register dest, Register src, int offset){
    emitREXWOp(dest);
    machineCode.put(op);
    machineCode.put((byte)(                  0b10 << 6  | // REG-MEM
                           ((dest.encoding & 0x7) << 3) |
                            (src.encoding & 0x7)));
    machineCode.putInt(offset);
  }

  private void emitLEA(Register dest, Register src, int offset){
    emitMemOp((byte)0x8D, dest, src, offset);
  }

  private void emitMovMem(Register dest, Register src, int offset){
    emitMemOp((byte)0x8B, dest, src, offset);
  }

  private void emitMovRegImm64(Register dest, long imm){
    emitREXWOp(dest);
    machineCode.put((byte)(0xB8 | (dest.encoding & 0x7))); // MOV r64, imm64
    machineCode.putLong(imm);
  }

  private void emitJmp(long addr){
    // Move callee address to RAX
    emitREXWOp(AMD64.rax);
    machineCode.put((byte)(0xB8 | (AMD64.rax.encoding & 0x7))); // MOV r64(RAX), imm64
    machineCode.putLong(addr);

    // Jump to callee
    machineCode.put((byte)0xFF); // JMP r/m64
    machineCode.put((byte)0b11100000); // FF /4 with RAX
  }

  private InstalledCode install(HotSpotResolvedJavaMethod resolvedMethod){
    byte[] code = Arrays.copyOf(machineCode.array(), machineCode.position());
    HotSpotCompiledCode nmethod = new HotSpotCompiledNmethod(resolvedMethod.getName(),
                                                             code,
                                                             code.length,
                                                             new Site[0],
                                                             null, // assumptions
                                                             null, // methods
                                                             null, // comments
                                                             new byte[0], // data section
                                                             16, // data section alignment
                                                             new DataPatch[0], // data section patches
                                                             true, // isImmutablePIC
                                                             16, // total frame size
                                                             null, // deopt rescue slot
                                                             resolvedMethod,
                                                             -1, // entry BCI
                                                             resolvedMethod.allocateCompileId(0),
                                                             0L, // compile state
                                                             false // has unsafe access
                                                            );
    resolvedMethod.setNotInlinableOrCompilable();
    return codeCache.setDefaultCode(resolvedMethod, nmethod);
  }

  public InstalledCode injectLoadFunc(Method method){
    HotSpotResolvedJavaMethod resolvedMethod =
                 (HotSpotResolvedJavaMethod)metaAccess.lookupJavaMethod(method);
    machineCode.clear();

    // Shuffle arguments - JIT arg to native arg
    emitLEA(AMD64.rdi,
            AMD64.rsi,
            Unsafe.ARRAY_BYTE_BASE_OFFSET); // 1st argument
    emitLEA(AMD64.rsi,
            AMD64.rdx,
            Unsafe.ARRAY_BYTE_BASE_OFFSET); // 2nd argument
    emitMovMem(AMD64.rdx,
               AMD64.rdx,
               ARRAY_LENGTH_OFFSET); // 3rd argument

    // Jump to callee
    emitJmp(DLL_LOAD_ADDR);

    // Create nmethod
    return install(resolvedMethod);
  }

  public InstalledCode injectLookupFunc(Method method){
    HotSpotResolvedJavaMethod resolvedMethod =
                 (HotSpotResolvedJavaMethod)metaAccess.lookupJavaMethod(method);
    machineCode.clear();

    // Shuffle arguments - JIT arg to native arg
    emitMovRegReg(AMD64.rdi, AMD64.rsi); // 1st argument
    emitLEA(AMD64.rsi,
            AMD64.rdx,
            Unsafe.ARRAY_BYTE_BASE_OFFSET); // 2nd argument

    // Jump to callee
    emitJmp(DLL_LOOKUP_ADDR);

    // Create nmethod
    return install(resolvedMethod);
  }

  public InstalledCode injectJmpToVoidMethod(Method method, long addr){
    HotSpotResolvedJavaMethod resolvedMethod =
                 (HotSpotResolvedJavaMethod)metaAccess.lookupJavaMethod(method);
    machineCode.clear();

    // Jump to callee
    emitJmp(addr);

    // Create nmethod
    return install(resolvedMethod);
  }

}
