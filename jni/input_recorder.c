#include <jni.h>

#include "jni_log.h"
#include "recorder.h"

JNIEXPORT void JNICALL Java_com_dll_touchrepeater_InputRecorder_nativeStop(JNIEnv *env,
		jobject thiz, jlong recorderPointer)
{
	struct recorder* recorder = (void *) (long) recorderPointer;
	stop(recorder);
}

JNIEXPORT jlong JNICALL Java_com_dll_touchrepeater_InputRecorder_nativeInit(JNIEnv *env,
		jobject thiz, jstring recordFilePath)
{
	const char* filePath = (*env)->GetStringUTFChars(env, recordFilePath, 0);
	struct recorder* recorder = init(filePath);

	(*env)->ReleaseStringUTFChars(env, recordFilePath, filePath);
	return (jlong) (long) recorder;
}

JNIEXPORT jint JNICALL Java_com_dll_touchrepeater_InputRecorder_nativeRecord(JNIEnv *env,
		jobject thiz, jlong recorderPointer)
{
	struct recorder* recorder = (void *) (long) recorderPointer;
	int recordResult = record(recorder);
	destroy(recorder);
	return recordResult;
}

