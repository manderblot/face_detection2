package com.example.mh.face_detection2

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.*
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.*
import java.util.*
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar

class MainActivity : CameraActivity() , CameraBridgeViewBase.CvCameraViewListener2  {

    init {
        mDetectorName[JAVA_DETECTOR] = "Java"
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)"
    }

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    log_d("OpenCV loaded successfully")
                    System.loadLibrary("detection_based_tracker")
                    setupDetector()
                    val idc = cameraViewList
                    mOpenCvCameraView.setCameraIndex(0)
                    mOpenCvCameraView.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        mOpenCvCameraView = camera_view
        mOpenCvCameraView.visibility = CameraBridgeViewBase.VISIBLE
        mOpenCvCameraView.setCvCameraViewListener(this)

        button_options.setOnClickListener {
            val snackbar = Snackbar.make(it,"",Snackbar.LENGTH_SHORT)
            snackbar.duration = 10000
            snackbar.setAction("yes",View.OnClickListener {
                if (mOpenCvCameraView.id == 0) {
                    mOpenCvCameraView.setCameraIndex(1)
                } else {
                    mOpenCvCameraView.setCameraIndex(0)
                }
                snackbar.dismiss()
            })
            snackbar.show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            log_d( "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
        } else {
            log_d("OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    protected override fun getCameraViewList(): MutableList<out CameraBridgeViewBase> {
        return Collections.singletonList(mOpenCvCameraView)
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }

    override fun onCameraViewStarted (width : Int, height : Int) {
        mGray = Mat()
        mRgba = Mat()
    }

    override fun onCameraViewStopped() {
        mGray.release()
        mRgba.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        mRgba = inputFrame!!.rgba()
        mGray = inputFrame!!.gray()

        if (mAbsoluteFaceSize == 0) {
            val height : Int = mGray.rows()
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize)
            }
            mNativeDetector!!.setMinFaceSize(mAbsoluteFaceSize)
        }

        val faces : MatOfRect = MatOfRect()
        if (mDetectionType == JAVA_DETECTOR) {
            mJavaDetector?.detectMultiScale(mGray, faces, 1.1, 2, 2,
                Size(mAbsoluteFaceSize.toDouble(), mAbsoluteFaceSize.toDouble()), Size()
            )
        } else if (mDetectionType == NATIVE_DETECTOR) {
            mNativeDetector?.detect(mGray, faces)
        } else {
            log_d("Detection method is not selected")
        }

        val facesArray = faces.toArray()
        var resRgba = Mat()
        for (i in 0..facesArray.size-1) {
            resRgba = Mat(mRgba,facesArray[i])
            Imgproc.blur(resRgba,resRgba,Size(30.0,30.0))
//            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, FACE_RECT_THICKNESS)
        }

        return mRgba
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        mItemFace50 = menu.findItem(R.id.size_50)
        mItemFace40 = menu.findItem(R.id.size_40)
        mItemFace30 = menu.findItem(R.id.size_30)
        mItemFace20 = menu.findItem(R.id.size_20)
        mItemType = menu.add(mDetectorName[mDetectionType])
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item) {
            mItemFace50 -> {
                mRelativeFaceSize = 0.5f
                mAbsoluteFaceSize = 0
                true
            }
            mItemFace40 -> {
                mRelativeFaceSize = 0.4f
                mAbsoluteFaceSize = 0
                true
            }
            mItemFace30 -> {
                mRelativeFaceSize = 0.3f
                mAbsoluteFaceSize = 0
                true
            }
            mItemFace20 -> {
                mRelativeFaceSize = 0.2f
                mAbsoluteFaceSize = 0
                true
            }
            mItemType -> {
                val tmpDetectorType : Int = (mDetectionType + 1) % mDetectorName.size
                val name : String = mDetectorName[tmpDetectorType]!!
                item.setTitle(name)
                setDetectorType(tmpDetectorType)
                showToast(name)
                log_d("type = " + tmpDetectorType + ", name = " + name)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun setDetectorType (type : Int) {
        mDetectionType = type
        if (type == NATIVE_DETECTOR) {
            log_d("Detection Based Tracker enabled")
            mNativeDetector?.start()
        } else {
            log_d("Cascade detector enabled")
            mNativeDetector?.stop()
        }
    }

    private fun setupDetector () {
        val cascadeDir : File = createCascadeDir(CASCADE_DIR_NAME)
        val cascadeFile : File = createCascadeFile(cascadeDir, CASCADE_FILE_NAME)

        val inputStream : InputStream = openRawResource(RES_ID_CASCADE_FILE)
        val outputStream : FileOutputStream? = getFileOutputStream(cascadeFile)
        if (outputStream != null) {
            copyStream(inputStream, outputStream)
        }

        setupJavaDetector(cascadeFile)
        setupNativeDetector(cascadeFile)

        cascadeDir.delete()
    }

    private fun createCascadeDir (dirName : String) : File {
        val cascadeDir = getDir(dirName, Context.MODE_PRIVATE)
        return cascadeDir
    }

    private fun createCascadeFile (cascadeDir : File, fileName : String) : File {
        val cascadeFile : File = File(cascadeDir, fileName)
        return cascadeFile
    }

    private fun openRawResource (res_d : Int) : InputStream{
        val inputStream : InputStream = resources.openRawResource(res_d)
        return inputStream
    }

    private fun getFileOutputStream (cascadeFile : File) : FileOutputStream? {
        var fileOutputStream : FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(cascadeFile)
        }catch (e : FileNotFoundException) {
            e.printStackTrace()
        }
        return fileOutputStream
    }

    private fun copyStream (inputStream: InputStream,outputStream: OutputStream) {
        try {
            val buffer = ByteArray(COPY_BUF_SIZE)
            var bytesRead : Int = inputStream.read(buffer)
            while (bytesRead != COPY_EOF) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }
        } catch (e : IOException) {
            e.printStackTrace()
        }
        try {
            inputStream.close()
            outputStream.close()
        } catch (e : IOException) {

        }
    }

    private fun setupJavaDetector (cascadeFile: File) {
        val cascadeFilePath : String = cascadeFile.absolutePath
        mJavaDetector = CascadeClassifier(cascadeFilePath)
        if (mJavaDetector!!.empty()) {
            log_d("Failed to load cascade classifier")
            mJavaDetector = null
        } else {
            log_d("Loaded cascade classifier from " + cascadeFilePath)
        }
    }

    private fun setupNativeDetector (cascadeFile: File) {
        val cascadeFilePath : String = cascadeFile.absolutePath
        mNativeDetector = DetectionBasedTracker(cascadeFilePath, 0)
    }

    private fun log_d (msg : String) {
        if (D) Log.d(TAG, TAG_SUB + " " + msg)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val D : Boolean = true
        private val TAG : String = "OpenCV"
        private val TAG_SUB : String = "MainActivity"

        private val FACE_RECT_COLOR : Scalar = Scalar(0.0,1.0,0.0,1.0)
        private val FACE_RECT_THICKNESS : Int = 3

        val JAVA_DETECTOR : Int = 0
        val NATIVE_DETECTOR : Int = 1

        private val RES_ID_CASCADE_FILE : Int = R.raw.lbpcascade_frontalface
        private val CASCADE_DIR_NAME : String = "cascade"
        private val CASCADE_FILE_NAME : String = "lbpcascade_frontalface.xml"
        private val COPY_BUF_SIZE : Int = 4096
        private val COPY_EOF : Int = -1

        private lateinit var mItemFace50 : MenuItem
        private lateinit var mItemFace40 : MenuItem
        private lateinit var mItemFace30 : MenuItem
        private lateinit var mItemFace20 : MenuItem
        private lateinit var mItemType : MenuItem

        private lateinit var mRgba : Mat
        private lateinit var mGray : Mat
        private lateinit var mCascadeFile : File
        private var mJavaDetector : CascadeClassifier? = null
        private var mNativeDetector : DetectionBasedTracker? = null

        private var mDetectionType : Int = JAVA_DETECTOR
        private val mDetectorName : Array<String?> = arrayOfNulls(2)

        private var mRelativeFaceSize : Float = 0.2f
        private var mAbsoluteFaceSize : Int = 0

        private lateinit var mOpenCvCameraView : CameraBridgeViewBase
    }

}
