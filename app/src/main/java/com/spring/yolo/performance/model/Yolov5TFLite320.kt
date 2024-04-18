package com.spring.yolo.performance.model

import android.content.Context
import android.graphics.RectF
import android.media.Image
import com.spring.yolo.performance.ml.Yolov5s320
import org.tensorflow.lite.support.model.Model

class Yolov5TFLite320(context: Context): Yolo {
    private var model: Yolov5s320 =
        Yolov5s320.newInstance(context, Model.Options.Builder().setDevice(Model.Device.GPU).build())
    override val rectangles = mutableListOf<RectF>()

    override fun preprocess(image: Image): ModelInput {
        val bitmap640 = imageToBitmap(image)
        val tensorBuffer = bitmapToTensorbuffer(bitmap640, resize = 320)

        return ModelInput.TensorBufferInput(tensorBuffer)
    }

    override fun inference(modelInput: ModelInput): FloatArray {
        val input = when (modelInput) {
            is ModelInput.TensorInput -> null
            is ModelInput.TensorBufferInput -> modelInput.tensorBuffer
        }

        // 추론
        val outputs = model.process(input!!)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        return outputFeature0.floatArray
    }

    override fun filtering(data: FloatArray, width: Int, height: Int): MutableList<RectF> {
        rectangles.clear()

        val DETECTION_SIZE = 85

        for (i in data.indices step DETECTION_SIZE) {
            val x: Float = data[i]
            val y: Float = data[i + 1]
            val w: Float = data[i + 2]
            val h: Float = data[i + 3]
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