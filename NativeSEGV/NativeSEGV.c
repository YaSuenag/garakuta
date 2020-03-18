#include <jni.h>
#include <stdio.h>

/*
 * Class:     NativeSEGV
 * Method:    doSEGV
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_NativeSEGV_doSEGV(JNIEnv *env, jclass cls){
  char *buf = NULL;
  *buf = '\0';
}

/*
 * Class:     NativeSEGV
 * Method:    doSEGVInLibC
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_NativeSEGV_doSEGVInLibC(JNIEnv *env, jclass cls){
  sprintf(NULL, NULL);
}
