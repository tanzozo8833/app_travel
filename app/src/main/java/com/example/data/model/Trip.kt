package com.example.data.model

import java.util.UUID

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "u_1",
    val title: String,
    val destination: String,
    val provinceCode: String,
    val coverUrl: String,
    val startDate: String, // e.g. "12/09/2026"
    val endDate: String,   // e.g. "14/09/2026"
    val durationText: String, // e.g. "3N2Đ"
    val status: TripStatus = TripStatus.UPCOMING,
    val totalEstimatedCostVnd: Long = 2400000,
    val spotsCount: Int = 8,
    val groupType: String = "Cặp đôi",
    val styleTags: List<String> = listOf("Chill sương mù", "Sống ảo", "Cà phê"),
    val isAiGenerated: Boolean = true,
    val weatherSummary: WeatherSummary = WeatherSummary(
        temperature = "18°C",
        weatherCondition = "Trời se lạnh, có sương mù nhẹ buổi sớm",
        aiTip = "Trưa nắng dịu — các quán cà phê view thung lũng đã xếp vào khung giờ vàng chụp ảnh."
    ),
    val days: List<TripDay> = emptyList()
)

enum class TripStatus(val titleVi: String) {
    UPCOMING("Sắp đi"),
    ONGOING("Đang đi"),
    PAST("Đã đi")
}

data class TripDay(
    val dayNumber: Int,
    val dateText: String,
    val title: String,
    val items: List<TripItem> = emptyList()
)

data class TripItem(
    val id: String = UUID.randomUUID().toString(),
    val timeSlot: String, // e.g. "05:00 - 07:30"
    val title: String,
    val description: String,
    val locationName: String,
    val category: PlaceCategory = PlaceCategory.PHOTO,
    val costVnd: Long = 50000,
    val bookingLabel: String? = null,
    val bookingUrl: String? = null,
    val isCheckedIn: Boolean = false
)

data class WeatherSummary(
    val temperature: String = "22°C",
    val weatherCondition: String = "Nắng đẹp, se lạnh",
    val aiTip: String = "Thời tiết lý tưởng để săn hoàng hôn."
)
