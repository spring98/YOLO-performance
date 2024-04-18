package com.spring.yolo.performance.model

import android.content.Context
import android.graphics.RectF
import android.media.Image
import com.spring.yolo.performance.ml.Yolov7Tiny640
import org.tensorflow.lite.support.model.Model

class Yolov7TFLite640 (context: Context): Yolo {
    override val rectangles = mutableListOf<RectF>()
    private var model: Yolov7Tiny640 =
        Yolov7Tiny640.newInstance(context, Model.Options.Builder().setDevice(Model.Device.GPU).build())

    override fun preprocess(image: Image): ModelInput {
        val bitmap640 = imageToBitmap(image)
        val tensorBuffer = bitmapToTensorbuffer(bitmap640, resize = 640)

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

        for (i in data.indices step 7) {
            val left: Float = data[i + 1] / 640
            val top: Float = data[i + 2] / 640
            val right: Float = data[i + 3] / 640
            val bottom: Float = data[i + 4] / 640
            val cls = data[i + 5]
            val confidence = data[i + 6]

            if (confidence > 0.5) {
                val l = convertRectRange(left) * width
                val t = top * height
                val r = convertRectRange(right) * width
                val b = bottom * height

                val rect = RectF(l, t, r, b)
                rectangles.add(rect)

                // rect 로그
//                Log.d("spring", "rect: $rect, cls: ${classNames[cls.toInt()]}, conf: $confidence")
            }
        }

        return rectangles
    }
}