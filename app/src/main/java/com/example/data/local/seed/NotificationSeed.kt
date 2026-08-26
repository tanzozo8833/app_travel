package com.example.data.local.seed

import com.example.data.local.entity.NotificationEntity

object NotificationSeed {
    val data = listOf(
        NotificationEntity(
            id = "notif_1",
            userId = "u_1",
            isSocial = false,
            title = "Gợi ý check-in gần bạn",
            message = "Bạn đang ở gần Đồi chè Cầu Đất (Lâm Đồng) — chạm để đóng dấu hộ chiếu nhé!",
            timeAgo = "10 phút trước",
            actionType = "checkin_nearby",
            isRead = false,
            createdAt = System.currentTimeMillis() - 600000
        ),
        NotificationEntity(
            id = "notif_2",
            userId = "u_1",
            isSocial = true,
            actorName = "Minh Trí",
            actorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop",
            title = "Tương tác mới",
            message = "minh.tri và 12 người khác đã thích bài viết Đà Lạt của bạn.",
            timeAgo = "30 phút trước",
            previewUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=200&auto=format&fit=crop",
            isRead = false,
            createdAt = System.currentTimeMillis() - 1800000
        ),
        NotificationEntity(
            id = "notif_3",
            userId = "u_1",
            isSocial = false,
            title = "Tiến độ tỉnh Lâm Đồng",
            message = "Bạn đã hoàn thành 4/5 điểm gợi ý tại Lâm Đồng. Còn Ga Đà Lạt nữa thôi!",
            timeAgo = "2 giờ trước",
            actionType = "province_progress",
            isRead = true,
            createdAt = System.currentTimeMillis() - 7200000
        ),
        NotificationEntity(
            id = "notif_4",
            userId = "u_1",
            isSocial = true,
            actorName = "Khánh Vy",
            actorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop",
            title = "Bình luận mới",
            message = "vy.explorer: 'Góc chụp này đỉnh quá Linh ơi! Đi lúc mấy giờ thế cậu?'",
            timeAgo = "3 giờ trước",
            isRead = true,
            createdAt = System.currentTimeMillis() - 10800000
        ),
        NotificationEntity(
            id = "notif_5",
            userId = "u_1",
            isSocial = false,
            title = "Huy hiệu mở khoá",
            message = "Chúc mừng! Bạn vừa mở khoá huy hiệu ☁️ Kẻ săn mây.",
            timeAgo = "Hôm qua",
            actionType = "badge_earned",
            isRead = true,
            createdAt = System.currentTimeMillis() - 86400000
        )
    )
}
