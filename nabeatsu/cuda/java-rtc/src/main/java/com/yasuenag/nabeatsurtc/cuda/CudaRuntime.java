package com.yasuenag.nabeatsurtc.cuda;

import java.lang.foreign.*;
import java.lang.invoke.*;


public class CudaRuntime{

  private static final SymbolLookup cudart;

  private MethodHandle hndCudaGetDeviceProperties;

  static{
    System.loadLibrary("cudart");
    cudart = SymbolLookup.loaderLookup();
  }

  public CudaRuntime(){
    hndCudaGetDeviceProperties = null;
  }

  public void cudaGetDeviceProperties(CudaDeviceProp prop, int device) throws Throwable{
    if(hndCudaGetDeviceProperties == null){
      MemorySegment func = cudart.find("cudaGetDeviceProperties").get();
      FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT);
      hndCudaGetDeviceProperties = Linker.nativeLinker().downcallHandle(func, desc);
    }

    int result = (int)hndCudaGetDeviceProperties.invoke(prop.getMem(), device);
    if(result != 0){ // cudaSuccess is 0
      throw new RuntimeException("cudaGetDeviceProperties() returns " + Integer.toString(result));
    }
  }

}
