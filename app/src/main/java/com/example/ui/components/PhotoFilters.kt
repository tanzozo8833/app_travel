package com.example.ui.components

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

object PhotoFilters {

    val Normal = ColorFilter.colorMatrix(ColorMatrix())

    // Vintage 90s Film with warm sepia and soft highlights
    val VintageFilm = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.15f, 0.05f, 0.05f, 0f, 15f,
                0.05f, 1.05f, 0.05f, 0f, 10f,
                0.02f, 0.05f, 0.85f, 0f, -5f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    // Thu Hà Nội: Warm romantic golden hour amber
    val AutumnWarmth = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.25f, 0.10f, 0.00f, 0f, 20f,
                0.10f, 1.15f, 0.00f, 0f, 10f,
                0.00f, 0.05f, 0.80f, 0f, -15f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    // Sương Mù Đà Lạt: Cool mist, pine green deep shadows, moody teal
    val DalatMist = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.85f, 0.10f, 0.05f, 0f, -5f,
                0.05f, 1.15f, 0.10f, 0f, 10f,
                0.10f, 0.15f, 1.20f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    // Quốc Khánh 2/9: Rich high-vibrancy, bold warm red-gold saturation
    val VibrantVietnam = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.30f, -0.05f, -0.05f, 0f, 10f,
                -0.05f, 1.20f, -0.05f, 0f, 5f,
                -0.05f, -0.05f, 1.15f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    // Black & White Noir / Cổ Điển
    val MonochromeNoir = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0.33f, 0.33f, 0.33f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    // Cyberpunk Sài Gòn: High contrast neon teal & magenta pop
    val Cyberpunk = ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.2f, 0.0f, 0.2f, 0f, 10f,
                0.0f, 1.1f, 0.3f, 0f, 5f,
                0.2f, 0.2f, 1.35f, 0f, 25f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    )

    fun getFilterForFrame(frameId: String): ColorFilter {
        return when (frameId) {
            "frame_national_day" -> VibrantVietnam
            "frame_hanoi_autumn" -> AutumnWarmth
            "frame_90s_polaroid" -> VintageFilm
            "frame_dalat_mist" -> DalatMist
            "frame_saigon_postcard" -> Cyberpunk
            "frame_wanderlist" -> AutumnWarmth
            "frame_monochrome" -> MonochromeNoir
            else -> VintageFilm
        }
    }

    fun getFilterName(frameId: String): String {
        return when (frameId) {
            "frame_national_day" -> "Tone Rực Rỡ Ánh Vàng Đỏ"
            "frame_hanoi_autumn" -> "Tone Thu Vàng Hà Nội"
            "frame_90s_polaroid" -> "Tone Film Cổ Điển 90s"
            "frame_dalat_mist" -> "Tone Sương Mù & Rừng Thông"
            "frame_saigon_postcard" -> "Tone Hoàng Hôn Sài Gòn"
            "frame_wanderlist" -> "Tone Phiêu Lưu Ấm Áp"
            "frame_monochrome" -> "Tone Trắng Đen Trầm Mặc"
            else -> "Tone Tự Nhiên"
        }
    }
}
