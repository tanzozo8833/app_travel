package com.example.ui.components

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

enum class CheckInColorMode(
    val id: String,
    val title: String,
    val subtext: String,
    val iconEmoji: String,
    val samplePreviewColor: Long
) {
    ORIGINAL(
        id = "original",
        title = "Màu Gốc",
        subtext = "Tự nhiên nguyên bản",
        iconEmoji = "📷",
        samplePreviewColor = 0xFF4A6572
    ),
    FILM(
        id = "film",
        title = "Màu Film",
        subtext = "Ấm áp Kodak 35mm",
        iconEmoji = "🎞️",
        samplePreviewColor = 0xFFD48B38
    ),
    BLACK_AND_WHITE(
        id = "monochrome",
        title = "Trắng Đen",
        subtext = "Photobooth Noir B&W",
        iconEmoji = "⚪",
        samplePreviewColor = 0xFF2C3437
    ),
    VIVID(
        id = "vivid",
        title = "Màu Tươi",
        subtext = "Rực rỡ & Sống động",
        iconEmoji = "✨",
        samplePreviewColor = 0xFF00897B
    ),
    ROSY_WHITE(
        id = "rosy",
        title = "Trắng Hồng",
        subtext = "Mịn da Glow Photobooth",
        iconEmoji = "🌸",
        samplePreviewColor = 0xFFE91E63
    ),
    VINTAGE(
        id = "vintage",
        title = "Vintage 90s",
        subtext = "Hoài niệm Retro Sepia",
        iconEmoji = "📻",
        samplePreviewColor = 0xFF8D6E63
    ),
    DALAT_MIST(
        id = "dalat_mist",
        title = "Sương Mù",
        subtext = "Thông xanh & Moody Teal",
        iconEmoji = "🌲",
        samplePreviewColor = 0xFF00796B
    ),
    CYBERPUNK(
        id = "cyberpunk",
        title = "Neon Night",
        subtext = "Tương phản Sài Gòn đêm",
        iconEmoji = "⚡",
        samplePreviewColor = 0xFF6200EE
    );

    fun getColorFilter(): ColorFilter = when (this) {
        ORIGINAL -> PhotoFilters.Normal
        FILM -> PhotoFilters.WarmFilm35mm
        BLACK_AND_WHITE -> PhotoFilters.PhotoboothMonochrome
        VIVID -> PhotoFilters.VibrantVietnam
        ROSY_WHITE -> PhotoFilters.RosyWhitePhotobooth
        VINTAGE -> PhotoFilters.VintageSepia90s
        DALAT_MIST -> PhotoFilters.DalatMist
        CYBERPUNK -> PhotoFilters.Cyberpunk
    }

    fun getFloatMatrix(): FloatArray? = PhotoFilters.getMatrixForMode(this)
}

object PhotoFilters {

    val MATRIX_ORIGINAL = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    // 1. Film 35mm: Warm analog glow, rich contrast, soft golden-amber highlights
    val MATRIX_FILM = floatArrayOf(
        1.18f, 0.08f, 0.02f, 0f, 18f,
        0.04f, 1.10f, 0.04f, 0f, 12f,
        0.02f, 0.04f, 0.88f, 0f, -8f,
        0f, 0f, 0f, 1f, 0f
    )

    // 2. Photobooth Noir B&W: Crisp, high-contrast monochrome with balanced luminosity
    val MATRIX_BLACK_AND_WHITE = floatArrayOf(
        0.35f, 0.50f, 0.15f, 0f, 5f,
        0.35f, 0.50f, 0.15f, 0f, 5f,
        0.35f, 0.50f, 0.15f, 0f, 5f,
        0f, 0f, 0f, 1f, 0f
    )

    // 3. Màu Tươi (Vivid / Bright Pop): Enhanced saturation & crisp dynamic range
    val MATRIX_VIVID = floatArrayOf(
        1.32f, -0.08f, -0.04f, 0f, 12f,
        -0.06f, 1.25f, -0.04f, 0f, 8f,
        -0.04f, -0.06f, 1.22f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
    )

    // 4. Tone Trắng Hồng (Rosy White Photobooth): Soft glow, bright skin tones, rosy tint
    val MATRIX_ROSY_WHITE = floatArrayOf(
        1.22f, 0.06f, 0.06f, 0f, 25f,
        0.04f, 1.12f, 0.04f, 0f, 18f,
        0.06f, 0.04f, 1.16f, 0f, 22f,
        0f, 0f, 0f, 1f, 0f
    )

