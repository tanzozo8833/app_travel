package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.annotation.StringRes
import com.example.R
import androidx.compose.ui.res.stringResource
import com.example.data.model.CheckInRecord
import com.example.data.model.Place
import com.example.data.model.Province
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

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

fun calculateDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Int {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return (r * c).toInt()
}

/**
 * Bộ lọc vùng dùng chính chuỗi tiếng Việt làm khoá so sánh với Province.region,
 * nên KHÔNG được dịch chuỗi đó. Hàm này chỉ tra ra nhãn hiển thị tương ứng,
 * giữ nguyên khoá bên dưới.
 */
@StringRes
private fun regionFilterLabelRes(regionKey: String): Int = when (regionKey) {
    "Tất cả" -> R.string.map_region_all
    "Bắc" -> R.string.map_region_north
    "Trung" -> R.string.map_region_central
    "Tây Nguyên" -> R.string.map_region_highlands
    "Nam" -> R.string.map_region_south
    "Biển Đảo" -> R.string.map_region_islands
    else -> R.string.map_region_all
}

@Composable
fun InteractiveMapScreen(
    places: List<Place>,
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

    // Dynamic Markers derived directly from real places, provinces, and checkIns
    val allMarkers = remember(places, provinces, checkIns) {
        places.map { place ->
            val province = provinces.find { it.code.equals(place.provinceCode, ignoreCase = true) || it.id == place.provinceId }
            val matchingCheckIn = checkIns.find { checkIn ->
                checkIn.placeName.equals(place.name, ignoreCase = true) ||
                (checkIn.provinceCode.equals(place.provinceCode, ignoreCase = true) && checkIn.placeName.contains(place.name, ignoreCase = true))
            }
            val isVisited = place.isVisited || (matchingCheckIn != null)
            val visitedDate = matchingCheckIn?.formattedDate ?: "16/08/2026"

            val lat = if (place.lat != 0.0) place.lat else (province?.centerLat ?: 16.0)
            val lng = if (place.lng != 0.0) place.lng else (province?.centerLng ?: 107.5)

            MapMarkerItem(
                id = place.id,
                title = place.name,
                provinceName = province?.shortName ?: province?.name ?: place.provinceCode,
                provinceCode = place.provinceCode,
                region = province?.region ?: "Bắc",
                lat = lat,
                lng = lng,
                photoUrl = place.coverUrl,
                isVisited = isVisited,
                visitedDate = visitedDate,
                stampSymbol = province?.stampSymbol ?: place.category.iconEmoji,
                highlightTag = place.tags.firstOrNull() ?: place.category.titleVi,
                checkinRadiusMeters = place.checkinRadiusMeters
            )
        }
    }

    // Keep selectedMarker in sync with reactive updates (e.g. when check-in happens)
    LaunchedEffect(allMarkers) {
        selectedMarker?.let { current ->
            val updated = allMarkers.find { it.id == current.id || (it.title == current.title && it.provinceCode == current.provinceCode) }
            if (updated != null) {
                selectedMarker = updated
            }
        }
    }

    val displayMarkers = remember(selectedRegionFilter, allMarkers) {
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

    Box(modifier = modifier) {
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
                                            contentDescription = stringResource(R.string.map_not_visited),
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
                                text = stringResource(R.string.map_title),
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
                                contentDescription = stringResource(R.string.map_change_style),
                                tint = Gold500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (layerStyle) {
                                    MapLayerStyle.TERRAIN -> stringResource(R.string.map_style_terrain)
                                    MapLayerStyle.SATELLITE -> stringResource(R.string.map_style_satellite)
                                    MapLayerStyle.CLASSIC -> stringResource(R.string.map_style_default)
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
                                        text = stringResource(regionFilterLabelRes(region)),
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
                    Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.map_reset_position), tint = Gold500, modifier = Modifier.size(16.dp))
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
                                        text = if (marker.isVisited) stringResource(R.string.map_marker_visited, marker.highlightTag) else stringResource(R.string.map_marker_suggested),
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
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                    shape = RoundedCornerShape(999.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (marker.isVisited) stringResource(R.string.map_stamp_again, marker.stampSymbol) else stringResource(R.string.map_stamp_first),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { selectedMarker = null }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = TextSecondary, modifier = Modifier.size(18.dp))
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
            places = places,
            provinces = provinces,
            onDismiss = { showGpsCheckInDialog = false },
            onConfirmCheckIn = { matchedPlace ->
                onQuickCheckIn(matchedPlace.name, matchedPlace.provinceCode, matchedPlace.provinceName)
                showGpsCheckInDialog = false
            }
        )
    }
}

