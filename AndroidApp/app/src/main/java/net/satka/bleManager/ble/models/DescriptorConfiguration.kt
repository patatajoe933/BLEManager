package net.satka.bleManager.ble.models

import com.google.gson.annotations.SerializedName //Kvůli minifikaci

data class DescriptorConfiguration (
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("disabled")
    val disabled: Boolean? = null,
    @SerializedName("order")
    val order: Int? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("minInt")
    val minInt: Long? = null,
    @SerializedName("maxInt")
    val maxInt: ULong? = null,
    @SerializedName("minFloat")
    val minFloat: Double? = null,
    @SerializedName("maxFloat")
    val maxFloat: Double? = null,
    @SerializedName("stepFloat")
    val stepFloat: Double? = null,
    @SerializedName("stepInt")
    val stepInt: UInt? = null,
    @SerializedName("alphaSlider")
    val alphaSlider: Boolean? = null,
    @SerializedName("options")
    val options: List<String>? = null,
    @SerializedName("maxBytes")
    val maxBytes: Int? = null,
)