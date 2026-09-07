package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import androidx.compose.ui.res.stringResource
import com.example.data.model.Place
import com.example.data.model.Province
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    places: List<Place>,
    provinces: List<Province>,
    onBack: () -> Unit,
    onPlaceClick: (Place) -> Unit,
    onProvinceClick: (Province) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val trendingKeywords = listOf("Đồi chè Cầu Đất", "Cà phê trứng Hà Nội", "Mã Pí Lèng", "Cầu Vàng Bà Nà", "Bánh căn", "Săn mây Đà Lạt")

    val searchResults = remember(searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) emptyList()
        else places.filter {
            it.name.lowercase().contains(q) ||
            it.address.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.tags.any { tag -> tag.lowercase().contains(q) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
    ) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = TextPrimary)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_placeholder), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Brand700) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.search_clear), tint = TextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(999.dp),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (searchQuery.isBlank()) {
                // Trending tags
                item {
                    Text(
                        text = stringResource(R.string.search_popular_header),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(trendingKeywords) { kw ->
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = BgSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6DFDB)),
                                modifier = Modifier.clickable { searchQuery = kw }
                            ) {
                                Text(
                                    text = kw,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 34 Provinces Quick Directory
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.search_provinces_header),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(provinces) { prov ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BgSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onProvinceClick(prov) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = prov.stampSymbol, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = prov.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = prov.highlightDescription, fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                            Text(text = stringResource(R.string.search_province_progress, prov.visitedPlacesCount, prov.suggestedPlacesCount), fontSize = 11.sp, color = Brand700)
                        }
                    }
                }
            } else {
                // Search Results
                item {
                    Text(
                        text = stringResource(R.string.search_results_header, searchQuery, searchResults.size),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(searchResults) { place ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onPlaceClick(place) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = place.coverUrl,
                                contentDescription = place.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = place.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = place.address, fontSize = 11.5.sp, color = TextSecondary, maxLines = 1)
                                Text(text = "${place.category.iconEmoji} ${place.category.titleVi} • ~${place.priceRangeVnd / 1000}k", fontSize = 10.sp, color = Brand700)
                            }
                        }
                    }
                }
            }
        }
    }
}
