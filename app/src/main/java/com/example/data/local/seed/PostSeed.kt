package com.example.data.local.seed

import com.example.data.local.entity.PostEntity

object PostSeed {
    val data = listOf(
        PostEntity(
            id = "post_1",
            userId = "u_1",
            authorName = "Thảo Linh",
            authorHandle = "linh.travels",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
            hasRecentCheckIn = true,
            locationLabel = "Đà Lạt, Lâm Đồng",
            provinceCode = "LDG",
            timeAgo = "2 giờ trước",
            caption = "Bình minh 5h sáng trên Đồi Chè Cầu Đất đẹp như tranh vẽ. Mây luồn qua từng ngọn đồi thông xanh ngắt, se se lạnh 16 độ cực chill!",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 0.8f,
            attachedTripId = "trip_dalat_3n2d",
            likeCount = 284,
            commentCount = 38,
            saveCount = 142,
            shareCount = 45,
            isLiked = true,
            isSaved = true,
            tags = listOf("DaLat", "SanMay", "LuminaTravel", "GenZ"),
            createdAt = System.currentTimeMillis() - 7200000
        ),
        PostEntity(
            id = "post_2",
            userId = "u_2",
            authorName = "Minh Trí",
            authorHandle = "tri.wanderer",
            authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop",
            hasRecentCheckIn = false,
            locationLabel = "Hà Giang",
            provinceCode = "HGG",
            timeAgo = "5 giờ trước",
            caption = "Chạm tay vào cột cờ Lũng Cú và đứng trên đỉnh Mã Pí Lèng nhìn xuống dòng sông Nho Quế xanh ngọc bích — cảm giác thiêng liêng nơi địa đầu Tổ quốc không từ ngữ nào tả xiết.",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 1.45f,
            attachedTripId = "trip_hagiang_4n3d",
            likeCount = 512,
            commentCount = 64,
            saveCount = 210,
            shareCount = 89,
            isLiked = false,
            isSaved = true,
            tags = listOf("HaGiang", "LungCu", "VietnamTravel", "MaPiLeng"),
            createdAt = System.currentTimeMillis() - 18000000
        ),
        PostEntity(
            id = "post_3",
            userId = "u_3",
            authorName = "Khánh Vy",
            authorHandle = "vy.explorer",
            authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop",
            hasRecentCheckIn = true,
            locationLabel = "Hà Nội",
            provinceCode = "HN",
            timeAgo = "1 ngày trước",
            caption = "Mùa thu Hà Nội có hoa sữa thơm nồng, dạo bước quanh Hồ Gươm ngắm Tháp Rùa và thưởng thức cốc cà phê trứng ấm nóng ở Giảng phố cổ.",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 1.0f,
            attachedTripId = null,
            likeCount = 189,
            commentCount = 19,
            saveCount = 76,
            shareCount = 23,
            isLiked = false,
            isSaved = false,
            tags = listOf("HaNoi", "Autumn", "PhoCo", "CafeTrung"),
            createdAt = System.currentTimeMillis() - 86400000
        ),
        PostEntity(
            id = "post_4",
            userId = "u_4",
            authorName = "Đức Anh",
            authorHandle = "anh.vietnam",
            authorAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop",
            hasRecentCheckIn = false,
            locationLabel = "Đà Nẵng",
            provinceCode = "DNG",
            timeAgo = "2 ngày trước",
            caption = "Cầu Vàng Bà Nà Hills lúc hoàng hôn buông xuống — vẻ đẹp kỳ vĩ giữa mây ngàn Đà Nẵng, kết hợp tắm biển Mỹ Khê và ăn hải sản đêm.",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 1.55f,
            attachedTripId = "trip_danang_3n2d",
            likeCount = 423,
            commentCount = 52,
            saveCount = 312,
            shareCount = 98,
            isLiked = true,
            isSaved = true,
            tags = listOf("DaNang", "CauVang", "BaNaHills", "MyKhe"),
            createdAt = System.currentTimeMillis() - 172800000
        ),
        PostEntity(
            id = "post_5",
            userId = "u_5",
            authorName = "Hương Giang",
            authorHandle = "giang.travel",
            authorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&auto=format&fit=crop",
            hasRecentCheckIn = true,
            locationLabel = "Ninh Bình",
            provinceCode = "NBH",
            timeAgo = "3 ngày trước",
            caption = "Tràng An non nước hữu tình, chèo thuyền xuyên qua các hang động kỳ ảo và ngắm lúa chín vàng óng bên dòng sông Ngô Đồng.",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 1.0f,
            attachedTripId = null,
            likeCount = 339,
            commentCount = 28,
            saveCount = 185,
            shareCount = 41,
            isLiked = true,
            isSaved = true,
            tags = listOf("NinhBinh", "TrangAn", "HangMua", "TamCoc"),
            createdAt = System.currentTimeMillis() - 259200000
        ),
        PostEntity(
            id = "post_6",
            userId = "u_6",
            authorName = "Hoàng Long",
            authorHandle = "long.wander",
            authorAvatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop",
            hasRecentCheckIn = false,
            locationLabel = "Phú Quốc, Kiên Giang",
            provinceCode = "KGG",
            timeAgo = "4 ngày trước",
            caption = "Hoàng hôn Sunset Sanato Phú Quốc chưa bao giờ làm du khách thất vọng. Biển trong vắt cát trắng mịn như nhung.",
            mediaUrls = listOf(
                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&auto=format&fit=crop",
                "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800&auto=format&fit=crop"
            ),
            aspectRatio = 1.2f,
            attachedTripId = null,
            likeCount = 612,
            commentCount = 74,
            saveCount = 288,
            shareCount = 112,
            isLiked = true,
            isSaved = true,
            tags = listOf("PhuQuoc", "Sunset", "Beach", "KienGiang"),
            createdAt = System.currentTimeMillis() - 345600000
        )
    )
}
