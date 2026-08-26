package com.example.data.local.seed

import com.example.data.local.entity.CheckInEntity
import com.example.data.model.CheckInMethod

object CheckInSeed {
    val data = listOf(
        CheckInEntity(
            id = "chk_1",
            userId = "u_1",
            placeName = "Đồi Chè Cầu Đất",
            provinceName = "Lâm Đồng",
            provinceCode = "LDG",
            timestamp = "2026-09-12 06:15",
            formattedDate = "12/09/2026",
            method = CheckInMethod.QUICK,
            photoUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=800&auto=format&fit=crop",
            trustScore = 1.0f,
            lat = 11.9056,
            lng = 108.5583,
            isInCityVault = true
        ),
        CheckInEntity(
            id = "chk_2",
            userId = "u_1",
            placeName = "Bánh Căn Nhà Chung",
            provinceName = "Lâm Đồng",
            provinceCode = "LDG",
            timestamp = "2026-09-12 08:45",
            formattedDate = "12/09/2026",
            method = CheckInMethod.PHOTO,
            photoUrl = "https://images.unsplash.com/photo-1569058242253-92a9c755a0ec?w=800&auto=format&fit=crop",
            trustScore = 1.0f,
            lat = 11.9350,
            lng = 108.4380,
            isInCityVault = true
        ),
        CheckInEntity(
            id = "chk_3",
            userId = "u_1",
            placeName = "Thác Datanla",
            provinceName = "Lâm Đồng",
            provinceCode = "LDG",
            timestamp = "2026-09-13 10:20",
            formattedDate = "13/09/2026",
            method = CheckInMethod.QUICK,
            photoUrl = null,
            trustScore = 1.0f,
            lat = 11.9022,
            lng = 108.4489,
            isInCityVault = false
        ),
        CheckInEntity(
            id = "chk_4",
            userId = "u_1",
            placeName = "Văn Miếu - Quốc Tử Giám",
            provinceName = "Hà Nội",
            provinceCode = "HN",
            timestamp = "2026-08-15 15:30",
            formattedDate = "15/08/2026",
            method = CheckInMethod.PHOTO,
            photoUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&auto=format&fit=crop",
            trustScore = 1.0f,
            lat = 21.0293,
            lng = 105.8360,
            isInCityVault = true
        ),
        CheckInEntity(
            id = "chk_5",
            userId = "u_1",
            placeName = "Đèo Mã Pí Lèng",
            provinceName = "Hà Giang",
            provinceCode = "HGG",
            timestamp = "2026-07-22 14:00",
            formattedDate = "22/07/2026",
            method = CheckInMethod.QUICK,
            photoUrl = null,
            trustScore = 1.0f,
            lat = 23.2389,
            lng = 105.4180,
            isInCityVault = false
        )
    )
}
