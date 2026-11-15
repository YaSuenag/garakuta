package com.yasuenag.presentation.jjugccc2026;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import sun.misc.Unsafe;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.CodeSegment;
import com.yasuenag.ffmasm.amd64.AVXAsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;
import com.yasuenag.ffmasmtools.jvmci.amd64.JVMCIAVXAsmBuilder;

import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Fork(value = 1,
      jvmArgsAppend = {
        "-XX:+IgnoreUnrecognizedVMOptions", // for JDK 27 or later to ignore JVMCI options
        "--enable-native-access=ALL-UNNAMED",
        "-Xms4g",
        "-Xmx4g",
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+EnableJVMCI",
        "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
        "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
        "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
        "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
        "--add-exports=jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED"
      })
@Warmup(iterations = 1, time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
public class Test{

  private static final int VECTOR_SIZE = 8192;

  private static final MethodHandle ffmMethod;

  public static native boolean checkAVXVNNI();

  private static void initJVMCI() throws Exception{
    var builder = new JVMCIAVXAsmBuilder();

    builder.emitPrologue()
    // Set the first element to each args because JIT'ed func would receive oop.
    // Not an array like FFM.
    // We should fetch arguments from the second because the first is
    // "this" object like C++.
/* add <ofs>, arg1                    */ .add(Register.RDX, Unsafe.ARRAY_BYTE_BASE_OFFSET, OptionalInt.empty())
/* add <ofs>, arg2                    */ .add(Register.RCX, Unsafe.ARRAY_BYTE_BASE_OFFSET, OptionalInt.empty())
/* mov     arg1, %r8                  */ .movMR(Register.RDX, Register.R8, OptionalInt.empty())
    // Array length would be stored as int just before the first element.
/* mov -4(arg1), %eax                 */ .movRM(Register.EAX, Register.RDX, OptionalInt.of(-4))
/* add     %rax, %r8                  */ .addRM(Register.R8, Register.RAX, OptionalInt.empty())
/* vpxor  %ymm0, %ymm0, %ymm0         */ .vpxor(Register.YMM0, Register.YMM0, Register.YMM0, OptionalInt.empty())
/* vpxor  %ymm1, %ymm1, %ymm1         */ .vpxor(Register.YMM1, Register.YMM1, Register.YMM1, OptionalInt.empty())
/* vpxor  %ymm2, %ymm2, %ymm2         */ .vpxor(Register.YMM2, Register.YMM2, Register.YMM2, OptionalInt.empty())
/* vpxor  %ymm3, %ymm3, %ymm3         */ .vpxor(Register.YMM3, Register.YMM3, Register.YMM3, OptionalInt.empty())
/* vpxor  %ymm4, %ymm4, %ymm4         */ .vpxor(Register.YMM4, Register.YMM4, Register.YMM4, OptionalInt.empty())
/* vpxor  %ymm5, %ymm5, %ymm5         */ .vpxor(Register.YMM5, Register.YMM5, Register.YMM5, OptionalInt.empty())
/* vpxor  %ymm6, %ymm6, %ymm6         */ .vpxor(Register.YMM6, Register.YMM6, Register.YMM6, OptionalInt.empty())
/* vpxor  %ymm7, %ymm7, %ymm7         */ .vpxor(Register.YMM7, Register.YMM7, Register.YMM7, OptionalInt.empty())

/* .align 16                          */ .alignTo16BytesWithNOP()
/* head:                              */ .label("loop")
/*   vmovdqu (arg1), %ymm8            */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(0))
/*   vpdpbusd (arg2), %ymm8, %ymm0    */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM0, OptionalInt.of(0))
/*   vmovdqu 32(arg1), %ymm8          */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(32))
/*   vpdpbusd 32(arg2), %ymm8, %ymm1  */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM1, OptionalInt.of(32))
/*   vmovdqu 64(arg1), %ymm8          */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(64))
/*   vpdpbusd 64(arg2), %ymm8, %ymm2  */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM2, OptionalInt.of(64))
/*   vmovdqu 96(arg1), %ymm8          */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(96))
/*   vpdpbusd 96(arg2), %ymm8, %ymm3  */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM3, OptionalInt.of(96))
/*   vmovdqu 128(arg1), %ymm8         */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(128))
/*   vpdpbusd 128(arg2), %ymm8, %ymm4 */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM4, OptionalInt.of(128))
/*   vmovdqu 160(arg1), %ymm8         */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(160))
/*   vpdpbusd 160(arg2), %ymm8, %ymm5 */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM5, OptionalInt.of(160))
/*   vmovdqu 192(arg1), %ymm8         */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(192))
/*   vpdpbusd 192(arg2), %ymm8, %ymm6 */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM6, OptionalInt.of(192))
/*   vmovdqu 224(arg1), %ymm8         */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(224))
/*   vpdpbusd 224(arg2), %ymm8, %ymm7 */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM7, OptionalInt.of(224))
/*   add       $256, arg1             */ .add(Register.RDX, 256, OptionalInt.empty())
/*   add       $256, arg2             */ .add(Register.RCX, 256, OptionalInt.empty())
/*   cmp       arg1, %r8              */ .cmpRM(Register.R8, Register.RDX, OptionalInt.empty())
/*   jne       head                   */ .jne("loop")

