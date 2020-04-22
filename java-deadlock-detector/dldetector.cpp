#include <jni.h>
#include <jvmti.h>

#ifndef _WINDOWS
#include <signal.h>
#include <unistd.h>
#endif

#include <cassert>
#include <iostream>

#include "dldetector.hpp"


static jmethodID Thread_getId_method;

void JNICALL OnVMInit(jvmtiEnv *jvmti, JNIEnv *env, jthread thread){
  jclass thread_cls = env->FindClass("java/lang/Thread");
  assert(thread_cls != NULL);

  Thread_getId_method = env->GetMethodID(thread_cls, "getId", "()J");
  assert(Thread_getId_method != NULL);
}

void JNICALL OnMonitorContendedEnter(jvmtiEnv *jvmti, JNIEnv *env, jthread thread, jobject object){
  jlong current_id = env->CallLongMethod(thread, Thread_getId_method);

  jobject monitor = object;
  while(true){
    jvmtiError result;

    SafetyJvmtiMonitorUsage usage(jvmti);
    result = jvmti->GetObjectMonitorUsage(monitor, &usage);
    if((result != JVMTI_ERROR_NONE) || // error
       (usage->owner == NULL)){ // Monitor was already released{
      return;
    }

    jlong id = env->CallLongMethod(usage->owner, Thread_getId_method);
    if(id == current_id){
      std::cerr << "dldetector: Dead lock was occurred!" << std::endl;

#ifndef _WINDOWS
      kill(getpid(), SIGQUIT);
#endif

      return;
    }

    jint state;
    result = jvmti->GetThreadState(usage->owner, &state);
    if(result != JVMTI_ERROR_NONE){
      // error
      return;
    }
    else if(state & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER){
      result = jvmti->GetCurrentContendedMonitor(usage->owner, &monitor);
      if((result != JVMTI_ERROR_NONE) || (monitor == NULL)){
        return;
      }
    }
    else{
      // Monitor owner does not wait to enter other monitor.
      return;
    }
  }

}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved){
  jvmtiEnv *jvmti;
  if(vm->GetEnv(reinterpret_cast<void **>(&jvmti), JVMTI_VERSION_1_0) != JNI_OK){
    std::cerr << "dldetector: Could not get JVMTI 1.0 interface" << std::endl;
    return -1;
  }

  jvmtiCapabilities capabilities = {0};
  capabilities.can_generate_monitor_events = 1;
  capabilities.can_get_monitor_info = 1;
  capabilities.can_get_current_contended_monitor = 1;
  CHECK_WITH_RETURN(jvmti, AddCapabilities(&capabilities));

  jvmtiEventCallbacks callbacks = {0};
  callbacks.VMInit = &OnVMInit;
  callbacks.MonitorContendedEnter = &OnMonitorContendedEnter;
  CHECK_WITH_RETURN(jvmti, SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks)));

  CHECK_WITH_RETURN(jvmti, SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, NULL));
  CHECK_WITH_RETURN(jvmti, SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_MONITOR_CONTENDED_ENTER, NULL));

  std::cout << "dldetector: loaded" << std::endl;
  return 0;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm){
  // Do nothing
}
