package com.example.ui.screens

import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.TransferHistory
import com.example.model.ShareableFile
import com.example.ui.viewmodel.MainViewModel
import java.io.File

// --- 1. Splash Screen ---
@Composable
fun SplashScreen(viewModel: MainViewModel) {
    var startProgress by remember { mutableStateOf(false) }
    val progressAnim by animateFloatAsState(
        targetValue = if (startProgress) 1f else 0f,
        animationSpec = twinProgressSpec(2000),
        label = "Progress"
    )

    LaunchedEffect(Unit) {
        startProgress = true
        delay(2500)
        viewModel.splashCompleted.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1F1C4D), Color(0xFF090A1A)),
                    center = Offset(540f, 960f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon / Logo with Pulsing Shadow
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "Infinity Share Logo",
                modifier = Modifier
                    .scale(scale)
                    .size(140.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(32.dp))
                    .padding(4.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "INFINITY SHARE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Text(
                text = "Lightning Fast Offline Share",
                fontSize = 14.sp,
                color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Beautiful Loading bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(240.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progressAnim },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF9E00FF),
                    trackColor = Color(0xFF13152F),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Checking storage space... ${(progressAnim * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun twinProgressSpec(duration: Int) = tween<Float>(
    durationMillis = duration,
    easing = LinearEasing
)

// --- 2. Onboarding Screen ---
@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardingPageData(
            title = "Fast File Sharing",
            description = "Transfer apps, photos, videos, and music with ultra fast 5Ghz Wi-Fi speeds without any internet connection.",
            icon = Icons.Filled.FlashOn,
            color = Color(0xFF00E5FF)
        ),
        OnboardingPageData(
            title = "Secure Phone Clone",
            description = "Easily migrate your contacts, photos, call logs, and messages from your old phone to your new phone securely.",
            icon = Icons.Filled.Phonelink,
            color = Color(0xFF9E00FF)
        ),
        OnboardingPageData(
            title = "Cross Platform Transfer",
            description = "Share files seamlessly with PC, Mac, iOS devices using a simple built-in Web browser share server.",
            icon = Icons.Filled.Devices,
            color = Color(0xFFD500F9)
        ),
        OnboardingPageData(
            title = "Built-in File Manager",
            description = "Organize, compress files to ZIP, extract, play high-definition media files straight inside the application.",
            icon = Icons.Filled.FolderOpen,
            color = Color(0xFF3DDC84)
        )
    )

    val currentPageData = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.onOnboardingFinished() }) {
                    Text("Skip", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                }
            }

            // Central Illustration / Icon Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(currentPageData.color.copy(alpha = 0.12f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                        .border(1.5.dp, currentPageData.color.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentPageData.icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = currentPageData.color
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = currentPageData.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentPageData.description,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 22.sp
                )
            }

            // Bottom Navigation Controllers
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(width = if (index == currentPage) 24.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentPage) currentPageData.color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                        )
                    }
                }

                // Next / Get Started Button
                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            viewModel.onOnboardingFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = currentPageData.color),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) "GET STARTED" else "NEXT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

fun MainViewModel.onOnboardingFinished() {
    onboardingCompleted.value = true
}

