#include <stdio.h>
#include <stdlib.h>
#include <cuda.h>
#include <cuda_runtime.h>
#include <nvrtc.h>

#define KERNEL_CODE \
"extern \"C\" __global__ void nabeatsuKernel(bool* result, int result_len) \n" \
"{ \n" \
"    int i = (blockIdx.x * blockDim.x) + threadIdx.x; \n" \
\
"    if (i < result_len) { \n" \
"        int val = i + 1; \n" \
"        result[i] = (val % 3) == 0; \n" \
\
"        while (val > 0) { \n" \
"            result[i] |= (val % 10) == 3; \n" \
"            val /= 10; \n" \
"        } \n" \
"    } \n" \
"}"

void GetInfo(struct cudaDeviceProp *devProp) {
  cudaGetDeviceProperties(devProp, 0);

  printf("Devuce: %s\n", devProp->name);
  printf("Global memory available on device in bytes: %lu\n", devProp->totalGlobalMem);
  printf("Shared memory available per block in bytes: %lu\n", devProp->sharedMemPerBlock);
  printf("Warp size in threads: %d\n", devProp->warpSize);
  printf("Maximum number of threads per block: %d\n", devProp->maxThreadsPerBlock);
  printf("Compute capacity: %d.%d\n", devProp->major, devProp->minor);
  printf("Clock frequency in kilohertz: %d\n", devProp->clockRate);
  printf("Number of multiprocessors on device: %d\n", devProp->multiProcessorCount);
}

char *CreatePTX(){
  nvrtcResult result;
  nvrtcProgram prog;
  size_t ptx_size;
  char *ptx;

  result = nvrtcCreateProgram(&prog, KERNEL_CODE, NULL, 0, NULL, NULL);
  if(result != NVRTC_SUCCESS){
    fprintf(stderr, "ERROR: %s:%d: %s\n", __FILE__, __LINE__, nvrtcGetErrorString(result));
    return NULL;
  }

  result = nvrtcCompileProgram(prog, 0, NULL);
  if(result != NVRTC_SUCCESS){
    fprintf(stderr, "ERROR: %s:%d: %s\n", __FILE__, __LINE__, nvrtcGetErrorString(result));
    nvrtcDestroyProgram(&prog);
    return NULL;
  }

  nvrtcGetPTXSize(prog, &ptx_size);
  ptx = (char *)malloc(ptx_size);
  nvrtcGetPTX(prog, ptx);

  nvrtcDestroyProgram(&prog);
  return ptx;
}

#define PRINT_CU_ERROR(errcode) \
  { \
    const char *str, *desc; \
    cuGetErrorName(cu_result, &str); \
    cuGetErrorString(cu_result, &desc); \
    fprintf(stderr, "ERROR: %s:%d: %s (%s)\n", __FILE__, __LINE__, str, desc); \
  }

CUresult InitCtx(CUcontext *ctx){
  CUresult cu_result;
  CUdevice device;

  cu_result = cuInit(0);
  if(cu_result != CUDA_SUCCESS){
    PRINT_CU_ERROR(cu_result)
  }
  else{
    cu_result = cuDeviceGet(&device, 0);
    if(cu_result != CUDA_SUCCESS){
      PRINT_CU_ERROR(cu_result)
    }
    else{
      cu_result = cuCtxCreate(ctx, 0, device);
      if(cu_result != CUDA_SUCCESS){
        PRINT_CU_ERROR(cu_result)
      }
    }
  }

  return cu_result;
}

CUresult GetKernelFunc(CUmodule *module, CUfunction *func, const char *ptx){
  CUresult cu_result;

  cu_result = cuModuleLoadData(module, ptx);
  if(cu_result != CUDA_SUCCESS){
    PRINT_CU_ERROR(cu_result)
  }
  else{
    cu_result = cuModuleGetFunction(func, *module, "nabeatsuKernel");
    if(cu_result != CUDA_SUCCESS){
      PRINT_CU_ERROR(cu_result)
    }
  }

  return cu_result;
}

int main(int argc, char *argv[]){
  int len;
  int blocks;
  int threads;
  struct cudaDeviceProp devProp;
  char *ptx;
  CUresult cu_result;
  CUcontext context;
  CUmodule module;
  CUfunction func;
  CUdeviceptr dev_result;
  unsigned char *result;
  int i;

  len = atoi(argv[1]);

  cu_result = InitCtx(&context);
  if(cu_result != CUDA_SUCCESS){
    return -1;
  }

  GetInfo(&devProp);
  printf("\n");

  ptx = CreatePTX();
  if(ptx == NULL){
    return -1;
  }
  cu_result = GetKernelFunc(&module, &func, ptx);
  free(ptx);
  if(cu_result != CUDA_SUCCESS){
    cuModuleUnload(module); // it will fail when cuModuleLoadData() fails.
    cuCtxDestroy(context);
    return -1;
  }

  cu_result = cuMemAlloc(&dev_result, len);
  if(cu_result == CUDA_SUCCESS){
    threads = devProp.maxThreadsPerBlock;
    blocks = (len / threads) + 1;
    {
      void *args[] = {&dev_result, &len};
      cu_result = cuLaunchKernel(func, blocks, 1, 1, threads, 1, 1, 0, NULL, args, NULL);;
      if(cu_result == CUDA_SUCCESS){
        result = (unsigned char *)malloc(len);
        cuMemcpyDtoH(result, dev_result, len);
        for(i = 0; i < len; i++){
          printf("%d%s\n", i + 1, result[i] == 1 ? " [aho]" : "");
        }
        free(result);
      }
      else{
        PRINT_CU_ERROR(cu_result)
      }
    }

    cuMemFree(dev_result);
  }
  else{
    PRINT_CU_ERROR(cu_result)
  }

  cuModuleUnload(module);
  cuCtxDestroy(context);
  return cu_result;
}
