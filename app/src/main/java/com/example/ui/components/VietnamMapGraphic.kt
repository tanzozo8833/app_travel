package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import com.example.data.model.Province
import com.example.ui.theme.*

@Composable
fun VietnamMapGraphic(
    provinces: List<Province>,
    onProvinceClick: (Province) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val visitedCount = provinces.count { it.visitedPlacesCount > 0 }
    val totalCount = if (provinces.isNotEmpty()) provinces.size else 34

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Brand900),
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.map_footprint_title),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Gold500,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = stringResource(R.string.map_footprint_progress, visitedCount, totalCount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnBrand
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Brand700.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = stringResource(R.string.map_nationwide),
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Map Visual Representation Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF03221B), Color(0xFF063A2E))
                        )
                    )
                    .border(1.dp, Color(0xFF155747), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // Background water / grid contour lines
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Region 1: Northern Vietnam (Bắc Bộ)
                    RegionMapRow(
                        regionName = "MIỀN BẮC",
                        provinces = provinces.filter { it.region.contains("Bắc", ignoreCase = true) },
                        onProvinceClick = onProvinceClick
                    )

                    // Region 2: Central Vietnam (Trung Bộ)
                    RegionMapRow(
                        regionName = "MIỀN TRUNG",
                        provinces = provinces.filter { it.region.contains("Trung", ignoreCase = true) },
                        onProvinceClick = onProvinceClick
                    )

                    // Region 3: Central Highlands & South (Tây Nguyên & Nam Bộ)
                    RegionMapRow(
                        regionName = stringResource(R.string.map_row_highlands_south),
                        provinces = provinces.filter { it.region.contains("Tây Nguyên", ignoreCase = true) || it.region.contains("Nam", ignoreCase = true) },
                        onProvinceClick = onProvinceClick
                    )
                }

                // Mandatory Sovereignty Box (Hoàng Sa & Trường Sa) - Section 13.4
                SovereigntyRecognitionCard(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Regional Stats Calculations
            val northProvinces = provinces.filter { it.region.contains("Bắc", ignoreCase = true) }
            val centralProvinces = provinces.filter { it.region.contains("Trung", ignoreCase = true) }
            val highlandsProvinces = provinces.filter { it.region.contains("Tây Nguyên", ignoreCase = true) }
            val southProvinces = provinces.filter { it.region.contains("Nam", ignoreCase = true) }

            val northVisited = northProvinces.count { it.visitedPlacesCount > 0 }
            val centralVisited = centralProvinces.count { it.visitedPlacesCount > 0 }
            val highlandsVisited = highlandsProvinces.count { it.visitedPlacesCount > 0 }
            val southVisited = southProvinces.count { it.visitedPlacesCount > 0 }

            // Stats footer bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MapStatItem(label = stringResource(R.string.map_stat_north), count = "$northVisited/${northProvinces.size}")
                MapStatItem(label = stringResource(R.string.map_stat_central), count = "$centralVisited/${centralProvinces.size}")
                MapStatItem(label = stringResource(R.string.map_stat_highlands), count = "$highlandsVisited/${highlandsProvinces.size}")
                MapStatItem(label = stringResource(R.string.map_stat_south), count = "$southVisited/${southProvinces.size}")
            }
        }
    }
}

@Composable
private fun RegionMapRow(
    regionName: String,
    provinces: List<Province>,
    onProvinceClick: (Province) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = regionName,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF6E998E),
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            provinces.take(4).forEach { prov ->
                val hasVisited = prov.visitedPlacesCount > 0
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (hasVisited) Gold500 else Color(0xFF0C4638),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onProvinceClick(prov) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp)
                    ) {
                        Text(
                            text = prov.stampSymbol,
                            fontSize = 12.sp
                        )
                        Text(
                            text = prov.shortName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasVisited) TextOnGold else Color(0xFFB5CCC5),
                            maxLines = 1
                        )
                        if (hasVisited) {
                            Text(
                                text = pluralStringResource(R.plurals.map_province_points, prov.visitedPlacesCount, prov.visitedPlacesCount),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextOnGold.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SovereigntyRecognitionCard(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xCC04201A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold500.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🇻🇳", fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CHỦ QUYỀN VIỆT NAM",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Gold500
                )
            }
            Text(
                text = "• QĐ. Hoàng Sa (Đà Nẵng)\n• QĐ. Trường Sa (Khánh Hoà)",
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                lineHeight = 10.sp
            )
        }
    }
}

@Composable
private fun MapStatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = Color(0xFF8BAAA1))
        Text(text = count, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold500)
    }
}
