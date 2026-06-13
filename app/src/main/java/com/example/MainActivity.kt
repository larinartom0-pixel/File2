package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.MediaTask
import com.example.ui.ConverterViewModel
import com.example.ui.theme.MyApplicationTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.graphics.Brush

// Retro Screen Palettes
data class RetroPalette(
    val bg: Color,
    val surface: Color,
    val border: Color,
    val accentAddress: String, // Hex string for debug logging or indicators
    val accent: Color, // Main retro highlight
    val accentSecondary: Color, // Badges, statuses
    val textPrimary: Color,
    val textSecondary: Color,
    val fontMonospace: Boolean,
    val buttonBg: Color,
    val selectedTabBackground: Color,
    val dpadHighlight: Color,
    // Professional Polish extensions with convenient default values to protect binary & call compatibility:
    val workspaceBg: Color = bg,
    val cardBg: Color = surface,
    val cardBorder: Color = border,
    val queueInnerBg: Color = bg,
    val queueHeaderBg: Color = surface,
    val queueBorder: Color = border,
    val actionBarGradients: List<Color> = listOf(surface, surface),
    val primaryButtonGradients: List<Color> = listOf(accent, accent),
    val secondaryButtonGradients: List<Color> = listOf(buttonBg, buttonBg),
    val progressBarGradients: List<Color> = listOf(accent, accentSecondary)
)

class MainActivity : ComponentActivity() {

    private val viewModel: ConverterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val selectedSkin by viewModel.currentSkin.collectAsState()
            val themePalette = getPalette(selectedSkin)

            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(themePalette.workspaceBg),
                    color = themePalette.workspaceBg
                ) {
                    RetroMainScreen(viewModel, themePalette, selectedSkin)
                }
            }
        }
    }
}

/**
 * Returns color palettes corresponding to classic Android releases
 */
fun getPalette(skin: String): RetroPalette {
    return if (skin == "Gingerbread") {
        val bg = Color(0xFF000000)
        val surface = Color(0xFF141414)
        val border = Color(0xFF333333)
        val accent = Color(0xFF72B000)
        val accentSecondary = Color(0xFFFF8800)
        val buttonBg = Color(0xFF222222)
        RetroPalette(
            bg = bg,
            surface = surface,
            border = border,
            accentAddress = "#72B000",
            accent = accent,
            accentSecondary = accentSecondary,
            textPrimary = Color(0xFFE5E5E5),
            textSecondary = Color(0xFF8E8E8E),
            fontMonospace = true,
            buttonBg = buttonBg,
            selectedTabBackground = Color(0xFF1A1A1A),
            dpadHighlight = Color(0xFF2A2A2A),
            // Gingerbread polished values:
            workspaceBg = Color(0xFF0F110D),
            cardBg = Color(0xFF181C15),
            cardBorder = Color(0xFF282F22),
            queueInnerBg = Color(0xFF0A0D08),
            queueHeaderBg = Color(0xFF12160F),
            queueBorder = Color(0xFF1C2217),
            actionBarGradients = listOf(Color(0xFF2E381A), Color(0xFF0F1407)),
            primaryButtonGradients = listOf(Color(0xFF72B000), Color(0xFF4C7500)),
            secondaryButtonGradients = listOf(Color(0xFF323B2A), Color(0xFF1A1F16)),
            progressBarGradients = listOf(Color(0xFF72B000), Color(0xFFFF8800))
        )
    } else {
        // Holographic Dark (Ice Cream Sandwich 4.0)
        val bg = Color(0xFF090C0E)
        val surface = Color(0xFF1B1E22)
        val border = Color(0xFF0099CC)
        val accent = Color(0xFF0099CC)
        val accentSecondary = Color(0xFF33B5E5)
        val buttonBg = Color(0xFF222930)
        RetroPalette(
            bg = bg,
            surface = surface,
            border = border,
            accentAddress = "#33B5E5",
            accent = accent,
            accentSecondary = accentSecondary,
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF919EAB),
            fontMonospace = false,
            buttonBg = buttonBg,
            selectedTabBackground = Color(0xFF1E2730),
            dpadHighlight = Color(0xFF293542),
            // ICS ("Professional Polish" theme matching HTML):
            workspaceBg = Color(0xFF121212),
            cardBg = Color(0xFF1A1A1A),
            cardBorder = Color(0xFF333333),
            queueInnerBg = Color(0xFF0A0A0A),
            queueHeaderBg = Color(0xFF151515),
            queueBorder = Color(0xFF222222),
            actionBarGradients = listOf(Color(0xFF333333), Color(0xFF111111)),
            primaryButtonGradients = listOf(Color(0xFF33B5E5), Color(0xFF0099CC)),
            secondaryButtonGradients = listOf(Color(0xFF444444), Color(0xFF222222)),
            progressBarGradients = listOf(Color(0xFF33B5E5), Color(0xFF0099CC))
        )
    }
}

