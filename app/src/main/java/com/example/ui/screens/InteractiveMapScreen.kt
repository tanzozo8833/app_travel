package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.CheckInRecord
import com.example.data.model.Province
import com.example.ui.theme.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MapLayerStyle {
    TERRAIN,   // Địa hình sinh thái Xanh
    SATELLITE, // Vệ tinh đêm Neon
    CLASSIC    // Bản đồ Lữ hành Cổ điển
}

data class MapMarkerItem(
    val id: String,
    val title: String,
    val provinceName: String,
    val provinceCode: String,
    val region: String, // "Bắc", "Trung", "Tây Nguyên", "Nam", "Biển Đảo"
    val lat: Double,
    val lng: Double,
    val photoUrl: String,
    val isVisited: Boolean,
    val visitedDate: String = "12/09/2026",
    val stampSymbol: String = "★",
    val highlightTag: String = "Di sản UNESCO",
    val checkinRadiusMeters: Int = 250
)

data class MarkerCluster(
    val id: String,
    val name: String,
    val region: String,
    val lat: Double,
    val lng: Double,
    val markers: List<MapMarkerItem>
)

data class NearbyMatchedPlace(
    val id: String,
    val name: String,
    val provinceName: String,
    val provinceCode: String,
    val distanceMeters: Int,
    val isMatchWithinRadius: Boolean,
    val stampSymbol: String
)

