package com.example.data.local.seed

import com.example.data.local.entity.UserProfileEntity

object UserProfileSeed {
    val data = listOf(
        UserProfileEntity(
            id = "u_1",
            username = "linh.travels",
            displayName = "Thảo Linh",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop",
            tagTitle = "GEN Z EXPLORER",
            bio = "Gen Z Explorer | Săn hoàng hôn & Foodtour Việt Nam 🇻🇳 ✨",
            topCityName = "Hà Nội",
            topCityCheckInCount = 4,
            visitedProvincesCount = 5,
            totalProvincesCount = 34,
            totalDistanceKm = 12500.0,
            tripsCompletedCount = 18,
            totalSavesCount = 3420,
            isActive = true
        ),
        UserProfileEntity(
            id = "u_2",
            username = "tan.explorer",
            displayName = "Đức Tân",
            avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=300&auto=format&fit=crop",
            tagTitle = "TREK MASTER",
            bio = "Chinh phục 63 tỉnh thành • Đam mê cắm trại & flycam 🏕️ 🏔️",
            topCityName = "Hà Giang",
            topCityCheckInCount = 8,
            visitedProvincesCount = 19,
            totalProvincesCount = 34,
            totalDistanceKm = 24800.0,
            tripsCompletedCount = 32,
            totalSavesCount = 8900,
            isActive = false
        )
    )
}