@Composable
fun RetroMainScreen(
    viewModel: ConverterViewModel,
    palette: RetroPalette,
    skinName: String
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("CONVERTER") } // CONVERTER, QUEUE, DIAGNOSTICS

    // Collectors
    val tasks by viewModel.allTasks.collectAsState()
    val isProcessingByEngine by viewModel.isProcessing.collectAsState()
    val consoleLogs by viewModel.consoleLogs.collectAsState()
    val zipStateProgress by viewModel.zipProgress.collectAsState()
    val zipOutputResult by viewModel.createdZipResult.collectAsState()

    // File selection launchers
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                viewModel.addSingleFile(uri, "wav")
            }
            Toast.makeText(context, "Added ${uris.size} file(s) to convert (Default WAV). Modify output below.", Toast.LENGTH_SHORT).show()
        }
    }

    val zipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.processZipArchive(it, "ogg")
            Toast.makeText(context, "Extracted and queued ZIP contents to default OGG conversions.", Toast.LENGTH_SHORT).show()
        }
    }

    // Font definitions matching retro console aesthetic
    val appFontFamily = if (palette.fontMonospace) FontFamily.Monospace else FontFamily.SansSerif

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.workspaceBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize().background(palette.workspaceBg)) {
            // 1. Simulated Android Status Bar (2.3/4.0 styling!)
            SimulatedStatusBar(palette, skinName)

            // 2. Vintage App Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(palette.actionBarGradients))
                    .drawBehind {
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(palette.accentSecondary, shape = RoundedCornerShape(2.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Retro Icon",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MediaConverter Pro",
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = "OFFLINE CORES PIPELINE",
                        fontFamily = appFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (isProcessingByEngine) palette.accentSecondary else palette.buttonBg,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .border(1.dp, palette.border, shape = RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isProcessingByEngine) "WORKING" else "IDLE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProcessingByEngine) Color.Black else palette.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "v1.4",
                    color = palette.accentSecondary,
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(thickness = 1.dp, color = palette.border)

            // 3. Simulated Retro TabHost Control Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.bg)
            ) {
                RetroTabButton(
                    title = "CONVERTER",
                    active = activeTab == "CONVERTER",
                    palette = palette,
                    appFontFamily = appFontFamily,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_converter"),
                    onClick = { activeTab = "CONVERTER" }
                )
                RetroTabButton(
                    title = "QUEUE (${tasks.size})",
                    active = activeTab == "QUEUE",
                    palette = palette,
                    appFontFamily = appFontFamily,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_queue"),
                    onClick = { activeTab = "QUEUE" }
                )
                RetroTabButton(
                    title = "SYSTEM",
                    active = activeTab == "DIAGNOSTICS",
                    palette = palette,
                    appFontFamily = appFontFamily,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_system"),
                    onClick = { activeTab = "DIAGNOSTICS" }
                )
            }

            HorizontalDivider(thickness = 1.dp, color = palette.border)

            // Dynamic System load meta row matching mockup specifications
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOCAL DECODING: MULTI-CORE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textSecondary,
                    fontFamily = appFontFamily
                )
                Text(
                    text = if (isProcessingByEngine) "CPU LOAD: 74%" else "CPU LOAD: 9%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isProcessingByEngine) palette.accentSecondary else palette.accent,
                    fontFamily = appFontFamily
                )
            }

            // 4. Content Screen Panels
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                when (activeTab) {
                    "CONVERTER" -> {
                        ConverterPanel(
                            viewModel = viewModel,
                            palette = palette,
                            appFontFamily = appFontFamily,
                            onPickFiles = { fileLauncher.launch("audio/*,video/*") },
                            onPickZip = { zipLauncher.launch("application/zip") },
                            zipOutputResult = zipOutputResult,
                            zipStateProgress = zipStateProgress,
                            onExportZip = { file -> shareFile(context, file) },
                            onDismissZip = { viewModel.dismissZipResult() }
                        )
                    }
                    "QUEUE" -> {
                        QueuePanel(
                            tasks = tasks,
                            palette = palette,
                            appFontFamily = appFontFamily,
                            onDelete = { task -> viewModel.deleteTask(task) },
                            onClearAll = { viewModel.clearAllHistory() },
                            onExportFile = { name -> exportSingleMedia(context, name) }
                        )
                    }
                    "DIAGNOSTICS" -> {
                        DiagnosticsPanel(
                            viewModel = viewModel,
                            palette = palette,
                            appFontFamily = appFontFamily,
                            consoleLogs = consoleLogs
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom TabHost Button with old Android styling aesthetics using drawBehind
 */
@Composable
fun RetroTabButton(
    title: String,
    active: Boolean,
    palette: RetroPalette,
    appFontFamily: FontFamily,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bottomIndicatorColor = if (active) palette.accent else Color.Transparent
    val backgroundColor = if (active) palette.selectedTabBackground else Color.Transparent

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .drawBehind {
                // Draw bottom border indicator
                if (active) {
                    drawRect(
                        color = bottomIndicatorColor,
                        topLeft = Offset(0f, size.height - 4.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width, 4.dp.toPx())
                    )
                }
                // Draw end division line
                drawLine(
                    color = palette.border,
                    start = Offset(size.width - 1.dp.toPx(), 0f),
                    end = Offset(size.width - 1.dp.toPx(), size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = appFontFamily,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            color = if (active) palette.accent else palette.textSecondary
        )
    }
}

/**
 * Top Status Bar mockup matching Gingerbread/Holo.
 * Custom built bars avoid raw external icon loading.
 */
@Composable
fun SimulatedStatusBar(palette: RetroPalette, skinName: String) {
    val systemTime = remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date())
    }

    val themeLabel = if (skinName == "Gingerbread") "Android 2.3" else "Android 4.0"
    val barColor = if (skinName == "Gingerbread") Color.Black else Color(0xFF040608)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(barColor)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Signal indicators drawn manually via Compose layout widgets!
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "3G",
                color = palette.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            
            // Retro wifi/network signal blocks
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.height(10.dp)
            ) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height((i * 2.5).dp)
                            .background(palette.accent)
                    )
                    Spacer(modifier = Modifier.width(1.5.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = themeLabel,
                color = palette.textSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Right side: Battery cell blocks & clock
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "87%",
                color = palette.accent,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            
            // Simulated retro battery container
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(9.dp)
                        .border(1.dp, palette.accent, shape = RoundedCornerShape(1.dp))
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.85f)
                            .background(palette.accent)
                    )
                }
                Spacer(modifier = Modifier.width(0.5.dp))
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(3.dp)
                        .background(palette.accent)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = systemTime,
                color = palette.accent,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConverterPanel(
    viewModel: ConverterViewModel,
    palette: RetroPalette,
    appFontFamily: FontFamily,
    onPickFiles: () -> Unit,
    onPickZip: () -> Unit,
    zipOutputResult: File?,
    zipStateProgress: Boolean,
    onExportZip: (File) -> Unit,
    onDismissZip: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf("WAV") }
    var useHighFidelity by remember { mutableStateOf(true) }

    val formats = listOf("WAV", "MP3", "OGG", "M4A", "MP4", "AVI", "MKV")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Zip processing card overlay
        if (zipStateProgress || zipOutputResult != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(palette.cardBg)
                        .border(1.dp, palette.accentSecondary, shape = RoundedCornerShape(3.dp))
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = palette.accentSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ZIP CONTAINER PROCESSOR",
                                fontFamily = appFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = palette.accentSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (zipStateProgress) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = palette.accentSecondary,
                                trackColor = palette.border
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Extracting files, queuing subtasks, transcoding local codecs...",
                                fontSize = 10.sp,
                                fontFamily = appFontFamily,
                                color = palette.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        zipOutputResult?.let { zipFile ->
                            Text(
                                text = "ARCHIVE EXPORT COMPLETED",
                                color = palette.accent,
                                fontSize = 13.sp,
                                fontFamily = appFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${zipFile.name}\nSize: ${formatSize(zipFile.length())}",
                                color = palette.textPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onExportZip(zipFile) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Brush.verticalGradient(palette.primaryButtonGradients))
                                        .border(1.dp, palette.accent, shape = RoundedCornerShape(2.dp)),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("EXPORT ZIP", color = Color.White, fontFamily = appFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onDismissZip,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Brush.verticalGradient(palette.secondaryButtonGradients))
                                        .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(2.dp)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.textSecondary),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Text("DISMISS", fontFamily = appFontFamily, fontSize = 11.sp, color = palette.textPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "1. CHOOSE INPUT SOURCE",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = appFontFamily,
                color = palette.accentSecondary
            )
        }

        // Grid-breaking layout with overlaid borders
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Single/Multiple file loader button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.verticalGradient(palette.secondaryButtonGradients))
                        .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                        .clickable { onPickFiles() }
                        .padding(14.dp)
                        .testTag("btn_select_files"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "MEDIA FILES",
                            fontFamily = appFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Select MP3/MP4/WAV...",
                            fontFamily = appFontFamily,
                            fontSize = 9.sp,
                            color = palette.textSecondary
                        )
                    }
                }

                // ZIP Archive processing loader button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.verticalGradient(palette.secondaryButtonGradients))
                        .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                        .clickable { onPickZip() }
                        .padding(14.dp)
                        .testTag("btn_select_zip"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = palette.accentSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ZIP ARCHIVE",
                            fontFamily = appFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "Extract -> Convert -> Re-zip",
                            fontFamily = appFontFamily,
                            fontSize = 9.sp,
                            color = palette.textSecondary
                        )
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "2. CHOOSE TARGET MEDIA FORMAT",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = appFontFamily,
                color = palette.accentSecondary
            )
        }

        item {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.cardBg)
                    .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                formats.forEach { format ->
                    val isSelected = selectedFormat == format
                    val formatGradients = if (isSelected) palette.primaryButtonGradients else palette.secondaryButtonGradients
                    val formatBorder = if (isSelected) palette.accent else palette.cardBorder
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.verticalGradient(formatGradients))
                            .border(1.dp, formatBorder, shape = RoundedCornerShape(2.dp))
                            .clickable { selectedFormat = format }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("ext_$format"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = format,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else palette.textPrimary
                        )
                    }
                }
            }
        }

        // Vintage checkboxes
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.cardBg)
                    .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                    .clickable { useHighFidelity = !useHighFidelity }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = useHighFidelity,
                    onCheckedChange = { useHighFidelity = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = palette.accent,
                        uncheckedColor = palette.border
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Enable local 44.1kHz High-Fidelity encoding",
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Uses uncompressed PCM and ADTS containers recursively.",
                        color = palette.textSecondary,
                        fontSize = 10.sp,
                        fontFamily = appFontFamily
                    )
                }
            }
        }

        // Helpful retro Android user explanation card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.cardBg)
                    .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "OFFLINE CONVERTER DOCUMENTATION",
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• WAV: Full uncompressed dynamic headers.\n" +
                                "• OGG/MP3: Stripped container wrapper encoding (ideal for local loop storage optimization).\n" +
                                "• ZIP archives: Fully processed on cache nodes of your Android instance in compliance with standard system constraints.",
                        fontSize = 10.sp,
                        fontFamily = appFontFamily,
                        color = palette.textSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QueuePanel(
    tasks: List<MediaTask>,
    palette: RetroPalette,
    appFontFamily: FontFamily,
    onDelete: (MediaTask) -> Unit,
    onClearAll: () -> Unit,
    onExportFile: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MEDIA TASK PIPELINE",
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Bold,
                color = palette.accentSecondary,
                fontSize = 12.sp
            )
            if (tasks.isNotEmpty()) {
                Button(
                    onClick = onClearAll,
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.verticalGradient(palette.secondaryButtonGradients))
                        .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(2.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = palette.accentSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CLEAR ALL", fontFamily = appFontFamily, fontSize = 10.sp, color = palette.textPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.queueInnerBg)
                    .border(1.dp, palette.queueBorder, shape = RoundedCornerShape(3.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = palette.textSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "QUEUE IS EMPTY",
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        fontFamily = appFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add single items or a ZIP folder to begin.",
                        color = palette.textSecondary,
                        fontSize = 11.sp,
                        fontFamily = appFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    MediaTaskRow(task, palette, appFontFamily, onDelete, onExportFile)
                }
            }
        }
    }
}

