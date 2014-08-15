LOCAL_PATH:= $(call my-dir)


include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE    := inputrecorder
LOCAL_SRC_FILES := input_recorder.c
	LOCAL_MODULE_PATH := $(TARGET_OUT_EXECUTABLES)
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE    := inputreplayer
LOCAL_SRC_FILES := input_replayer.c
	LOCAL_MODULE_PATH := $(TARGET_OUT_EXECUTABLES)
include $(BUILD_SHARED_LIBRARY)