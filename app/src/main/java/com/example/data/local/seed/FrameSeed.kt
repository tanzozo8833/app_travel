package com.example.data.local.seed

import com.example.data.local.entity.LuminaFrameEntity

object FrameSeed {
    val data = listOf(
        LuminaFrameEntity(
            id = "frame_national_day",
            name = "Quốc Khánh 2/9",
            badgeLabel = "🇻🇳 Quốc Khánh 2/9",
            subtitle = "2/9/1945 - 2/9/2026 · ĐỘC LẬP TỰ DO HẠNH PHÚC",
            topTitle = "★ VIỆT NAM TRONG TIM TÔI ★",
            bottomTagline = "LUMINA PASSPORT EVENT STAMP • 2026",
            previewColor = 0xFFD93025,
            provinceCode = null,
            isSeasonal = true
        ),
        LuminaFrameEntity(
            id = "frame_hanoi_autumn",
            name = "Thu Hà Nội",
            badgeLabel = "🍂 Thu Hà Nội",
            subtitle = "PHỐ CỔ HOÀN KIẾM · HOA SỮA & CÀ PHÊ TRỨNG",
            topTitle = "HÀ NỘI 36 PHỐ PHƯỜNG",
            bottomTagline = "LUMINA STAMP • HANOI AUTUMN VIBES",
            previewColor = 0xFFB26A00,
            provinceCode = "HN",
            isSeasonal = false
        ),
        LuminaFrameEntity(
            id = "frame_90s_polaroid",
            name = "90s Polaroid",
            badgeLabel = "🎞 90s Polaroid",
            subtitle = "MEMORIES OF VIETNAM · TIMELESS MOMENTS",
            topTitle = "LUMINA INSTANT FILM",
            bottomTagline = "EST. 2026 • GEN Z TRAVEL DIARY",
            previewColor = 0xFF5A6B64,
            provinceCode = null,
            isSeasonal = false
        ),
        LuminaFrameEntity(
            id = "frame_dalat_mist",
            name = "Sương Mù Đà Lạt",
            badgeLabel = "🌲 Đà Lạt 1500M",
            subtitle = "SĂN MÂY ĐỒI THÔNG · 16°C SE LẠNH",
            topTitle = "XỨ SỞ NGÀN HOA & SƯƠNG",
            bottomTagline = "LUMINA PASSPORT • LAM DONG STAMP",
            previewColor = 0xFF0A5C42,
            provinceCode = "LDG",
            isSeasonal = false
        ),
        LuminaFrameEntity(
            id = "frame_wanderlist",
            name = "Wanderlist Explorer",
            badgeLabel = "🧭 Wanderlist",
            subtitle = "34 PROVINCES ODYSSEY · GEN Z EXPLORER",
            topTitle = "LUMINA TRAVEL PASSPORT",
            bottomTagline = "OFFICIAL VIETNAM FOOTPRINT RECORD",
            previewColor = 0xFF043027,
            provinceCode = null,
            isSeasonal = false
        ),
        LuminaFrameEntity(
            id = "frame_saigon_postcard",
            name = "Bưu Thiếp Sài Gòn",
            badgeLabel = "💌 Bưu Thiếp Sài Gòn",
            subtitle = "HÒN NGỌC VIỄN ĐÔNG · PHỐ XÁ NHỘN NHỊP",
            topTitle = "GREETINGS FROM SAIGON",
            bottomTagline = "POSTCARD FROM VIETNAM • 2026",
            previewColor = 0xFF12855F,
            provinceCode = "HCM",
            isSeasonal = false
        )
    )
}
