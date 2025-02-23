package net.satka.bleManager.ble.models

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import net.satka.bleManager.ble.services.BluetoothDeviceConnectionService
import net.satka.bleManager.ble.wrappers.BooleanWrapper
import net.satka.bleManager.ble.wrappers.CharacteristicValueWrapper
import net.satka.bleManager.ble.wrappers.ColorWrapper
import net.satka.bleManager.ble.wrappers.DateTime32BigEndianWrapper
import net.satka.bleManager.ble.wrappers.DateTime32Wrapper
import net.satka.bleManager.ble.wrappers.DateTime64BigEndianWrapper
import net.satka.bleManager.ble.wrappers.DateTime64Wrapper
import net.satka.bleManager.ble.wrappers.DoubleBigEndianWrapper
import net.satka.bleManager.ble.wrappers.DoubleWrapper
import net.satka.bleManager.ble.wrappers.FloatBigEndianWrapper
import net.satka.bleManager.ble.wrappers.FloatWrapper
import net.satka.bleManager.ble.wrappers.HalfFloatBigEndianWrapper
import net.satka.bleManager.ble.wrappers.HalfFloatWrapper
import net.satka.bleManager.ble.wrappers.SInt16BigEndianWrapper
import net.satka.bleManager.ble.wrappers.SInt16Wrapper
import net.satka.bleManager.ble.wrappers.SInt32BigEndianWrapper
import net.satka.bleManager.ble.wrappers.SInt32Wrapper
import net.satka.bleManager.ble.wrappers.SInt64BigEndianWrapper
import net.satka.bleManager.ble.wrappers.SInt64Wrapper
import net.satka.bleManager.ble.wrappers.SInt8BigEndianWrapper
import net.satka.bleManager.ble.wrappers.SInt8Wrapper
import net.satka.bleManager.ble.wrappers.StringWrapper
import net.satka.bleManager.ble.wrappers.TimeBigEndianWrapper
import net.satka.bleManager.ble.wrappers.TimeWrapper
import net.satka.bleManager.ble.wrappers.UInt16BigEndianWrapper
import net.satka.bleManager.ble.wrappers.UInt16Wrapper
import net.satka.bleManager.ble.wrappers.UInt32BigEndianWrapper
import net.satka.bleManager.ble.wrappers.UInt32Wrapper
import net.satka.bleManager.ble.wrappers.UInt64BigEndianWrapper
import net.satka.bleManager.ble.wrappers.UInt64Wrapper
import net.satka.bleManager.ble.wrappers.UInt8BigEndianWrapper
import net.satka.bleManager.ble.wrappers.UInt8Wrapper
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

