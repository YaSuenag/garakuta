package helper;

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

  private final MetaAccessProvider metaAccess;

  private final CodeCacheProvider codeCache;

  private final HotSpotVMConfigAccess config;

  private final ByteBuffer machineCode;

  private final long dllLookupAddr;

  public AsmInjector(){
    JVMCIBackend backend = JVMCI.getRuntime().getHostJVMCIBackend();
    metaAccess = backend.getMetaAccess();
    codeCache = backend.getCodeCache();
    config = new HotSpotVMConfigAccess(HotSpotJVMCIRuntime.runtime()
                                                          .getConfigStore());
    machineCode = ByteBuffer.allocate(32).order(ByteOrder.nativeOrder());
    dllLookupAddr = config.getAddress("os::dll_lookup", null);
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

  private void emitLEA(Register dest, Register src, int offset){
    // LEA
    emitREXWOp(dest);
    machineCode.put((byte)0x8D); // LEA
    machineCode.put((byte)(                  0b10 << 6  | // REG-MEM
                           ((dest.encoding & 0x7) << 3) |
                            (src.encoding & 0x7)));
    machineCode.putInt(offset);
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
                                                             new Assumptions.Assumption[0],
                                                             new ResolvedJavaMethod[]{resolvedMethod},
                                                             new HotSpotCompiledCode.Comment[0],
                                                             new byte[0],
                                                             16,
                                                             new DataPatch[0],
                                                             false,
                                                             16,
                                                             null,
                                                             resolvedMethod,
                                                             -1,
                                                             resolvedMethod.allocateCompileId(0),
                                                             0L,
                                                             false);
    resolvedMethod.setNotInlinableOrCompilable();
    return codeCache.setDefaultCode(resolvedMethod, nmethod);
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
    emitJmp(dllLookupAddr);

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
