package com.example.coffeeshop.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalDateTime {
        val dateString = json.asString
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME // Xử lý định dạng ISO 8601
        return LocalDateTime.parse(dateString, formatter)
    }
}