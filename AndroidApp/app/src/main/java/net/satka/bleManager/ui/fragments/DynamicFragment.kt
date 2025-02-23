package net.satka.bleManager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import net.satka.bleManager.R
import net.satka.bleManager.ble.models.CharacteristicModel
import net.satka.bleManager.ui.components.SatkaButton
import net.satka.bleManager.ui.components.SatkaCheckbox
import net.satka.bleManager.ui.components.SatkaColorPicker
import net.satka.bleManager.ui.components.SatkaDatePicker
import net.satka.bleManager.ui.components.SatkaDateTimePicker
import net.satka.bleManager.ui.components.SatkaDropdownMenu
import net.satka.bleManager.ui.components.SatkaEditNumber
import net.satka.bleManager.ui.components.SatkaEditText
import net.satka.bleManager.ui.components.SatkaRichTextView
import net.satka.bleManager.ui.components.SatkaSlider
import net.satka.bleManager.ui.components.SatkaSwitch
import net.satka.bleManager.ui.components.SatkaTextView
import net.satka.bleManager.ui.components.SatkaTimePicker
import net.satka.bleManager.ui.models.DeviceViewModel
import java.util.UUID
import kotlin.math.roundToInt

//TODO: LeakCanary - otestovat úniky paměti
//TODO: Rozvoj: nějaké prvky pro vizuaizaci hodnot. Graf, teploměr... Ale to až později.
//TODO: Rozvoj: Nastavení typu na libovolné UUID charakteirstiky
class DynamicFragment : Fragment() {

