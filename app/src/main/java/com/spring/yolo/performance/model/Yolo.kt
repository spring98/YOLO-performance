package com.spring.yolo.performance.model

import android.graphics.*
import android.media.Image
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

interface Yolo {
    val rectangles: MutableList<RectF>

    fun preprocess(image: Image): ModelInput

    fun inference(modelInput: ModelInput): FloatArray

    fun filtering(data: FloatArray, width: Int, height: Int): MutableList<RectF>

    fun imageToBitmap(image: Image): Bitmap {
        // RGBA888 를 Bitmap 으로 변환 (640*480)
        val bitmap = rgba8888ToBitmap(image)

        // 검정 패딩 크기
        val paddingSize = 80

        // 패딩을 주어 640*480 에서 640*640 로 정사각형 모양으로 변환
        return bitmap.addPadding(Color.BLACK, 0, paddingSize, 0, paddingSize)
    }

    fun bitmapToTensorbuffer(bitmap640: Bitmap, resize: Int): TensorBuffer {
        // TensorImage 객체 생성
        val tensorImage = TensorImage(DataType.FLOAT32)

        // bitmap 을 TensorImage 에 로드
        tensorImage.load(bitmap640)

        // 이미지 처리기 생성
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(resize, resize, ResizeOp.ResizeMethod. BILINEAR))
            .add(Rot90Op(-1))  // Adjust image rotation
            .add(NormalizeOp(0f, 255f))         // Normalize image
            .build()

        // 이미지 처리
        val processedImage = imageProcessor.process(tensorImage)

        // TensorBuffer 생성
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 3, resize, resize), DataType.FLOAT32)
        inputFeature0.loadBuffer(processedImage.buffer) // Load image buffer

        return inputFeature0
    }


    fun rgba8888ToBitmap(image: Image): Bitmap {
        val width = image.width
        val height = image.height
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride

        val bitmapBuffer: ByteBuffer
        if (rowStride == width * pixelStride) {
            bitmapBuffer = buffer
        }

        else {
            bitmapBuffer = ByteBuffer.allocateDirect(width * height * pixelStride)
            for (i in 0 until height) {
                val rowStart = i * rowStride
                val rowEnd = rowStart + width * pixelStride
                buffer.position(rowStart)
                buffer.limit(rowEnd)
                bitmapBuffer.put(buffer.slice()) // Copy the buffer slice to correct for padding
            }
            bitmapBuffer.rewind()
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(bitmapBuffer)

        return bitmap
    }


    fun Bitmap.addPadding(
        color: Int = Color.BLACK,
        left: Int = 0,
        top: Int = 0,
        right: Int = 0,
        bottom: Int = 0
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            width + left + right, // width in pixels
            height + top + bottom, // height in pixels
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawRect(
                Rect(left, top, bitmap.width - right, bitmap.height - bottom),
                this
            )
        }
        Paint().apply {
            canvas.drawBitmap(
                this@addPadding, // bitmap
                0f + left, // left
                0f + top, // top
                this // paint
            )
        }
        return bitmap
    }

    fun convertRectRange(x: Float): Float {
        val data = (2 * x - 0.5).toFloat()

        return when {
            data < 0 -> 0f
            data > 1 -> 1f
            else -> data
        }
    }

    val classNames: Array<String>
        get() = arrayOf(
            "person",
            "bicycle",
            "car",
            "motorcycle",
            "airplane",
            "bus",
            "train",
            "truck",
            "boat",
            "traffic light",
            "fire hydrant",
            "stop sign",
            "parking meter",
            "bench",
            "bird",
            "cat",
            "dog",
            "horse",
            "sheep",
            "cow",
            "elephant",
            "bear",
            "zebra",
            "giraffe",
            "backpack",
            "umbrella",
            "handbag",
            "tie",
            "suitcase",
            "frisbee",
            "skis",
            "snowboard",
            "sports ball",
            "kite",
            "baseball bat",
            "baseball glove",
            "skateboard",
            "surfboard",
            "tennis racket",
            "bottle",
            "wine glass",
            "cup",
            "fork",
            "knife",
            "spoon",
            "bowl",
            "banana",
            "apple",
            "sandwich",
            "orange",
            "broccoli",
            "carrot",
            "hot dog",
            "pizza",
            "donut",
            "cake",
            "chair",
            "couch",
            "potted plant",
            "bed",
            "dining table",
            "toilet",
            "tv",
            "laptop",
            "mouse",
            "remote",
            "keyboard",
            "cell phone",
            "microwave",
            "oven",
            "toaster",
            "sink",
            "refrigerator",
            "book",
            "clock",
            "vase",
            "scissors",
            "teddy bear",
            "hair drier",
            "toothbrush"
        )
}