    // 5. Vintage 90s Sepia: Retro nostalgic warm tint
    val MATRIX_VINTAGE = floatArrayOf(
        1.20f, 0.12f, 0.00f, 0f, 22f,
        0.08f, 1.08f, 0.02f, 0f, 14f,
        0.00f, 0.06f, 0.82f, 0f, -12f,
        0f, 0f, 0f, 1f, 0f
    )

    // 6. Sương Mù Đà Lạt: Cool mist & pine green
    val MATRIX_DALAT_MIST = floatArrayOf(
        0.86f, 0.10f, 0.04f, 0f, -6f,
        0.04f, 1.16f, 0.10f, 0f, 10f,
        0.08f, 0.14f, 1.22f, 0f, 16f,
        0f, 0f, 0f, 1f, 0f
    )

    // 7. Cyberpunk Neon
    val MATRIX_CYBERPUNK = floatArrayOf(
        1.25f, 0.00f, 0.20f, 0f, 12f,
        0.00f, 1.15f, 0.30f, 0f, 6f,
        0.20f, 0.20f, 1.38f, 0f, 28f,
        0f, 0f, 0f, 1f, 0f
    )

    val Normal = ColorFilter.colorMatrix(ColorMatrix(MATRIX_ORIGINAL))
    val WarmFilm35mm = ColorFilter.colorMatrix(ColorMatrix(MATRIX_FILM))
    val PhotoboothMonochrome = ColorFilter.colorMatrix(ColorMatrix(MATRIX_BLACK_AND_WHITE))
    val VibrantVietnam = ColorFilter.colorMatrix(ColorMatrix(MATRIX_VIVID))
    val RosyWhitePhotobooth = ColorFilter.colorMatrix(ColorMatrix(MATRIX_ROSY_WHITE))
    val VintageSepia90s = ColorFilter.colorMatrix(ColorMatrix(MATRIX_VINTAGE))
    val DalatMist = ColorFilter.colorMatrix(ColorMatrix(MATRIX_DALAT_MIST))
    val Cyberpunk = ColorFilter.colorMatrix(ColorMatrix(MATRIX_CYBERPUNK))

    fun getMatrixForMode(mode: CheckInColorMode): FloatArray? {
        return when (mode) {
            CheckInColorMode.ORIGINAL -> null
            CheckInColorMode.FILM -> MATRIX_FILM
            CheckInColorMode.BLACK_AND_WHITE -> MATRIX_BLACK_AND_WHITE
            CheckInColorMode.VIVID -> MATRIX_VIVID
            CheckInColorMode.ROSY_WHITE -> MATRIX_ROSY_WHITE
            CheckInColorMode.VINTAGE -> MATRIX_VINTAGE
            CheckInColorMode.DALAT_MIST -> MATRIX_DALAT_MIST
            CheckInColorMode.CYBERPUNK -> MATRIX_CYBERPUNK
        }
    }

    fun getFilterForFrame(frameId: String): ColorFilter {
        return when (frameId) {
            "frame_national_day" -> VibrantVietnam
            "frame_hanoi_autumn" -> WarmFilm35mm
            "frame_90s_polaroid" -> VintageSepia90s
            "frame_dalat_mist" -> DalatMist
            "frame_saigon_postcard" -> Cyberpunk
            "frame_monochrome" -> PhotoboothMonochrome
            "frame_rosy" -> RosyWhitePhotobooth
            else -> WarmFilm35mm
        }
    }

    fun getFilterName(frameId: String): String {
        return when (frameId) {
            "frame_national_day" -> "Tone Rực Rỡ Ánh Vàng Đỏ"
            "frame_hanoi_autumn" -> "Tone Thu Vàng 35mm"
            "frame_90s_polaroid" -> "Tone Film Cổ Điển 90s"
            "frame_dalat_mist" -> "Tone Sương Mù & Rừng Thông"
            "frame_saigon_postcard" -> "Tone Hoàng Hôn Sài Gòn"
            "frame_monochrome" -> "Tone Trắng Đen Noir"
            "frame_rosy" -> "Tone Trắng Hồng Photobooth"
            else -> "Tone Tự Nhiên"
        }
    }
}