// --- 3. Permission Screen ---
@Composable
fun PermissionScreen(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C1E))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Storage & Devices Permission",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "To allow seamless file transfer and device scanning, Infinity Share requires access to photos, files, Wi-Fi, and nearby devices.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Checklist of permissions
            val permissions = listOf(
                "Photos & Videos access" to Icons.Default.PhotoLibrary,
                "Storage / Local Files access" to Icons.Default.Folder,
                "Nearby P2P Devices search" to Icons.Default.Radar,
                "Wi-Fi connection control" to Icons.Default.Wifi,
                "Download Notifications" to Icons.Default.NotificationsActive
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF13152F), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                permissions.forEach { (title, icon) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1A1C3C), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    viewModel.permissionsGranted.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("grant_permissions_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E00FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("GRANT ALL PERMISSIONS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

// --- 4. Home Dashboard Screen ---
@Composable
fun HomeDashboardScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Built-in Minimal Media Player bar if music is playing
            viewModel.activeTrack.value?.let { track ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .clickable { onNavigate("media_player") }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (track.type == "MUSIC") Icons.Default.MusicNote else Icons.Default.VideoCameraBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = track.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Now Playing - Tap to expand",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = { viewModel.togglePlayback() }) {
                                Icon(
                                    imageVector = if (viewModel.isMediaPlaying.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "PlayPause",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { viewModel.playNextTrack() }) {
                                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Top Header Custom Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Infinity Share",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Instant P2P Network connected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onNavigate("history") },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Send / Receive Giant Dual Cards (Hero Elements)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Send Card (Premium Indigo)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clickable { onNavigate("send_select") }
                        .testTag("send_button_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text(text = "SEND", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Share any file", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }

                // Receive Card (Fresh Emerald)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .clickable { onNavigate("receive_wait") }
                        .testTag("receive_button_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Receive", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Column {
                            Text(text = "RECEIVE", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "Acquire files", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Extra Utilities Bar (Rounded minimalist styling)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                UtilityItem(icon = Icons.Default.Phonelink, label = "Phone Clone") { onNavigate("phone_clone") }
                UtilityItem(icon = Icons.Default.FolderZip, label = "ZIP Tool") { onNavigate("file_manager") }
                UtilityItem(icon = Icons.Default.Web, label = "Web Share") { onNavigate("web_share") }
                UtilityItem(icon = Icons.Default.FindInPage, label = "Dup Finder") { onNavigate("dup_finder") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Categories Header
            Text(
                text = "Local Media",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Category Grid (With Clean Minimalism custom pastel highlights)
            val categories = listOf(
                MediaCategoryData("Music", Icons.Default.MusicNote, Color(0xFF00E5FF), "music"),
                MediaCategoryData("Videos", Icons.Default.VideoLibrary, Color(0xFF9E00FF), "video"),
                MediaCategoryData("Photos", Icons.Default.Image, Color(0xFFD500F9), "photo"),
                MediaCategoryData("Docs", Icons.Default.Description, Color(0xFF3DDC84), "document"),
                MediaCategoryData("Apps", Icons.Default.Android, Color(0xFFFF9100), "apps"),
                MediaCategoryData("Storage", Icons.Default.Storage, Color(0xFFFFD600), "file_manager")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(categories) { cat ->
                    val circleBgColor = when (cat.label) {
                        "Music" -> Color(0xFFDBEAFE)
                        "Videos" -> Color(0xFFFEE2E2)
                        "Photos" -> Color(0xFFF3E8FF)
                        "Docs" -> Color(0xFFFEF9C3)
                        "Apps" -> Color(0xFFFFEDD5)
                        else -> Color(0xFFCFFAFE)
                    }
                    val iconColor = when (cat.label) {
                        "Music" -> Color(0xFF2563EB)
                        "Videos" -> Color(0xFFDC2626)
                        "Photos" -> Color(0xFF9333EA)
                        "Docs" -> Color(0xFFB45309)
                        "Apps" -> Color(0xFFEA580C)
                        else -> Color(0xFF0891B2)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onNavigate(cat.route) }
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(circleBgColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = cat.icon, contentDescription = cat.label, tint = iconColor, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cat.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Storage Monitor Card (Clean Minimalism HTML style)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STORAGE ANALYSIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "64.2 GB / 128 GB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented storage progress bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        // System (30%)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.30f)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        // Media (15%)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.15f)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        // Other (5%)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.05f)
                                .background(Color(0xFFF97316))
                        )
                        // Free space (50%)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(0.50f)
                                .background(Color.Transparent)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "System", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Media", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFF97316), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Other", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Connection Statistics Card (Clean Minimalism styled)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.WifiTethering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Local Share Network Hotspot", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "SSID: InfinityShare_P2P_Network", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "ACTIVE",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UtilityItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

data class MediaCategoryData(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

// --- 5. Send Flow Screen (File Selector) ---
@Composable
fun SendFileSelectorScreen(
    viewModel: MainViewModel,
    initialTab: String = "Photos",
    onNavigate: (String) -> Unit
) {
    val tabs = listOf("Photos", "Videos", "Music", "Docs", "Apps")
    var selectedTab by remember(initialTab) {
        mutableStateOf(if (initialTab in tabs) initialTab else "Photos")
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0C1E))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Select Files to Send", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        bottomBar = {
            if (viewModel.selectedFilesToShare.isNotEmpty()) {
                Surface(
                    color = Color(0xFF13152F),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${viewModel.selectedFilesToShare.size} items selected",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            val totalSize = viewModel.selectedFilesToShare.sumOf { it.size }
                            Text(
                                text = formatSize(totalSize),
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onNavigate("send_pair") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E00FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("send_selected_items_button")
                        ) {
                            Text(text = "SEND NOW", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF0B0C1E)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color(0xFF13152F))
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                tabs.forEach { tab ->
                    val active = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .drawBehind {
                                if (active) {
                                    drawRoundRect(
                                        color = Color(0xFF00E5FF),
                                        topLeft = Offset(0f, size.height - 8f),
                                        size = Size(size.width, 8f),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }
                            }
                    ) {
                        Text(
                            text = tab,
                            color = if (active) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // File items
            val activeListFlow = when (selectedTab) {
                "Photos" -> viewModel.photos
                "Videos" -> viewModel.videos
                "Music" -> viewModel.music
                "Docs" -> viewModel.documents
                "Apps" -> viewModel.apps
                else -> viewModel.apps
            }

            val fileItems by activeListFlow.collectAsState()

            if (fileItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "No files found of this type", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fileItems) { file ->
                        val isSelected = viewModel.selectedFilesToShare.any { it.path == file.path }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleSelection(file) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF9E00FF).copy(alpha = 0.15f) else Color(0xFF13152F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) BorderStroke(1.dp, Color(0xFF9E00FF)) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFF1A1C3C), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (file.type) {
                                                "PHOTO" -> Icons.Default.Image
                                                "VIDEO" -> Icons.Default.VideoLibrary
                                                "MUSIC" -> Icons.Default.MusicNote
                                                "APP" -> Icons.Default.Android
                                                else -> Icons.Default.Description
                                            },
                                            contentDescription = null,
                                            tint = Color(0xFF00E5FF)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = file.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = file.sizeFormatted,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleSelection(file) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF9E00FF))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 5B. Send Flow: Pair Screen ---
@Composable
fun SendPairScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    var isSearching by remember { mutableStateOf(true) }

    // Radar Animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radiusRatio by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )

    LaunchedEffect(Unit) {
        delay(4000)
        isSearching = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C1E))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Searching for Receiver",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ensure receiver's Infinity Share is open",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulse Radar Box
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .drawBehind {
                        if (isSearching) {
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = 1f - radiusRatio),
                                radius = 120.dp.toPx() * radiusRatio,
                                style = Stroke(width = 4f)
                            )
                            drawCircle(
                                color = Color(0xFF9E00FF).copy(alpha = 1f - ((radiusRatio + 0.5f) % 1f)),
                                radius = 120.dp.toPx() * ((radiusRatio + 0.5f) % 1f),
                                style = Stroke(width = 4f)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFF13152F), CircleShape)
                        .border(1.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (isSearching) {
                Text(text = "Scanning nearby devices...", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            } else {
                // Device Found Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.startSharing("SAMSUNG Galaxy S24")
                            onNavigate("transferring")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13152F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF3DDC84).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFF3DDC84))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = "SAMSUNG Galaxy S24", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "Tap to pair and transfer", color = Color(0xFF3DDC84), fontSize = 12.sp)
                            }
                        }

                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF3DDC84))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Or QR Code Pairing Button
            OutlinedButton(
                onClick = { onNavigate("scan_qr") },
                border = BorderStroke(1.dp, Color(0xFF9E00FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF9E00FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SCAN QR CODE", color = Color.White)
            }
        }
    }
}

// --- Scan QR Screen ---
@Composable
fun ScanQrScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A1A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Align QR Code to Connect", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(28.dp))

            // Simulated Camera viewfinder box with scanning line
            val infiniteTransition = rememberInfiniteTransition(label = "scan")
            val scanLineOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scan_line"
            )

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .drawBehind {
                            val lineY = size.height * scanLineOffset
                            drawLine(
                                color = Color(0xFF00E5FF),
                                start = Offset(0f, lineY),
                                end = Offset(size.width, lineY),
                                strokeWidth = 6f
                            )
                        }
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    viewModel.startSharing("QR Connected Client")
                    onNavigate("transferring")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text(text = "SIMULATE SUCCESSFUL SCAN", color = Color(0xFF0B0C1E), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 6. Receive Wait Screen ---
@Composable
fun ReceiveWaitScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C1E))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Waiting for Sender", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Show this QR code to the sender to connect",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Dynamic Custom QR Code Drawing inside Composable Canvas!
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val blocks = 15
                    val blockSize = size.width / blocks
                    // Draw clean procedural matrix resembling a beautiful QR code
                    for (x in 0 until blocks) {
                        for (y in 0 until blocks) {
                            val isBorderBlock = (x < 4 && y < 4) || (x >= blocks - 4 && y < 4) || (x < 4 && y >= blocks - 4)
                            val isCenterBlock = (x in 1..2 && y in 1..2) || (x >= blocks - 3 && x <= blocks - 2 && y in 1..2) || (x in 1..2 && y >= blocks - 3 && y <= blocks - 2)
                            val isDot = (x + y) % 3 == 0 || (x * y) % 5 == 1 || (x - y) % 7 == 2

                            if (isBorderBlock || isDot) {
                                val blockColor = if (isCenterBlock) Color(0xFF9E00FF) else Color(0xFF0B0C1E)
                                drawRoundRect(
                                    color = blockColor,
                                    topLeft = Offset(x * blockSize, y * blockSize),
                                    size = Size(blockSize - 1f, blockSize - 1f),
                                    cornerRadius = CornerRadius(4f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Action Simulator
            Button(
                onClick = {
                    viewModel.startReceiving("Pixel 8 Pro")
                    onNavigate("transferring")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                modifier = Modifier.testTag("simulate_receive_button")
            ) {
                Text(text = "SIMULATE FILE RECEPTION", color = Color(0xFF0B0C1E), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- Active Transferring Progress Screen ---
@Composable
fun ActiveTransferScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val totalBytes = viewModel.activeTransferringFiles.sumOf { it.fileSize }
    val progress = viewModel.transferProgress.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C1E))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (viewModel.transferMode.value == "SEND") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (viewModel.transferMode.value == "SEND") "Sending Files..." else "Receiving Files...",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Connected to: ${viewModel.connectedDeviceName.value}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Giant circular speed meter
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF9E00FF),
                    strokeWidth = 10.dp,
                    trackColor = Color(0xFF13152F),
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = viewModel.transferSpeed.value, color = Color(0xFF00E5FF), fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text(text = "Transfer Speed", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // File items list being transferred
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF13152F), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(text = "Transfer Queue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.activeTransferringFiles) { file ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = file.fileName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = formatSize(file.fileSize), color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                            }

                            if (progress >= 1f) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF3DDC84))
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF00E5FF), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!viewModel.isTransferring.value) {
                Button(
                    onClick = { onNavigate("home") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3DDC84)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(text = "DONE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        viewModel.isTransferring.value = false
                        onNavigate("home")
                    },
                    border = BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(text = "CANCEL TRANSFER", color = Color.Red)
                }
            }
        }
    }
}

// --- 7. Phone Clone Screen ---
@Composable
fun PhoneCloneScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0C1E))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Phone Clone", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF0B0C1E)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isCloning.value) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Cloning Device...", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Do not disconnect Wi-Fi network", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(40.dp))

                    CircularProgressIndicator(
                        progress = { viewModel.cloneProgress.value },
                        modifier = Modifier.size(120.dp),
                        color = Color(0xFF9E00FF),
                        strokeWidth = 8.dp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(text = "Migrated: ${viewModel.clonedTypes.joinToString()}", color = Color(0xFF00E5FF), fontSize = 14.sp)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(imageVector = Icons.Default.Phonelink, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Migrate everything in 1 Tap",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select whether this phone is the old device (sender) or the new device (receiver).",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = { viewModel.startPhoneCloning(isNewPhone = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E00FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "I AM SENDER (OLD PHONE)", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.startPhoneCloning(isNewPhone = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "I AM RECEIVER (NEW PHONE)", fontWeight = FontWeight.Bold, color = Color(0xFF0B0C1E))
                    }
                }
            }
        }
    }
}

