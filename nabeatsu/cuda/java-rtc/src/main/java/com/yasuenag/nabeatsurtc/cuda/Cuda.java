package com.yasuenag.nabeatsurtc.cuda;

import java.lang.foreign.*;
import java.lang.invoke.*;


public class Cuda{

  private static final SymbolLookup cuda;

  private MethodHandle hndCuInit;

  private MethodHandle hndCuDeviceGet;

  private MethodHandle hndCuCtxCreate;

  private MethodHandle hndCuModuleLoadData;

  private MethodHandle hndCuModuleGetFunction;

  private MethodHandle hndCuMemAlloc;

  private MethodHandle hndCuLaunchKernel;

  private MethodHandle hndCuMemcpyDtoH;

  private MethodHandle hndCuMemFree;

  private MethodHandle hndCuModuleUnload;

  private MethodHandle hndCuCtxDestroy;

  static{
    System.loadLibrary("cuda");
    cuda = SymbolLookup.loaderLookup();
  }

  public Cuda(){
    hndCuInit = null;
    hndCuDeviceGet = null;
    hndCuCtxCreate = null;
    hndCuModuleLoadData = null;
    hndCuModuleGetFunction = null;
    hndCuMemAlloc = null;
    hndCuLaunchKernel = null;
    hndCuMemcpyDtoH = null;
    hndCuMemFree = null;
    hndCuModuleUnload = null;
    hndCuCtxDestroy = null;
  }

  public void cuInit(int flags) throws Throwable{
    if(hndCuInit == null){
      MemorySegment func = cuda.find("cuInit").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
      hndCuInit = Linker.nativeLinker().downcallHandle(func, desc);
    }

    int result = (int)hndCuInit.invoke(flags);
    if(result != 0){ // CUDA_SUCCESS is 0
      throw new RuntimeException("cuInit() returns " + Integer.toString(result));
    }
  }

  public int cuDeviceGet(int ordinal) throws Throwable{
    if(hndCuDeviceGet == null){
      MemorySegment func = cuda.find("cuDeviceGet").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
      hndCuDeviceGet = Linker.nativeLinker().downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      // CUdevice is int
      var cDevice = arena.allocate(ValueLayout.JAVA_INT);
      int result = (int)hndCuDeviceGet.invoke(cDevice, ordinal);
      if(result != 0){ // CUDA_SUCCESS is 0
        throw new RuntimeException("cuDeviceGet() returns " + Integer.toString(result));
      }
      return cDevice.get(ValueLayout.JAVA_INT, 0);
    }
  }

  public long cuCtxCreate(int flags, int device) throws Throwable{
    if(hndCuCtxCreate == null){
      MemorySegment func = cuda.find("cuCtxCreate").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
      hndCuCtxCreate = Linker.nativeLinker().downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      // CUcontext is pointer
      var cCtx = arena.allocate(ValueLayout.ADDRESS);
      int result = (int)hndCuCtxCreate.invoke(cCtx, flags, device);
      if(result != 0){ // CUDA_SUCCESS is 0
        throw new RuntimeException("cuCtxCreate() returns " + Integer.toString(result));
      }
      return cCtx.get(ValueLayout.ADDRESS, 0).address();
    }
  }

  public long cuModuleLoadData(byte[] ptx) throws Throwable{
    if(hndCuModuleLoadData == null){
      MemorySegment func = cuda.find("cuModuleLoadData").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
      hndCuModuleLoadData = Linker.nativeLinker().downcallHandle(func, desc, Linker.Option.critical(true));
    }

    try(var arena = Arena.ofConfined()){
      var module = arena.allocate(ValueLayout.ADDRESS); // CUmodule is a pointer
      var cPtx = MemorySegment.ofArray(ptx);
      int result = (int)hndCuModuleLoadData.invoke(module, cPtx);
      if(result != 0){ // CUDA_SUCCESS is 0
        throw new RuntimeException("cuModuleLoadData() returns " + Integer.toString(result));
      }
      return module.get(ValueLayout.ADDRESS, 0).address();
    }
  }

