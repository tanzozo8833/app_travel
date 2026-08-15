package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateTripPlan(
        destination: String,
        durationDays: Int,
        budgetVnd: Long,
        groupType: String,
        styleTags: List<String>,
        customNotes: String
    ): Trip = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Using smart local AI engine for trip planning")
            return@withContext generateLocalSmartTrip(destination, durationDays, budgetVnd, groupType, styleTags, customNotes)
        }

        try {
            val prompt = """
                Bạn là chuyên gia thiết kế lịch trình du lịch Lumina dành cho Gen Z tại Việt Nam.
                Hãy tạo lịch trình chi tiết theo định dạng JSON cho chuyến đi:
                - Điểm đến: $destination
                - Số ngày: $durationDays ngày (${durationDays}N${durationDays - 1}Đ)
                - Ngân sách: ${budgetVnd} VNĐ / người
                - Đi với: $groupType
                - Phong cách: ${styleTags.joinToString(", ")}
                - Ghi chú: $customNotes

                Trả về ĐÚNG 1 JSON Object (không kèm markdown format) theo cấu trúc sau:
                {
                  "title": "Tiêu đề hấp dẫn ngắn gọn",
                  "temperature": "22°C",
                  "weatherCondition": "Trời se lạnh, có sương mù nhẹ",
                  "aiTip": "Mẹo hữu ích của AI về thời gian/trang phục",
                  "days": [
                    {
                      "dayNumber": 1,
                      "title": "Tiêu đề ngày 1",
                      "items": [
                        {
                          "timeSlot": "05:00 - 07:30",
                          "title": "Tên hoạt động",
                          "description": "Mô tả trải nghiệm thú vị",
                          "locationName": "Tên địa điểm thực tế",
                          "category": "PHOTO", // NATURE, CAFE, FOOD, CULTURE, PHOTO, STAY, ACTIVITY
                          "costVnd": 50000,
                          "bookingLabel": "Vé tham quan (nếu có)"
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API failed with code ${response.code}, fallback to smart engine")
                return@withContext generateLocalSmartTrip(destination, durationDays, budgetVnd, groupType, styleTags, customNotes)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val cleanedJson = rawText.replace("```json", "").replace("```", "").trim()
            val planObj = JSONObject(cleanedJson)

            parseJsonToTrip(planObj, destination, durationDays, budgetVnd, groupType, styleTags)
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateTripPlan, falling back to smart local plan", e)
            generateLocalSmartTrip(destination, durationDays, budgetVnd, groupType, styleTags, customNotes)
        }
    }

    private fun parseJsonToTrip(
        obj: JSONObject,
        destination: String,
        durationDays: Int,
        budgetVnd: Long,
        groupType: String,
        styleTags: List<String>
    ): Trip {
        val title = obj.optString("title", "$destination ${durationDays}N${durationDays - 1}Đ Tự Túc")
        val temperature = obj.optString("temperature", "24°C")
        val weatherCondition = obj.optString("weatherCondition", "Thời tiết mát mẻ, nắng nhẹ")
        val aiTip = obj.optString("aiTip", "Nên chuẩn bị giày thể thao êm ái và áo khoác mỏng.")

        val daysList = mutableListOf<TripDay>()
        val daysJson = obj.optJSONArray("days")
        var totalSpots = 0

        if (daysJson != null) {
            for (i in 0 until daysJson.length()) {
                val dayObj = daysJson.optJSONObject(i) ?: continue
                val dayNum = dayObj.optInt("dayNumber", i + 1)
                val dayTitle = dayObj.optString("title", "Khám phá ngày $dayNum")
                val itemsList = mutableListOf<TripItem>()

                val itemsJson = dayObj.optJSONArray("items")
                if (itemsJson != null) {
                    for (j in 0 until itemsJson.length()) {
                        val itemObj = itemsJson.optJSONObject(j) ?: continue
                        totalSpots++
                        val catStr = itemObj.optString("category", "PHOTO")
                        val cat = try {
                            PlaceCategory.valueOf(catStr)
                        } catch (e: Exception) {
                            PlaceCategory.PHOTO
                        }

                        itemsList.add(
                            TripItem(
                                timeSlot = itemObj.optString("timeSlot", "08:00 - 10:00"),
                                title = itemObj.optString("title", "Tham quan"),
                                description = itemObj.optString("description", ""),
                                locationName = itemObj.optString("locationName", destination),
                                category = cat,
                                costVnd = itemObj.optLong("costVnd", 50000),
                                bookingLabel = if (itemObj.has("bookingLabel") && !itemObj.isNull("bookingLabel")) itemObj.getString("bookingLabel") else null
                            )
                        )
                    }
                }

                daysList.add(
                    TripDay(
                        dayNumber = dayNum,
                        dateText = "Ngày $dayNum",
                        title = dayTitle,
                        items = itemsList
                    )
                )
            }
        }

        return Trip(
            id = UUID.randomUUID().toString(),
            title = title,
            destination = destination,
            provinceCode = getProvinceCode(destination),
            coverUrl = getCoverUrlForDestination(destination),
            startDate = "15/09/2026",
            endDate = "17/09/2026",
            durationText = "${durationDays} ngày ${durationDays - 1} đêm",
            status = TripStatus.UPCOMING,
            totalEstimatedCostVnd = budgetVnd,
            spotsCount = if (totalSpots > 0) totalSpots else 6,
            groupType = groupType,
            styleTags = styleTags,
            isAiGenerated = true,
            weatherSummary = WeatherSummary(
                temperature = temperature,
                weatherCondition = weatherCondition,
                aiTip = aiTip
            ),
            days = daysList
        )
    }

    suspend fun askAi(
        question: String,
        contextDestination: String = "Đà Lạt",
        pluginType: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalSmartChatResponse(question, contextDestination, pluginType)
        }

        try {
            val systemPrompt = "Bạn là trợ lý AI du lịch Lumina Gen Z tại Việt Nam. Giọng điệu thân thiện, sành điệu, đưa ra gợi ý địa điểm chính xác, mẹo chụp ảnh, quán ăn ngon và lời khuyên thiết thực."
            val userPrompt = "Điểm đến đang quan tâm: $contextDestination. Câu hỏi: $question"

            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", "$systemPrompt\n\n$userPrompt") })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext getLocalSmartChatResponse(question, contextDestination, pluginType)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            parts?.optJSONObject(0)?.optString("text") ?: getLocalSmartChatResponse(question, contextDestination, pluginType)
        } catch (e: Exception) {
            getLocalSmartChatResponse(question, contextDestination, pluginType)
        }
    }

    private fun generateLocalSmartTrip(
        destination: String,
        durationDays: Int,
        budgetVnd: Long,
        groupType: String,
        styleTags: List<String>,
        customNotes: String
    ): Trip {
        val dest = if (destination.isNotBlank()) destination else "Đà Lạt"
        val isDaLat = dest.contains("Đà Lạt", ignoreCase = true) || dest.contains("Lâm Đồng", ignoreCase = true)
        val isHaGiang = dest.contains("Hà Giang", ignoreCase = true)
        val isDaNang = dest.contains("Đà Nẵng", ignoreCase = true) || dest.contains("Hội An", ignoreCase = true)
        val isHaNoi = dest.contains("Hà Nội", ignoreCase = true)

        val title = when {
            isDaLat -> "Đà Lạt ${durationDays}N${durationDays - 1}Đ Săn Mây & Cà Phê Rừng Thông"
            isHaGiang -> "Hà Giang ${durationDays}N${durationDays - 1}Đ Chinh Phục Mã Pí Lèng & Mùa Hoa"
            isDaNang -> "Đà Nẵng - Hội An ${durationDays}N${durationDays - 1}Đ Biển Xanh & Phố Cổ"
            isHaNoi -> "Hà Nội ${durationDays}N${durationDays - 1}Đ 36 Phố Phường & Cà Phê Trứng"
            else -> "$dest ${durationDays}N${durationDays - 1}Đ Trọn Vẹn Cùng Lumina"
        }

        val days = mutableListOf<TripDay>()
        for (day in 1..durationDays) {
            val items = when (day) {
                1 -> listOf(
                    TripItem(
                        timeSlot = "05:30 - 08:00",
                        title = if (isDaLat) "Săn mây Đồi Chè Cầu Đất" else "Đón bình minh và ăn sáng đặc sản",
                        description = if (isDaLat) "Đón tia nắng đầu tiên xuyên qua biển mây bồng bềnh và quạt gió khổng lồ." else "Bắt đầu ngày mới tràn đầy năng lượng tại địa điểm ngắm cảnh đẹp nhất.",
                        locationName = if (isDaLat) "Đồi Chè Cầu Đất" else "$dest Trung Tâm",
                        category = PlaceCategory.PHOTO,
                        costVnd = 50000,
                        bookingLabel = "Xe đưa đón săn mây (Klook)"
                    ),
                    TripItem(
                        timeSlot = "09:00 - 11:30",
                        title = if (isDaLat) "Bánh Căn Nhà Chung & Cà phê" else "Check-in quán cà phê view đẹp",
                        description = "Nạp năng lượng với đặc sản nóng giòn và thưởng thức đồ uống signature.",
                        locationName = if (isDaLat) "Bánh Căn Nhà Chung" else "Cà phê $dest",
                        category = PlaceCategory.FOOD,
                        costVnd = 60000
                    ),
                    TripItem(
                        timeSlot = "14:30 - 17:30",
                        title = if (isDaLat) "Chill Cà Phê Thung Lũng Mây" else "Khám phá danh thắng biểu tượng",
                        description = "Ngồi ngắm hoàng hôn buông lãng đãng và lưu lại những bức ảnh sống ảo tuyệt đẹp.",
                        locationName = if (isDaLat) "Cà Phê Thung Lũng Mây" else "Điểm tham quan $dest",
                        category = PlaceCategory.CAFE,
                        costVnd = 65000
                    )
                )
                2 -> listOf(
                    TripItem(
                        timeSlot = "08:30 - 11:30",
                        title = if (isDaLat) "Máng trượt xuyên rừng Thác Datanla" else "Trải nghiệm hoạt động ngoài trời",
                        description = "Cảm giác lướt qua tán rừng xanh mát và hoà mình vào thiên nhiên tươi đẹp.",
                        locationName = if (isDaLat) "Thác Datanla" else "$dest Hoạt Động",
                        category = PlaceCategory.ACTIVITY,
                        costVnd = 170000,
                        bookingLabel = "Vé trải nghiệm ưu đãi"
                    ),
                    TripItem(
                        timeSlot = "18:00 - 21:00",
                        title = "Foodtour chợ đêm & dạo phố",
                        description = "Khám phá ẩm thực đường phố về đêm và tận hưởng không khí sôi động.",
                        locationName = "Chợ đêm $dest",
                        category = PlaceCategory.FOOD,
                        costVnd = 150000
                    )
                )
                else -> listOf(
                    TripItem(
                        timeSlot = "09:00 - 11:30",
                        title = "Check-in di tích văn hoá & mua quà lưu niệm",
                        description = "Lưu lại con dấu kỷ niệm cuối cùng và mua đặc sản làm quà cho bạn bè.",
                        locationName = "$dest Di Tích",
                        category = PlaceCategory.CULTURE,
                        costVnd = 40000
                    )
                )
            }

            days.add(
                TripDay(
                    dayNumber = day,
                    dateText = "Ngày $day",
                    title = if (day == 1) "Khởi hành & Khám phá trung tâm" else if (day == 2) "Trải nghiệm thiên nhiên & Ẩm thực" else "Check-in di tích & Mua quà",
                    items = items
                )
            )
        }

        return Trip(
            id = UUID.randomUUID().toString(),
            title = title,
            destination = dest,
            provinceCode = getProvinceCode(dest),
            coverUrl = getCoverUrlForDestination(dest),
            startDate = "15/09/2026",
            endDate = "17/09/2026",
            durationText = "${durationDays} ngày ${durationDays - 1} đêm",
            status = TripStatus.UPCOMING,
            totalEstimatedCostVnd = budgetVnd,
            spotsCount = days.sumOf { it.items.size },
            groupType = groupType,
            styleTags = styleTags.ifEmpty { listOf("Chill", "Sống ảo", "Ẩm thực") },
            isAiGenerated = true,
            weatherSummary = WeatherSummary(
                temperature = if (isDaLat) "18°C" else "26°C",
                weatherCondition = if (isDaLat) "Se lạnh, sương mù sáng sớm" else "Nắng nhẹ, gió mát",
                aiTip = "Đã xếp các quán view đẹp vào khung giờ hoàng hôn để chụp ảnh đẹp nhất!"
            ),
            days = days
        )
    }

    private fun getLocalSmartChatResponse(question: String, destination: String, plugin: String?): String {
        return when {
            plugin == "food" || question.contains("ăn gì", ignoreCase = true) || question.contains("món", ignoreCase = true) -> {
                "🍜 **Top đặc sản $destination nhất định phải thử:**\n\n" +
                        "1. **Bánh căn xíu mại nóng hổi** — Giòn rụm bên ngoài, xíu mại béo ngậy chấm ngập mỡ hành (35k–50k).\n" +
                        "2. **Lẩu gà lá é / Bò tơ** — Nước dùng ngọt thanh, the cay giải lạnh tuyệt vời (180k–250k/nồi).\n" +
                        "3. **Bánh tráng nướng 'Pizza Việt Nam'** — Ăn kèm sữa đậu nành nóng ở chợ đêm.\n\n" +
                        "💡 *Mẹo Lumina:* Nên đi ăn từ 17:30 để tránh phải xếp hàng lâu nhé!"
            }
            plugin == "outfit" || question.contains("mặc gì", ignoreCase = true) || question.contains("đồ", ignoreCase = true) -> {
                "👗 **Gợi ý outfit chụp ảnh tại $destination:**\n\n" +
                        "• **Săn mây sáng sớm (05:00):** Áo trench coat màu be / nâu đất hoặc cardigan len dày, khăn choàng mỏng.\n" +
                        "• **Cà phê ban ngày:** Váy hoa nhí vintage, tone màu pastel hoặc trắng kem tương phản với màu thông xanh.\n" +
                        "• **Dạo phố tối:** Áo hoodie ấm áp hoặc jacket da mix cùng sneaker thoải mái.\n\n" +
                        "📸 Bảng màu ảnh đẹp nhất: Be (#D8C4B6), Xanh Rừng (#0A5C42) và Nâu đất."
            }
            plugin == "cost" || question.contains("chi phí", ignoreCase = true) || question.contains("tiền", ignoreCase = true) -> {
                "💰 **Bảng chi phí ước tính cho chuyến $destination 3N2Đ (1 người):**\n\n" +
                        "• Di chuyển (xe limousine khứ hồi): ~600.000đ\n" +
                        "• Homestay xinh trung tâm (2 đêm chia đôi): ~500.000đ\n" +
                        "• Thuê xe máy & xăng xe: ~250.000đ\n" +
                        "• Ăn uống thả ga (3 ngày): ~700.000đ\n" +
                        "• Vé tham quan & cà phê check-in: ~350.000đ\n" +
                        "─────────────────────\n" +
                        "👉 **Tổng cộng:** ~2.400.000đ / người (Cực kỳ hợp lý cho Gen Z!)"
            }
            plugin == "photo" || question.contains("chụp", ignoreCase = true) || question.contains("sống ảo", ignoreCase = true) -> {
                "📸 **Top góc chụp triệu view tại $destination:**\n\n" +
                        "1. **Đồi Chè Cầu Đất (05:15 - 06:30)**: Chụp ngược sáng bắt trọn tia nắng bình minh xuyên qua lớp sương mù.\n" +
                        "2. **Cà Phê Thung Lũng Mây (16:30 - 17:45)**: Góc ban công gỗ ngắm hoàng hôn màu cam cháy rực rỡ.\n" +
                        "3. **Ga Cổ Đà Lạt (08:30 - 10:00)**: Góc toa tàu gỗ cổ điển phong cách phim Hong Kong thập niên 90."
            }
            else -> {
                "✨ Chào bạn! Tại **$destination**, Lumina gợi ý bạn nên dậy sớm săn mây lúc 05:30 và dành buổi chiều ngắm hoàng hôn tại các quán cà phê thung lũng. Bạn có muốn mình tạo lịch trình chi tiết theo ngày hay tìm quán ăn ngon gần bạn không?"
            }
        }
    }

    private fun getProvinceCode(destination: String): String {
        return when {
            destination.contains("Lâm Đồng", true) || destination.contains("Đà Lạt", true) -> "LDG"
            destination.contains("Hà Nội", true) -> "HN"
            destination.contains("Đà Nẵng", true) -> "DNG"
            destination.contains("Hà Giang", true) -> "HGG"
            destination.contains("Huế", true) -> "TTH"
            destination.contains("Hồ Chí Minh", true) || destination.contains("Sài Gòn", true) -> "HCM"
            destination.contains("Phú Quốc", true) || destination.contains("Kiên Giang", true) -> "KGG"
            destination.contains("Sa Pa", true) || destination.contains("Lào Cai", true) -> "LCA"
            destination.contains("Quảng Ninh", true) || destination.contains("Hạ Long", true) -> "QNH"
            destination.contains("Ninh Bình", true) -> "NBH"
            else -> "VN"
        }
    }

    private fun getCoverUrlForDestination(destination: String): String {
        return when {
            destination.contains("Hà Giang", true) -> "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=800&auto=format&fit=crop"
            destination.contains("Hà Nội", true) -> "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop"
            destination.contains("Đà Nẵng", true) -> "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800&auto=format&fit=crop"
            destination.contains("Phú Quốc", true) -> "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop"
            destination.contains("Ninh Bình", true) -> "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop"
            destination.contains("Huế", true) -> "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=800&auto=format&fit=crop"
            else -> "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop"
        }
    }
}
