/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_opencv_samples_fd_DetectionBasedTracker */

#ifndef _Included_com_example_mh_face_detection2_DetectionBasedTracker
#define _Included_com_example_mh_face_detection2_DetectionBasedTracker
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_opencv_samples_fd_DetectionBasedTracker
 * Method:    nativeCreateObject
 * Signature: (Ljava/lang/String;F)J
 */
JNIEXPORT jlong JNICALL
Java_com_example_mh_face_1detection2_DetectionBasedTracker_nativeCreateObject(JNIEnv *,
                                                                              jobject,
                                                                              jstring,
                                                                              jint);
/*
 * Class:     org_opencv_samples_fd_DetectionBasedTracker
 * Method:    nativeDestroyObject
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_com_example_mh_face_1detection2_DetectionBasedTracker_nativeDestroyObject(JNIEnv *,
                                                                               jobject,
                                                                               jlong);

/*
 * Class:     org_opencv_samples_fd_DetectionBasedTracker
 * Method:    nativeStart
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_example_mh_face_1detection2_DetectionBasedTracker_nativeStart(JNIEnv *,
        jobject,
        jlong);

/*
 * Class:     org_opencv_samples_fd_DetectionBasedTracker
 * Method:    nativeStop
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_example_mh_face_1detection2_DetectionBasedTracker_nativeStop(
        JNIEnv *, jobject, jlong);

  /*
   * Class:     org_opencv_samples_fd_DetectionBasedTracker
   * Method:    nativeSetFaceSize
   * Signature: (JI)V
   */
JNIEXPORT void JNICALL Java_com_example_mh_face_1detection2_DetectionBasedTracker_nativeSetFaceSize(
        JNIEnv *,
        jobject,
        jlong,jint);

/*
 * Class:     org_opencv_samples_fd_DetectionBasedTracker
 * Method:    nativeDetect
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_com_example_mh_face_1detection2_DetectionBasedTracker_nativeDetect(
        JNIEnv *, jobject,
        jlong,
        jlong,
        jlong);

#ifdef __cplusplus
}
#endif
#endif
