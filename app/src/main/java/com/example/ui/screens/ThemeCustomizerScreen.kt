package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WallpaperContainer
import com.example.ui.theme.*
import com.example.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizerScreen(viewModel: ChatViewModel) {
    val themeConfig by viewModel.themeConfig.collectAsState()

    var selectedPalette by remember(themeConfig) { mutableStateOf(themeConfig.selectedPalette) }
    var isDark by remember(themeConfig) { mutableStateOf(themeConfig.isDarkMode) }
    var bubbleShape by remember(themeConfig) { mutableStateOf(themeConfig.bubbleShape) }
    var wallpaperType by remember(themeConfig) { mutableStateOf(themeConfig.wallpaperType) }
    var brightness by remember(themeConfig) { mutableStateOf(themeConfig.wallpaperBrightness) }
    var opacity by remember(themeConfig) { mutableStateOf(themeConfig.wallpaperOpacity) }

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCodeText by remember { mutableStateOf("") }

    val previewCustomization = remember(selectedPalette, isDark, bubbleShape, wallpaperType, brightness, opacity) {
        ChatThemeCustomization(
            selectedPalette = selectedPalette,
            isDarkMode = isDark,
            bubbleShape = bubbleShape,
            wallpaperType = wallpaperType,
            wallpaperBrightness = brightness,
            wallpaperOpacity = opacity
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Appearance & Wallpapers", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Theme")
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Import Theme")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancel") }

                    Button(
                        onClick = {
                            viewModel.repository.updateTheme(previewCustomization)
                            viewModel.showToastNotification("Theme & Wallpaper applied!")
                            viewModel.navigateBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Apply Theme") }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Live Interactive Preview
            item {
                Text(
                    text = "Live Chat Preview",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    WallpaperContainer(theme = previewCustomization) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Incoming preview bubble
                            Surface(
                                shape = if (bubbleShape == BubbleShapeType.ROUNDED_SLOPE) RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
                                else if (bubbleShape == BubbleShapeType.SLEEK_GEOMETRIC) RoundedCornerShape(2.dp, 10.dp, 10.dp, 10.dp)
                                else RoundedCornerShape(20.dp),
                                color = if (isDark) DarkIncomingBubble else LightIncomingBubble,
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Marcus Vance",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = selectedPalette.accentColor
                                    )
                                    Text(
                                        text = "Yo bro! How does the new theme look? 🔥",
                                        fontSize = 13.sp,
                                        color = if (isDark) DarkBubbleTextIn else LightBubbleTextIn
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Outgoing preview bubble
                            Surface(
                                shape = if (bubbleShape == BubbleShapeType.ROUNDED_SLOPE) RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
                                else if (bubbleShape == BubbleShapeType.SLEEK_GEOMETRIC) RoundedCornerShape(10.dp, 2.dp, 10.dp, 10.dp)
                                else RoundedCornerShape(20.dp),
                                color = if (isDark) selectedPalette.outgoingBubbleDark else LightOutgoingBubble,
                                modifier = Modifier
                                    .widthIn(max = 220.dp)
                                    .align(Alignment.End)
                            ) {
                                Text(
                                    text = "Crisp and readable! Perfect contrast. ✓✓",
                                    fontSize = 13.sp,
                                    color = if (isDark) DarkBubbleTextOut else LightBubbleTextOut,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Dark / Light Mode Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Sleek boyish slate navy aesthetic", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(checked = isDark, onCheckedChange = { isDark = it })
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            // Palette Presets
            item {
                Text("Color Theme Palette", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(PaletteTheme.values()) { pal ->
                        val isSelected = selectedPalette == pal
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, pal.primary) else null,
                            modifier = Modifier
                                .clickable { selectedPalette = pal }
                                .padding(vertical = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(pal.primary))
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(pal.outgoingBubbleDark))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = pal.displayName, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
            }

            // Bubble Shape Selector
            item {
                Text("Chat Bubble Style", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BubbleShapeType.values().forEach { shape ->
                        FilterChip(
                            selected = bubbleShape == shape,
                            onClick = { bubbleShape = shape },
                            label = { Text(shape.label, fontSize = 12.sp) }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
            }

            // Wallpaper Engine
            item {
                Text("Wallpaper Texture", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WallpaperType.values()) { wp ->
                        FilterChip(
                            selected = wallpaperType == wp,
                            onClick = { wallpaperType = wp },
                            label = { Text(wp.label, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Brightness & Opacity Sliders
                Text("Wallpaper Brightness (${(brightness * 100).toInt()}%)", fontSize = 13.sp)
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 0.2f..1.0f
                )

                Text("Pattern Opacity (${(opacity * 100).toInt()}%)", fontSize = 13.sp)
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.1f..1.0f
                )

                Button(
                    onClick = {
                        wallpaperType = WallpaperType.CUSTOM_IMAGE
                        viewModel.showToastNotification("Sample JPG custom wallpaper loaded!")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Custom JPG/JPEG Wallpaper")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        val jsonCode = remember { viewModel.repository.exportThemeJson() }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Theme JSON") },
            text = {
                Column {
                    Text("Share or copy this configuration to import on another device:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = jsonCode,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showExportDialog = false
                    viewModel.showToastNotification("Theme JSON copied to clipboard!")
                }) { Text("Copy Code") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close") }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Theme Code") },
            text = {
                Column {
                    Text("Paste a valid Friends Chat theme JSON:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importCodeText,
                        onValueChange = { importCodeText = it },
                        placeholder = { Text("{\n  \"palette\": \"ROYAL_NAVY\"\n...}") },
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val ok = viewModel.repository.importThemeJson(importCodeText)
                    if (ok) {
                        viewModel.showToastNotification("Theme imported and applied!")
                        showImportDialog = false
                    } else {
                        viewModel.showToastNotification("Invalid theme JSON")
                    }
                }) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }
}
