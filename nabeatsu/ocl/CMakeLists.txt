cmake_minimum_required(VERSION 3.10)
project(nabeatsu-cl CXX)

find_package(OpenCL 2.0 REQUIRED)

include_directories(${OpenCL_INCLUDE_DIRS})
set(CMAKE_BUILD_TYPE Debug)

add_executable(nabeatsu-cl nabeatsu.cpp ../common.cpp)
target_link_libraries(nabeatsu-cl ${OpenCL_LIBRARIES})
