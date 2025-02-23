package net.satka.bleManager.ui.components

import android.icu.text.DecimalFormatSymbols
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.wear.compose.material.ContentAlpha
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.Strictness
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import net.satka.bleManager.R
import net.satka.bleManager.utils.DateTimeFormatterUtil
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun SatkaTextView(
    text: String,
    backgroundColor: Color = Color.Transparent,
    isEnabled: Boolean = true,
    style: TextStyle? = null
) {
    Text(
        text = text,
        modifier = Modifier
            .background(backgroundColor)
            .then(if (!isEnabled) Modifier.alpha(ContentAlpha.disabled) else Modifier),
        style = style ?: LocalTextStyle.current,
    )
}

@Composable
fun SatkaRichTextView(
    json: String,
    isEnabled: Boolean = true,
    style: TextStyle? = null
) {
    val gson = GsonBuilder().setStrictness(Strictness.LENIENT)
        .create()

    var text = "";

    var jsonObject: JsonObject? = null
    var backgroundColor = Color.Transparent
    var textColor = Color.Unspecified
    var title = false

    try {
        jsonObject = gson.fromJson(json, JsonObject::class.java)
        text = jsonObject?.get("text")?.asString ?: ""

        backgroundColor = jsonObject?.get("background")?.asString?.let {
            Color(android.graphics.Color.parseColor(it))
        } ?: Color.Transparent

        textColor = jsonObject?.get("color")?.asString?.let {
            Color(android.graphics.Color.parseColor(it))
        } ?: Color.Unspecified

        title = jsonObject?.get("title")?.asBoolean ?: false
    } catch (e: Exception) {
        text = e.message ?: "";
    }

    var textStyle = if (title) MaterialTheme.typography.titleMedium else LocalTextStyle.current
    if (textColor != Color.Unspecified) {
        textStyle = textStyle.copy(color = textColor)
    }

    SatkaTextView(
        text = text,
        backgroundColor = backgroundColor,
        isEnabled = isEnabled,
        style = textStyle
    )
}

@Composable
fun SatkaCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isEnabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier
                .then(if (!isEnabled) Modifier.alpha(ContentAlpha.disabled) else Modifier)
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = isEnabled
        )
    }
}