/* vpaddd      %ymm1, %ymm0, %ymm0    */ .vpaddd(Register.YMM0, Register.YMM1, Register.YMM0, OptionalInt.empty())
/* vpaddd      %ymm3, %ymm2, %ymm2    */ .vpaddd(Register.YMM2, Register.YMM3, Register.YMM2, OptionalInt.empty())
/* vpaddd      %ymm5, %ymm4, %ymm4    */ .vpaddd(Register.YMM4, Register.YMM5, Register.YMM4, OptionalInt.empty())
/* vpaddd      %ymm7, %ymm6, %ymm6    */ .vpaddd(Register.YMM6, Register.YMM7, Register.YMM6, OptionalInt.empty())
/* vpaddd      %ymm2, %ymm0, %ymm0    */ .vpaddd(Register.YMM0, Register.YMM2, Register.YMM0, OptionalInt.empty())
/* vpaddd      %ymm6, %ymm4, %ymm4    */ .vpaddd(Register.YMM4, Register.YMM6, Register.YMM4, OptionalInt.empty())
/* vpaddd      %ymm4, %ymm0, %ymm0    */ .vpaddd(Register.YMM0, Register.YMM4, Register.YMM0, OptionalInt.empty())

/* vextracti128 $0x1, %ymm0, %xmm1    */ .vextracti128(Register.YMM0, Register.XMM1, OptionalInt.empty(), (byte)0x1)
/* vpaddd      %xmm1, %xmm0, %xmm0    */ .vpaddd(Register.XMM0, Register.XMM1, Register.XMM0, OptionalInt.empty())
/* vpshufd     $0x4e, %xmm0, %xmm1    */ .vpshufd(Register.XMM1, Register.XMM0, OptionalInt.empty(), (byte)0x4e)
/* vpaddd      %xmm1, %xmm0, %xmm0    */ .vpaddd(Register.XMM0, Register.XMM1, Register.XMM0, OptionalInt.empty())
/* vpshufd     $0xb1, %xmm0, %xmm1    */ .vpshufd(Register.XMM1, Register.XMM0, OptionalInt.empty(), (byte)0xb1)
/* vpaddd      %xmm1, %xmm0, %xmm0    */ .vpaddd(Register.XMM0, Register.XMM1, Register.XMM0, OptionalInt.empty())
/* vmovd       %xmm0, %eax            */ .vmovdB(Register.XMM0, Register.EAX, OptionalInt.empty())
                                         .emitEpilogue();

    var targetMethod = Test.class.getDeclaredMethod("jvmciImpl", byte[].class, byte[].class);
    builder.install(targetMethod, 16);
  }

  private static void initJVMCI4way() throws Exception{
    var builder = new JVMCIAVXAsmBuilder();

    builder.emitPrologue()
    // Set the first element to each args because JIT'ed func would receive oop.
    // Not an array like FFM.
    // We should fetch arguments from the second because the first is
    // "this" object like C++.
/* add <ofs>, arg1                    */ .add(Register.RDX, Unsafe.ARRAY_BYTE_BASE_OFFSET, OptionalInt.empty())
/* add <ofs>, arg2                    */ .add(Register.RCX, Unsafe.ARRAY_BYTE_BASE_OFFSET, OptionalInt.empty())
/* mov     arg1, %r8                  */ .movMR(Register.RDX, Register.R8, OptionalInt.empty())
    // Array length would be stored as int just before the first element.
/* mov -4(arg1), %eax                 */ .movRM(Register.EAX, Register.RDX, OptionalInt.of(-4))
/* add     %rax, %r8                  */ .addRM(Register.R8, Register.RAX, OptionalInt.empty())
/* vpxor  %ymm0, %ymm0, %ymm0         */ .vpxor(Register.YMM0, Register.YMM0, Register.YMM0, OptionalInt.empty())
/* vpxor  %ymm1, %ymm1, %ymm1         */ .vpxor(Register.YMM1, Register.YMM1, Register.YMM1, OptionalInt.empty())
/* vpxor  %ymm2, %ymm2, %ymm2         */ .vpxor(Register.YMM2, Register.YMM2, Register.YMM2, OptionalInt.empty())
/* vpxor  %ymm3, %ymm3, %ymm3         */ .vpxor(Register.YMM3, Register.YMM3, Register.YMM3, OptionalInt.empty())

/* .align 16                          */ .alignTo16BytesWithNOP()
/* head:                              */ .label("loop")
/*   vmovdqu (arg1), %ymm8            */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(0))
/*   vpdpbusd (arg2), %ymm8, %ymm0    */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM0, OptionalInt.of(0))
/*   vmovdqu 32(arg1), %ymm8          */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(32))
/*   vpdpbusd 32(arg2), %ymm8, %ymm1  */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM1, OptionalInt.of(32))
/*   vmovdqu 64(arg1), %ymm8          */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(64))
/*   vpdpbusd 64(arg2), %ymm8, %ymm2  */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM2, OptionalInt.of(64))
/*   vmovdqu 96(arg1), %ymm8          */ .vmovdquRM(Register.YMM8, Register.RDX, OptionalInt.of(96))
/*   vpdpbusd 96(arg2), %ymm8, %ymm3  */ .vpdpbusd(Register.YMM8, Register.RCX, Register.YMM3, OptionalInt.of(96))
/*   add       $128, arg1             */ .add(Register.RDX, 128, OptionalInt.empty())
/*   add       $128, arg2             */ .add(Register.RCX, 128, OptionalInt.empty())
/*   cmp       arg1, %r8              */ .cmpRM(Register.R8, Register.RDX, OptionalInt.empty())
/*   jne       head                   */ .jne("loop")

