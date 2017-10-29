//
// Created by wrath on 10/28/2017.
//

#include <jni.h>
#include <math.h>
#include <string.h>
#include <pthread.h>
#include <android/log.h>
#include <assert.h>
#include <unistd.h>
#include <stdio.h>


// Android log function wrappers
static const char* kTAG = "VSA-jni";
#define LOGI(...) \
    ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
    ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
    ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))
#define LOGD(...) \
    ((void)__android_log_print(ANDROID_LOG_DEBUG, kTAG, __VA_ARGS__))

#define TRUE 1
#define FALSE 0


// processing callback to handler class
typedef struct vsa_context {
    JavaVM  *javaVM;
    jclass   vsaActivityClz;
    jobject  vsaActivityObj;
    pthread_mutex_t  lock;
    int      done;
} VSAContext;
VSAContext g_ctx;


/*
 * processing one time initialization:
 *     Called when library is first loaded from java code
 *     Cache the javaVM into our context
 *     Make global reference since we are using them from a native thread
 * Note:
 *     All resources allocated here are never released by application
 *     we rely on system to free all global refs when it goes away;
 *     the pairing function JNI_OnUnload() never gets called at all.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    memset(&g_ctx, 0, sizeof(g_ctx)); //clear any garbage values if they exist

    g_ctx.javaVM = vm; //cache the javaVM
    LOGD("JavaVM cached");

    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR; // JNI version not supported.
    }

    //set initial values
    g_ctx.done = FALSE;
    g_ctx.vsaActivityObj = NULL;

    LOGI("VSA Library Initialized");

    return  JNI_VERSION_1_6;

}


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

/*
 * Main working thread function. From a pthread,
 *      calling back to VideoStreamAnalyzerActivity::updateGraphs() to display data on UI
 */

void * updateGraphs(void* context) {
    VSAContext *pctx = (VSAContext*) context;
    JavaVM *javaVM = pctx->javaVM;
    JNIEnv *env;

    jint res = (*javaVM)->GetEnv(javaVM, (void**)&env, JNI_VERSION_1_6);
    if(res != JNI_OK) {
        res = (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);
        if(res != JNI_OK) {
            LOGE("Failed to AttachCurrentThread, ErrorCode = %d", res);
            return NULL;
        }
    }

    /*get VideoStreamAnalyzerActivity updateGraphs function
     * "(Ljava/lang/String;)V" is the java functions signature (argType1;argType2;...)returnType
     */
    jmethodID graphsId = (*env)->GetMethodID(env, pctx->vsaActivityClz, "updateGraphs",
                                             "(Ljava/lang/String;)V");

    /*used to make sure sleeping takes only 1 second*/
    struct timeval beginTime, currTime, usedTime, leftTime;
    const struct timeval kOneSecond = {
            (__kernel_time_t)1,
            (__kernel_suseconds_t) 0
    };

    LOGI("Start collecting data");
    int counter = 0;

    while(TRUE) {
        //set the begin time
        gettimeofday(&beginTime, NULL);

        //lock so that you can change values securely
        pthread_mutex_lock(&pctx->lock);
        int done = pctx->done;

        if(pctx->done) {
            //reset the value for next use
            pctx->done = FALSE;
        }
        pthread_mutex_unlock(&pctx->lock);
        if(done) {
            break;
        }

        //call the callback function
        char num[32];
        snprintf(num, 32, "%d", counter);
        (*env)->CallVoidMethod(env, pctx->vsaActivityObj, graphsId,
                               (*env)->NewStringUTF(env, num));

        //increase counter
        counter++;

        //calculate how much to sleep
        gettimeofday(&currTime, NULL);
        timersub(&currTime, &beginTime, &usedTime); //used time for calculation
        timersub(&kOneSecond, &usedTime, &leftTime); //time left 1s - usedTime

        //sleep thread
        struct timespec sleepTime;
        sleepTime.tv_sec = leftTime.tv_sec;
        sleepTime.tv_nsec = leftTime.tv_usec * 1000; //change from micro 10^-6 to nano 10^-9

        if(sleepTime.tv_sec <= 1){
            nanosleep(&sleepTime, NULL);
        } else {
            LOGI("Processing Took longer than 1 second");
        }
    }

    LOGI("Stop collecting data");

    (*javaVM)->DetachCurrentThread(javaVM);
    return context;
}

/*
 * Interface to Java side to start VSA, caller is from onResume()
 */
JNIEXPORT void JNICALL
Java_com_smartrg_smartrgapp_Activities_VideoStreamAnalyzerActivity_startVSA(JNIEnv *env,
                                                                            jobject instance) {
    pthread_t threadInfo_;
    pthread_attr_t threadAttr_;

    /*initializes the thread attribute object attr and fills it with default
     * values for the attributes
    */
    pthread_attr_init(&threadAttr_);

    //In the detached state, the thread's resources are released immediately when it terminates
    pthread_attr_setdetachstate(&threadAttr_, PTHREAD_CREATE_DETACHED);

    pthread_mutex_init(&g_ctx.lock, NULL);

    //cache the VideoStreamAnalyzerActivity class and object
    jclass clz = (*env)->GetObjectClass(env, instance);
    g_ctx.vsaActivityClz = (*env)->NewGlobalRef(env, clz);
    g_ctx.vsaActivityObj = (*env)->NewGlobalRef(env, instance);

    int result = pthread_create(&threadInfo_, &threadAttr_, updateGraphs, &g_ctx);
    assert(result == 0); //make sure thread created successfully

    pthread_attr_destroy(&threadAttr_);

    (void)result;

}

/*
 * Interface to Java side to stop VSA:
 *    we need to hold and make sure our native thread has finished before return
 *    for a clean shutdown. The caller is from onPause()
 */
JNIEXPORT void JNICALL
Java_com_smartrg_smartrgapp_Activities_VideoStreamAnalyzerActivity_stopVSA(JNIEnv *env,
                                                                           jobject instance) {
    //lock so you can change values securely
    pthread_mutex_lock(&g_ctx.lock);
    g_ctx.done = TRUE;
    pthread_mutex_unlock(&g_ctx.lock);

    //wait for vsa thread to flip the done flag
    // waiting for ticking thread to flip the done flag
    struct timespec sleepTime;
    memset(&sleepTime, 0, sizeof(sleepTime));
    sleepTime.tv_nsec = 100000000;
    while (g_ctx.done) {
        nanosleep(&sleepTime, NULL);
    }

    //release object we allocated from startVSA() function
    (*env)->DeleteGlobalRef(env, g_ctx.vsaActivityClz);
    (*env)->DeleteGlobalRef(env, g_ctx.vsaActivityObj);
    g_ctx.vsaActivityObj = NULL;
    g_ctx.vsaActivityClz = NULL;

    pthread_mutex_destroy(&g_ctx.lock);

}