sealed class CharacteristicModel<T>(
    val uuid: UUID,
    val order: Int,
    val onWrite: (UUID, ByteArray) -> Unit,
    val disabled: Boolean,
    val label: String? = null
) {
    companion object {
        const val DEFAULT_SLIDER_DEBOUNCE_MILLIS = 300L
        const val DEFAULT_FLOAT_SLIDER_STEP = 1f
        const val DEFAULT_INT_SLIDER_STEP = 1
        const val DEFAULT_FLOAT_SLIDER_MIN = 0f
        const val DEFAULT_FLOAT_SLIDER_MAX = 100f
        const val DEFAULT_INT_SLIDER_MIN = 0
        const val DEFAULT_INT_SLIDER_MAX = 100
    }

    protected val valueState: MutableState<T?> = mutableStateOf(null)
    val value: T?
        get() = valueState.value

    abstract val wrapper: CharacteristicValueWrapper<T>

    open fun write(newValue: T) {
        if (!disabled && value != newValue) {
            valueState.value = newValue
            val bytes = wrapper.toBytes(newValue)
            onWrite(uuid, bytes)
        }
    }

    open fun setByteValue(newValue: ByteArray) {
        valueState.value = wrapper.fromBytes(newValue)
    }

    class ServiceName(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    //Read-only texts
    class TitleView(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    class TextView(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    class RichTextView(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    //Text fields
    class TextField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val maxBytes: Int
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    class PasswordField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val maxBytes: Int
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    class PINField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val maxBytes: Int
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    //Integers
    //Unsigned LE
    class UInt8Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UByte,
        val max: UByte
    ) : CharacteristicModel<UByte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UByte> = UInt8Wrapper()
    }

    class UInt16Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UShort,
        val max: UShort
    ) : CharacteristicModel<UShort>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UShort> = UInt16Wrapper()
    }

    class UInt32Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UInt,
        val max: UInt
    ) : CharacteristicModel<UInt>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UInt> = UInt32Wrapper()
    }

    class UInt64Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: ULong,
        val max: ULong
    ) : CharacteristicModel<ULong>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<ULong> = UInt64Wrapper()
    }

    //Signed LE
    class SInt8Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Byte,
        val max: Byte
    ) : CharacteristicModel<Byte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Byte> = SInt8Wrapper()
    }

    class SInt16Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Short,
        val max: Short
    ) : CharacteristicModel<Short>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Short> = SInt16Wrapper()
    }

    class SInt32Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Int,
        val max: Int
    ) : CharacteristicModel<Int>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Int> = SInt32Wrapper()
    }

    class SInt64Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Long,
        val max: Long
    ) : CharacteristicModel<Long>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Long> = SInt64Wrapper()
    }

    //Unsigned BE
    class UInt8beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UByte,
        val max: UByte
    ) : CharacteristicModel<UByte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UByte> = UInt8BigEndianWrapper()
    }

    class UInt16beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UShort,
        val max: UShort
    ) : CharacteristicModel<UShort>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UShort> = UInt16BigEndianWrapper()
    }

    class UInt32beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UInt,
        val max: UInt
    ) : CharacteristicModel<UInt>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UInt> = UInt32BigEndianWrapper()
    }

    class UInt64beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: ULong,
        val max: ULong
    ) : CharacteristicModel<ULong>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<ULong> = UInt64BigEndianWrapper()
    }

    //Signed BE
    class SInt8beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Byte,
        val max: Byte
    ) : CharacteristicModel<Byte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Byte> = SInt8BigEndianWrapper()
    }

    class SInt16beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Short,
        val max: Short
    ) : CharacteristicModel<Short>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Short> = SInt16BigEndianWrapper()
    }

    class SInt32beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Int,
        val max: Int
    ) : CharacteristicModel<Int>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Int> = SInt32BigEndianWrapper()
    }

    class SInt64beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Long,
        val max: Long
    ) : CharacteristicModel<Long>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Long> = SInt64BigEndianWrapper()
    }

    //Floats LE
    class HalfFloatField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = HalfFloatWrapper()
    }

    class FloatField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = FloatWrapper()
    }

    class DoubleField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Double,
        val max: Double
    ) : CharacteristicModel<Double>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Double> = DoubleWrapper()
    }

    //Floats BE
    class HalfFloatBeField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = HalfFloatBigEndianWrapper()
    }

    class FloatBeField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = FloatBigEndianWrapper()
    }

    class DoubleBeField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Double,
        val max: Double
    ) : CharacteristicModel<Double>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Double> = DoubleBigEndianWrapper()
    }

    //Boolean checkbox
    class Checkbox(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<Boolean>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Boolean> = BooleanWrapper()
    }

    //Boolean switch
    class Switch(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<Boolean>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Boolean> = BooleanWrapper()
    }

    //Date LE
    class Date32Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> = DateTime32Wrapper()
    }

    class Date64Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> = DateTime64Wrapper()
    }

    //DateTime LE
    class DateTime32Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> = DateTime32Wrapper()
    }

    class DateTime64Field(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> = DateTime64Wrapper()
    }

    //Date BE
    class Date32beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> =
            DateTime32BigEndianWrapper()
    }

    class Date64beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> =
            DateTime64BigEndianWrapper()
    }

    //DateTime BE
    class DateTime32beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> =
            DateTime32BigEndianWrapper()
    }

    class DateTime64beField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalDateTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalDateTime> =
            DateTime64BigEndianWrapper()
    }

    //Time LE
    class TimeField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalTime> = TimeWrapper()
    }

    //Time BE
    class TimeBeField(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<LocalTime>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<LocalTime> = TimeBigEndianWrapper()
    }

    //Button
    class Button(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?
    ) : CharacteristicModel<UByte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UByte> = UInt8Wrapper()
    }

    //Sliders LE
    class UInt8Slider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UByte,
        val max: UByte,
        val step: UByte,
        val debounceMillis: Long,
    ) : CharacteristicModel<UByte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UByte> = UInt8Wrapper()
    }

    class UInt16Slider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UShort,
        val max: UShort,
        val step: UShort,
        val debounceMillis: Long,
    ) : CharacteristicModel<UShort>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UShort> = UInt16Wrapper()
    }

    class SInt8Slider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Byte,
        val max: Byte,
        val step: Byte,
        val debounceMillis: Long,
    ) : CharacteristicModel<Byte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Byte> = SInt8Wrapper()
    }

    class SInt16Slider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Short,
        val max: Short,
        val step: Short,
        val debounceMillis: Long,
    ) : CharacteristicModel<Short>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Short> = SInt16Wrapper()
    }

    class FloatSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float,
        val step: Float,
        val debounceMillis: Long,
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = FloatWrapper()
    }

    class HalfSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float,
        val step: Float,
        val debounceMillis: Long,
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = HalfFloatWrapper()
    }

    //Sliders BE
    class UInt8beSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UByte,
        val max: UByte,
        val step: UByte,
        val debounceMillis: Long,
    ) : CharacteristicModel<UByte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UByte> = UInt8BigEndianWrapper()
    }

    class UInt16beSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: UShort,
        val max: UShort,
        val step: UShort,
        val debounceMillis: Long,
    ) : CharacteristicModel<UShort>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<UShort> = UInt16BigEndianWrapper()
    }

    class SInt8beSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Byte,
        val max: Byte,
        val step: Byte,
        val debounceMillis: Long,
    ) : CharacteristicModel<Byte>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Byte> = SInt8BigEndianWrapper()
    }

    class SInt16beSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Short,
        val max: Short,
        val step: Short,
        val debounceMillis: Long,
    ) : CharacteristicModel<Short>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Short> = SInt16BigEndianWrapper()
    }

    class FloatBeSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float,
        val step: Float,
        val debounceMillis: Long,
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = FloatBigEndianWrapper()
    }

    class HalfBeSlider(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val min: Float,
        val max: Float,
        val step: Float,
        val debounceMillis: Long,
    ) : CharacteristicModel<Float>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Float> = HalfFloatBigEndianWrapper()
    }

    class ColorPicker(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val showAlphaSlider: Boolean,
    ) : CharacteristicModel<Color>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<Color> = ColorWrapper()
    }

    class Dropdown(
        uuid: UUID,
        order: Int,
        onWrite: (UUID, ByteArray) -> Unit,
        disabled: Boolean,
        label: String?,
        val options: List<String>,
    ) : CharacteristicModel<String>(uuid, order, onWrite, disabled, label) {
        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }

    class Error(
        uuid: UUID,
        onWrite: (UUID, ByteArray) -> Unit,
        errorText: String
    ) : CharacteristicModel<String>(uuid, -1, onWrite, true) {
        init {
            valueState.value = errorText
        }

        override fun write(newValue: String) {

        }

        override fun setByteValue(newValue: ByteArray) {

        }

        override val wrapper: CharacteristicValueWrapper<String> = StringWrapper()
    }
}

