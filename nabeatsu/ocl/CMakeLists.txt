cmake_minimum_required(VERSION 3.12)
project(nabeatsu-cl CXX)

cmake_policy(SET CMP0074 NEW)
find_package(OpenCL 2.0 REQUIRED)

include_directories(${OpenCL_INCLUDE_DIRS})
set(CMAKE_BUILD_TYPE Debug)

add_executable(nabeatsu-cl nabeatsu.cpp ../common.cpp)
target_link_libraries(nabeatsu-cl ${OpenCL_LIBRARIES})