    private val uuid: String
        get() = requireArguments().getString(ARG_UUID, "")
    private val deviceViewModel: DeviceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dynamic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        composeView.setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) {
                    dynamicDarkColorScheme(requireContext())
                } else {
                    dynamicLightColorScheme(requireContext())
                }
            ) {
                DeviceDetail()
            }
        }
    }

    @Composable
    private fun DeviceDetail() {
        val service = deviceViewModel.deviceModel?.getService(UUID.fromString(uuid))
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                service?.getCharacteristics()?.filter { it !is CharacteristicModel.ServiceName }
                    ?.forEach { characteristic ->
                        Control(characteristic)
                    }
            }
        }
    }

    @Composable
    private fun Control(characteristic: CharacteristicModel<*>) {
        when (characteristic) {
            is CharacteristicModel.TitleView -> {
                SatkaTextView(
                    text = characteristic.value ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    isEnabled = !characteristic.disabled
                )
            }

            is CharacteristicModel.TextView -> {
                SatkaTextView(
                    text = characteristic.value ?: "",
                    isEnabled = !characteristic.disabled
                )
            }

            is CharacteristicModel.RichTextView -> {
                SatkaRichTextView(
                    json = characteristic.value ?: "",
                    isEnabled = !characteristic.disabled
                )
            }

            is CharacteristicModel.TextField -> {
                SatkaEditText(
                    value = characteristic.value ?: "",
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    maxBytes = characteristic.maxBytes,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.PasswordField -> {
                SatkaEditText(
                    value = characteristic.value ?: "",
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Password,
                    maxBytes = characteristic.maxBytes,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.PINField -> {
                SatkaEditText(
                    value = characteristic.value ?: "",
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.NumberPassword,
                    maxBytes = characteristic.maxBytes,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //TODO: u všech prvků; test na výchozí prázdné hodnoty
            is CharacteristicModel.Checkbox -> {
                SatkaCheckbox(
                    checked = characteristic.value ?: false,
                    text = characteristic.label ?: "",
                    isEnabled = !characteristic.disabled,
                    onCheckedChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Switch -> {
                SatkaSwitch(
                    checked = characteristic.value ?: false,
                    text = characteristic.label ?: "",
                    isEnabled = !characteristic.disabled,
                    onCheckedChange = { characteristic.write(it) }
                )
            }
            //LITTLE ENDIAN
            //Unsigned Integers LE
            is CharacteristicModel.UInt8Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0u,
                    stringToNumber = { it.toUByteOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.UInt16Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0u,
                    stringToNumber = { it.toUShortOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.UInt32Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0u,
                    stringToNumber = { it.toUIntOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.UInt64Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0uL,
                    stringToNumber = { it.toULongOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //Signed Integers LE
            is CharacteristicModel.SInt8Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toByteOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.SInt16Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toShortOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.SInt32Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toIntOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.SInt64Field -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toLongOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //Floats LE
            is CharacteristicModel.HalfFloatField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0f,
                    stringToNumber = { it.toFloatOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Decimal,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.FloatField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0f,
                    stringToNumber = { it.toFloatOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Decimal,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.DoubleField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0.0,
                    stringToNumber = { it.toDoubleOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Decimal,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //BIG ENDIAN
            //Unsigned Integers LE
            is CharacteristicModel.UInt8beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0u,
                    stringToNumber = { it.toUByteOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.UInt16beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0u,
                    stringToNumber = { it.toUShortOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.UInt32beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0u,
                    stringToNumber = { it.toUIntOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.UInt64beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0uL,
                    stringToNumber = { it.toULongOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //Signed Integers BE
            is CharacteristicModel.SInt8beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toByteOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.SInt16beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toShortOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.SInt32beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toIntOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.SInt64beField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0,
                    stringToNumber = { it.toLongOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Number,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //Floats LE
            is CharacteristicModel.HalfFloatBeField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0f,
                    stringToNumber = { it.toFloatOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Decimal,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.FloatBeField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0f,
                    stringToNumber = { it.toFloatOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Decimal,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.DoubleBeField -> {
                SatkaEditNumber(
                    value = characteristic.value ?: 0.0,
                    stringToNumber = { it.toDoubleOrNull() },
                    hint = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    keyboardType = KeyboardType.Decimal,
                    min = characteristic.min,
                    max = characteristic.max,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Button -> {
                SatkaButton(
                    text = characteristic.label?: "",
                    isEnabled = !characteristic.disabled,
                    onClick = { characteristic.write(((characteristic.value?: 0u) + 1u).toUByte()) }
                )
            }

            //Date and Time LE
            is CharacteristicModel.TimeField -> {
                SatkaTimePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Date32Field -> {
                SatkaDatePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.DateTime32Field -> {
                SatkaDateTimePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Date64Field -> {
                SatkaDatePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.DateTime64Field -> {
                SatkaDateTimePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //Date and Time BE
            is CharacteristicModel.TimeBeField -> {
                SatkaTimePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Date32beField -> {
                SatkaDatePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.DateTime32beField -> {
                SatkaDateTimePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Date64beField -> {
                SatkaDatePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.DateTime64beField -> {
                SatkaDateTimePicker(
                    context = requireActivity(),
                    value = characteristic.value,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //Sliders LE
            is CharacteristicModel.FloatSlider -> {
                SatkaSlider(
                    value = characteristic.value?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min..characteristic.max,
                    stepSize = characteristic.step,
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.HalfSlider -> {
                SatkaSlider(
                    value = characteristic.value?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min..characteristic.max,
                    stepSize = characteristic.step,
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //Sliders BE
            is CharacteristicModel.FloatBeSlider -> {
                SatkaSlider(
                    value = characteristic.value?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min..characteristic.max,
                    stepSize = characteristic.step,
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.HalfBeSlider -> {
                SatkaSlider(
                    value = characteristic.value?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min..characteristic.max,
                    stepSize = characteristic.step,
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it) }
                )
            }

            //INTEGER Sliders LE
            is CharacteristicModel.UInt8Slider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = {
                        characteristic.write(it.roundToInt().toUByte())
                    }
                )
            }

            is CharacteristicModel.UInt16Slider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it.roundToInt().toUShort()) }
                )
            }

            is CharacteristicModel.SInt8Slider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it.roundToInt().toByte()) }
                )
            }

            is CharacteristicModel.SInt16Slider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = {
                        characteristic.write(it.roundToInt().toShort())
                    }
                )
            }

            //INTEGER Sliders BE
            is CharacteristicModel.UInt8beSlider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it.roundToInt().toUByte()) }
                )
            }

            is CharacteristicModel.UInt16beSlider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it.roundToInt().toUShort()) }
                )
            }

            is CharacteristicModel.SInt8beSlider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it.roundToInt().toByte()) }
                )
            }

            is CharacteristicModel.SInt16beSlider -> {
                SatkaSlider(
                    value = characteristic.value?.toFloat()?: 0f,
                    label = characteristic.label,
                    isEnabled = !characteristic.disabled,
                    valueRange = characteristic.min.toFloat()..characteristic.max.toFloat(),
                    stepSize = characteristic.step.toFloat(),
                    debounceMillis = characteristic.debounceMillis,
                    onValueChange = { characteristic.write(it.roundToInt().toShort()) }
                )
            }

            is CharacteristicModel.ColorPicker -> {
                SatkaColorPicker(
                    label = characteristic.label?: "",
                    value = characteristic.value?: Color.White,
                    isEnabled = !characteristic.disabled,
                    showAlphaSlider = characteristic.showAlphaSlider,
                    onColorSelected = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Dropdown -> {
                SatkaDropdownMenu(
                    label = characteristic.label?: "",
                    value = characteristic.value?: "",
                    isEnabled = !characteristic.disabled,
                    options = characteristic.options,
                    onSelectionChange = { characteristic.write(it) }
                )
            }

            is CharacteristicModel.Error -> {
                SatkaTextView(
                    text = characteristic.value ?: ""
                )
            }

            else -> {
                Text(text = "Unknown type")
            }
        }
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }

    companion object {
        // the name for the argument
        private const val ARG_UUID = "ARG_INDEX"

        // Use this function to create instances of the fragment
        // and set the passed data as arguments
        fun newInstance(uuid: String) = DynamicFragment().apply {
            arguments = bundleOf(
                ARG_UUID to uuid
            )
        }
    }
}
