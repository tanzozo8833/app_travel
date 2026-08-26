package com.example.data.local.converters

import androidx.room.TypeConverter
import com.example.data.model.CheckInMethod
import com.example.data.model.PlaceCategory
import com.example.data.model.TripDay
import com.example.data.model.TripItem
import com.example.data.model.TripStatus
import com.example.data.model.WeatherSummary
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class LuminaTypeConverters {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val tripDayListType = Types.newParameterizedType(List::class.java, TripDay::class.java)
    private val tripDayListAdapter = moshi.adapter<List<TripDay>>(tripDayListType)

    private val weatherSummaryAdapter = moshi.adapter(WeatherSummary::class.java)

    // String List Converters
    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return if (list == null) "[]" else stringListAdapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            stringListAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // TripDay List Converters
    @TypeConverter
    fun fromTripDayList(list: List<TripDay>?): String {
        return if (list == null) "[]" else tripDayListAdapter.toJson(list)
    }

    @TypeConverter
    fun toTripDayList(json: String?): List<TripDay> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            tripDayListAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // WeatherSummary Converters
    @TypeConverter
    fun fromWeatherSummary(summary: WeatherSummary?): String {
        return if (summary == null) "" else weatherSummaryAdapter.toJson(summary)
    }

    @TypeConverter
    fun toWeatherSummary(json: String?): WeatherSummary {
        if (json.isNullOrBlank()) return WeatherSummary()
        return try {
            weatherSummaryAdapter.fromJson(json) ?: WeatherSummary()
        } catch (_: Exception) {
            WeatherSummary()
        }
    }

    // Enum Converters
    @TypeConverter
    fun fromPlaceCategory(category: PlaceCategory?): String {
        return category?.name ?: PlaceCategory.PHOTO.name
    }

    @TypeConverter
    fun toPlaceCategory(value: String?): PlaceCategory {
        return try {
            if (value != null) PlaceCategory.valueOf(value) else PlaceCategory.PHOTO
        } catch (_: Exception) {
            PlaceCategory.PHOTO
        }
    }

    @TypeConverter
    fun fromTripStatus(status: TripStatus?): String {
        return status?.name ?: TripStatus.UPCOMING.name
    }

    @TypeConverter
    fun toTripStatus(value: String?): TripStatus {
        return try {
            if (value != null) TripStatus.valueOf(value) else TripStatus.UPCOMING
        } catch (_: Exception) {
            TripStatus.UPCOMING
        }
    }

    @TypeConverter
    fun fromCheckInMethod(method: CheckInMethod?): String {
        return method?.name ?: CheckInMethod.QUICK.name
    }

    @TypeConverter
    fun toCheckInMethod(value: String?): CheckInMethod {
        return try {
            if (value != null) CheckInMethod.valueOf(value) else CheckInMethod.QUICK
        } catch (_: Exception) {
            CheckInMethod.QUICK
        }
    }
}