/**
 * Task row styled with block meters, status indicators, and custom pixel styling
 */
@Composable
fun MediaTaskRow(
    task: MediaTask,
    palette: RetroPalette,
    appFontFamily: FontFamily,
    onDelete: (MediaTask) -> Unit,
    onExportFile: (String) -> Unit
) {
    val statusColor = when (task.status) {
        "QUEUED" -> palette.textSecondary
        "CONVERTING" -> palette.accentSecondary
        "COMPLETED" -> palette.accent
        else -> Color.Red
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(palette.cardBg)
            .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (task.isArchiveTask) Icons.Default.Info else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (task.isArchiveTask) palette.accentSecondary else palette.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.fileName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatSize(task.fileSize)} | ${task.originalExtension.uppercase()} ➜ ${task.targetExtension.uppercase()}",
                        color = palette.textSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Delete task
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Item",
                    tint = palette.textSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete(task) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar or action indicators
            if (task.status == "CONVERTING") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        RetroCustomProgressBar(progress = task.progress, palette = palette)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        color = palette.accentSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else if (task.status == "COMPLETED") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SUCCESS | ${formatSize(task.outputSize)}",
                        color = palette.accent,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Button(
                        onClick = { onExportFile(task.outputFileName) },
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.verticalGradient(palette.primaryButtonGradients))
                            .border(1.dp, palette.accent, shape = RoundedCornerShape(2.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SAVE", fontSize = 9.sp, fontFamily = appFontFamily, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (task.status == "FAILED") {
                Text(
                    text = "ERROR: ${task.error ?: "Transcode failed"}",
                    color = Color.Red,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * A beautiful gradient-filled progress indicator matching the CSS specifications
 */
@Composable
fun RetroCustomProgressBar(progress: Float, palette: RetroPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(Color.Black)
            .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(1.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Brush.verticalGradient(palette.progressBarGradients))
        )
    }
}

@Composable
fun DiagnosticsPanel(
    viewModel: ConverterViewModel,
    palette: RetroPalette,
    appFontFamily: FontFamily,
    consoleLogs: List<String>
) {
    val systemSkins = listOf("Gingerbread", "Holo Dark")
    val concurrencyLimit by viewModel.concurrencyLimit.collectAsState()
    val activeSkinName by viewModel.currentSkin.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Dynamic Skin configuration block selection
        item {
            Text(
                text = "RETRO INTERFACE SKINS",
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Bold,
                color = palette.accentSecondary,
                fontSize = 12.sp
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.cardBg)
                    .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                systemSkins.forEach { skin ->
                    val isSelected = (skin == "Gingerbread" && activeSkinName == "Gingerbread") ||
                            (skin == "Holo Dark" && activeSkinName == "Holo_Dark")
                    val skinGradients = if (isSelected) palette.primaryButtonGradients else palette.secondaryButtonGradients
                    val skinBorderColor = if (isSelected) palette.accent else palette.cardBorder
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Brush.verticalGradient(skinGradients))
                            .border(1.dp, skinBorderColor, shape = RoundedCornerShape(3.dp))
                            .clickable {
                                if (skin == "Gingerbread") {
                                    viewModel.selectSkin("Gingerbread")
                                } else {
                                    viewModel.selectSkin("Holo_Dark")
                                }
                            }
                            .padding(vertical = 11.dp)
                            .testTag("skin_$skin"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = skin.uppercase(),
                            fontFamily = appFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else palette.textPrimary
                        )
                    }
                }
            }
        }

        // Concurrency pipeline setting (1 to 3 simultaneous processors, as detailed in specifications)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(palette.cardBg)
                    .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "QUEUE CONCURRENCY CAP: $concurrencyLimit CORES",
                    fontFamily = appFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Controls concurrent files executed in real time.",
                    fontFamily = appFontFamily,
                    color = palette.textSecondary,
                    fontSize = 9.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(1, 2, 3).forEach { coreOpt ->
                        val active = concurrencyLimit == coreOpt
                        val coreGradients = if (active) palette.primaryButtonGradients else palette.secondaryButtonGradients
                        val coreBorder = if (active) palette.accent else palette.cardBorder
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Brush.verticalGradient(coreGradients))
                                .border(1.dp, coreBorder, shape = RoundedCornerShape(2.dp))
                                .clickable { viewModel.setConcurrency(coreOpt) }
                                .padding(vertical = 8.dp)
                                .testTag("concurrency_$coreOpt"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$coreOpt STREAM(S)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else palette.textPrimary
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "SYSTEM BOOT LOGS",
                fontFamily = appFontFamily,
                fontWeight = FontWeight.Bold,
                color = palette.accentSecondary,
                fontSize = 12.sp
            )
        }

        // Scrolling vintage terminal console
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.Black)
                    .border(1.dp, palette.cardBorder, shape = RoundedCornerShape(3.dp))
                    .padding(8.dp)
            ) {
                val scrollState = rememberScrollState()
                // Launch effect to keep console automatically scrolled to the latest boot entries
                LaunchedEffect(consoleLogs.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    consoleLogs.forEach { logLine ->
                        Text(
                            text = logLine,
                            color = palette.accent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// Native File Provider Share Helpers
fun shareFile(context: Context, file: File?) {
    if (file == null || !file.exists()) {
        Toast.makeText(context, "Export error: file handle missing", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val authority = "com.aistudio.mediaconverter.retrox.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Transcoded ZIP Archive"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

fun exportSingleMedia(context: Context, outputFileName: String) {
    try {
        val outDir = File(context.cacheDir, "output_media")
        val file = File(outDir, outputFileName)
        if (!file.exists()) {
            Toast.makeText(context, "No media file generated locally.", Toast.LENGTH_SHORT).show()
            return
        }

        val authority = "com.aistudio.mediaconverter.retrox.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Transcoded File"))
    } catch (e: Exception) {
        Toast.makeText(context, "Save/Export failure: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