// --- 8. File Manager Screen ---
@Composable
fun FileManagerScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val currentDir by viewModel.currentDirectory.collectAsState()
    val files by viewModel.directoryFiles.collectAsState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<ShareableFile?>(null) }
    var folderNameInput by remember { mutableStateOf("") }
    var renameInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0C1E))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (currentDir?.parentFile != null) {
                                viewModel.navigateUp()
                            } else {
                                onNavigate("home")
                            }
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(text = "Built-in Explorer", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = currentDir?.name ?: "Root", color = Color(0xFF00E5FF), fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    IconButton(onClick = { showCreateFolderDialog = true }) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = Color.White)
                    }
                }
            }
        },
        containerColor = Color(0xFF0B0C1E)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Folder action buttons or status
            if (files.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Directory is empty", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files) { file ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (file.type == "DIRECTORY") {
                                        viewModel.loadDirectoryFiles(File(file.path))
                                    } else {
                                        viewModel.selectMediaAndPlay(file)
                                        onNavigate("media_player")
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13152F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = when (file.type) {
                                            "DIRECTORY" -> Icons.Default.Folder
                                            "ZIP" -> Icons.Default.FolderZip
                                            "MUSIC" -> Icons.Default.MusicNote
                                            "VIDEO" -> Icons.Default.VideoLibrary
                                            "PHOTO" -> Icons.Default.Image
                                            else -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        tint = if (file.type == "DIRECTORY") Color(0xFFFFD600) else Color(0xFF00E5FF),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = file.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = if (file.type == "DIRECTORY") "Folder" else file.sizeFormatted, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    }
                                }

                                Row {
                                    if (file.type == "ZIP") {
                                        IconButton(onClick = { viewModel.extractZip(file) }) {
                                            Icon(imageVector = Icons.Default.Unarchive, contentDescription = "Extract", tint = Color(0xFF3DDC84))
                                        }
                                    } else if (file.type != "DIRECTORY") {
                                        IconButton(onClick = { viewModel.compressToZip(file) }) {
                                            Icon(imageVector = Icons.Default.Archive, contentDescription = "ZIP", tint = Color(0xFF00E5FF))
                                        }
                                    }
                                    IconButton(onClick = {
                                        renameInput = file.name
                                        showRenameDialog = file
                                    }) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = Color.White)
                                    }
                                    IconButton(onClick = { viewModel.deleteFile(file) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New folder dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create Folder") },
            text = {
                TextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    label = { Text("Folder Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val current = currentDir
                    if (current != null && folderNameInput.isNotEmpty()) {
                        val newFolder = File(current, folderNameInput)
                        if (!newFolder.exists()) {
                            newFolder.mkdirs()
                            viewModel.loadDirectoryFiles(current)
                        }
                    }
                    showCreateFolderDialog = false
                    folderNameInput = ""
                }) {
                    Text("CREATE")
                }
            }
        )
    }

    // Rename dialog
    showRenameDialog?.let { file ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename File") },
            text = {
                TextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (renameInput.isNotEmpty()) {
                        viewModel.renameFile(file, renameInput)
                    }
                    showRenameDialog = null
                }) {
                    Text("RENAME")
                }
            }
        )
    }
}

