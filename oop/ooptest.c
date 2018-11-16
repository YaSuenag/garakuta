#include <jni.h>
#include <stdio.h>

#ifndef _Included_OopInspectTest
#define _Included_OopInspectTest
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     OopInspectTest
 * Method:    printThisOopAddress
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_OopInspectTest_printThisOopAddress(
                                                JNIEnv *env, jobject this_obj){
  printf("this object = %p\n", *((void **)this_obj));
}

#ifdef __cplusplus
}
#endif
#endif
