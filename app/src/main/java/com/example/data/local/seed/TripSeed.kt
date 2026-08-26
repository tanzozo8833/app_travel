package com.example.data.local.seed

import com.example.data.local.entity.TripEntity
import com.example.data.model.PlaceCategory
import com.example.data.model.TripDay
import com.example.data.model.TripItem
import com.example.data.model.TripStatus
import com.example.data.model.WeatherSummary

object TripSeed {
    val data = listOf(
        TripEntity(
            id = "trip_dalat_3n2d",
            userId = "u_1",
            title = "Đà Lạt 3N2Đ Chill Sương Mù & Cà Phê Mây",
            destination = "Đà Lạt, Lâm Đồng",
            provinceCode = "LDG",
            coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            startDate = "12/09/2026",
            endDate = "14/09/2026",
            durationText = "3 ngày 2 đêm",
            status = TripStatus.ONGOING,
            totalEstimatedCostVnd = 2400000,
            spotsCount = 8,
            groupType = "Cặp đôi",
            styleTags = listOf("Chill sương mù", "Sống ảo", "Cà phê"),
            isAiGenerated = true,
            weatherSummary = WeatherSummary(
                temperature = "18°C",
                weatherCondition = "Trời se lạnh, có sương mù nhẹ buổi sớm",
                aiTip = "Trưa nắng dịu — các quán cà phê view thung lũng đã xếp vào khung giờ vàng chụp ảnh."
            ),
            days = listOf(
                TripDay(
                    dayNumber = 1,
                    dateText = "12/09",
                    title = "Săn mây Cầu Đất & Cà phê Thung Lũng",
                    items = listOf(
                        TripItem(
                            timeSlot = "05:00 - 07:30",
                            title = "Săn mây Đồi Chè Cầu Đất",
                            description = "Đón bình minh rực rỡ và biển mây bồng bềnh tại Cầu Đất, chụp ảnh bên quạt gió.",
                            locationName = "Đồi Chè Cầu Đất",
                            category = PlaceCategory.PHOTO,
                            costVnd = 50000,
                            bookingLabel = "Vé xe săn mây đón tận nơi (Klook)",
                            bookingUrl = "https://klook.com",
                            isCheckedIn = true
                        ),
                        TripItem(
                            timeSlot = "08:30 - 10:30",
                            title = "Ăn sáng Bánh Căn Nhà Chung",
                            description = "Thưởng thức bánh căn trứng cút lòng đào kèm chén xíu mại hành lá thơm ngậy.",
                            locationName = "Bánh Căn Nhà Chung",
                            category = PlaceCategory.FOOD,
                            costVnd = 45000,
                            isCheckedIn = true
                        ),
                        TripItem(
                            timeSlot = "14:00 - 16:30",
                            title = "Chill Cà Phê Thung Lũng Mây",
                            description = "Ngồi ban công gỗ ngắm thung lũng thông xanh, thưởng thức latte ấm và ngắm hoàng hôn.",
                            locationName = "Cà Phê Thung Lũng Mây",
                            category = PlaceCategory.CAFE,
                            costVnd = 65000,
                            isCheckedIn = true
                        )
                    )
                ),
                TripDay(
                    dayNumber = 2,
                    dateText = "13/09",
                    title = "Máng trượt Datanla & Chợ đêm",
                    items = listOf(
                        TripItem(
                            timeSlot = "09:00 - 12:00",
                            title = "Thử thách Máng trượt Thác Datanla",
                            description = "Trải nghiệm cảm giác lướt máng trượt xuyên qua tán thông đại ngàn và ngắm thác.",
                            locationName = "Thác Datanla",
                            category = PlaceCategory.ACTIVITY,
                            costVnd = 170000,
                            bookingLabel = "Vé vào cổng & máng trượt khứ hồi",
                            isCheckedIn = true
                        ),
                        TripItem(
                            timeSlot = "18:30 - 21:30",
                            title = "Foodtour Chợ Đêm Đà Lạt",
                            description = "Bánh tráng nướng cô Ba, sữa đậu nành nóng, dâu tây lắc và khoai lang nướng.",
                            locationName = "Chợ Đêm Đà Lạt",
                            category = PlaceCategory.FOOD,
                            costVnd = 120000,
                            isCheckedIn = false
                        )
                    )
                ),
                TripDay(
                    dayNumber = 3,
                    dateText = "14/09",
                    title = "Check-in Ga Cổ & Tạm biệt Đà Lạt",
                    items = listOf(
                        TripItem(
                            timeSlot = "08:30 - 10:30",
                            title = "Chụp ảnh phong cách Retro tại Ga Đà Lạt",
                            description = "Chiêm ngưỡng đầu tàu hơi nước cổ và các toa tàu gỗ mang đậm dấu ấn thập niên 30.",
                            locationName = "Ga Xe Lửa Đà Lạt",
                            category = PlaceCategory.CULTURE,
                            costVnd = 20000,
                            isCheckedIn = false
                        )
                    )
                )
            )
        ),
        TripEntity(
            id = "trip_hagiang_4n3d",
            userId = "u_1",
            title = "Hà Giang 4N3Đ Chinh Phục Cung Đường Hạnh Phúc",
            destination = "Hà Giang",
            provinceCode = "HGG",
            coverUrl = "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=800&auto=format&fit=crop",
            startDate = "20/10/2026",
            endDate = "23/10/2026",
            durationText = "4 ngày 3 đêm",
            status = TripStatus.UPCOMING,
            totalEstimatedCostVnd = 3500000,
            spotsCount = 10,
            groupType = "Bạn bè",
            styleTags = listOf("Phượt xe máy", "Đèo Mã Pí Lèng", "Hoa tam giác mạch"),
            isAiGenerated = true
        ),
        TripEntity(
            id = "trip_danang_3n2d",
            userId = "u_1",
            title = "Đà Nẵng - Hội An 3N2Đ Biển Mỹ Khê & Phố Đèn Lồng",
            destination = "Đà Nẵng",
            provinceCode = "DNG",
            coverUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800&auto=format&fit=crop",
            startDate = "15/06/2026",
            endDate = "17/06/2026",
            durationText = "3 ngày 2 đêm",
            status = TripStatus.PAST,
            totalEstimatedCostVnd = 2800000,
            spotsCount = 9,
            groupType = "Một mình",
            styleTags = listOf("Nghỉ dưỡng", "Biển", "Ẩm thực"),
            isAiGenerated = false
        )
    )
}