// --- 9. Built-in Media Player ---
@Composable
fun MediaPlayerScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val track = viewModel.activeTrack.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A1A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (track == null) {
            Text(text = "No track playing", color = Color.White)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Built-in Media Player", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Box(modifier = Modifier.size(48.dp)) // spacer
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Disc Rotating Graphic
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(Color(0xFF13152F), CircleShape)
                        .border(3.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (track.type == "MUSIC") Icons.Default.MusicNote else Icons.Default.VideoCameraBack,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(90.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Title
                Text(
                    text = track.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Format: ${track.type} • Local File",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Track slider
                Slider(
                    value = viewModel.mediaProgress.value,
                    onValueChange = { viewModel.mediaProgress.value = it },
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF9E00FF),
                        thumbColor = Color(0xFF00E5FF)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.playPreviousTrack() }, modifier = Modifier.size(56.dp)) {
                        Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(
                        onClick = { viewModel.togglePlayback() },
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF9E00FF), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isMediaPlaying.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(onClick = { viewModel.playNextTrack() }, modifier = Modifier.size(56.dp)) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

// --- 11. Transfer History Screen ---
@Composable
fun TransferHistoryScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val history by viewModel.transferHistory.collectAsState()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0C1E))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Transfer History", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF0B0C1E)
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.HistoryToggleOff, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No transfer history yet", color = Color.White.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13152F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (item.direction == "SEND") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (item.direction == "SEND") Color(0xFF9E00FF) else Color(0xFF00E5FF)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = item.fileName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = "${formatSize(item.fileSize)} • Status: ${item.status}", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                            }

                            Text(
                                text = item.transferSpeed,
                                color = Color(0xFF3DDC84),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 12. Settings Screen ---
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0C1E))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF0B0C1E)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Dark Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Dark Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Save battery with premium dark canvas", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
                Switch(
                    checked = viewModel.isDarkMode.value,
                    onCheckedChange = { viewModel.isDarkMode.value = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF))
                )
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Default Folder
            Column {
                Text(text = "Default Download Path", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = viewModel.defaultDownloadFolder.value,
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Speed preference
            Column {
                Text(text = "Transfer Bandwidth Priority", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("High Speed (5Ghz)", "Maximum Compatibility (2.4Ghz)").forEach { mode ->
                        val selected = viewModel.transferSpeedPref.value == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) Color(0xFF9E00FF) else Color(0xFF13152F), RoundedCornerShape(8.dp))
                                .clickable { viewModel.transferSpeedPref.value = mode }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = mode, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Language
            Column {
                Text(text = "App Language", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("English", "Bengali (বাংলা)", "Hindi").forEach { lang ->
                        val selected = viewModel.appLanguage.value == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) Color(0xFF00E5FF) else Color(0xFF13152F), RoundedCornerShape(8.dp))
                                .clickable { viewModel.appLanguage.value = lang }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = lang, color = if (selected) Color(0xFF0B0C1E) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- 13. Web Browser Share Screen ---
@Composable
fun WebShareScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C1E))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { onNavigate("home") }, modifier = Modifier.align(Alignment.Start)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(imageVector = Icons.Default.Web, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Cross Platform Web Share", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Connect any PC, iPhone, or Smart TV by navigating to the address below in their web browser. No app required!",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Web Server Address Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13152F)),
                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "IP Server Address", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(
                        text = "http://192.168.1.100:8080",
                        color = Color(0xFF00E5FF),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(text = "Both devices must be on the same Wi-Fi", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// --- 14. Duplicate File Finder Screen ---
@Composable
fun DuplicateFinderScreen(viewModel: MainViewModel, onNavigate: (String) -> Unit) {
    val duplicates = remember { viewModel.findDuplicateFiles() }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0C1E))
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onNavigate("home") }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Duplicate File Finder", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF0B0C1E)
    ) { paddingValues ->
        if (duplicates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF3DDC84), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "No duplicate files found! Your space is clean.", color = Color.White.copy(alpha = 0.7f))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Found ${duplicates.size} redundant files taking up storage.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(duplicates) { file ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF13152F))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Default.FindInPage, contentDescription = null, tint = Color(0xFFD500F9))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = file.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = "${file.sizeFormatted} • ${file.path}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteFile(file) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Utils ---
fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
