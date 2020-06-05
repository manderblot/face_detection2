package com.example.mh.face_detection2

import org.opencv.core.Mat
import org.opencv.core.MatOfRect

/**
 * class DetectionBasedTracker
 */
class DetectionBasedTracker(cascadeName: String, minFaceSize: Int) {
    /**
     * start
     */
    fun start() {
        nativeStart(mNativeObj)
    }

    /**
     * stop
     */
    fun stop() {
        nativeStop(mNativeObj)
    }

    /**
     * setMinFaceSize
     */
    fun setMinFaceSize(size: Int) {
        nativeSetFaceSize(mNativeObj, size)
    }

    /**
     * detect
     */
    fun detect(imageGray: Mat, faces: MatOfRect) {
        nativeDetect(
            mNativeObj,
            imageGray.nativeObjAddr,
            faces.nativeObjAddr
        )
    }

    /**
     * release
     */
    fun release() {
        nativeDestroyObject(mNativeObj)
        mNativeObj = 0
    }

    private var mNativeObj: Long = 0

    private external fun nativeStart(thiz: Long)
    private external fun nativeStop(thiz: Long)
    private external fun nativeSetFaceSize(thiz: Long, size: Int)
    private external fun nativeDetect(
        thiz: Long,
        inputImage: Long,
        faces: Long
    )
    private external fun nativeCreateObject(cascadeName: String,minFaceSize: Int) : Long
    private external fun nativeDestroyObject(thiz: Long)

    init {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize)
    }
}