/* vpaddd      %ymm1, %ymm0, %ymm0    */ .vpaddd(Register.YMM0, Register.YMM1, Register.YMM0, OptionalInt.empty())
/* vpaddd      %ymm3, %ymm2, %ymm2    */ .vpaddd(Register.YMM2, Register.YMM3, Register.YMM2, OptionalInt.empty())
/* vpaddd      %ymm2, %ymm0, %ymm0    */ .vpaddd(Register.YMM0, Register.YMM2, Register.YMM0, OptionalInt.empty())

/* vextracti128 $0x1, %ymm0, %xmm1    */ .vextracti128(Register.YMM0, Register.XMM1, OptionalInt.empty(), (byte)0x1)
/* vpaddd      %xmm1, %xmm0, %xmm0    */ .vpaddd(Register.XMM0, Register.XMM1, Register.XMM0, OptionalInt.empty())
/* vpshufd     $0x4e, %xmm0, %xmm1    */ .vpshufd(Register.XMM1, Register.XMM0, OptionalInt.empty(), (byte)0x4e)
/* vpaddd      %xmm1, %xmm0, %xmm0    */ .vpaddd(Register.XMM0, Register.XMM1, Register.XMM0, OptionalInt.empty())
/* vpshufd     $0xb1, %xmm0, %xmm1    */ .vpshufd(Register.XMM1, Register.XMM0, OptionalInt.empty(), (byte)0xb1)
/* vpaddd      %xmm1, %xmm0, %xmm0    */ .vpaddd(Register.XMM0, Register.XMM1, Register.XMM0, OptionalInt.empty())
/* vmovd       %xmm0, %eax            */ .vmovdB(Register.XMM0, Register.EAX, OptionalInt.empty())
                                         .emitEpilogue();

    var targetMethod = Test.class.getDeclaredMethod("jvmci4wayImpl", byte[].class, byte[].class);
    builder.install(targetMethod, 16);
  }

  private static MethodHandle initFFM() throws Exception{
    var funcAddr = SymbolLookup.loaderLookup().findOrThrow("cpp_impl");
    var desc = FunctionDescriptor.of(ValueLayout.JAVA_INT,
                                     ValueLayout.ADDRESS,
                                     ValueLayout.ADDRESS,
                                     ValueLayout.JAVA_INT);
    return Linker.nativeLinker().downcallHandle(funcAddr, desc, Linker.Option.critical(true));
  }

  static{
    // Check OS & CPU arch
    if(!System.getProperty("os.name").equals("Linux")){
      throw new RuntimeException("This program can run on Linux only!");
    }
    if(!System.getProperty("os.arch").equals("amd64")){
      throw new RuntimeException("This program can run on AMD64 only!");
    }

    // Load native library
    try(var in = Test.class.getResourceAsStream("/libnative.so")){
      var lib = Files.createTempFile("libnative-", ".so");
      lib.toFile().deleteOnExit();
      Files.copy(in, lib, StandardCopyOption.REPLACE_EXISTING);
      System.load(lib.toString());
    }
    catch(IOException e){
      throw new UncheckedIOException(e);
    }

    // Check to support AVX VNNI
    if(!checkAVXVNNI()){
      throw new RuntimeException("CPU does not support AVX VNNI!");
    }

    // Initialize
    try{
      if(Runtime.version().feature() < 27){
        initJVMCI();
        initJVMCI4way();
      }
      ffmMethod = initFFM();
    }
    catch(Exception e){
      throw new RuntimeException(e);
    }
  }

  @State(Scope.Thread)
  public static class VectorHolder{

    public byte[] src1;
    public byte[] src2;

    @Setup(Level.Invocation)
    public void setup(){
      src1 = new byte[VECTOR_SIZE];
      src2 = new byte[VECTOR_SIZE];

      var rand = new Random();
      rand.nextBytes(src1);
      rand.nextBytes(src2);
    }

  }

  @Benchmark
  public int java(VectorHolder holder){
    return javaImpl(holder.src1, holder.src2);
  }

  @Benchmark
  public int jni(VectorHolder holder){
    return jniImpl(holder.src1, holder.src2);
  }

  @Benchmark
  public int jvmci(VectorHolder holder){
    return jvmciImpl(holder.src1, holder.src2);
  }

  @Benchmark
  public int jvmci4way(VectorHolder holder){
    return jvmci4wayImpl(holder.src1, holder.src2);
  }

  @Benchmark
  public int ffm(VectorHolder holder){
    return ffmImpl(holder.src1, holder.src2);
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  private int javaImpl(byte[] src1, byte[] src2){
    int dotProduct = 0;
    for(int i = 0; i < src1.length; i++){
      dotProduct += Byte.toUnsignedInt(src1[i]) * src2[i];
    }
    return dotProduct;
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  private native int jniImpl(byte[] src1, byte[] src2);

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  private int jvmciImpl(byte[] src1, byte[] src2){
    throw new IllegalStateException("This method should be installed by JVMCI");
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  private int jvmci4wayImpl(byte[] src1, byte[] src2){
    throw new IllegalStateException("This method should be installed by JVMCI");
  }

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  private int ffmImpl(byte[] src1, byte[] src2){
    try{
      var src1Seg = MemorySegment.ofArray(src1);
      var src2Seg = MemorySegment.ofArray(src2);
      return (int)ffmMethod.invokeExact(src1Seg, src2Seg, src1.length);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public static void main(String[] args){
    var test = new Test();
    var vec = new VectorHolder();
    vec.setup();

    IO.println("Pure Java = " + test.java(vec));
    IO.println("      JNI = " + test.jni(vec));
    IO.println("      FFM = " + test.ffm(vec));
    if(Runtime.version().feature() < 27){
      IO.println("    JVMCI = " + test.jvmci(vec));
      IO.println("JVMCI4way = " + test.jvmci4way(vec));
    }
  }
}