@Composable
fun SatkaEditText(
    hint: String? = null,
    isEnabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    value: String? = null,
    maxBytes: Int = Int.MAX_VALUE,
    onValueChange: (String) -> Unit
) {
    var text by remember { mutableStateOf(value ?: "") }

    LaunchedEffect(value) {
        text = value ?: ""
    }

    var isError by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    fun sendValue() {
        if (!isError) {
            hasUnsavedChanges = false
            onValueChange(text)
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            val bytes = newText.toByteArray(Charsets.UTF_8)
            isError = bytes.size > maxBytes
            hasUnsavedChanges = newText != text
            text = newText
        },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(stringResource(R.string.maximum_bytes_allowed, maxBytes))
            }
        },
        label = {
            hint?.let {
                Text(
                    text = it
                )
            }
        },
        enabled = isEnabled,
        maxLines = 3,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                sendValue()
            },
        ),
        trailingIcon = {
            if (hasUnsavedChanges && isEnabled && !isError) {
                IconButton(onClick = {
                    sendValue()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        },
        visualTransformation = if (keyboardType == KeyboardType.Password || keyboardType == KeyboardType.NumberPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun <T> SatkaEditNumber(
    hint: String? = null,
    isEnabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Number,
    value: T? = null,
    onValueChange: (T) -> Unit,
    stringToNumber: (String) -> T?,
    min: T,
    max: T,
) where T : Comparable<T> {
    var text by remember { mutableStateOf(value?.toString() ?: "0") }

    LaunchedEffect(value) {
        text = value?.toString() ?: "0"
    }

    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    fun sendValue() {
        if (!isError) {
            val res = stringToNumber(text)
            if (res != null) {
                hasUnsavedChanges = false
                onValueChange(res)
            }
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            val res = stringToNumber(newText)
            isError = res == null || res !in min..max
            hasUnsavedChanges = newText != text
            text = newText
        },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(stringResource(R.string.value_must_be_number_between_and, min, max))
            }
        },
        label = {
            hint?.let {
                Text(
                    it
                )
            }
        },
        enabled = isEnabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                sendValue()
            }
        ),
        trailingIcon = {
            if (hasUnsavedChanges && isEnabled && !isError) {
                IconButton(onClick = {
                    sendValue()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        },
        visualTransformation = if (keyboardType == KeyboardType.Password || keyboardType == KeyboardType.NumberPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SatkaButton(
    text: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun SatkaSwitch(
    text: String,
    checked: Boolean,
    isEnabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier
                .then(if (!isEnabled) Modifier.alpha(ContentAlpha.disabled) else Modifier)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = isEnabled
        )
    }
}

@Composable
fun SatkaColorPicker(
    label: String,
    value: Color = Color.White,
    isEnabled: Boolean = true,
    onColorSelected: (Color) -> Unit,
    showAlphaSlider: Boolean = false
) {
    var selectedColor by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        selectedColor = value
    }

    var showPicker by remember { mutableStateOf(false) }

    Button(
        onClick = { showPicker = true },
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(selectedColor)
                    .border(1.dp, Color.LightGray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label)
        }
    }

    if (showPicker) {
        ColorPickerDialogBuilder
            .with(LocalContext.current)
            .setTitle(label)
            .initialColor(selectedColor.toArgb())
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .showAlphaSlider(showAlphaSlider)
            .density(12)
            .setPositiveButton(stringResource(R.string.ok)) { _, selected, _ ->
                selectedColor = Color(selected)
                onColorSelected(selectedColor)
                showPicker = false
            }
            .setNegativeButton(stringResource(R.string.cancel)) { _, _ ->
                showPicker = false
            }
            .build()
            .show()
    }
}

@OptIn(FlowPreview::class)
@Composable
fun SatkaSlider(
    label: String? = null,
    isEnabled: Boolean = true,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    stepSize: Float = 1f,
    onValueChange: (Float) -> Unit,
    debounceMillis: Long = 300
) {
    val separator = DecimalFormatSymbols.getInstance().decimalSeparatorString
    var sliderPosition by remember { mutableFloatStateOf(value) }

    LaunchedEffect(value) {
        sliderPosition = value
    }

    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            snapshotFlow { sliderPosition }
                .debounce(debounceMillis)
                .collect { onValueChange(it) }
        }
        onDispose { job.cancel() }
    }

    Column {
        Text(
            text = "${label ?: "Value"}: %.2f".format(sliderPosition)
                .replace(Regex("${separator}00\$"), ""),
            modifier = Modifier
                .then(if (!isEnabled) Modifier.alpha(ContentAlpha.disabled) else Modifier)
        )
        Slider(
            value = if (sliderPosition.isNaN()) valueRange.start else sliderPosition,
            onValueChange = {
                sliderPosition = it
            },
            valueRange = valueRange,
            steps = if (stepSize == 0f) 0 else {
                (((valueRange.endInclusive - valueRange.start) / stepSize).toInt() - 1)
                    .coerceAtLeast(0)
            },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun SatkaTimePicker(
    context: FragmentActivity,
    label: String? = null,
    isEnabled: Boolean = true,
    value: LocalTime? = null,
    onValueChange: (LocalTime) -> Unit
) {
    var selectedTime by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        selectedTime = value
    }

    val formattedTime = remember(selectedTime) {
        DateTimeFormatterUtil.formatTime(
            context,
            selectedTime?.hour,
            selectedTime?.minute
        ) ?: ""
    }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = formattedTime,
        onValueChange = { },
        readOnly = true,
        label = {
            label?.let {
                Text(
                    it
                )
            }
        },
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth(),
        interactionSource = interactionSource
    )

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Focus) {
                val is24HourFormat = DateFormat.is24HourFormat(context)
                val timePickerDialog = MaterialTimePicker.Builder()
                    .setHour(selectedTime?.hour ?: 0)
                    .setMinute(selectedTime?.minute ?: 0)
                    .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                    .setTimeFormat(if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                    .build()

                timePickerDialog.addOnPositiveButtonClickListener {
                    val tm = LocalTime.of(timePickerDialog.hour, timePickerDialog.minute)
                    selectedTime = tm
                    onValueChange(tm)
                    focusManager.clearFocus()
                }

                timePickerDialog.addOnDismissListener {
                    focusManager.clearFocus()
                }

                timePickerDialog.show(
                    context.supportFragmentManager,
                    "timePicker"
                )
            }
        }
    }
}

