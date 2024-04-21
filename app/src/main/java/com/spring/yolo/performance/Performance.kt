package com.spring.yolo.performance

import com.google.gson.annotations.SerializedName

data class Performances(
    @SerializedName("temperature_data") val performances: List<Performance>
)

data class Performance(
    @SerializedName(value = "time") var time : String,
    @SerializedName(value = "temperature") var temperature : String,
    @SerializedName(value = "frame") var frame : String,
)
