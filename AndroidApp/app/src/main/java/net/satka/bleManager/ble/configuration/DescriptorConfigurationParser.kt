package net.satka.bleManager.ble.configuration

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.Strictness
import net.satka.bleManager.ble.models.DescriptorConfiguration
import java.io.StringReader
import java.lang.reflect.Type

//TODO: Rozvoj: časem zařídit, aby klíče byly case-insensitive
object DescriptorConfigurationParser {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ULong::class.java, ULongSerializer())
        .registerTypeAdapter(UInt::class.java, UIntSerializer())
        .setStrictness(Strictness.LENIENT)
        .create()

    fun parseJson(jsonString: String?): DescriptorConfiguration? {
        if (jsonString.isNullOrBlank() || jsonString.all { it == '\u0000' }) {
            throw IllegalArgumentException("Descriptor value is empty")
        }

        val reader = StringReader(jsonString)
        try {
            return gson.fromJson(reader, DescriptorConfiguration::class.java)
        } catch (ex: Exception) {
            Log.e("JSON Parser", ex.toString())
            throw ex
        }
    }

    private class ULongSerializer : JsonSerializer<ULong>, JsonDeserializer<ULong> {
        override fun serialize(src: ULong, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString())
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ULong {
            return json.asString.toULong()
        }
    }

    private class UIntSerializer : JsonSerializer<UInt>, JsonDeserializer<UInt> {
        override fun serialize(src: UInt, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.toString())
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UInt {
            return json.asString.toUInt()
        }
    }
}