@Composable
fun SatkaDatePicker(
    context: FragmentActivity,
    label: String? = null,
    isEnabled: Boolean = true,
    value: LocalDateTime? = null,
    onValueChange: (LocalDateTime) -> Unit
) {
    var selectedDate by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        selectedDate = value
    }

    val formattedDate = remember(selectedDate) {
        DateTimeFormatterUtil.formatDate(
            context,
            selectedDate?.year,
            selectedDate?.monthValue,
            selectedDate?.dayOfMonth
        ) ?: ""
    }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = formattedDate,
        onValueChange = { },
        readOnly = true,
        label = {
            label?.let {
                Text(
                    it
                )
            }
        },
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth(),
        interactionSource = interactionSource
    )

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Focus) {
                val datePicker = MaterialDatePicker.Builder.datePicker().build()
                datePicker.addOnPositiveButtonClickListener { selection ->
                    val dt = Instant.ofEpochMilli(selection)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime()
                    selectedDate = dt
                    onValueChange(dt)
                    focusManager.clearFocus()
                }
                datePicker.addOnDismissListener {
                    focusManager.clearFocus()
                }
                datePicker.show(
                    context.supportFragmentManager,
                    "datePicker"
                )
            }
        }
    }
}

@Composable
fun SatkaDateTimePicker(
    context: FragmentActivity,
    label: String? = null,
    isEnabled: Boolean = true,
    value: LocalDateTime? = null,
    onValueChange: (LocalDateTime) -> Unit
) {
    var selectedDateTime by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        selectedDateTime = value
    }

    val formattedDateTime = remember(selectedDateTime) {
        // Assuming you have a DateTimeFormatterUtil available
        DateTimeFormatterUtil.formatDateTime(
            context, // Using context parameter
            selectedDateTime?.year,
            selectedDateTime?.monthValue,
            selectedDateTime?.dayOfMonth,
            selectedDateTime?.hour,
            selectedDateTime?.minute
        ) ?: ""
    }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = formattedDateTime,
        onValueChange = { },
        readOnly = true,
        label = {
            label?.let {
                Text(
                    it
                )
            }
        },
        enabled = isEnabled,
        modifier = Modifier.fillMaxWidth(),
        interactionSource = interactionSource
    )

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is FocusInteraction.Focus) {
                val datePicker = MaterialDatePicker.Builder.datePicker().build()
                datePicker.addOnPositiveButtonClickListener { dateSelection ->
                    val selectedDate = Instant.ofEpochMilli(dateSelection)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()

                    val is24HourFormat =
                        DateFormat.is24HourFormat(context) // Using context parameter
                    val timePicker = MaterialTimePicker.Builder()
                        .setHour(selectedDateTime?.hour ?: 0)
                        .setMinute(selectedDateTime?.minute ?: 0)
                        .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .setTimeFormat(
                            if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
                        )
                        .build()

                    timePicker.addOnPositiveButtonClickListener {
                        val dt = selectedDate.atTime(timePicker.hour, timePicker.minute)
                        selectedDateTime = dt
                        onValueChange(dt) // Invoke callback
                        focusManager.clearFocus()
                    }

                    timePicker.addOnDismissListener {
                        focusManager.clearFocus()
                    }

                    timePicker.show(
                        context.supportFragmentManager, // Using context parameter
                        "timePicker"
                    )
                }

                datePicker.addOnDismissListener {
                    focusManager.clearFocus()
                }

                datePicker.show(
                    context.supportFragmentManager, // Using context parameter
                    "datePicker"
                )
            }
        }
    }
}

@Composable
fun SatkaDropdownMenu(
    label: String? = null,
    isEnabled: Boolean = true,
    options: List<String>,
    value: String? = null,
    onSelectionChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    var selectedOptionText by remember { mutableStateOf(value ?: "") }

    LaunchedEffect(value) {
        selectedOptionText = value ?: ""
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOptionText,
            onValueChange = { },
            readOnly = true,
            label = {
                label?.let {
                    Text(
                        it
                    )
                }
            },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interactionSource
        )
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                if (interaction is FocusInteraction.Focus) {
                    expanded = true
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(onClick = {
                    selectedOptionText = selectionOption
                    expanded = false
                    focusManager.clearFocus()
                    onSelectionChange(selectionOption)
                }, text = {
                    Text(selectionOption)
                })
            }
        }
    }
}
