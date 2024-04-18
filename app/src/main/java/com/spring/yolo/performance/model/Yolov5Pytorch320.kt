package com.spring.yolo.performance.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.media.Image
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.PyTorchAndroid
import org.pytorch.torchvision.TensorImageUtils

class Yolov5Pytorch320(context: Context): Yolo {
    private var model: Module = PyTorchAndroid.loadModuleFromAsset(context.assets, "yolov5s-320.torchscript")
    override val rectangles = mutableListOf<RectF>()

    override fun preprocess(image: Image): ModelInput {
        val bitmap640 = imageToBitmap(image)

        val matrix = Matrix()
        matrix.postRotate(90f)

        val bitmap640_90 = Bitmap.createBitmap(bitmap640, 0 ,0, bitmap640.width, bitmap640.height, matrix, true)
        val bitmap320 = Bitmap.createScaledBitmap(bitmap640_90, 320, 320, true)

        // Bitmap To Tensor
        return ModelInput.TensorInput(
            TensorImageUtils.bitmapToFloat32Tensor(
                bitmap320,
                // NO_MEAN_RGB
                floatArrayOf(0.0f, 0.0f, 0.0f),
                // NO_STD_RGB
                floatArrayOf(1.0f, 1.0f, 1.0f)
            )
        )
    }

    override fun inference(modelInput: ModelInput): FloatArray {
        val input = when (modelInput) {
            is ModelInput.TensorInput -> IValue.from(modelInput.tensor)
            is ModelInput.TensorBufferInput -> null
        }

        // 추론
        val outputTuple = model.forward(input!!).toTuple()
        return outputTuple[0].toTensor().dataAsFloatArray
    }

    override fun filtering(data: FloatArray, width: Int, height: Int): MutableList<RectF> {
        rectangles.clear()

        val DETECTION_SIZE = 85

        for (i in data.indices step DETECTION_SIZE) {
            val x: Float = data[i] / 320
            val y: Float = data[i + 1] / 320
            val w: Float = data[i + 2] / 320
            val h: Float = data[i + 3] / 320
            val confidence = data[i + 4]

            val left: Float = x - w/2
            val top: Float = y - h/2
            val right: Float = x + w/2
            val bottom: Float = y + h/2

            var maxClassScore = data[i + 5]
            var cls = 0
            for (j in 0 until DETECTION_SIZE - 5) {
                if (data[i + 5 + j] > maxClassScore) {
                    maxClassScore = data[i + 5 + j]
                    cls = j
                }
            }

            if (confidence > 0.5) {
                val l = convertRectRange(left) * width
                val t = top * height
                val r = convertRectRange(right) * width
                val b = bottom * height

                val rect = RectF(l, t, r, b)
                rectangles.add(rect)

                // rect 로그
//                Log.d("spring", "rect: ${rect}, conf: $confidence, cls: ${classNames[cls]}")
            }
        }

        return rectangles
    }

}