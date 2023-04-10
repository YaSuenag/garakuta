package com.yasuenag.nabeatsurtc.cuda;

import java.lang.foreign.*;
import java.lang.invoke.*;

// from $CUDA_HOME/include/driver_types.h
// this struct seems to be aligned to 4-bytes.
public class CudaDeviceProp{

  public static class CudaUUID{

    private final GroupLayout layout;

    public CudaUUID(){
      layout = MemoryLayout.structLayout(
                 MemoryLayout.sequenceLayout(16, ValueLayout.JAVA_BYTE)
                             .withName("bytes")
               );
    }

    public GroupLayout getLayout(){
      return layout;
    }

  }

  private final GroupLayout layout;

  private final MemorySegment mem;

  private VarHandle hndTotalGlobalMem;

  private VarHandle hndSharedMemPerBlock;

  private VarHandle hndWrapSize;

  private VarHandle hndMaxThreadsPerBlock;

  private VarHandle hndMajor;

  private VarHandle hndMinor;

  private VarHandle hndClockRate;

  private VarHandle hndMultiProcessorCount;

  public CudaDeviceProp(){
    layout = MemoryLayout.structLayout(
               MemoryLayout.sequenceLayout(256, ValueLayout.JAVA_BYTE).withName("name"),
               (new CudaUUID()).getLayout().withName("uuid"),
               MemoryLayout.sequenceLayout(8, ValueLayout.JAVA_BYTE).withName("luid"),
               ValueLayout.JAVA_INT.withName("luidDeviceNodeMask"), // unsigned
               MemoryLayout.paddingLayout(32),
               ValueLayout.JAVA_LONG.withName("totalGlobalMem"), // unsigned (size_t)
               ValueLayout.JAVA_LONG.withName("sharedMemPerBlock"), // unsigned (size_t)
               ValueLayout.JAVA_INT.withName("regPerBlock"),
               ValueLayout.JAVA_INT.withName("wrapSize"),
               ValueLayout.JAVA_LONG.withName("memPitch"), // unsigned (size_t)
               ValueLayout.JAVA_INT.withName("maxThreadsPerBlock"),
               MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("maxThreadsDim"),
               MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("maxGridSize"),
               ValueLayout.JAVA_INT.withName("clockRate"),
               ValueLayout.JAVA_LONG.withName("totalConstMem"), // unsigned (size_t)
               ValueLayout.JAVA_INT.withName("major"),
               ValueLayout.JAVA_INT.withName("minor"),
               ValueLayout.JAVA_LONG.withName("textureAlignment"), // unsigned (size_t)
               ValueLayout.JAVA_LONG.withName("texturePitchAlignment"), // unsigned (size_t)
               ValueLayout.JAVA_INT.withName("deviceOverlap"),
               ValueLayout.JAVA_INT.withName("multiProcessorCount"),
               ValueLayout.JAVA_INT.withName("kernelExecTimeoutEnabled"),
               ValueLayout.JAVA_INT.withName("integrated"),
               ValueLayout.JAVA_INT.withName("acnMapHostMemory"),
               ValueLayout.JAVA_INT.withName("computeMode"),
               ValueLayout.JAVA_INT.withName("maxTexture1D"),
               ValueLayout.JAVA_INT.withName("maxTexture1DMipmap"),
               ValueLayout.JAVA_INT.withName("maxTexture1DLinear"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxTexture2D"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxTexture2DMipmap"),
               MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("maxTexture2DLinear"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxTexture2DGather"),
               MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("maxTexture3D"),
               MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("maxTexture3DAlt"),
               ValueLayout.JAVA_INT.withName("maxTextureCubemap"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxTexture1DLayered"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxTextureCubemapLayered"),
               ValueLayout.JAVA_INT.withName("maxSurface1D"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxSurface2D"),
               MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("maxSurface3D"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxSurface1DLayered"),
               MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT).withName("maxSurface2DLayered"),
               ValueLayout.JAVA_INT.withName("maxSurfaceCubemap"),
               MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_INT).withName("maxSurfaceCubemapLayered"),
               ValueLayout.JAVA_LONG.withName("surfaceAlignment"), // unsigned (size_t)
               ValueLayout.JAVA_INT.withName("concurrentKernels"),
               ValueLayout.JAVA_INT.withName("ECCEnabled"),
               ValueLayout.JAVA_INT.withName("pciBusID"),
               ValueLayout.JAVA_INT.withName("pciDeviceID"),
               ValueLayout.JAVA_INT.withName("pciDomainID"),
               ValueLayout.JAVA_INT.withName("tccDriver"),
               ValueLayout.JAVA_INT.withName("asyncEngineCount"),
               ValueLayout.JAVA_INT.withName("unifiedAddressing"),
               ValueLayout.JAVA_INT.withName("memoryClockRate"),
               ValueLayout.JAVA_INT.withName("memoryBusWidth"),
               ValueLayout.JAVA_INT.withName("l2CacheSize"),
               ValueLayout.JAVA_INT.withName("persistingL2CacheMaxSize"),
               ValueLayout.JAVA_INT.withName("maxThreadsPerMultiProcessor"),
               ValueLayout.JAVA_INT.withName("streamPrioritiesSupported"),
               ValueLayout.JAVA_INT.withName("globalL1CacheSupported"),
               ValueLayout.JAVA_INT.withName("localL1CacheSupported"),
               ValueLayout.JAVA_LONG.withName("sharedMemPerMultiprocessor"), // unsigned (size_t)
               ValueLayout.JAVA_INT.withName("regsPerMultiprocessor"),
               ValueLayout.JAVA_INT.withName("managedMemory"),
               ValueLayout.JAVA_INT.withName("isMultiGpuBoard"),
               ValueLayout.JAVA_INT.withName("multiGpuBoardGroupID"),
               ValueLayout.JAVA_INT.withName("hostNativeAtomicSupported"),
               ValueLayout.JAVA_INT.withName("singleToDoublePrecisionPerfRatio"),
               ValueLayout.JAVA_INT.withName("pageableMemoryAccess"),
               ValueLayout.JAVA_INT.withName("concurrentManagedAccess"),
               ValueLayout.JAVA_INT.withName("computePreemptionSupported"),
               ValueLayout.JAVA_INT.withName("canUseHostPointerForRegisteredMem"),
               ValueLayout.JAVA_INT.withName("cooperativeLaunch"),
               ValueLayout.JAVA_INT.withName("cooperativeMultiDeviceLaunch"),
               ValueLayout.JAVA_LONG.withName("sharedMemPerBlockOptin"), // unsigned (size_t)
               ValueLayout.JAVA_INT.withName("pageableMemoryAccessUsesHostPageTables"),
               ValueLayout.JAVA_INT.withName("directManagedMemAccessFromHost"),
               ValueLayout.JAVA_INT.withName("maxBlocksPerMultiProcessor"),
               ValueLayout.JAVA_INT.withName("accessPolicyMaxWindowSize"),
               ValueLayout.JAVA_LONG.withName("reservedSharedMemPerBlock") // unsigned (size_t)
             );
    SegmentAllocator allocator = SegmentAllocator.nativeAllocator(SegmentScope.auto());
    mem = allocator.allocate(layout);

    hndTotalGlobalMem = null;
    hndSharedMemPerBlock = null;
    hndWrapSize = null;
    hndMaxThreadsPerBlock = null;
    hndMajor = null;
    hndMinor = null;
    hndClockRate = null;
    hndMultiProcessorCount = null;
  }

  GroupLayout getLayout(){
    return layout;
  }

  MemorySegment getMem(){
    return mem;
  }

  public String name(){
    return mem.getUtf8String(layout.byteOffset(MemoryLayout.PathElement.groupElement("name")));
  }

  public long totalGlobalMem(){
    if(hndTotalGlobalMem == null){
      hndTotalGlobalMem = layout.varHandle(MemoryLayout.PathElement.groupElement("totalGlobalMem"));
    }
    return (long)hndTotalGlobalMem.get(mem);
  }

  public long sharedMemPerBlock(){
    if(hndSharedMemPerBlock == null){
      hndSharedMemPerBlock = layout.varHandle(MemoryLayout.PathElement.groupElement("sharedMemPerBlock"));
    }
    return (long)hndSharedMemPerBlock.get(mem);
  }

  public int wrapSize(){
    if(hndWrapSize == null){
      hndWrapSize = layout.varHandle(MemoryLayout.PathElement.groupElement("wrapSize"));
    }
    return (int)hndWrapSize.get(mem);
  }

  public int maxThreadsPerBlock(){
    if(hndMaxThreadsPerBlock == null){
      hndMaxThreadsPerBlock = layout.varHandle(MemoryLayout.PathElement.groupElement("maxThreadsPerBlock"));
    }
    return (int)hndMaxThreadsPerBlock.get(mem);
  }

  public int major(){
    if(hndMajor == null){
      hndMajor = layout.varHandle(MemoryLayout.PathElement.groupElement("major"));
    }
    return (int)hndMajor.get(mem);
  }

  public int minor(){
    if(hndMinor == null){
      hndMinor = layout.varHandle(MemoryLayout.PathElement.groupElement("minor"));
    }
    return (int)hndMinor.get(mem);
  }

  public int clockRate(){
    if(hndClockRate == null){
      hndClockRate = layout.varHandle(MemoryLayout.PathElement.groupElement("clockRate"));
    }
    return (int)hndClockRate.get(mem);
  }

  public int multiProcessorCount(){
    if(hndMultiProcessorCount == null){
      hndMultiProcessorCount = layout.varHandle(MemoryLayout.PathElement.groupElement("multiProcessorCount"));
    }
    return (int)hndMultiProcessorCount.get(mem);
  }

}
