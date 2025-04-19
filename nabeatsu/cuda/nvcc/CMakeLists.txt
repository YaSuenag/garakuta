cmake_minimum_required(VERSION 3.31)
project(nabeatsu-cuda LANGUAGES CXX CUDA)

find_package(CUDAToolkit REQUIRED)
set(CMAKE_BUILD_TYPE Debug)

add_executable(nabeatsu-cuda cuda.cpp nabeatsu.cu ../../common.cpp)