fun setupCharacteristicModel(
    uuid: UUID,
    descriptorValueModel: DescriptorValueModel?,
    onWrite: (UUID, ByteArray) -> Unit,
    isWritable: Boolean
): CharacteristicModel<*>? {
    Log.d("Characteristic model", "Creating value: $descriptorValueModel?.valueType")
    if (descriptorValueModel != null) {
        val disabledByDescriptor = descriptorValueModel.configuration?.disabled ?: false
        val disabled = !isWritable || disabledByDescriptor
        val order = descriptorValueModel.configuration?.order ?: 0
        return when (descriptorValueModel.configuration?.type?.lowercase()) {
            "servicename" -> {
                CharacteristicModel.ServiceName(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled
                )
            }

            "titleview" -> {
                CharacteristicModel.TitleView(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabledByDescriptor
                )
            }

            "textview" -> {
                CharacteristicModel.TextView(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabledByDescriptor
                )
            }

            "richtextview" -> {
                CharacteristicModel.RichTextView(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabledByDescriptor
                )
            }

            "text" -> {
                CharacteristicModel.TextField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    maxBytes = (descriptorValueModel.configuration?.maxBytes
                        ?: BluetoothDeviceConnectionService.GATT_MAX_ATTR_LEN).coerceAtLeast(
                        0
                    ).coerceAtMost(BluetoothDeviceConnectionService.GATT_MAX_ATTR_LEN),
                    label = descriptorValueModel.configuration?.label
                )
            }

            "password" -> {
                CharacteristicModel.PasswordField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    maxBytes = (descriptorValueModel.configuration?.maxBytes
                        ?: BluetoothDeviceConnectionService.GATT_MAX_ATTR_LEN).coerceAtLeast(
                        0
                    ).coerceAtMost(BluetoothDeviceConnectionService.GATT_MAX_ATTR_LEN),
                    label = descriptorValueModel.configuration?.label
                )
            }

            "pin" -> {
                CharacteristicModel.PINField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    maxBytes = (descriptorValueModel.configuration?.maxBytes
                        ?: BluetoothDeviceConnectionService.GATT_MAX_ATTR_LEN).coerceAtLeast(
                        0
                    ).coerceAtMost(BluetoothDeviceConnectionService.GATT_MAX_ATTR_LEN),
                    label = descriptorValueModel.configuration?.label
                )
            }

            "check" -> {
                CharacteristicModel.Checkbox(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "switch" -> {
                CharacteristicModel.Switch(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            //LITTLE ENDIAN
            //Unsigned Integers LE
            "uint8" -> {
                CharacteristicModel.UInt8Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUByte()
                            ?: UInt8Wrapper.MIN_VALUE, UInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUByte()
                            ?: UInt8Wrapper.MAX_VALUE, UInt8Wrapper.MAX_VALUE
                    )
                )
            }

            "uint16" -> {
                CharacteristicModel.UInt16Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUShort()
                            ?: UInt16Wrapper.MIN_VALUE, UInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUShort()
                            ?: UInt16Wrapper.MAX_VALUE, UInt16Wrapper.MAX_VALUE
                    )
                )
            }

            "uint32" -> {
                CharacteristicModel.UInt32Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUInt()
                            ?: UInt32Wrapper.MIN_VALUE, UInt32Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUInt()
                            ?: UInt32Wrapper.MAX_VALUE, UInt32Wrapper.MAX_VALUE
                    )
                )
            }

            "uint64" -> {
                CharacteristicModel.UInt64Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toULong()
                            ?: UInt64Wrapper.MIN_VALUE, UInt64Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt
                            ?: UInt64Wrapper.MAX_VALUE, UInt64Wrapper.MAX_VALUE
                    )
                )
            }

            //Signed Integers LE
            "sint8" -> {
                CharacteristicModel.SInt8Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toByte()
                            ?: SInt8Wrapper.MIN_VALUE, SInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toByte()
                            ?: SInt8Wrapper.MAX_VALUE, SInt8Wrapper.MAX_VALUE
                    )
                )
            }

            "sint16" -> {
                CharacteristicModel.SInt16Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toShort()
                            ?: SInt16Wrapper.MIN_VALUE, SInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toShort()
                            ?: SInt16Wrapper.MAX_VALUE, SInt16Wrapper.MAX_VALUE
                    )
                )
            }

            "sint32" -> {
                CharacteristicModel.SInt32Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toInt()
                            ?: SInt32Wrapper.MIN_VALUE, SInt32Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toInt()
                            ?: SInt32Wrapper.MAX_VALUE, SInt32Wrapper.MAX_VALUE
                    )
                )
            }

            "sint64" -> {
                CharacteristicModel.SInt64Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt
                            ?: SInt64Wrapper.MIN_VALUE, SInt64Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toLong()
                            ?: SInt64Wrapper.MAX_VALUE, SInt64Wrapper.MAX_VALUE
                    )
                )
            }

            //Floats LE
            "half" -> {
                CharacteristicModel.HalfFloatField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: HalfFloatWrapper.MIN_VALUE, HalfFloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: HalfFloatWrapper.MAX_VALUE, HalfFloatWrapper.MAX_VALUE
                    )
                )
            }

            "float" -> {
                CharacteristicModel.FloatField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: FloatWrapper.MIN_VALUE, FloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: FloatWrapper.MAX_VALUE, FloatWrapper.MAX_VALUE
                    )
                )
            }

            "double" -> {
                CharacteristicModel.DoubleField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat
                            ?: DoubleWrapper.MIN_VALUE, DoubleWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat
                            ?: DoubleWrapper.MAX_VALUE, DoubleWrapper.MAX_VALUE
                    )
                )
            }

            //BIG ENDIAN
            //Unsigned Integers LE
            "uint8be" -> {
                CharacteristicModel.UInt8beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUByte()
                            ?: UInt8Wrapper.MIN_VALUE, UInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUByte()
                            ?: UInt8Wrapper.MAX_VALUE, UInt8Wrapper.MAX_VALUE
                    )
                )
            }

            "uint16be" -> {
                CharacteristicModel.UInt16beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUShort()
                            ?: UInt16Wrapper.MIN_VALUE, UInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUShort()
                            ?: UInt16Wrapper.MAX_VALUE, UInt16Wrapper.MAX_VALUE
                    )
                )
            }

            "uint32be" -> {
                CharacteristicModel.UInt32beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUInt()
                            ?: UInt32Wrapper.MIN_VALUE, UInt32Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUInt()
                            ?: UInt32Wrapper.MAX_VALUE, UInt32Wrapper.MAX_VALUE
                    )
                )
            }

            "uint64be" -> {
                CharacteristicModel.UInt64beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toULong()
                            ?: UInt64Wrapper.MIN_VALUE, UInt64Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt
                            ?: UInt64Wrapper.MAX_VALUE, UInt64Wrapper.MAX_VALUE
                    )
                )
            }

            //Signed Integers LE
            "sint8be" -> {
                CharacteristicModel.SInt8beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toByte()
                            ?: SInt8Wrapper.MIN_VALUE, SInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toByte()
                            ?: SInt8Wrapper.MAX_VALUE, SInt8Wrapper.MAX_VALUE
                    )
                )
            }

            "sint16be" -> {
                CharacteristicModel.SInt16beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toShort()
                            ?: SInt16Wrapper.MIN_VALUE, SInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toShort()
                            ?: SInt16Wrapper.MAX_VALUE, SInt16Wrapper.MAX_VALUE
                    )
                )
            }

            "sint32be" -> {
                CharacteristicModel.SInt32beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toInt()
                            ?: SInt32Wrapper.MIN_VALUE, SInt32Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toInt()
                            ?: SInt32Wrapper.MAX_VALUE, SInt32Wrapper.MAX_VALUE
                    )
                )
            }

            "sint64be" -> {
                CharacteristicModel.SInt64beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt
                            ?: SInt64Wrapper.MIN_VALUE, SInt64Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toLong()
                            ?: SInt64Wrapper.MAX_VALUE, SInt64Wrapper.MAX_VALUE
                    )
                )
            }

            //Floats BE
            "halfbe" -> {
                CharacteristicModel.HalfFloatBeField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: HalfFloatWrapper.MIN_VALUE, HalfFloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: HalfFloatWrapper.MAX_VALUE, HalfFloatWrapper.MAX_VALUE
                    )
                )
            }

            "floatbe" -> {
                CharacteristicModel.FloatBeField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: FloatWrapper.MIN_VALUE, FloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: FloatWrapper.MAX_VALUE, FloatWrapper.MAX_VALUE
                    )
                )
            }

            "doublebe" -> {
                CharacteristicModel.DoubleBeField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat
                            ?: DoubleWrapper.MIN_VALUE, DoubleWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat
                            ?: DoubleWrapper.MAX_VALUE, DoubleWrapper.MAX_VALUE
                    )
                )
            }

            //Button
            "button" -> {
                CharacteristicModel.Button(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            //DATE AND TIME
            //LE

            "time" -> {
                CharacteristicModel.TimeField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "date32" -> {
                CharacteristicModel.Date32Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "datetime32" -> {
                CharacteristicModel.DateTime32Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "date64" -> {
                CharacteristicModel.Date64Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "datetime64" -> {
                CharacteristicModel.DateTime64Field(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            //BE
            "timebe" -> {
                CharacteristicModel.TimeBeField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "date32be" -> {
                CharacteristicModel.Date32beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "datetime32be" -> {
                CharacteristicModel.DateTime32beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "date64be" -> {
                CharacteristicModel.Date64beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            "datetime64be" -> {
                CharacteristicModel.DateTime64beField(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label
                )
            }

            //Sliders LE
            "floatslider" -> {
                CharacteristicModel.FloatSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MIN, FloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MAX, FloatWrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepFloat?.toFloat()
                        ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_STEP,
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "halfslider" -> {
                CharacteristicModel.HalfSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MIN,
                        HalfFloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MAX,
                        HalfFloatWrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepFloat?.toFloat()
                        ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_STEP,
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            //Sliders BE
            "floatsliderbe" -> {
                CharacteristicModel.FloatBeSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MIN, FloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MAX, FloatWrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepFloat?.toFloat()
                        ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_STEP,
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "halfsliderbe" -> {
                CharacteristicModel.HalfBeSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MIN,
                        HalfFloatWrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxFloat?.toFloat()
                            ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_MAX,
                        HalfFloatWrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepFloat?.toFloat()
                        ?: CharacteristicModel.DEFAULT_FLOAT_SLIDER_STEP,
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            //INT sliders LE
            "uint8slider" -> {
                CharacteristicModel.UInt8Slider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toUByte(),
                        UInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toUByte(),
                        UInt8Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toUByte()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toUByte(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "uint16slider" -> {
                CharacteristicModel.UInt16Slider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toUShort(),
                        UInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toUShort(),
                        UInt16Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toUShort()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toUShort(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "sint8slider" -> {
                CharacteristicModel.SInt8Slider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toByte(),
                        SInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toByte(),
                        SInt8Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toByte()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toByte(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "sint16slider" -> {
                CharacteristicModel.SInt16Slider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toShort(),
                        SInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toShort(),
                        SInt16Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toShort()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toShort(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            //INT sliders BE
            "uint8sliderbe" -> {
                CharacteristicModel.UInt8beSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toUByte(),
                        UInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toUByte(),
                        UInt8Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toUByte()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toUByte(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "uint16sliderbe" -> {
                CharacteristicModel.UInt16beSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toUShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toUShort(),
                        UInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toUShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toUShort(),
                        UInt16Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toUShort()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toUShort(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "sint8sliderbe" -> {
                CharacteristicModel.SInt8beSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toByte(),
                        SInt8Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toByte()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toByte(),
                        SInt8Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toByte()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toByte(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "sint16sliderbe" -> {
                CharacteristicModel.SInt16beSlider(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    min = maxOf(
                        descriptorValueModel.configuration?.minInt?.toShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MIN.toShort(),
                        SInt16Wrapper.MIN_VALUE
                    ),
                    max = minOf(
                        descriptorValueModel.configuration?.maxInt?.toShort()
                            ?: CharacteristicModel.DEFAULT_INT_SLIDER_MAX.toShort(),
                        SInt16Wrapper.MAX_VALUE
                    ),
                    step = descriptorValueModel.configuration?.stepInt?.toShort()
                        ?: CharacteristicModel.DEFAULT_INT_SLIDER_STEP.toShort(),
                    debounceMillis = CharacteristicModel.DEFAULT_SLIDER_DEBOUNCE_MILLIS
                )
            }

            "color" -> {
                CharacteristicModel.ColorPicker(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    showAlphaSlider = descriptorValueModel.configuration?.alphaSlider ?: false
                )
            }

            "dropdown" -> {
                CharacteristicModel.Dropdown(
                    uuid = uuid,
                    order = order,
                    onWrite = onWrite,
                    disabled = disabled,
                    label = descriptorValueModel.configuration?.label,
                    options = descriptorValueModel.configuration?.options ?: listOf()
                )
            }

            null -> CharacteristicModel.Error(
                uuid = uuid,
                onWrite = onWrite,
                errorText = "${uuid}\nInvalid Descriptor Configuration:\n${descriptorValueModel.errorText}"
            )

            else -> CharacteristicModel.Error(
                uuid = uuid,
                onWrite = onWrite,
                errorText = "Unknown Type: ${descriptorValueModel.configuration?.type?.lowercase()}"
            )
        }
    }

    return null
}

