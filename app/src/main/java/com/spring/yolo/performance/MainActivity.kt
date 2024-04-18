package com.spring.yolo.performance

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import com.spring.yolo.performance.model.Yolo
import com.spring.yolo.performance.model.Yolov5Pytorch320
import com.spring.yolo.performance.model.Yolov5TFLite320
import com.spring.yolo.performance.model.Yolov7TFLite640

@Suppress("LocalVariableName", "PrivatePropertyName")
@ExperimentalGetImage class MainActivity : AppCompatActivity() {
    private val TAG: String = "spring"
    private lateinit var preview: PreviewView
    private lateinit var overlayView: OverlayView

    private var model: Yolo? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        supportActionBar?.hide()
        preview = findViewById(R.id.viewFinder)
        overlayView = findViewById(R.id.overlayView)

        model = Yolov5Pytorch320(baseContext)
//        model = Yolov5TFLite320(baseContext)
//        model = Yolov7TFLite640(baseContext)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(preview.surfaceProvider)
                }

            val imageAnalysis = setupImageAnalysis()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll() // Unbind all use cases before rebinding
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupImageAnalysis(): ImageAnalysis {
        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
//            .setTargetResolution(Size(640, 480)) // Set the resolution you need
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Only analyze the latest image
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->

            analyzeImage(imageProxy)

            // 이미지 메모리 해제
            imageProxy.close()
        }

        return imageAnalysis
    }

    private fun analyzeImage(image: ImageProxy) {
        if (model == null) return

        // 처리 시작 시간 측정
        val start = System.currentTimeMillis()

        image.image?.run {
            /**
             * 1. Image Preprocess: Camera Image > bitmap > TensorImage or Tensor
             * 2. Image Inference: TensorImage or Tensor > [Model Inference] > FloatArray
             * 3. Result Data Filtering: FloatArray > Rect, Confidence, ClassName
             */
            val tensor = model!!.preprocess(this)
            val results = model!!.inference(tensor)
            val rectangles = model!!.filtering(results, preview.width, preview.height)

            // OverlayView 에 사각형 그리기 요청
            overlayView.drawRectangles(rectangles)

            // 총 걸린 시간 및 frame 계산
            val elapseTime = System.currentTimeMillis() - start
            Log.d(TAG, "$elapseTime ms, ${1000 / elapseTime} fps")

            // 이미지 메모리 해제
            image.close()
        }
    }
}