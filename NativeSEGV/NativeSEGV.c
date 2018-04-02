#include <jni.h>

/*
 * Class:     NativeSEGV
 * Method:    doSEGV
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_NativeSEGV_doSEGV(JNIEnv *env, jclass cls){
  char buf[1];
  buf[100] = '\0';
}
