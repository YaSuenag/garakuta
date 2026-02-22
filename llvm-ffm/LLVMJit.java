import java.io.*;
import java.lang.foreign.*;
import java.lang.invoke.*;


public class LLVMJit implements AutoCloseable{

  private static final MethodHandle free;

  private static final MethodHandle LLVMInitializeX86TargetInfo;
  private static final MethodHandle LLVMInitializeX86Target;
  private static final MethodHandle LLVMInitializeX86TargetMC;
  private static final MethodHandle LLVMInitializeX86AsmPrinter;
  private static final MethodHandle LLVMInitializeX86AsmParser;
  private static final MethodHandle LLVMCreateMemoryBufferWithMemoryRangeCopy;
  private static final MethodHandle LLVMParseBitcode2;
  private static final MethodHandle LLVMCreateJITCompilerForModule;
  private static final MethodHandle LLVMGetFunctionAddress;
  private static final MethodHandle LLVMDisposeExecutionEngine;
  private static final MethodHandle LLVMDisposeMemoryBuffer;

  private MemorySegment engine;

  static{
    var linker = Linker.nativeLinker();

    free = linker.downcallHandle(
      linker.defaultLookup().findOrThrow("free"),
      FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    System.loadLibrary("LLVM");
    var lookup = SymbolLookup.loaderLookup();

    LLVMInitializeX86TargetInfo = linker.downcallHandle(
      lookup.findOrThrow("LLVMInitializeX86TargetInfo"),
      FunctionDescriptor.ofVoid()
    );
    LLVMInitializeX86Target = linker.downcallHandle(
      lookup.findOrThrow("LLVMInitializeX86Target"),
      FunctionDescriptor.ofVoid()
    );
    LLVMInitializeX86TargetMC = linker.downcallHandle(
      lookup.findOrThrow("LLVMInitializeX86TargetMC"),
      FunctionDescriptor.ofVoid()
    );
    LLVMInitializeX86AsmPrinter = linker.downcallHandle(
      lookup.findOrThrow("LLVMInitializeX86AsmPrinter"),
      FunctionDescriptor.ofVoid()
    );
    LLVMInitializeX86AsmParser = linker.downcallHandle(
      lookup.findOrThrow("LLVMInitializeX86AsmParser"),
      FunctionDescriptor.ofVoid()
    );
    LLVMCreateMemoryBufferWithMemoryRangeCopy = linker.downcallHandle(
      lookup.findOrThrow("LLVMCreateMemoryBufferWithMemoryRangeCopy"),
      FunctionDescriptor.of(
        ValueLayout.ADDRESS,                     // result
        ValueLayout.ADDRESS,                     // InputData
        linker.canonicalLayouts().get("size_t"), // InputDataLength
        ValueLayout.ADDRESS                      // BufferName
      ),
      Linker.Option.critical(true)
    );
    LLVMParseBitcode2 = linker.downcallHandle(
      lookup.findOrThrow("LLVMParseBitcode2"),
      FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // result
        ValueLayout.ADDRESS,  // MemBuf
        ValueLayout.ADDRESS   // OutModule
      )
    );
    LLVMCreateJITCompilerForModule = linker.downcallHandle(
      lookup.findOrThrow("LLVMCreateJITCompilerForModule"),
      FunctionDescriptor.of(
        ValueLayout.JAVA_INT, // result
        ValueLayout.ADDRESS,  // OutJIT
        ValueLayout.ADDRESS,  // M
        ValueLayout.JAVA_INT, // OptLevel
        ValueLayout.ADDRESS   // OutError
      )
    );
    LLVMGetFunctionAddress = linker.downcallHandle(
      lookup.findOrThrow("LLVMGetFunctionAddress"),
      FunctionDescriptor.of(
        ValueLayout.ADDRESS,  // result
        ValueLayout.ADDRESS,  // EE
        ValueLayout.ADDRESS   // Name
      )
    );
    LLVMDisposeExecutionEngine = linker.downcallHandle(
      lookup.findOrThrow("LLVMDisposeExecutionEngine"),
      FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
    LLVMDisposeMemoryBuffer = linker.downcallHandle(
      lookup.findOrThrow("LLVMDisposeMemoryBuffer"),
      FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
  }

  /* from llvm-c/Target.h
   * Assumes LLVM_NATIVE_TARGET is defined to "LLVMInitializeX86Target".
   */
  private void llvmInitializeNativeTarget(){
    try{
      LLVMInitializeX86TargetInfo.invoke();
      LLVMInitializeX86Target.invoke();
      LLVMInitializeX86TargetMC.invoke();
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  /* from llvm-c/Target.h
   * Assumes LLVM_NATIVE_ASMPRINTER is defined to "LLVMInitializeX86AsmPrinter".
   */
  private void llvmInitializeNativeAsmPrinter(){
    try{
      LLVMInitializeX86AsmPrinter.invoke();
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  /* from llvm-c/Target.h
   * Assumes LLVM_NATIVE_ASMPARSER is defined to "LLVMInitializeX86AsmParser".
   */
  private void llvmInitializeNativeAsmParser(){
    try{
      LLVMInitializeX86AsmParser.invoke();
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public LLVMJit(){
    llvmInitializeNativeTarget();
    llvmInitializeNativeAsmPrinter();
    llvmInitializeNativeAsmParser();

    engine = null;
  }

  public byte[] generateBitCode(String src) throws IOException, InterruptedException{
    var clangProc = new ProcessBuilder("clang", "-x", "c", "-emit-llvm", "-c", "-o", "-", "-")
                          .redirectErrorStream(true)
                          .start();

    try(var writer = clangProc.outputWriter()){
      writer.write(src);
    }
    clangProc.waitFor();
    return clangProc.getInputStream().readAllBytes();
  }

  public void compile(byte[] bitcode, String moduleName, int optoLevel){
    if(engine != null){
      throw new IllegalStateException("This instance is already used.");
    }

    try(var arena = Arena.ofConfined()){
      /* Create module */
      MemorySegment namePtr = arena.allocateFrom(moduleName);
      MemorySegment memBufRef = (MemorySegment)LLVMCreateMemoryBufferWithMemoryRangeCopy.invoke(
        MemorySegment.ofArray(bitcode), // InputData
        bitcode.length,                 // InputDataLength
        namePtr                         // BufferName
      );
      MemorySegment moduleRef = arena.allocate(ValueLayout.ADDRESS);
      LLVMParseBitcode2.invoke(memBufRef, moduleRef);
      LLVMDisposeMemoryBuffer.invoke(memBufRef);
      MemorySegment module = moduleRef.get(ValueLayout.ADDRESS, 0);

      /* Create engine */
      MemorySegment executionEngineRef = arena.allocate(ValueLayout.ADDRESS);
      MemorySegment ptrErrorMsg = arena.allocate(ValueLayout.ADDRESS);
      int jitResult = (int)LLVMCreateJITCompilerForModule.invoke(
        executionEngineRef, // OutJIT
        module,             // M
        optoLevel,          // OptLevel: See "enum CodeGenOptLevel" in llvm/Support/CodeGen.h
        ptrErrorMsg         // OutError
      );
      if(jitResult != 0){
        MemorySegment errorMsg = ptrErrorMsg.get(ValueLayout.ADDRESS, 0);
        String msg = errorMsg.getString(0);
        free.invoke(errorMsg);
        throw new RuntimeException("LLVMCreateJITCompilerForModule: " + msg);
      }
      engine = executionEngineRef.get(ValueLayout.ADDRESS, 0);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  public MethodHandle getFunction(String name, FunctionDescriptor desc){
    try(var arena = Arena.ofConfined()){
      MemorySegment namePtr = arena.allocateFrom(name);
      MemorySegment funcPtr = (MemorySegment)LLVMGetFunctionAddress.invoke(
        engine,  // EE
        namePtr  // Name
      );
      return Linker.nativeLinker().downcallHandle(funcPtr, desc);
    }
    catch(Throwable t){
      throw new RuntimeException(t);
    }
  }

  @Override
  public void close() throws IOException{
    if(engine != null){
      try{
        LLVMDisposeExecutionEngine.invoke(engine);
      }
      catch(Throwable t){
        throw new RuntimeException(t);
      }
    }
  }

}
