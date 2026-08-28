package com.example.data.local.seed

import com.example.data.local.entity.CommentEntity

object CommentSeed {
    val data = listOf(
        CommentEntity(
            id = "c_1",
            postId = "post_1",
            userId = "u_2",
            authorName = "Minh Trí",
            authorHandle = "tri.wanderer",
            authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop",
            content = "Góc săn mây này đẹp quá Linh ơi! Đi lúc 5h sáng có lạnh lắm không cậu?",
            timeAgo = "1 giờ trước",
            likeCount = 12,
            isLiked = true,
            createdAt = System.currentTimeMillis() - 3600000
        ),
        CommentEntity(
            id = "c_2",
            postId = "post_1",
            userId = "u_3",
            authorName = "Khánh Vy",
            authorHandle = "vy.explorer",
            authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop",
            content = "Hôm nào cho mình xin lịch trình chi tiết với nhé, nhìn ảnh mê xỉu luôn á! ✨",
            timeAgo = "45 phút trước",
            likeCount = 8,
            isLiked = false,
            createdAt = System.currentTimeMillis() - 2700000
        ),
        CommentEntity(
            id = "c_3",
            postId = "post_2",
            userId = "u_1",
            authorName = "Thảo Linh",
            authorHandle = "linh.travels",
            authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop",
            content = "Tự hào Việt Nam quá anh Trí! Chúc mừng anh đã chạm cột cờ 🇻🇳",
            timeAgo = "3 giờ trước",
            likeCount = 24,
            isLiked = true,
            createdAt = System.currentTimeMillis() - 10800000
        )
    )
}
