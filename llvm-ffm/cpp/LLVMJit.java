import java.lang.foreign.*;
import java.lang.invoke.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;


public class LLVMJit implements AutoCloseable{

  private long ptrJIT;

  private final Map<String, MethodHandle> functions;

  private static native void init0();

  static{
    System.loadLibrary("llvm-jit");
    init0();
  }

  private native void compile0(String src, int optoLevel);

  private native long getFunctionPointer0(String name);

  private native void dispose0();

  public LLVMJit(){
    ptrJIT = 0L;
    functions = new ConcurrentHashMap<>();
  }

  public void compile(String src, int optoLevel){
    if(ptrJIT != 0L){
      throw new IllegalStateException("Already compiled.");
    }

    compile0(src, optoLevel);
  }

  private MethodHandle createMethodHandle(String name, FunctionDescriptor desc){
    long ptr = getFunctionPointer0(name);
    return Linker.nativeLinker()
                 .downcallHandle(MemorySegment.ofAddress(ptr), desc);
  }

  public MethodHandle getFunction(String name, FunctionDescriptor desc){
    if(ptrJIT == 0L){
      throw new IllegalStateException("Not yet compiled.");
    }

    return functions.computeIfAbsent(name, k -> createMethodHandle(k, desc));
  }

  @Override
  public void close() throws IOException{
    dispose0();
  }

}
