package com.example.data.model

data class Province(
    val id: Int,
    val code: String,
    val name: String,
    val shortName: String,
    val region: String, // Bắc, Trung, Nam, Tây Nguyên
    val centerLat: Double,
    val centerLng: Double,
    val coverUrl: String,
    val stampSymbol: String, // Icon/Symbol for stamp
    val highlightDescription: String,
    val suggestedPlacesCount: Int = 5,
    val visitedPlacesCount: Int = 0,
    val isCompleted: Boolean = false
)

data class Place(
    val id: String,
    val provinceId: Int,
    val provinceCode: String,
    val name: String,
    val category: PlaceCategory,
    val address: String,
    val description: String,
    val coverUrl: String,
    val checkinRadiusMeters: Int = 200,
    val isSuggested: Boolean = true,
    val priceRangeVnd: Long = 50000,
    val tags: List<String> = emptyList(),
    val rating: Double = 4.8,
    val isVisited: Boolean = false
)

enum class PlaceCategory(val titleVi: String, val iconEmoji: String) {
    NATURE("Thiên nhiên", "🏔"),
    CAFE("Cà phê & Chill", "☕"),
    FOOD("Ẩm thực đặc sản", "🍜"),
    CULTURE("Văn hoá & Di tích", "🏛"),
    PHOTO("Check-in & Sống ảo", "📸"),
    STAY("Nghỉ dưỡng & Homestay", "🛏"),
    ACTIVITY("Hoạt động & Trải nghiệm", "🎒")
}
