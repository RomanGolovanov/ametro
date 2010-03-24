LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ametro
LOCAL_SRC_FILES := org_ametro_jni_Natives.c

include $(BUILD_SHARED_LIBRARY)