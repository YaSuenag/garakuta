#include <jni.h>


inline int native_impl(char *src1, char *src2, int len){
  int dot_product = 0;
  for(int i = 0; i < len; i++){
    dot_product += static_cast<unsigned char>(src1[i]) * src2[i];
  }
  return dot_product;
}

extern "C" int cpp_impl(char *src1, char *src2, int len){
  return native_impl(src1, src2, len);
}

extern "C" JNIEXPORT jboolean JNICALL Java_com_yasuenag_presentation_jjugccc2026_Test_checkAVXVNNI(JNIEnv*, jclass){
  int eax;
  asm volatile(
    "mov $0x7, %%eax \n"
    "mov $0x1, %%ecx \n"
    "cpuid             "
    : "=a"(eax) : : "ebx", "ecx", "edx");
  return (eax >> 4) & 1;
}

extern "C" JNIEXPORT jint JNICALL Java_com_yasuenag_presentation_jjugccc2026_Test_jniImpl(JNIEnv *env, jobject, jbyteArray src1, jbyteArray src2){
  jbyte *native_src1 = env->GetByteArrayElements(src1, nullptr);
  jbyte *native_src2 = env->GetByteArrayElements(src2, nullptr);

  int result = native_impl(reinterpret_cast<char*>(native_src1), reinterpret_cast<char*>(native_src2), env->GetArrayLength(src1));

  env->ReleaseByteArrayElements(src1, native_src1, JNI_ABORT);
  env->ReleaseByteArrayElements(src2, native_src2, JNI_ABORT);

  return result;
}