@Composable
fun GpsSpatialCheckInDialog(
    places: List<Place>,
    provinces: List<Province>,
    onDismiss: () -> Unit,
    onConfirmCheckIn: (NearbyMatchedPlace) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var isLocating by remember { mutableStateOf(true) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var detectedLat by remember { mutableDoubleStateOf(0.0) }
    var detectedLng by remember { mutableDoubleStateOf(0.0) }
    var accuracyMeters by remember { mutableFloatStateOf(10f) }
    var selectedMatch by remember { mutableStateOf<NearbyMatchedPlace?>(null) }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!granted) {
            isLocating = false
            locationError = context.getString(R.string.map_gps_no_permission)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.let {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    it.isLocationEnabled
                } else {
                    it.isProviderEnabled(LocationManager.GPS_PROVIDER) || it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                }
            } catch (e: Exception) {
                false
            }
        } ?: false

        if (!isGpsEnabled) {
            isLocating = false
            locationError = context.getString(R.string.map_gps_disabled)
            return@LaunchedEffect
        }

        try {
            var foundLoc: Location? = null
            val cancelToken = CancellationTokenSource()
            withTimeoutOrNull(9000L) {
                try {
                    val task = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelToken.token)
                    foundLoc = withContext(Dispatchers.IO) {
                        com.google.android.gms.tasks.Tasks.await(task)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            if (foundLoc == null) {
                try {
                    val lastTask = fusedLocationClient.lastLocation
                    foundLoc = withContext(Dispatchers.IO) {
                        com.google.android.gms.tasks.Tasks.await(lastTask)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            if (foundLoc != null) {
                detectedLat = foundLoc!!.latitude
                detectedLng = foundLoc!!.longitude
                accuracyMeters = if (foundLoc!!.hasAccuracy()) foundLoc!!.accuracy else 15f
                isLocating = false
            } else {
                isLocating = false
                locationError = context.getString(R.string.map_gps_no_signal)
            }
        } catch (e: Exception) {
            isLocating = false
            locationError = context.getString(R.string.map_gps_error, e.localizedMessage ?: context.getString(R.string.map_gps_error_title))
        }
    }

    // Compute nearest places dynamically from real database places and real device coordinates
    val nearbyCandidates = remember(places, provinces, detectedLat, detectedLng, isLocating) {
        if (places.isEmpty() || isLocating || (detectedLat == 0.0 && detectedLng == 0.0)) {
            emptyList()
        } else {
            places.map { place ->
                val province = provinces.find { it.code.equals(place.provinceCode, ignoreCase = true) || it.id == place.provinceId }
                val pLat = if (place.lat != 0.0) place.lat else (province?.centerLat ?: 0.0)
                val pLng = if (place.lng != 0.0) place.lng else (province?.centerLng ?: 0.0)
                val distance = calculateDistanceMeters(detectedLat, detectedLng, pLat, pLng)
                NearbyMatchedPlace(
                    id = place.id,
                    name = place.name,
                    provinceName = province?.shortName ?: province?.name ?: place.provinceCode,
                    provinceCode = place.provinceCode,
                    distanceMeters = distance,
                    isMatchWithinRadius = distance <= place.checkinRadiusMeters.coerceAtLeast(300),
                    stampSymbol = province?.stampSymbol ?: place.category.iconEmoji
                )
            }.sortedBy { it.distanceMeters }.take(5)
        }
    }

    LaunchedEffect(nearbyCandidates) {
        if (selectedMatch == null && nearbyCandidates.isNotEmpty()) {
            selectedMatch = nearbyCandidates.first()
        }
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
                                text = stringResource(R.string.map_gps_header),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Brand700
                            )
                            Text(
                                text = stringResource(R.string.map_gps_dialog_title),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = TextSecondary, modifier = Modifier.size(16.dp))
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
                            text = stringResource(R.string.map_gps_reading),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.map_gps_reading_sub),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }
                } else if (locationError != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = locationError ?: stringResource(R.string.map_gps_error_title),
                            fontSize = 11.5.sp,
                            color = Color(0xFFE65100),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    )
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand700),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(stringResource(R.string.map_gps_open_settings), fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    val nearestProv = provinces.minByOrNull { calculateDistanceMeters(detectedLat, detectedLng, it.centerLat, it.centerLng) }
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
                                    text = stringResource(R.string.map_gps_coords, "%.4f".format(detectedLat), "%.4f".format(detectedLng)),
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.map_gps_accuracy, accuracyMeters.toInt(), nearestProv?.shortName ?: stringResource(R.string.map_gps_fallback_region)),
                                    fontSize = 9.5.sp,
                                    color = TextSecondary
                                )
                            }
                            Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFE8F5E9)) {
                                Text(
                                    text = stringResource(R.string.map_gps_real_locked),
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
                        text = stringResource(R.string.map_gps_nearest_header),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (nearbyCandidates.isEmpty()) {
                        Text(
                            text = stringResource(R.string.map_gps_no_place),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
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
                                                val distStr = if (place.distanceMeters < 1000) "${place.distanceMeters}m" else "${"%.1f".format(place.distanceMeters / 1000.0)}km"
                                                Text(
                                                    text = stringResource(R.string.map_gps_distance, distStr, stringResource(if (place.isMatchWithinRadius) R.string.map_gps_within_radius else R.string.map_gps_outside_radius)),
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
                                text = stringResource(R.string.map_gps_save_checkin, match.provinceName),
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
