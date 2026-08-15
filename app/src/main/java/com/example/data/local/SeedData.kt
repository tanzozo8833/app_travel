package com.example.data.local

import com.example.data.model.*

object SeedData {

    val provinces = listOf(
        Province(
            id = 1,
            code = "HN",
            name = "Thành phố Hà Nội",
            shortName = "Hà Nội",
            region = "Bắc",
            centerLat = 21.0285,
            centerLng = 105.8542,
            coverUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop",
            stampSymbol = "🏛",
            highlightDescription = "Thủ đô ngàn năm văn hiến, phố cổ trầm mặc, cà phê trứng và mùa thu xao xuyến.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 4,
            isCompleted = false
        ),
        Province(
            id = 2,
            code = "LDG",
            name = "Tỉnh Lâm Đồng",
            shortName = "Đà Lạt",
            region = "Tây Nguyên",
            centerLat = 11.9404,
            centerLng = 108.4583,
            coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            stampSymbol = "🌲",
            highlightDescription = "Xứ sở ngàn hoa, đồi thông săn mây bồng bềnh, quán cà phê trong rừng thơ mộng.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 4,
            isCompleted = false
        ),
        Province(
            id = 3,
            code = "DNG",
            name = "Thành phố Đà Nẵng",
            shortName = "Đà Nẵng",
            region = "Trung",
            centerLat = 16.0544,
            centerLng = 108.2022,
            coverUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800&auto=format&fit=crop",
            stampSymbol = "🌉",
            highlightDescription = "Thành phố đáng sống nhất Việt Nam, Cầu Vàng trên mây, biển Mỹ Khê cát trắng.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 3,
            isCompleted = false
        ),
        Province(
            id = 4,
            code = "HGG",
            name = "Tỉnh Hà Giang",
            shortName = "Hà Giang",
            region = "Bắc",
            centerLat = 22.8233,
            centerLng = 104.9839,
            coverUrl = "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=800&auto=format&fit=crop",
            stampSymbol = "🏔",
            highlightDescription = "Cực Bắc hùng vĩ, hẻm vực Tu Sản, đèo Mã Pí Lèng và mùa hoa tam giác mạch.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 5,
            isCompleted = true
        ),
        Province(
            id = 5,
            code = "TTH",
            name = "Tỉnh Thừa Thiên Huế",
            shortName = "Huế",
            region = "Trung",
            centerLat = 16.4637,
            centerLng = 107.5909,
            coverUrl = "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=800&auto=format&fit=crop",
            stampSymbol = "🏯",
            highlightDescription = "Cố đô trầm mặc, sông Hương thơ mộng, ẩm thực cung đình và lăng tẩm uy nghiêm.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 2,
            isCompleted = false
        ),
        Province(
            id = 6,
            code = "HCM",
            name = "Thành phố Hồ Chí Minh",
            shortName = "TP. Hồ Chí Minh",
            region = "Nam",
            centerLat = 10.8231,
            centerLng = 106.6297,
            coverUrl = "https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=800&auto=format&fit=crop",
            stampSymbol = "🏙",
            highlightDescription = "Hòn ngọc Viễn Đông sôi động, phố đi bộ Nguyễn Huệ, ẩm thực đường phố muôn màu.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 4,
            isCompleted = false
        ),
        Province(
            id = 7,
            code = "QNH",
            name = "Tỉnh Quảng Ninh",
            shortName = "Hạ Long",
            region = "Bắc",
            centerLat = 20.9505,
            centerLng = 107.0734,
            coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            stampSymbol = "⛵",
            highlightDescription = "Kỳ quan thiên nhiên thế giới Vịnh Hạ Long, đảo Cô Tô hoang sơ, Yên Tử thanh tịnh.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 0,
            isCompleted = false
        ),
        Province(
            id = 8,
            code = "KGG",
            name = "Tỉnh Kiên Giang",
            shortName = "Phú Quốc",
            region = "Nam",
            centerLat = 10.2899,
            centerLng = 103.9840,
            coverUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop",
            stampSymbol = "🏝",
            highlightDescription = "Đảo ngọc Phú Quốc, hoàng hôn Bãi Sao, lặn ngắm san hô Hòn Mây Rút.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 1,
            isCompleted = false
        ),
        Province(
            id = 9,
            code = "NBH",
            name = "Tỉnh Ninh Bình",
            shortName = "Ninh Bình",
            region = "Bắc",
            centerLat = 20.2506,
            centerLng = 105.9745,
            coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop",
            stampSymbol = "🚣",
            highlightDescription = "Vịnh Hạ Long trên cạn, quần thể danh thắng Tràng An di sản thế giới, Hang Múa.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 0,
            isCompleted = false
        ),
        Province(
            id = 10,
            code = "LCA",
            name = "Tỉnh Lào Cai",
            shortName = "Sa Pa",
            region = "Bắc",
            centerLat = 22.3364,
            centerLng = 103.8438,
            coverUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop",
            stampSymbol = "🚡",
            highlightDescription = "Nóc nhà Đông Dương Fansipan, ruộng bậc thang Mường Hoa, sương mù bản Cát Cát.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 0,
            isCompleted = false
        ),
        Province(
            id = 11,
            code = "KHA",
            name = "Tỉnh Khánh Hoà",
            shortName = "Nha Trang",
            region = "Trung",
            centerLat = 12.2388,
            centerLng = 109.1967,
            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop",
            stampSymbol = "🌊",
            highlightDescription = "Vịnh biển Nha Trang ngập nắng, tháp Bà Ponagar huyền bí, đảo Điệp Sơn.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 0,
            isCompleted = false
        ),
        Province(
            id = 12,
            code = "BDH",
            name = "Tỉnh Bình Định",
            shortName = "Quy Nhơn",
            region = "Trung",
            centerLat = 13.7830,
            centerLng = 109.2197,
            coverUrl = "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=800&auto=format&fit=crop",
            stampSymbol = "🏖",
            highlightDescription = "Eo Gió lộng gió, Kỳ Co nước biển xanh màu ngọc bích, xứ nẫu bình yên.",
            suggestedPlacesCount = 5,
            visitedPlacesCount = 0,
            isCompleted = false
        )
    )

