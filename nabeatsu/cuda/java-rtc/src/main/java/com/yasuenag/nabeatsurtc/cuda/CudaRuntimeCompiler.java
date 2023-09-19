package com.yasuenag.nabeatsurtc.cuda;

import java.lang.foreign.*;
import java.lang.invoke.*;


public class CudaRuntimeCompiler{

  private static final SymbolLookup nvrtc;

  private MethodHandle hndNvrtcCreateProgram;

  private MethodHandle hndNvrtcCompileProgram;

  private MethodHandle hndNvrtcGetPTXSize;

  private MethodHandle hndNvrtcGetPTX;

  private MethodHandle hndNvrtcDestroyProgram;

  static{
    System.loadLibrary("nvrtc");
    nvrtc = SymbolLookup.loaderLookup();
  }

  public CudaRuntimeCompiler(){
    hndNvrtcCreateProgram = null;
    hndNvrtcCompileProgram = null;
    hndNvrtcGetPTXSize = null;
    hndNvrtcGetPTX = null;
    hndNvrtcDestroyProgram = null;
  }

  public long nvrtcCreateProgram(String src, String name, String[] headers, String[] includeNames) throws Throwable{
    if(hndNvrtcCreateProgram == null){
      MemorySegment func = nvrtc.find("nvrtcCreateProgram").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
      hndNvrtcCreateProgram = Linker.nativeLinker().downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      var prog = arena.allocate(ValueLayout.ADDRESS); // nvrtcProgram is a pointer
      var cSrc = arena.allocateUtf8String(src);
      MemorySegment cName = name == null ? MemorySegment.NULL : arena.allocateUtf8String(name);
      int numHeaders = headers == null ? 0 : headers.length;
      MemorySegment cHeaders = MemorySegment.NULL;
      MemorySegment cIncludeNames = MemorySegment.NULL;
      if(numHeaders > 0){
        cHeaders = arena.allocateArray(ValueLayout.ADDRESS, numHeaders);
        cIncludeNames = arena.allocateArray(ValueLayout.ADDRESS, numHeaders);
        for(int i = 0; i < numHeaders; i++){
          ((MemorySegment)cHeaders).setAtIndex(ValueLayout.ADDRESS, i, arena.allocateUtf8String(headers[i]));
          ((MemorySegment)cIncludeNames).setAtIndex(ValueLayout.ADDRESS, i, arena.allocateUtf8String(includeNames[i]));
        }
      }

      int result = (int)hndNvrtcCreateProgram.invoke(prog, cSrc, cName, numHeaders, cHeaders, cIncludeNames);
      if(result != 0){ // NVRTC_SUCCESS is 0
        throw new RuntimeException("nvrtcCreateProgram() returns " + Integer.toString(result));
      }

      return prog.get(ValueLayout.ADDRESS, 0).address();
    }
  }

  public void nvrtcCompileProgram(long prog, String[] options) throws Throwable{
    if(hndNvrtcCompileProgram == null){
      MemorySegment func = nvrtc.find("nvrtcCompileProgram").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
      hndNvrtcCompileProgram = Linker.nativeLinker().downcallHandle(func, desc);
    }

    int numOptions = 0;
    try(var arena = Arena.ofConfined()){
      MemorySegment cOptions = MemorySegment.NULL;
      if((options != null) && (options.length > 0)){
        numOptions = options.length;
        cOptions = arena.allocateArray(ValueLayout.ADDRESS, numOptions);
        for(int i = 0; i < numOptions; i++){
          ((MemorySegment)cOptions).setAtIndex(ValueLayout.ADDRESS, i, arena.allocateUtf8String(options[i]));
        }
      }

      int result = (int)hndNvrtcCompileProgram.invoke(prog, numOptions, cOptions);
      if(result != 0){ // NVRTC_SUCCESS is 0
        throw new RuntimeException("nvrtcCompileProgram() returns " + Integer.toString(result));
      }
    }
  }

  public long nvrtcGetPTXSize(long prog) throws Throwable{
    if(hndNvrtcGetPTXSize == null){
      MemorySegment func = nvrtc.find("nvrtcGetPTXSize").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
      hndNvrtcGetPTXSize = Linker.nativeLinker().downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      var cSize = arena.allocate(ValueLayout.JAVA_LONG); // size_t
      int result = (int)hndNvrtcGetPTXSize.invoke(prog, cSize);
      if(result != 0){ // NVRTC_SUCCESS is 0
        throw new RuntimeException("nvrtcGetPTXSize() returns " + Integer.toString(result));
      }
      return cSize.get(ValueLayout.JAVA_LONG, 0);
    }
  }

  public byte[] nvrtcGetPTX(long prog) throws Throwable{
    if(hndNvrtcGetPTX == null){
      MemorySegment func = nvrtc.find("nvrtcGetPTX").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
      hndNvrtcGetPTX = Linker.nativeLinker().downcallHandle(func, desc);
    }

    long size = nvrtcGetPTXSize(prog);
    try(var arena = Arena.ofConfined()){
      var mem = arena.allocate(size);
      int result = (int)hndNvrtcGetPTX.invoke(prog, mem);
      if(result != 0){ // NVRTC_SUCCESS is 0
        throw new RuntimeException("nvrtcGetPTX() returns " + Integer.toString(result));
      }
      return mem.toArray(ValueLayout.JAVA_BYTE);
    }
  }

  public void nvrtcDestroyProgram(long prog) throws Throwable{
    if(hndNvrtcDestroyProgram == null){
      MemorySegment func = nvrtc.find("nvrtcDestroyProgram").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);
      hndNvrtcDestroyProgram = Linker.nativeLinker().downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      var cProg = arena.allocate(ValueLayout.JAVA_LONG, prog);
      int result = (int)hndNvrtcDestroyProgram.invoke(cProg);
      if(result != 0){ // NVRTC_SUCCESS is 0
        throw new RuntimeException("nvrtcDestroyProgram() returns " + Integer.toString(result));
      }
    }
  }

}
