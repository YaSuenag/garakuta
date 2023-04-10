cmake_minimum_required(VERSION 3.12)
project(nabeatsu-cuda CXX CUDA)

cmake_policy(SET CMP0074 NEW)
find_package(CUDA REQUIRED)

set(CMAKE_BUILD_TYPE Debug)

add_executable(nabeatsu-cuda cuda.cpp nabeatsu.cu ../../common.cpp)
set_property(TARGET nabeatsu-cuda PROPERTY CUDA_ARCHITECTURES OFF)
