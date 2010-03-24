#include "org_ametro_jni_Natives.h"

JNIEXPORT jobjectArray JNICALL Java_org_ametro_jni_Natives_SplitCsvString(
		JNIEnv *env, jclass thiz, jstring source, jchar separator) {

	if(source == 0) return 0;
	jsize length = (*env)->GetStringLength(env, source);
	if (length > 0) {
		const jchar* str = (*env)->GetStringChars(env, source, 0);
		jsize i;
		jsize count = 1;
		for (i = 0; i < length; i++) {
			if (str[i] == separator) {
				count++;
			}
		}
		jobjectArray res = (*env)->NewObjectArray(env, count,
				(*env)->FindClass(env, "java/lang/String"),
				(*env)->NewStringUTF(env, ""));

		const jchar* start = str;
		jsize position = 0;
		jsize len = 0;
		for (i = 0; i < length; i++) {
			if (str[i] == separator) {
				(*env)->SetObjectArrayElement(env, res, position,
						(*env)->NewString(env, start, len));
				position++;
				len = -1;
				start = str + i + 1;
			}
			len++;
		}
		if (len > 0) {
			(*env)->SetObjectArrayElement(env, res, position,
					(*env)->NewString(env, start, len));
		}

		(*env)->ReleaseStringChars(env, source, str);
		return res;
	}
	return 0;
}

JNIEXPORT jobjectArray JNICALL Java_org_ametro_jni_Natives_getVisibleRenderElements(
		JNIEnv *env, jclass thiz, jobjectArray elements, jobjectArray views,
		jint filter) {
	return elements;
}
