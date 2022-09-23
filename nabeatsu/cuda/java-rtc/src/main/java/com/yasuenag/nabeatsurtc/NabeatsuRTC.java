package com.yasuenag.nabeatsurtc;

import com.yasuenag.nabeatsurtc.cuda.*;


public class NabeatsuRTC{

  private static final String KERNEL_CODE =
    """
      extern "C" __global__ void nabeatsuKernel(bool* result, int result_len)
      {
        int i = (blockIdx.x * blockDim.x) + threadIdx.x;

        if (i < result_len) {
          int val = i + 1;
          result[i] = (val % 3) == 0;

          while (val > 0) {
            result[i] |= (val % 10) == 3;
            val /= 10;
          }
        }
      }
    """;

  private static CudaDeviceProp getInfo(CudaRuntime runtime) throws Throwable{
    var prop = new CudaDeviceProp();
    runtime.cudaGetDeviceProperties(prop, 0);

    System.out.println("Device: " + prop.name());
    System.out.println("Global memory available on device in bytes: " + Long.toUnsignedString(prop.totalGlobalMem()));
    System.out.println("Shared memory available per block in bytes: " + Long.toUnsignedString(prop.sharedMemPerBlock()));
    System.out.println("Warp size in threads: " + Integer.toString(prop.wrapSize()));
    System.out.println("Maximum number of threads per block: " + Integer.toString(prop.maxThreadsPerBlock()));
    System.out.format("Compute capacity: %d.%d\n", prop.major(), prop.minor());
    System.out.println("Clock frequency in kilohertz: " + Integer.toString(prop.clockRate()));
    System.out.println("Number of multiprocessors on device: " + Integer.toString(prop.multiProcessorCount()));

    return prop;
  }

  private static long initCtx(Cuda cuda) throws Throwable{
    cuda.cuInit(0);
    int device = cuda.cuDeviceGet(0);
    return cuda.cuCtxCreate(0, device);
  }

  private static byte[] createPTX() throws Throwable{
    var compiler = new CudaRuntimeCompiler();
    long prog = compiler.nvrtcCreateProgram(KERNEL_CODE, null, null, null);
    try{
      compiler.nvrtcCompileProgram(prog, null);
      return compiler.nvrtcGetPTX(prog);
    }
    finally{
      compiler.nvrtcDestroyProgram(prog);
    }
  }

  private static void showResult(byte[] result){
    for(int i = 0; i < result.length; i++){
      System.out.print(i + 1);
      if(result[i] == 1){
        System.out.print(" [aho]");
      }
      System.out.println();
    }
  }

  public static void main(String[] args) throws Throwable{
    int len = Integer.parseInt(args[0]);;

    var runtime = new CudaRuntime();
    var prop = getInfo(runtime);

    byte[] ptx = createPTX();

    var cuda = new Cuda();
    long ctx = initCtx(cuda);
    try{
      long module = cuda.cuModuleLoadData(ptx);
      try{
        long func = cuda.cuModuleGetFunction(module, "nabeatsuKernel");
        int threads = prop.maxThreadsPerBlock();
        int blocks = (len / threads) + 1;
        long dev_result = cuda.cuMemAlloc(len);
        try{
          cuda.cuLaunchKernel(func, blocks, 1, 1, threads, 1, 1, 0, 0, new Object[]{dev_result, len}, null);
          byte[] result = (byte[])cuda.cuMemcpyDtoH(new byte[0], dev_result, len);
          showResult(result);
        }
        finally{
          cuda.cuMemFree(dev_result);
        }
      }
      finally{
        cuda.cuModuleUnload(module);
      }
    }
    finally{
      cuda.cuCtxDestroy(ctx);
    }
  }

}
