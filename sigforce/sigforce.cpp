#include <cstring>
#include <iostream>

#include <jni.h>
#include <jvmti.h>


extern const char _binary_com_yasuenag_garakuta_sigforce_Transformer_class_start[];
extern const char _binary_com_yasuenag_garakuta_sigforce_Transformer_class_end[];
static const jsize transformer_len = _binary_com_yasuenag_garakuta_sigforce_Transformer_class_end - _binary_com_yasuenag_garakuta_sigforce_Transformer_class_start;

static bool replaced = false;


void JNICALL OnClassFileLoadHook(jvmtiEnv *jvmti,
                                 JNIEnv *env,
                                 jclass class_being_redefined,
                                 jobject loader,
                                 const char *name,
                                 jobject protection_domain,
                                 jint class_data_len,
                                 const unsigned char *class_data,
                                 jint *new_class_data_len,
                                 unsigned char **new_class_data){
  if(!replaced && strcmp(name, "sun/tools/attach/VirtualMachineImpl") == 0){
    jclass transformer_cls = env->DefineClass("com/yasuenag/garakuta/sigforce/Transformer",
                                              nullptr,
                                              reinterpret_cast<const jbyte *>(_binary_com_yasuenag_garakuta_sigforce_Transformer_class_start),
                                              transformer_len);
    if(transformer_cls == nullptr){
      // Maybe exception thrown due to older JDK (JDK 23 or earlier).
      // Return immediately - The error should be reported
      // and the process should be aborted by VM.
      return;
    }

    jmethodID transform_method = env->GetStaticMethodID(transformer_cls,
                                                        "transform",
                                                        "([B)[B");
    jbyteArray jba_class_data = env->NewByteArray(class_data_len);
    env->SetByteArrayRegion(jba_class_data, 0, class_data_len, reinterpret_cast<const jbyte *>(class_data));
    jbyteArray transformed_data = static_cast<jbyteArray>(env->CallStaticObjectMethod(transformer_cls,
                                                                                      transform_method,
                                                                                      jba_class_data));
    *new_class_data_len = env->GetArrayLength(transformed_data);
    jvmti->Allocate(*new_class_data_len, new_class_data);
    env->GetByteArrayRegion(transformed_data, 0, *new_class_data_len, reinterpret_cast<jbyte *>(*new_class_data));

    replaced = true;
    std::cout << "sigforce: transformed: " << name << std::endl;
  }
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved){
  jvmtiEnv *jvmti;
  if(vm->GetEnv(reinterpret_cast<void **>(&jvmti), JVMTI_VERSION_21) != JNI_OK){
    std::cerr << "sigforce: Could not get JVMTI 21 interface" << std::endl;
    return -1;
  }

  jvmtiCapabilities capabilities = {0};
  capabilities.can_generate_all_class_hook_events = 1;
  jvmti->AddCapabilities(&capabilities);

  jvmtiEventCallbacks callbacks = {0};
  callbacks.ClassFileLoadHook = &OnClassFileLoadHook;
  jvmti->SetEventCallbacks(&callbacks, sizeof(jvmtiEventCallbacks));
  jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, nullptr);

  std::cout << "sigforce: loaded" << std::endl;
  return 0;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM *vm){
  // Do nothing
}
