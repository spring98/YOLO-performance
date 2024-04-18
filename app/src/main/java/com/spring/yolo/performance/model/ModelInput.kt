package com.spring.yolo.performance.model

import org.pytorch.Tensor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

sealed class ModelInput {
    data class TensorInput(val tensor: Tensor) : ModelInput()
    data class TensorBufferInput(val tensorBuffer: TensorBuffer) : ModelInput()
}