    val places = listOf(
        // Lâm Đồng / Đà Lạt
        Place(
            id = "p_ldg_1",
            provinceId = 2,
            provinceCode = "LDG",
            name = "Đồi Chè Cầu Đất",
            category = PlaceCategory.PHOTO,
            address = "Thôn Trường Thọ, Xã Trạm Hành, Đà Lạt",
            description = "Điểm săn mây và bình minh ngoạn mục nhất Đà Lạt với thảm chè xanh mướt và cối xay gió khổng lồ.",
            coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            checkinRadiusMeters = 500,
            priceRangeVnd = 50000,
            tags = listOf("Săn mây", "Bình minh", "Đồi chè"),
            isVisited = true
        ),
        Place(
            id = "p_ldg_2",
            provinceId = 2,
            provinceCode = "LDG",
            name = "Thác Datanla",
            category = PlaceCategory.ACTIVITY,
            address = "Đèo Prenn, Phường 3, Đà Lạt",
            description = "Trải nghiệm máng trượt xuyên rừng thông dài nhất Đông Nam Á và đu dây vượt thác mạo hiểm.",
            coverUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=800&auto=format&fit=crop",
            checkinRadiusMeters = 300,
            priceRangeVnd = 170000,
            tags = listOf("Máng trượt", "Thác nước", "Mạo hiểm"),
            isVisited = true
        ),
        Place(
            id = "p_ldg_3",
            provinceId = 2,
            provinceCode = "LDG",
            name = "Cà Phê Thung Lũng Mây",
            category = PlaceCategory.CAFE,
            address = "Hoàng Hoa Thám, Phường 10, Đà Lạt",
            description = "Quán cà phê nhà gỗ view thung lũng sương mù lãng đãng, góc sống ảo ngắm hoàng hôn tuyệt đẹp.",
            coverUrl = "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop",
            checkinRadiusMeters = 100,
            priceRangeVnd = 65000,
            tags = listOf("Cà phê view", "Hoàng hôn", "Nhà gỗ"),
            isVisited = true
        ),
        Place(
            id = "p_ldg_4",
            provinceId = 2,
            provinceCode = "LDG",
            name = "Bánh Căn Nhà Chung",
            category = PlaceCategory.FOOD,
            address = "Số 1 Nhà Chung, Phường 3, Đà Lạt",
            description = "Quán bánh căn nóng giòn trứ danh ăn kèm chén xíu mại béo ngậy thơm nức mũi.",
            coverUrl = "https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?w=800&auto=format&fit=crop",
            checkinRadiusMeters = 80,
            priceRangeVnd = 45000,
            tags = listOf("Đặc sản", "Ăn sáng", "Xíu mại"),
            isVisited = true
        ),
        Place(
            id = "p_ldg_5",
            provinceId = 2,
            provinceCode = "LDG",
            name = "Ga Xe Lửa Đà Lạt",
            category = PlaceCategory.CULTURE,
            address = "Quang Trung, Phường 9, Đà Lạt",
            description = "Nhà ga cổ kính xây dựng từ thời Pháp với kiến trúc ba mái chóp phỏng theo đỉnh Langbiang.",
            coverUrl = "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=800&auto=format&fit=crop",
            checkinRadiusMeters = 150,
            priceRangeVnd = 20000,
            tags = listOf("Cổ điển", "Check-in", "Lịch sử"),
            isVisited = false
        ),
        // Hà Nội
        Place(
            id = "p_hn_1",
            provinceId = 1,
            provinceCode = "HN",
            name = "Văn Miếu - Quốc Tử Giám",
            category = PlaceCategory.CULTURE,
            address = "58 Quốc Tử Giám, Đống Đa, Hà Nội",
            description = "Trường đại học đầu tiên của Việt Nam với Khuê Văn Các biểu tượng tri thức Thăng Long.",
            coverUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop",
            checkinRadiusMeters = 200,
            priceRangeVnd = 70000,
            tags = listOf("Di tích", "Khuê Văn Các", "Lịch sử"),
            isVisited = true
        ),
        Place(
            id = "p_hn_2",
            provinceId = 1,
            provinceCode = "HN",
            name = "Cà Phê Giảng (Cà Phê Trứng)",
            category = PlaceCategory.FOOD,
            address = "39 Nguyễn Hữu Huân, Hoàn Kiếm, Hà Nội",
            description = "Nơi khai sinh món cà phê trứng thơm ngậy trứ danh Hà thành từ năm 1946.",
            coverUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&auto=format&fit=crop",
            checkinRadiusMeters = 80,
            priceRangeVnd = 35000,
            tags = listOf("Cà phê trứng", "Phố cổ", "Đặc sản"),
            isVisited = true
        )
    )