@Composable
fun InteractiveMapScreen(
    provinces: List<Province>,
    checkIns: List<CheckInRecord>,
    onQuickCheckIn: (placeName: String, provinceCode: String, provinceName: String) -> Unit,
    onProvinceClick: (Province) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var layerStyle by remember { mutableStateOf(MapLayerStyle.TERRAIN) }
    var selectedMarker by remember { mutableStateOf<MapMarkerItem?>(null) }
    var selectedRegionFilter by remember { mutableStateOf("Tất cả") }
    var showSpatialDebugHud by remember { mutableStateOf(false) }
    var showGpsCheckInDialog by remember { mutableStateOf(false) }

    // Map bounds in Vietnam (EPSG:4326)
    val vietnamBounds = remember {
        LatLngBounds(
            LatLng(8.0, 101.8),  // Southwest bound
            LatLng(23.6, 114.8)  // Northeast bound
        )
    }

    // Google Maps Camera initialization
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(16.0, 107.5), 5.2f)
    }

    // Map properties & UI Settings
    val mapProperties = remember(layerStyle) {
        MapProperties(
            latLngBoundsForCameraTarget = vietnamBounds,
            minZoomPreference = 4.5f,
            maxZoomPreference = 18f,
            mapType = when (layerStyle) {
                MapLayerStyle.TERRAIN -> MapType.TERRAIN
                MapLayerStyle.SATELLITE -> MapType.SATELLITE
                MapLayerStyle.CLASSIC -> MapType.NORMAL
            }
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    // Markers List (Visited and Suggested Unvisited)
    val allMarkers = remember {
        mutableStateListOf(
            // Visited places (Đặc, màu thương hiệu Lumina/Gold, ảnh thật, con dấu)
            MapMarkerItem(
                id = "m_hgg",
                title = "Đèo Mã Pí Lèng & Sông Nho Quế",
                provinceName = "Hà Giang",
                provinceCode = "HGG",
                region = "Bắc",
                lat = 23.2389,
                lng = 105.4180,
                photoUrl = "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "22/07/2026",
                stampSymbol = "🏔",
                highlightTag = "Tứ đại đỉnh đèo"
            ),
            MapMarkerItem(
                id = "m_hn",
                title = "Văn Miếu - Hồ Hoàn Kiếm",
                provinceName = "Hà Nội",
                provinceCode = "HN",
                region = "Bắc",
                lat = 21.0285,
                lng = 105.8542,
                photoUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "15/08/2026",
                stampSymbol = "🏛",
                highlightTag = "Thủ đô ngàn năm"
            ),
            MapMarkerItem(
                id = "m_qnh",
                title = "Vịnh Hạ Long Kỳ Quan",
                provinceName = "Quảng Ninh",
                provinceCode = "QNH",
                region = "Bắc",
                lat = 20.9101,
                lng = 107.0843,
                photoUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "08/08/2026",
                stampSymbol = "⛵",
                highlightTag = "Kỳ quan Thế giới"
            ),
            MapMarkerItem(
                id = "m_nbh",
                title = "Tràng An - Tam Cốc",
                provinceName = "Ninh Bình",
                provinceCode = "NBH",
                region = "Bắc",
                lat = 20.2506,
                lng = 105.9745,
                photoUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "10/08/2026",
                stampSymbol = "🚣",
                highlightTag = "Di sản Kép UNESCO"
            ),
            MapMarkerItem(
                id = "m_tth",
                title = "Đại Nội Kinh Thành Huế",
                provinceName = "Thừa Thiên Huế",
                provinceCode = "TTH",
                region = "Trung",
                lat = 16.4637,
                lng = 107.5909,
                photoUrl = "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "18/06/2026",
                stampSymbol = "🏯",
                highlightTag = "Cố đô Di sản"
            ),
            MapMarkerItem(
                id = "m_dng",
                title = "Cầu Vàng Bà Nà Hills",
                provinceName = "Đà Nẵng",
                provinceCode = "DNG",
                region = "Trung",
                lat = 15.9989,
                lng = 107.9961,
                photoUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "16/06/2026",
                stampSymbol = "🌉",
                highlightTag = "Thành phố đáng sống"
            ),
            MapMarkerItem(
                id = "m_qnm",
                title = "Phố Cổ Hội An",
                provinceName = "Quảng Nam",
                provinceCode = "QNM",
                region = "Trung",
                lat = 15.8801,
                lng = 108.3380,
                photoUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "14/06/2026",
                stampSymbol = "🏮",
                highlightTag = "Đèn lồng cổ kính"
            ),
            MapMarkerItem(
                id = "m_ldg",
                title = "Đồi Chè Cầu Đất & Săn Mây",
                provinceName = "Lâm Đồng",
                provinceCode = "LDG",
                region = "Tây Nguyên",
                lat = 11.9056,
                lng = 108.5583,
                photoUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "12/09/2026",
                stampSymbol = "🌲",
                highlightTag = "Xứ sở ngàn hoa"
            ),
            MapMarkerItem(
                id = "m_hcm",
                title = "Nhà Thờ Đức Bà & Bến Bạch Đằng",
                provinceName = "TP. Hồ Chí Minh",
                provinceCode = "HCM",
                region = "Nam",
                lat = 10.7798,
                lng = 106.6990,
                photoUrl = "https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "05/09/2026",
                stampSymbol = "🏙",
                highlightTag = "Hòn ngọc Viễn Đông"
            ),
            MapMarkerItem(
                id = "m_kgg",
                title = "Bãi Sao & Hòn Thơm",
                provinceName = "Kiên Giang",
                provinceCode = "KGG",
                region = "Biển Đảo",
                lat = 10.2899,
                lng = 103.9840,
                photoUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "02/05/2026",
                stampSymbol = "🏝",
                highlightTag = "Đảo Ngọc Phú Quốc"
            ),

            // Suggested Unvisited Places (Rỗng, mờ, viền nét đứt, gợi ý hành trình)
            MapMarkerItem(
                id = "m_sapa",
                title = "Đỉnh Fansipan Nóc Nhà Đông Dương",
                provinceName = "Lào Cai",
                provinceCode = "LCI",
                region = "Bắc",
                lat = 22.3034,
                lng = 103.7753,
                photoUrl = "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=400&auto=format&fit=crop",
                isVisited = false,
                stampSymbol = "⛰️",
                highlightTag = "Gợi ý khám phá"
            ),
            MapMarkerItem(
                id = "m_qb",
                title = "Động Phong Nha - Kẻ Bàng",
                provinceName = "Quảng Bình",
                provinceCode = "QBH",
                region = "Trung",
                lat = 17.5855,
                lng = 106.2829,
                photoUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=400&auto=format&fit=crop",
                isVisited = false,
                stampSymbol = "🦇",
                highlightTag = "Gợi ý khám phá"
            ),
            MapMarkerItem(
                id = "m_py",
                title = "Gành Đá Đĩa & Mũi Điện",
                provinceName = "Phú Yên",
                provinceCode = "PYE",
                region = "Trung",
                lat = 13.3533,
                lng = 109.2972,
                photoUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400&auto=format&fit=crop",
                isVisited = false,
                stampSymbol = "🌊",
                highlightTag = "Gợi ý khám phá"
            ),
            MapMarkerItem(
                id = "m_nt",
                title = "Tháp Bà Ponagar & Vịnh Nha Trang",
                provinceName = "Khánh Hòa",
                provinceCode = "KHA",
                region = "Trung",
                lat = 12.2605,
                lng = 109.1956,
                photoUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=400&auto=format&fit=crop",
                isVisited = false,
                stampSymbol = "🕌",
                highlightTag = "Gợi ý khám phá"
            ),
            MapMarkerItem(
                id = "m_cd",
                title = "Nghĩa trang Hàng Dương & Côn Đảo",
                provinceName = "Bà Rịa - Vũng Tàu",
                provinceCode = "BRV",
                region = "Biển Đảo",
                lat = 8.6833,
                lng = 106.6000,
                photoUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&auto=format&fit=crop",
                isVisited = false,
                stampSymbol = "🕊️",
                highlightTag = "Gợi ý khám phá"
            )
        )
    }

    val displayMarkers = remember(selectedRegionFilter, allMarkers.toList()) {
        if (selectedRegionFilter == "Tất cả") {
            allMarkers
        } else {
            allMarkers.filter { it.region == selectedRegionFilter }
        }
    }

    // Dynamic Zoom Clustering based on cameraPositionState.position.zoom (< 6.5f)
    val currentZoom = cameraPositionState.position.zoom
    val isClusteredMode = currentZoom < 6.5f

    val clusters = remember(displayMarkers, isClusteredMode) {
        if (!isClusteredMode) {
            emptyList()
        } else {
            val grouped = displayMarkers.groupBy { it.provinceName }
            grouped.map { (provName, items) ->
                val avgLat = items.map { it.lat }.average()
                val avgLng = items.map { it.lng }.average()
                MarkerCluster(
                    id = "cluster_${provName}",
                    name = provName,
                    region = items.first().region,
                    lat = avgLat,
                    lng = avgLng,
                    markers = items
                )
            }
        }
    }

    // Track points for visited route polyline
    val visitedTrackLatLngs = remember(displayMarkers) {
        displayMarkers.filter { it.isVisited }.map { LatLng(it.lat, it.lng) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ==========================================
        // 1. GoogleMap COMPOSABLE CONTAINER
        // ==========================================
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapClick = {
                selectedMarker = null
            }
        ) {
            // Check-in Radius Circle for selected marker
            selectedMarker?.let { marker ->
                Circle(
                    center = LatLng(marker.lat, marker.lng),
                    radius = marker.checkinRadiusMeters.toDouble(),
                    fillColor = Brand500.copy(alpha = 0.25f),
                    strokeColor = Gold500,
                    strokeWidth = 3f
                )
            }

            // GPS Travel Route Polyline (2 layers: outer glow + inner bright)
            if (visitedTrackLatLngs.size > 1) {
                // Outer glowing line
                Polyline(
                    points = visitedTrackLatLngs,
                    color = Gold500.copy(alpha = 0.45f),
                    width = 14f,
                    geodesic = true
                )
                // Inner sharp route line
                Polyline(
                    points = visitedTrackLatLngs,
                    color = Gold500,
                    width = 5f,
                    geodesic = true
                )
            }

            // Sovereign Islands Overlay Markers (Hoàng Sa & Trường Sa) 🇻🇳
            // Quần đảo Hoàng Sa (16.5, 112.0)
            MarkerComposable(
                keys = arrayOf("sovereignty_hoang_sa"),
                state = rememberMarkerState(position = LatLng(16.5, 112.0)),
                title = "Quần đảo Hoàng Sa (TP. Đà Nẵng)",
                onClick = { true }
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xF0082019),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold500),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🇻🇳 Q.Đ HOÀNG SA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "(TP. Đà Nẵng)",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold500
                        )
                    }
                }
            }

            // Quần đảo Trường Sa (10.0, 114.0)
            MarkerComposable(
                keys = arrayOf("sovereignty_truong_sa"),
                state = rememberMarkerState(position = LatLng(10.0, 114.0)),
                title = "Quần đảo Trường Sa (T. Khánh Hòa)",
                onClick = { true }
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xF0082019),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold500),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🇻🇳 Q.Đ TRƯỜNG SA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "(T. Khánh Hòa)",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold500
                        )
                    }
                }
            }

            // Clusters (when zoom < 6.5f) OR Individual Markers (when zoom >= 6.5f)
            if (isClusteredMode) {
                clusters.forEach { cluster ->
                    val visitedCount = cluster.markers.count { it.isVisited }
                    val totalCount = cluster.markers.size

                    MarkerComposable(
                        keys = arrayOf<Any>(cluster.id, visitedCount, totalCount),
                        state = rememberMarkerState(position = LatLng(cluster.lat, cluster.lng)),
                        title = cluster.name,
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(cluster.lat, cluster.lng),
                                        7.8f
                                    )
                                )
                            }
                            true
                        }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Brand700,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold500),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(text = "🟢", fontSize = 9.sp)
                                Text(
                                    text = "${cluster.name} · $visitedCount/$totalCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    Icons.Default.ZoomIn,
                                    contentDescription = "Zoom",
                                    tint = Gold500,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Individual Markers
                displayMarkers.forEach { marker ->
                    val isSelected = selectedMarker?.id == marker.id
                    val markerSize = if (isSelected) 56.dp else 44.dp

                    MarkerComposable(
                        keys = arrayOf<Any>(marker.id, isSelected, marker.isVisited),
                        state = rememberMarkerState(position = LatLng(marker.lat, marker.lng)),
                        title = marker.title,
                        snippet = marker.provinceName,
                        onClick = {
                            selectedMarker = marker
                            true
                        }
                    ) {
                        if (marker.isVisited) {
                            // Visited Marker: Circular Image with Gold border and Stamp Emblem
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(markerSize)
                            ) {
                                // Outer glow circle
                                Box(
                                    modifier = Modifier
                                        .size(markerSize)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Gold500.copy(alpha = 0.7f) else Brand500.copy(alpha = 0.4f)
                                        )
                                )

                                // Circular photo
                                AsyncImage(
                                    model = marker.photoUrl,
                                    contentDescription = marker.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(markerSize - 8.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.8.dp,
                                            color = if (isSelected) Gold500 else Color.White,
                                            shape = CircleShape
                                        )
                                )

                                // Stamp badge emblem
                                Surface(
                                    shape = CircleShape,
                                    color = Brand900,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold500),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = marker.stampSymbol,
                                            fontSize = 8.5.sp,
                                            color = Gold500
                                        )
                                    }
                                }
                            }
                        } else {
                            // Suggested Unvisited Marker: Translucent circle with plus icon
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(markerSize)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xCC082019),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold500),
                                    modifier = Modifier.size(markerSize - 6.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = marker.stampSymbol,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Gold500,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(15.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Chưa đến",
                                            tint = TextOnGold,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. TOP CONTROL OVERLAY
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEE082019)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x444ECBA5)),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🗺️ GOOGLE MAPS ENGINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Gold500
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isClusteredMode) Color(0x33FFA726) else Color(0x334CAF50)
                                ) {
                                    Text(
                                        text = if (isClusteredMode) "Cluster Mode" else "Point View",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isClusteredMode) Color(0xFFFFB74D) else Color(0xFF81C784),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Bản Đồ Hành Trình Việt Nam 🇻🇳",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        // Layer switch button
                        Row(
                            modifier = Modifier
                                .background(Color(0x33FFFFFF), RoundedCornerShape(999.dp))
                                .clickable {
                                    layerStyle = when (layerStyle) {
                                        MapLayerStyle.TERRAIN -> MapLayerStyle.SATELLITE
                                        MapLayerStyle.SATELLITE -> MapLayerStyle.CLASSIC
                                        MapLayerStyle.CLASSIC -> MapLayerStyle.TERRAIN
                                    }
                                }
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Đổi kiểu",
                                tint = Gold500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (layerStyle) {
                                    MapLayerStyle.TERRAIN -> "Địa hình"
                                    MapLayerStyle.SATELLITE -> "Vệ tinh"
                                    MapLayerStyle.CLASSIC -> "Mặc định"
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Region Filter Chips & Spatial Check-in CTA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            listOf("Tất cả", "Bắc", "Trung", "Tây Nguyên", "Nam", "Biển Đảo").forEach { region ->
                                val isSelected = selectedRegionFilter == region
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isSelected) Gold500 else Color(0x22FFFFFF),
                                    modifier = Modifier.clickable { selectedRegionFilter = region }
                                ) {
                                    Text(
                                        text = region,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) TextOnGold else Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // GPS Check-in Button on Map
                        Button(
                            onClick = { showGpsCheckInDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 3.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Gold500, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "Check-in GPS", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. FLOATING CONTROLS (+ / - / HUD / RESET)
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom in (+)
            Surface(
                shape = CircleShape,
                color = Color(0xDD082019),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold500),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom in", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Zoom out (-)
            Surface(
                shape = CircleShape,
                color = Color(0xDD082019),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334ECBA5)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        coroutineScope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom out", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Spatial HUD Toggle
            Surface(
                shape = CircleShape,
                color = if (showSpatialDebugHud) Gold500 else Color(0xDD082019),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold500),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(38.dp)
                    .clickable { showSpatialDebugHud = !showSpatialDebugHud }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = "Spatial HUD",
                        tint = if (showSpatialDebugHud) TextOnGold else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Reset Center
            Surface(
                shape = CircleShape,
                color = Color(0xDD082019),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334ECBA5)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(16.0, 107.5),
                                    5.2f
                                )
                            )
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Reset vị trí", tint = Gold500, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ==========================================
        // 4. SPATIAL ENGINE INFO HUD
        // ==========================================
        AnimatedVisibility(
            visible = showSpatialDebugHud,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 100.dp, end = 14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF002130F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x664ECBA5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛰️ POSTGIS & GOOGLE MAPS SPATIAL ENGINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Gold500
                        )
                        IconButton(onClick = { showSpatialDebugHud = false }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Bounding Box: ST_Intersects(location, ST_MakeEnvelope(101.8, 8.0, 114.8, 23.6, 4326))\n" +
                                "• Clustering: Dynamic Zoom Trigger (< 6.5f → Region Grouping)\n" +
                                "• Current Camera Zoom: ${String.format("%.2f", currentZoom)}x → ${if (isClusteredMode) "Cluster Mode" else "Point Mode"}\n" +
                                "• Sovereign Badges: 🇻🇳 Hoàng Sa (16.5°N, 112.0°E), Trường Sa (10.0°N, 114.0°E)",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFBEDBD0),
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // ==========================================
        // 5. SELECTED PIN PREVIEW DRAWER AT BOTTOM
        // ==========================================
        AnimatedVisibility(
            visible = selectedMarker != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 14.dp, end = 14.dp)
        ) {
            selectedMarker?.let { marker ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDE7E2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = marker.photoUrl,
                                contentDescription = marker.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (marker.isVisited) Brand100 else Color(0xFFFFECB3)
                                ) {
                                    Text(
                                        text = if (marker.isVisited) "Đã đến • ${marker.highlightTag}" else "Gợi ý khám phá",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (marker.isVisited) Brand700 else Color(0xFFF57F17),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (marker.isVisited) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "• ${marker.visitedDate}", fontSize = 9.sp, color = TextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = marker.title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "📍 ${marker.provinceName} • WGS84: ${String.format("%.4f", marker.lat)}, ${String.format("%.4f", marker.lng)}",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onQuickCheckIn(marker.title, marker.provinceCode, marker.provinceName)
                                        // Update marker status in local list
                                        val idx = allMarkers.indexOfFirst { it.id == marker.id }
                                        if (idx != -1) {
                                            allMarkers[idx] = allMarkers[idx].copy(
                                                isVisited = true,
                                                visitedDate = "16/08/2026"
                                            )
                                        }
                                        selectedMarker = selectedMarker?.copy(isVisited = true, visitedDate = "16/08/2026")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                    shape = RoundedCornerShape(999.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (marker.isVisited) "Đóng dấu lại ${marker.stampSymbol}" else "Đóng dấu lần đầu ⦿",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { selectedMarker = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // 6. SPATIAL CHECK-IN DIALOG
    // ==========================================
    if (showGpsCheckInDialog) {
        GpsSpatialCheckInDialog(
            provinces = provinces,
            onDismiss = { showGpsCheckInDialog = false },
            onConfirmCheckIn = { matchedPlace ->
                onQuickCheckIn(matchedPlace.name, matchedPlace.provinceCode, matchedPlace.provinceName)
                // Add marker to visited list if not yet
                val existingIdx = allMarkers.indexOfFirst { it.title == matchedPlace.name }
                if (existingIdx != -1) {
                    allMarkers[existingIdx] = allMarkers[existingIdx].copy(isVisited = true, visitedDate = "16/08/2026")
                } else {
                    allMarkers.add(
                        MapMarkerItem(
                            id = "m_${System.currentTimeMillis()}",
                            title = matchedPlace.name,
                            provinceName = matchedPlace.provinceName,
                            provinceCode = matchedPlace.provinceCode,
                            region = "Tây Nguyên",
                            lat = 11.9404,
                            lng = 108.4583,
                            photoUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=400&auto=format&fit=crop",
                            isVisited = true,
                            visitedDate = "16/08/2026",
                            stampSymbol = matchedPlace.stampSymbol,
                            highlightTag = "Check-in GPS Thực tế"
                        )
                    )
                }
                showGpsCheckInDialog = false
            }
        )
    }
}

@Composable
fun GpsSpatialCheckInDialog(
    provinces: List<Province>,
    onDismiss: () -> Unit,
    onConfirmCheckIn: (NearbyMatchedPlace) -> Unit
) {
    var isLocating by remember { mutableStateOf(true) }
    val detectedLat by remember { mutableDoubleStateOf(11.9404) }
    val detectedLng by remember { mutableDoubleStateOf(108.4583) }
    val accuracyMeters by remember { mutableIntStateOf(4) }
    var selectedMatch by remember { mutableStateOf<NearbyMatchedPlace?>(null) }

    // Simulated 5 nearest places matching ST_DWithin + ST_Distance
    val nearbyCandidates = remember {
        listOf(
            NearbyMatchedPlace("p_1", "Đồi Chè Cầu Đất & Săn Mây", "Lâm Đồng", "LDG", 42, true, "🌲"),
            NearbyMatchedPlace("p_2", "Hồ Tuyền Lâm & Thiền Viện Trúc Lâm", "Lâm Đồng", "LDG", 180, true, "🌸"),
            NearbyMatchedPlace("p_3", "Quảng Trường Lâm Viên & Hoa Dã Quỳ", "Lâm Đồng", "LDG", 320, false, "🌼"),
            NearbyMatchedPlace("p_4", "Thác Datanla & Máng trượt rừng", "Lâm Đồng", "LDG", 510, false, "💦"),
            NearbyMatchedPlace("p_5", "Ga Xe Lửa Cổ Đà Lạt", "Lâm Đồng", "LDG", 850, false, "🚂")
        )
    }

    LaunchedEffect(Unit) {
        delay(1600)
        isLocating = false
        selectedMatch = nearbyCandidates.first()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = BgSurface),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Brand100, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Brand700, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SPATIAL CHECK-IN ENGINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )
                            Text(
                                text = "Quét Vị Trí & Khớp Địa Điểm",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isLocating) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Brand700, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Đang lấy toạ độ GPS foreground...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "expo-location getCurrentPosition (~2s)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSunken, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Toạ độ WGS84: $detectedLat, $detectedLng",
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Độ chính xác: ±${accuracyMeters}m • ST_Contains: Lâm Đồng (LDG)",
                                    fontSize = 9.5.sp,
                                    color = TextSecondary
                                )
                            }
                            Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFE8F5E9)) {
                                Text(
                                    text = "GPS Khóa 🔒",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Khớp ST_DWithin (Radius 250m) - 5 Địa điểm gần nhất:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        nearbyCandidates.forEach { place ->
                            val isSelected = selectedMatch?.id == place.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Brand100 else BgCanvas,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Brand700 else Color(0xFFE0E0E0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMatch = place }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = place.stampSymbol, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = place.name,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Brand700 else TextPrimary
                                            )
                                            Text(
                                                text = "Cách ${place.distanceMeters}m • ${if (place.isMatchWithinRadius) "Trong bán kính khớp" else "Khoảng cách xa hơn"}",
                                                fontSize = 9.5.sp,
                                                color = if (place.isMatchWithinRadius) Brand700 else TextSecondary
                                            )
                                        }
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMatch = place },
                                        colors = RadioButtonDefaults.colors(selectedColor = Brand700)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    selectedMatch?.let { match ->
                        Button(
                            onClick = { onConfirmCheckIn(match) },
                            colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Lưu Check-in & Đóng Dấu (Point 4326) ⦿",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
