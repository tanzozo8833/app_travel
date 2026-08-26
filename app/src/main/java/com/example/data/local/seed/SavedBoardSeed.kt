package com.example.data.local.seed

import com.example.data.local.entity.SavedBoardEntity

object SavedBoardSeed {
    val data = listOf(
        SavedBoardEntity(
            id = "b_1",
            userId = "u_1",
            name = "Đà Lạt mùa sau",
            coverUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            postCount = 14,
            isPrivate = true
        ),
        SavedBoardEntity(
            id = "b_2",
            userId = "u_1",
            name = "Quán cà phê xinh",
            coverUrl = "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800&auto=format&fit=crop",
            postCount = 28,
            isPrivate = false
        ),
        SavedBoardEntity(
            id = "b_3",
            userId = "u_1",
            name = "Outfit đi biển",
            coverUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=800&auto=format&fit=crop",
            postCount = 9,
            isPrivate = true
        )
    )
}