    val sampleTrips = listOf(
        Trip(
            id = "trip_dalat_3n2d",
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
        Trip(
            id = "trip_hagiang_4n3d",
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
        Trip(
            id = "trip_danang_3n2d",
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

    val posts = listOf(
        Post(
            id = "post_1",
            authorName = "Thảo Linh",
            authorHandle = "linh.travels",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
            hasRecentCheckIn = true,
            locationLabel = "Đà Lạt, Lâm Đồng",
            provinceCode = "LDG",
            timeAgo = "2 giờ trước",
            caption = "Bình minh 5h sáng trên Đồi Chè Cầu Đất đẹp như tranh vẽ ☕ Mây luồn qua từng ngọn thông, se se lạnh 16 độ cực chill!",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 0.78f,
            attachedTrip = sampleTrips[0],
            likeCount = 284,
            commentCount = 38,
            saveCount = 142,
            shareCount = 45,
            isLiked = true,
            tags = listOf("DaLat", "SanMay", "LuminaTravel", "GenZ")
        ),
        Post(
            id = "post_2",
            authorName = "Minh Trí",
            authorHandle = "tri.wanderer",
            authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop",
            hasRecentCheckIn = false,
            locationLabel = "Hà Giang",
            provinceCode = "HGG",
            timeAgo = "5 giờ trước",
            caption = "Chạm tay vào cột cờ Lũng Cú — cảm giác thiêng liêng nơi địa đầu Tổ quốc không từ ngữ nào tả xiết 🇻🇳",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 1.15f,
            attachedTrip = sampleTrips[1],
            likeCount = 512,
            commentCount = 64,
            saveCount = 210,
            shareCount = 89,
            isLiked = false,
            tags = listOf("HaGiang", "LungCu", "VietnamTravel")
        ),
        Post(
            id = "post_3",
            authorName = "Khánh Vy",
            authorHandle = "vy.explorer",
            authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop",
            hasRecentCheckIn = true,
            locationLabel = "Hà Nội",
            provinceCode = "HN",
            timeAgo = "1 ngày trước",
            caption = "Mùa thu Hà Nội có hoa sữa thơm nồng và cốc cà phê trứng ấm nóng ở Giảng phố cổ 🍂",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 0.85f,
            attachedTrip = null,
            likeCount = 189,
            commentCount = 19,
            saveCount = 76,
            shareCount = 23,
            isLiked = false,
            tags = listOf("HaNoi", "Autumn", "PhoCo")
        ),
        Post(
            id = "post_4",
            authorName = "Đức Anh",
            authorHandle = "anh.vietnam",
            authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop",
            hasRecentCheckIn = false,
            locationLabel = "Đà Nẵng",
            provinceCode = "DNG",
            timeAgo = "2 ngày trước",
            caption = "Cầu Vàng Bà Nà Hills lúc hoàng hôn buông xuống — vẻ đẹp kỳ vĩ giữa mây ngàn Đà Nẵng 🌉",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 0.72f,
            attachedTrip = sampleTrips[2],
            likeCount = 423,
            commentCount = 52,
            saveCount = 312,
            shareCount = 98,
            isLiked = true,
            tags = listOf("DaNang", "CauVang", "BaNaHills")
        )
    )

    val frames = listOf(
        LuminaFrame(
            id = "frame_national_day",
            name = "Quốc Khánh 2/9",
            badgeLabel = "🇻🇳 Quốc Khánh 2/9",
            subtitle = "2/9/1945 - 2/9/2026 · ĐỘC LẬP TỰ DO HẠNH PHÚC",
            topTitle = "★ VIỆT NAM TRONG TIM TÔI ★",
            bottomTagline = "LUMINA PASSPORT EVENT STAMP • 2026",
            previewColor = 0xFFD93025,
            isSeasonal = true
        ),
        LuminaFrame(
            id = "frame_hanoi_autumn",
            name = "Thu Hà Nội",
            badgeLabel = "🍂 Thu Hà Nội",
            subtitle = "PHỐ CỔ HOÀN KIẾM · HOA SỮA & CÀ PHÊ TRỨNG",
            topTitle = "HÀ NỘI 36 PHỐ PHƯỜNG",
            bottomTagline = "LUMINA STAMP • HANOI AUTUMN VIBES",
            previewColor = 0xFFB26A00,
            provinceCode = "HN"
        ),
        LuminaFrame(
            id = "frame_90s_polaroid",
            name = "90s Polaroid",
            badgeLabel = "🎞 90s Polaroid",
            subtitle = "MEMORIES OF VIETNAM · TIMELESS MOMENTS",
            topTitle = "LUMINA INSTANT FILM",
            bottomTagline = "EST. 2026 • GEN Z TRAVEL DIARY",
            previewColor = 0xFF5A6B64
        ),
        LuminaFrame(
            id = "frame_dalat_mist",
            name = "Sương Mù Đà Lạt",
            badgeLabel = "🌲 Đà Lạt 1500M",
            subtitle = "SĂN MÂY ĐỒI THÔNG · 16°C SE LẠNH",
            topTitle = "XỨ SỞ NGÀN HOA & SƯƠNG",
            bottomTagline = "LUMINA PASSPORT • LAM DONG STAMP",
            previewColor = 0xFF0A5C42,
            provinceCode = "LDG"
        ),
        LuminaFrame(
            id = "frame_wanderlist",
            name = "Wanderlist Explorer",
            badgeLabel = "🧭 Wanderlist",
            subtitle = "34 PROVINCES ODYSSEY · GEN Z EXPLORER",
            topTitle = "LUMINA TRAVEL PASSPORT",
            bottomTagline = "OFFICIAL VIETNAM FOOTPRINT RECORD",
            previewColor = 0xFF043027
        ),
        LuminaFrame(
            id = "frame_saigon_postcard",
            name = "Bưu Thiếp Sài Gòn",
            badgeLabel = "💌 Bưu Thiếp Sài Gòn",
            subtitle = "HÒN NGỌC VIỄN ĐÔNG · PHỐ XÁ NHỘN NHỊP",
            topTitle = "GREETINGS FROM SAIGON",
            bottomTagline = "POSTCARD FROM VIETNAM • 2026",
            previewColor = 0xFF12855F,
            provinceCode = "HCM"
        )
    )

    val badges = listOf(
        AchievementBadge(
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
        AchievementBadge(
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
        AchievementBadge(
            id = "b_3",
            code = "province_10",
            name = "Người thuộc đường",
            description = "Đã in dấu chân tại 10 tỉnh thành khắp đất nước.",
            iconEmoji = "🧭",
            isEarned = false,
            progressCurrent = 5,
            progressMax = 10
        ),
        AchievementBadge(
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
        AchievementBadge(
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
        AchievementBadge(
            id = "b_6",
            code = "foodie",
            name = "Bụng không đáy",
            description = "Check-in 20 quán ẩm thực & đặc sản vùng miền.",
            iconEmoji = "🍜",
            isEarned = false,
            progressCurrent = 14,
            progressMax = 20
        ),
        AchievementBadge(
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
        AchievementBadge(
            id = "b_8",
            code = "frame_collector",
            name = "Nhà sưu tầm khung",
            description = "Đã chụp ảnh và sử dụng 5 khung ảnh sự kiện khác nhau.",
            iconEmoji = "🖼",
            isEarned = false,
            progressCurrent = 4,
            progressMax = 5
        )
    )

    val notifications = listOf(
        NotificationItem(
            id = "notif_1",
            isSocial = false,
            title = "Gợi ý check-in gần bạn",
            message = "Bạn đang ở gần Đồi chè Cầu Đất (Lâm Đồng) — chạm để đóng dấu hộ chiếu nhé!",
            timeAgo = "10 phút trước",
            actionType = "checkin_nearby"
        ),
        NotificationItem(
            id = "notif_2",
            isSocial = true,
            actorName = "Minh Trí",
            actorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop",
            title = "Tương tác mới",
            message = "minh.tri và 12 người khác đã thích bài viết Đà Lạt của bạn.",
            timeAgo = "30 phút trước",
            previewUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=200&auto=format&fit=crop"
        ),
        NotificationItem(
            id = "notif_3",
            isSocial = false,
            title = "Tiến độ tỉnh Lâm Đồng",
            message = "Bạn đã hoàn thành 4/5 điểm gợi ý tại Lâm Đồng. Còn Ga Đà Lạt nữa thôi!",
            timeAgo = "2 giờ trước",
            actionType = "province_progress"
        ),
        NotificationItem(
            id = "notif_4",
            isSocial = true,
            actorName = "Khánh Vy",
            actorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop",
            title = "Bình luận mới",
            message = "vy.explorer: 'Góc chụp này đỉnh quá Linh ơi! Đi lúc mấy giờ thế cậu?'",
            timeAgo = "3 giờ trước"
        ),
        NotificationItem(
            id = "notif_5",
            isSocial = false,
            title = "Huy hiệu mở khoá",
            message = "Chúc mừng! Bạn vừa mở khoá huy hiệu ☁️ Kẻ săn mây.",
            timeAgo = "Hôm qua",
            actionType = "badge_earned"
        )
    )

    val savedBoards = listOf(
        SavedBoard(
            id = "b_1",
            name = "Đà Lạt mùa sau",
            coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            postCount = 14
        ),
        SavedBoard(
            id = "b_2",
            name = "Quán cà phê xinh",
            coverUrl = "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop",
            postCount = 28
        ),
        SavedBoard(
            id = "b_3",
            name = "Outfit đi biển",
            coverUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop",
            postCount = 9
        )
    )

    val checkIns = mutableListOf(
        CheckInRecord(
            id = "chk_1",
            placeName = "Đồi Chè Cầu Đất",
            provinceName = "Lâm Đồng",
            provinceCode = "LDG",
            timestamp = "2026-09-12 06:15",
            formattedDate = "12/09/2026",
            method = CheckInMethod.QUICK,
            photoUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            trustScore = 1.0f
        ),
        CheckInRecord(
            id = "chk_2",
            placeName = "Bánh Căn Nhà Chung",
            provinceName = "Lâm Đồng",
            provinceCode = "LDG",
            timestamp = "2026-09-12 08:45",
            formattedDate = "12/09/2026",
            method = CheckInMethod.PHOTO,
            photoUrl = "https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?w=800&auto=format&fit=crop",
            trustScore = 1.0f
        ),
        CheckInRecord(
            id = "chk_3",
            placeName = "Thác Datanla",
            provinceName = "Lâm Đồng",
            provinceCode = "LDG",
            timestamp = "2026-09-13 10:20",
            formattedDate = "13/09/2026",
            method = CheckInMethod.QUICK,
            trustScore = 1.0f
        ),
        CheckInRecord(
            id = "chk_4",
            placeName = "Văn Miếu - Quốc Tử Giám",
            provinceName = "Hà Nội",
            provinceCode = "HN",
            timestamp = "2026-08-15 15:30",
            formattedDate = "15/08/2026",
            method = CheckInMethod.PHOTO,
            trustScore = 1.0f
        ),
        CheckInRecord(
            id = "chk_5",
            placeName = "Đèo Mã Pí Lèng",
            provinceName = "Hà Giang",
            provinceCode = "HGG",
            timestamp = "2026-07-22 14:00",
            formattedDate = "22/07/2026",
            method = CheckInMethod.QUICK,
            trustScore = 1.0f
        )
    )

    val defaultProfile = UserProfile(
        id = "u_linh",
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
        totalSavesCount = 3420
    )
}
