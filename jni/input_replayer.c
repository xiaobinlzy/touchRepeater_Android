#include <jni.h>

#include "jni_log.h"
#include "replayer.h"

#ifndef _Included_com_dll_touchrepeater_InputReplayer
#define _Included_com_dll_touchrepeater_InputReplayer

JNIEXPORT jlong JNICALL Java_com_dll_touchrepeater_InputReplayer_nativeInit
  (JNIEnv *env, jobject thiz, jstring replayFilePath)
{
	const char *filePath = (*env)->GetStringUTFChars(env, replayFilePath, 0);
	struct replayer* replayer = init(filePath);
	(*env)->ReleaseStringUTFChars(env, replayFilePath, filePath);
	return (jlong)(long) replayer;
}

JNIEXPORT jint JNICALL Java_com_dll_touchrepeater_InputReplayer_nativeReplay
  (JNIEnv *env, jobject thiz, jlong replayerPointer, jint repeatTimes)
{
	struct replayer* replayer = (void *)(long) replayerPointer;
	int replayResult = replay(replayer, repeatTimes);
	destroy(replayer);
	return replayResult;
}

JNIEXPORT void JNICALL Java_com_dll_touchrepeater_InputReplayer_nativeStop
  (JNIEnv *env, jobject thiz, jlong replayerPointer)
{
	struct replayer* replayer = (void *)(long) replayerPointer;
	stop(replayer);
}

#endif