  public long cuModuleGetFunction(long module, String name) throws Throwable{
    if(hndCuModuleGetFunction == null){
      MemorySegment func = cuda.find("cuModuleGetFunction").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
      hndCuModuleGetFunction = Linker.nativeLinker().downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      var func = arena.allocate(ValueLayout.ADDRESS); // CUfunction is a pointer;
      var cName = arena.allocateFrom(name);
      int result = (int)hndCuModuleGetFunction.invoke(func, module, cName);
      if(result != 0){ // CUDA_SUCCESS is 0
        throw new RuntimeException("cuModuleGetFunction() returns " + Integer.toString(result));
      }
      return func.get(ValueLayout.ADDRESS, 0).address();
    }
  }

  public long cuMemAlloc(long size) throws Throwable{
    if(hndCuMemAlloc == null){
      var linker = Linker.nativeLinker();
      MemorySegment func = cuda.find("cuMemAlloc").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, linker.canonicalLayouts().get("size_t"));
      hndCuMemAlloc = linker.downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      var dptr = arena.allocate(ValueLayout.JAVA_LONG); // CUdeviceptr is an unsigned long
      int result = (int)hndCuMemAlloc.invoke(dptr, size);
      if(result != 0){ // CUDA_SUCCESS is 0
        throw new RuntimeException("cuMemAlloc() returns " + Integer.toString(result));
      }
      return dptr.get(ValueLayout.JAVA_LONG, 0);
    }
  }

  private MemorySegment convertToArgumentArray(SegmentAllocator allocator, Object[] args){
    var dargs = allocator.allocate(ValueLayout.ADDRESS, args.length);
    for(int i = 0; i < args.length; i++){
      MemorySegment mem = switch(args[i]){
        case Byte v -> allocator.allocateFrom(ValueLayout.JAVA_BYTE, v.byteValue());
        case Character v -> allocator.allocateFrom(ValueLayout.JAVA_CHAR, v.charValue());
        case Double v -> allocator.allocateFrom(ValueLayout.JAVA_DOUBLE, v.doubleValue());
        case Float v -> allocator.allocateFrom(ValueLayout.JAVA_FLOAT, v.floatValue());
        case Integer v -> allocator.allocateFrom(ValueLayout.JAVA_INT, v.intValue());
        case Long v -> allocator.allocateFrom(ValueLayout.JAVA_LONG, v.longValue());
        case Short v -> allocator.allocateFrom(ValueLayout.JAVA_SHORT, v.shortValue());
        case null -> MemorySegment.NULL;
        default -> throw new IllegalArgumentException("Unsupported type for kernel argument");
      };
      dargs.setAtIndex(ValueLayout.ADDRESS, i, mem);
    }
    return dargs;
  }

  public void cuLaunchKernel(long func, int gridDimX, int gridDimY, int gridDimZ, int blockDimX, int blockDimY, int blockDimZ, int sharedMemBytes, long stream, Object[] kernelParams, Object[] extra) throws Throwable{ // CUstream is a pointer
    if(hndCuLaunchKernel == null){
      MemorySegment fn = cuda.find("cuLaunchKernel").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, // result
                                                      ValueLayout.JAVA_LONG, // func
                                                      ValueLayout.JAVA_INT, // gridDimX
                                                      ValueLayout.JAVA_INT, // gridDimY
                                                      ValueLayout.JAVA_INT, // gridDimZ
                                                      ValueLayout.JAVA_INT, // blockDimX
                                                      ValueLayout.JAVA_INT, // blockDimY
                                                      ValueLayout.JAVA_INT, // blockDimZ
                                                      ValueLayout.JAVA_INT, // sharedMemBytes
                                                      ValueLayout.JAVA_LONG, // stream
                                                      ValueLayout.ADDRESS, // kernelParams
                                                      ValueLayout.ADDRESS // extra
                                                      );
      hndCuLaunchKernel = Linker.nativeLinker().downcallHandle(fn, desc);
    }

    try(var arena = Arena.ofConfined()){
      MemorySegment cKernelParams = kernelParams == null ? MemorySegment.NULL : convertToArgumentArray(arena, kernelParams);
      MemorySegment cExtra = extra == null ? MemorySegment.NULL : convertToArgumentArray(arena, extra);
      int result = (int)hndCuLaunchKernel.invoke(func, gridDimX, gridDimY, gridDimZ, blockDimX, blockDimY, blockDimZ, sharedMemBytes, stream, cKernelParams, cExtra);
      if(result != 0){ // CUDA_SUCCESS is 0
        throw new RuntimeException("cuLaunchKernel() returns " + Integer.toString(result));
      }
    }
  }

  public Object cuMemcpyDtoH(Object target, long srcDevice, long byteCount) throws Throwable{
    if(hndCuMemcpyDtoH == null){
      var linker = Linker.nativeLinker();
      MemorySegment func = cuda.find("cuMemcpyDtoH").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, linker.canonicalLayouts().get("size_t"));
      hndCuMemcpyDtoH = linker.downcallHandle(func, desc);
    }

    try(var arena = Arena.ofConfined()){
      ValueLayout cType = switch(target){
        case byte[] v -> ValueLayout.JAVA_BYTE;
        case char[] v -> ValueLayout.JAVA_CHAR;
        case double[] v -> ValueLayout.JAVA_DOUBLE;
        case float[] v -> ValueLayout.JAVA_FLOAT;
        case int[] v -> ValueLayout.JAVA_INT;
        case long[] v -> ValueLayout.JAVA_LONG;
        case short[] v -> ValueLayout.JAVA_SHORT;
        default -> throw new IllegalArgumentException("Unsupported type for memcpy");
      };
      MemorySegment cDstHost = arena.allocate(cType, byteCount / cType.byteSize());
      int result = (int)hndCuMemcpyDtoH.invoke(cDstHost, srcDevice, byteCount);
      if(result != 0){ // CUDA_SUCCESS is 0
        throw new RuntimeException("cuMemcpyDtoH() returns " + Integer.toString(result));
      }

      return switch(target){
        case byte[] v -> cDstHost.toArray(ValueLayout.JAVA_BYTE);
        case char[] v -> cDstHost.toArray(ValueLayout.JAVA_CHAR);
        case double[] v -> cDstHost.toArray(ValueLayout.JAVA_DOUBLE);
        case float[] v -> cDstHost.toArray(ValueLayout.JAVA_FLOAT);
        case int[] v -> cDstHost.toArray(ValueLayout.JAVA_INT);
        case long[] v -> cDstHost.toArray(ValueLayout.JAVA_LONG);
        case short[] v -> cDstHost.toArray(ValueLayout.JAVA_SHORT);
        default -> throw new IllegalArgumentException("Should not reach here");
      };
    }
  }

  public void cuMemFree(long dptr) throws Throwable{
    if(hndCuMemFree == null){
      MemorySegment func = cuda.find("cuMemFree").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
      hndCuMemFree = Linker.nativeLinker().downcallHandle(func, desc);
    }

    int result = (int)hndCuMemFree.invoke(dptr);
    if(result != 0){ // CUDA_SUCCESS is 0
      throw new RuntimeException("cuMemFree() returns " + Integer.toString(result));
    }
  }

  public void cuModuleUnload(long module) throws Throwable{
    if(hndCuModuleUnload == null){
      MemorySegment func = cuda.find("cuModuleUnload").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
      hndCuModuleUnload = Linker.nativeLinker().downcallHandle(func, desc);
    }

    int result = (int)hndCuModuleUnload.invoke(module);
    if(result != 0){ // CUDA_SUCCESS is 0
      throw new RuntimeException("cuModuleUnload() returns " + Integer.toString(result));
    }
  }

  public void cuCtxDestroy(long ctx) throws Throwable{
    if(hndCuCtxDestroy == null){
      MemorySegment func = cuda.find("cuCtxDestroy").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
      hndCuCtxDestroy = Linker.nativeLinker().downcallHandle(func, desc);
    }

    int result = (int)hndCuCtxDestroy.invoke(ctx);
    if(result != 0){ // CUDA_SUCCESS is 0
      throw new RuntimeException("cuCtxDestroy() returns " + Integer.toString(result));
    }
  }

}
