//
// Created by wrath on 10/28/2017.
//

#include <jni.h>
#include <math.h>

#define TRUE 1
#define FALSE 0


/* This is a trivial JNI example where we use a native method
* to return a new VM String. See the corresponding Java source
        * file located at:
*
*   SmartRGApp/app/src/main/java/com.smartrg.smartrgapp/Activities/VideoStreamAnalyzerActivity
*/
JNIEXPORT jstring JNICALL
Java_com_smartrg_smartrgapp_Activities_VideoStreamAnalyzerActivity_getVSAString(JNIEnv *env,
                                                                                jobject instance) {

    return (*env)->NewStringUTF(env, "Hello from JNI!");
}