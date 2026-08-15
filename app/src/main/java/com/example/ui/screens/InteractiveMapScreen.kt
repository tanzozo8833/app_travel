package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CheckInRecord
import com.example.data.model.Province
import com.example.ui.components.StampBadge
import com.example.ui.theme.*

enum class MapLayerStyle {
    TERRAIN,
    SATELLITE,
    DARK
}

data class MapMarkerItem(
    val id: String,
    val title: String,
    val provinceName: String,
    val provinceCode: String,
    val lat: Double,
    val lng: Double,
    val photoUrl: String,
    val isVisited: Boolean,
    val visitedDate: String = "12/09/2026",
    val stampSymbol: String = "★"
)

@Composable
fun InteractiveMapScreen(
    provinces: List<Province>,
    checkIns: List<CheckInRecord>,
    onQuickCheckIn: (placeName: String, provinceCode: String, provinceName: String) -> Unit,
    onProvinceClick: (Province) -> Unit,
    modifier: Modifier = Modifier
) {
    var layerStyle by remember { mutableStateOf(MapLayerStyle.TERRAIN) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset(0f, 0f)) }
    var selectedMarker by remember { mutableStateOf<MapMarkerItem?>(null) }

    // Map bounds in Vietnam
    // Lat range: ~8.5 (Cà Mau) to 23.4 (Hà Giang) -> height = ~15 deg
    // Lng range: ~102.1 (Điện Biên) to 109.5 (Phú Yên) -> width = ~7.5 deg
    val minLat = 8.3
    val maxLat = 23.5
    val minLng = 102.0
    val maxLng = 110.0

    // Build markers list from check-ins and provinces
    val markers = remember(checkIns, provinces) {
        listOf(
            MapMarkerItem(
                id = "m_hgg",
                title = "Đèo Mã Pí Lèng",
                provinceName = "Hà Giang",
                provinceCode = "HGG",
                lat = 22.8233,
                lng = 104.9839,
                photoUrl = "https://images.unsplash.com/photo-1570789210967-2cac24afeb00?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "22/07/2026",
                stampSymbol = "🏔"
            ),
            MapMarkerItem(
                id = "m_hn",
                title = "Văn Miếu - Quốc Tử Giám",
                provinceName = "Hà Nội",
                provinceCode = "HN",
                lat = 21.0285,
                lng = 105.8542,
                photoUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "15/08/2026",
                stampSymbol = "🏛"
            ),
            MapMarkerItem(
                id = "m_nbh",
                title = "Tràng An - Bái Đính",
                provinceName = "Ninh Bình",
                provinceCode = "NBH",
                lat = 20.2506,
                lng = 105.9745,
                photoUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "10/08/2026",
                stampSymbol = "🚣"
            ),
            MapMarkerItem(
                id = "m_tth",
                title = "Đại Nội Cố Đô",
                provinceName = "Thừa Thiên Huế",
                provinceCode = "TTH",
                lat = 16.4637,
                lng = 107.5909,
                photoUrl = "https://images.unsplash.com/photo-1569154941061-e231b4725ef1?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "18/06/2026",
                stampSymbol = "🏯"
            ),
            MapMarkerItem(
                id = "m_dng",
                title = "Cầu Vàng Bà Nà",
                provinceName = "Đà Nẵng",
                provinceCode = "DNG",
                lat = 16.0544,
                lng = 108.2022,
                photoUrl = "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "16/06/2026",
                stampSymbol = "🌉"
            ),
            MapMarkerItem(
                id = "m_ldg",
                title = "Đồi Chè Cầu Đất",
                provinceName = "Lâm Đồng",
                provinceCode = "LDG",
                lat = 11.9404,
                lng = 108.4583,
                photoUrl = "https://images.unsplash.com/photo-1528127269322-539801943592?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "12/09/2026",
                stampSymbol = "🌲"
            ),
            MapMarkerItem(
                id = "m_hcm",
                title = "Nhà Thờ Đức Bà",
                provinceName = "TP. Hồ Chí Minh",
                provinceCode = "HCM",
                lat = 10.8231,
                lng = 106.6297,
                photoUrl = "https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "05/09/2026",
                stampSymbol = "🏙"
            ),
            MapMarkerItem(
                id = "m_kgg",
                title = "Bãi Sao Phú Quốc",
                provinceName = "Kiên Giang",
                provinceCode = "KGG",
                lat = 10.2899,
                lng = 103.9840,
                photoUrl = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=400&auto=format&fit=crop",
                isVisited = true,
                visitedDate = "02/05/2026",
                stampSymbol = "🏝"
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (layerStyle) {
                    MapLayerStyle.TERRAIN -> Color(0xFF0F261E)
                    MapLayerStyle.SATELLITE -> Color(0xFF081C15)
                    MapLayerStyle.DARK -> Color(0xFF0B1411)
                }
            )
    ) {
        // Map Canvas with Gestures
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.85f, 3.5f)
                        panOffset = Offset(
                            x = panOffset.x + pan.x,
                            y = panOffset.y + pan.y
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            zoomScale = if (zoomScale > 1.5f) 1.0f else 2.2f
                        },
                        onTap = {
                            selectedMarker = null
                        }
                    )
                }
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()

            // Function to map lat/lng to screen pixels
            fun projectCoordinates(lat: Double, lng: Double): Offset {
                // Normalize lng (x) and lat (y, inverted because lat decreases southward)
                val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
                val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat())

                val basePaddingX = canvasWidth * 0.12f
                val basePaddingY = canvasHeight * 0.10f
                val usableW = canvasWidth * 0.76f
                val usableH = canvasHeight * 0.80f

                val rawX = basePaddingX + normX * usableW
                val rawY = basePaddingY + normY * usableH

                // Apply pan and zoom centered on canvas
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f

                val transformedX = centerX + (rawX - centerX + panOffset.x) * zoomScale
                val transformedY = centerY + (rawY - centerY + panOffset.y) * zoomScale

                return Offset(transformedX, transformedY)
            }

            // Draw Base Vietnam Map & Travel Tracks
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Grid lines (MapLibre vector style)
                val gridColor = Color(0x1A4ECBA5)
                for (i in 0..10) {
                    val y = i * (canvasHeight / 10f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                }
                for (i in 0..8) {
                    val x = i * (canvasWidth / 8f)
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f
                    )
                }

                // GPS Route Polyline connecting visited check-in points
                val trackPoints = markers.map { projectCoordinates(it.lat, it.lng) }
                if (trackPoints.size > 1) {
                    val path = Path()
                    path.moveTo(trackPoints.first().x, trackPoints.first().y)
                    for (i in 1 until trackPoints.size) {
                        path.lineTo(trackPoints[i].x, trackPoints[i].y)
                    }

                    // Outer glowing route line
                    drawPath(
                        path = path,
                        color = Gold500.copy(alpha = 0.35f),
                        style = Stroke(
                            width = 6f * zoomScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 15f), 0f)
                        )
                    )

                    // Inner bright route line
                    drawPath(
                        path = path,
                        color = Gold500,
                        style = Stroke(
                            width = 2.5f * zoomScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                        )
                    )
                }

                // Sovereignty Markers: Hoàng Sa & Trường Sa
                val hoangSaPos = projectCoordinates(16.5, 112.0)
                val truongSaPos = projectCoordinates(10.0, 114.5)

                // Hoàng Sa
                drawCircle(
                    color = Color(0xFFD93025).copy(alpha = 0.4f),
                    radius = 14f * zoomScale,
                    center = hoangSaPos
                )
                drawCircle(
                    color = Gold500,
                    radius = 5f * zoomScale,
                    center = hoangSaPos
                )

                // Trường Sa
                drawCircle(
                    color = Color(0xFFD93025).copy(alpha = 0.4f),
                    radius = 14f * zoomScale,
                    center = truongSaPos
                )
                drawCircle(
                    color = Gold500,
                    radius = 5f * zoomScale,
                    center = truongSaPos
                )
            }

            // Interactive Circular Photo Markers
            markers.forEach { marker ->
                val pos = projectCoordinates(marker.lat, marker.lng)
                val isSelected = selectedMarker?.id == marker.id
                val markerSize = (if (isSelected) 56.dp else 44.dp)

                Box(
                    modifier = Modifier
                        .offset(
                            x = (pos.x / (canvasWidth / constraints.maxWidth)).dp - (markerSize / 2),
                            y = (pos.y / (canvasHeight / constraints.maxHeight)).dp - (markerSize / 2)
                        )
                        .size(markerSize)
                        .clickable { selectedMarker = marker }
                ) {
                    // Circular photo badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Outer pulse ring if visited
                        Box(
                            modifier = Modifier
                                .size(markerSize)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Gold500.copy(alpha = 0.5f) else Brand500.copy(alpha = 0.3f)
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
                                    width = if (isSelected) 2.5.dp else 1.5.dp,
                                    color = if (isSelected) Gold500 else Color.White,
                                    shape = CircleShape
                                )
                        )

                        // Stamp badge emblem on bottom right of marker
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
                                    fontSize = 8.sp,
                                    color = Gold500
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Control Overlay: Title & Stats
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD082019)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334ECBA5)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🗺 BẢN ĐỒ DẤU CHÂN MAPLIBRE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Gold500
                            )
                        }
                        Text(
                            text = "Lịch Sử Hành Trình Việt Nam",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "8 điểm check-in có ảnh • 12,500 km đã đi",
                            fontSize = 11.sp,
                            color = Color(0xFFBCE0D4)
                        )
                    }

                    // Map layer switch
                    Row(
                        modifier = Modifier
                            .background(Color(0x33000000), RoundedCornerShape(999.dp))
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                layerStyle = when (layerStyle) {
                                    MapLayerStyle.TERRAIN -> MapLayerStyle.SATELLITE
                                    MapLayerStyle.SATELLITE -> MapLayerStyle.DARK
                                    MapLayerStyle.DARK -> MapLayerStyle.TERRAIN
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Đổi kiểu bản đồ",
                                tint = Gold500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Zoom Controls (+ / - / Reset VN) on right side
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Zoom in (+)
            Surface(
                shape = CircleShape,
                color = Color(0xDD082019),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold500),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { zoomScale = (zoomScale + 0.35f).coerceAtMost(3.5f) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom in", tint = Color.White)
                }
            }

            // Zoom out (-)
            Surface(
                shape = CircleShape,
                color = Color(0xDD082019),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334ECBA5)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { zoomScale = (zoomScale - 0.35f).coerceAtLeast(0.85f) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom out", tint = Color.White)
                }
            }

            // Reset Center
            Surface(
                shape = CircleShape,
                color = Color(0xDD082019),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x334ECBA5)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        zoomScale = 1.0f
                        panOffset = Offset(0f, 0f)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Reset vị trí", tint = Gold500, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Selected Pin Preview Drawer
        AnimatedVisibility(
            visible = selectedMarker != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
        ) {
            selectedMarker?.let { marker ->
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
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
                                .clip(RoundedCornerShape(14.dp))
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
                                Text(
                                    text = "ĐÃ ĐÓNG DẤU HỘ CHIẾU",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Brand700
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "• ${marker.visitedDate}", fontSize = 9.sp, color = TextSecondary)
                            }
                            Text(
                                text = marker.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "📍 ${marker.provinceName} • Toạ độ: ${marker.lat.toString().take(5)}, ${marker.lng.toString().take(5)}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        onQuickCheckIn(marker.title, marker.provinceCode, marker.provinceName)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                    shape = RoundedCornerShape(999.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(text = "Đóng dấu lại ⦿", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(onClick = { selectedMarker = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
