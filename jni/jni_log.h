/*
 * jni_log.h
 *
 *  Created on: 2014年6月13日
 *      Author: DLL
 */

#include <android/log.h>

#ifndef JNI_LOG_H_
#define JNI_LOG_H_



#define TAG "JNI_LOG"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#endif	/* JNI_LOG_H_ */
