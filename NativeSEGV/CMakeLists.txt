cmake_minimum_required(VERSION 3.10)
project(NativeSEGV C)

find_package(JNI REQUIRED)
find_package(Java COMPONENTS Development REQUIRED)
include (UseJava)

include_directories(${JNI_INCLUDE_DIRS})
set(CMAKE_BUILD_TYPE Debug)

add_library(segv SHARED NativeSEGV.c)
add_jar(NativeSEGV NativeSEGV.java ENTRY_POINT NativeSEGV)
