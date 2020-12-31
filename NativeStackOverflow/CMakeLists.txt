cmake_minimum_required(VERSION 3.10)
project(NativeStackOverflow C ASM)

if(${CMAKE_SYSTEM_PROCESSOR} STREQUAL "x86_64")
  set(ASM_SRC NativeOverflow.s)
else()
  message(FATAL_ERROR "Unsupported processor:" ${CMAKE_SYSTEM_PROCESSOR})
endif()

find_package(Java COMPONENTS Development REQUIRED)
include (UseJava)

set(CMAKE_ASM_CREATE_SHARED_LIBRARY ${CMAKE_C_CREATE_SHARED_LIBRARY})
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -z noexecstack")

set(CMAKE_BUILD_TYPE Debug)

add_library(overflow SHARED ${ASM_SRC})
add_jar(NativeStackOverflow NativeStackOverflow.java ENTRY_POINT NativeStackOverflow)
