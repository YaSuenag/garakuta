#pragma once

#include <jvmti.h>

#include <iostream>


class SafetyJvmtiMonitorUsage{
  private:
    jvmtiEnv *env;
    jvmtiMonitorUsage usage;
  public:
    SafetyJvmtiMonitorUsage(jvmtiEnv *jvmti) : usage({0}), env(jvmti) {}

    ~SafetyJvmtiMonitorUsage(){
      if(usage.waiters != NULL){
        env->Deallocate(reinterpret_cast<unsigned char *>(usage.waiters));
      }
      if(usage.notify_waiters != NULL){
        env->Deallocate(reinterpret_cast<unsigned char *>(usage.notify_waiters));
      }
    }

    constexpr jvmtiMonitorUsage* operator -> () noexcept { return &usage; }
    constexpr jvmtiMonitorUsage* operator & () noexcept { return &usage; }
};

#define CHECK_WITH_RETURN(jvmti, the_call) \
  { \
    jvmtiError result = jvmti->the_call; \
    if(result != JVMTI_ERROR_NONE){ \
      char *name; \
      jvmti->GetErrorName(result, &name); \
      std::cerr << "dldetector: " << name << std::endl; \
      jvmti->Deallocate(reinterpret_cast<unsigned char *>(name)); \
      return result; \
    } \
  }
