#include "cuda_runtime.h"
#include "device_launch_parameters.h"

#include <iostream>

__global__ void nabeatsuKernel(bool* result, int result_len)
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

void showInfo() {
    cudaDeviceProp devProp;
    cudaGetDeviceProperties(&devProp, 0);

    std::cout << "Devuce: " << devProp.name << std::endl;
    std::cout << "Global memory available on device in bytes: " << devProp.totalGlobalMem << std::endl;
    std::cout << "Shared memory available per block in bytes: " << devProp.sharedMemPerBlock << std::endl;
    std::cout << "Warp size in threads: " << devProp.warpSize << std::endl;
    std::cout << "Maximum number of threads per block: " << devProp.maxThreadsPerBlock << std::endl;
    std::cout << "Compute capacity: " << devProp.major << "." <<  devProp.minor << std::endl;
    std::cout << "Clock frequency in kilohertz: " << devProp.clockRate << std::endl;
    std::cout << "Number of multiprocessors on device: " << devProp.multiProcessorCount << std::endl;
}

int getMaxThreadsPerBlock() {
    cudaDeviceProp devProp;
    cudaGetDeviceProperties(&devProp, 0);
    return devProp.maxThreadsPerBlock;
}

bool invokeNabeatsu(bool *result, int result_len, int nBlock, int nThread)
{
    cudaError_t cudaStatus;

    cudaStatus = cudaSetDevice(0);
    if (cudaStatus != cudaSuccess) {
        std::cerr << "cudaSetDevice failed!" << std::endl;
        return false;
    }

    bool* dev_result = NULL;
    cudaStatus = cudaMalloc((void**)&dev_result, result_len * sizeof(bool));
    if (cudaStatus != cudaSuccess) {
        std::cerr << "cudaMalloc failed!" << std::endl;
        return false;
    }

    nabeatsuKernel <<<nBlock, nThread>>> (dev_result, result_len);

    cudaStatus = cudaGetLastError();
    if (cudaStatus != cudaSuccess) {
        std::cerr << "nabeatsuKernel failed: " <<  cudaGetErrorString(cudaStatus) << std::endl;
        cudaFree(dev_result);
        return false;
    }

    cudaStatus = cudaDeviceSynchronize();
    if (cudaStatus != cudaSuccess) {
        std::cerr << "cudaDeviceSynchronize failed!" << std::endl;
        cudaFree(dev_result);
        return false;
    }

    cudaStatus = cudaMemcpy(result, dev_result, result_len * sizeof(bool), cudaMemcpyDeviceToHost);
    if (cudaStatus != cudaSuccess) {
        std::cerr << "cudaMemcpy failed!" << std::endl;
        cudaFree(dev_result);
        return false;
    }

    cudaFree(dev_result);
    cudaDeviceReset();

    return true;
}
