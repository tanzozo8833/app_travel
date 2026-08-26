package com.example.data.local.seed

import com.example.data.local.entity.AchievementBadgeEntity

object BadgeSeed {
    val data = listOf(
        AchievementBadgeEntity(
            id = "b_1",
            code = "first_stamp",
            name = "Dấu đầu tiên",
            description = "Hoàn thành lượt check-in đầu tiên trên hành trình khám phá Việt Nam.",
            iconEmoji = "🌟",
            isEarned = true,
            progressCurrent = 1,
            progressMax = 1,
            earnedDate = "12/09/2026"
        ),
        AchievementBadgeEntity(
            id = "b_2",
            code = "province_5",
            name = "Kẻ lữ hành",
            description = "Chinh phục và đóng dấu hộ chiếu tại 5 tỉnh thành.",
            iconEmoji = "🎒",
            isEarned = true,
            progressCurrent = 5,
            progressMax = 5,
            earnedDate = "13/09/2026"
        ),
        AchievementBadgeEntity(
            id = "b_3",
            code = "province_10",
            name = "Người thuộc đường",
            description = "Đã in dấu chân tại 10 tỉnh thành khắp đất nước.",
            iconEmoji = "🧭",
            isEarned = false,
            progressCurrent = 5,
            progressMax = 10,
            earnedDate = null
        ),
        AchievementBadgeEntity(
            id = "b_4",
            code = "complete_province",
            name = "Thổ địa Hà Giang",
            description = "Đã check-in 100% địa điểm gợi ý tại tỉnh Hà Giang.",
            iconEmoji = "🏅",
            isEarned = true,
            progressCurrent = 5,
            progressMax = 5,
            earnedDate = "22/07/2026"
        ),
        AchievementBadgeEntity(
            id = "b_5",
            code = "sunrise_hunter",
            name = "Kẻ săn mây",
            description = "Thực hiện 3 lần check-in săn mây trước 06:00 sáng.",
            iconEmoji = "☁️",
            isEarned = true,
            progressCurrent = 3,
            progressMax = 3,
            earnedDate = "12/09/2026"
        ),
        AchievementBadgeEntity(
            id = "b_6",
            code = "foodie",
            name = "Bụng không đáy",
            description = "Check-in 20 quán ẩm thực & đặc sản vùng miền.",
            iconEmoji = "🍜",
            isEarned = false,
            progressCurrent = 14,
            progressMax = 20,
            earnedDate = null
        ),
        AchievementBadgeEntity(
            id = "b_7",
            code = "three_regions",
            name = "Xuyên Việt",
            description = "Đã check-in đủ cả 3 miền Bắc — Trung — Nam.",
            iconEmoji = "🇻🇳",
            isEarned = true,
            progressCurrent = 3,
            progressMax = 3,
            earnedDate = "10/08/2026"
        ),
        AchievementBadgeEntity(
            id = "b_8",
            code = "frame_collector",
            name = "Nhà sưu tầm khung",
            description = "Đã chụp ảnh và sử dụng 5 khung ảnh sự kiện khác nhau.",
            iconEmoji = "🖼",
            isEarned = false,
            progressCurrent = 4,
            progressMax = 5,
            earnedDate = null
        )
    )
}
