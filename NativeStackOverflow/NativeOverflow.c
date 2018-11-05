#include <jni.h>

/*
 * Class:     NativeStackOverflow
 * Method:    doOverflow
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_NativeStackOverflow_doOverflow(JNIEnv *env, jclass cls){
  asm volatile("xorq %rax, %rax;"
               "1:"
               "  movq %rax, (%rsp);"
               "  subq $0x8, %rsp;"
               "  jmp 1b;");
}
