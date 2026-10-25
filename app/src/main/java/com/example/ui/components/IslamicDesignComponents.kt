package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.preferences.IslamicColorTheme
import com.example.core.preferences.ThemePreference
import com.example.core.utils.BilingualStrings
import com.example.domain.prayer.DailyPrayerType
import com.example.ui.theme.AmiriQuranFontFamily
import com.example.ui.theme.IslamicThemePalette
import com.example.ui.theme.LocalIslamicThemePalette
import com.example.ui.theme.VerifiedEmerald
import com.example.ui.theme.paletteForIslamicTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Responsive layout metrics computed dynamically from BoxWithConstraints for any Android device
 * (compact phones <360dp, standard phones 360-599dp, foldables/tablets >=600dp).
 */
data class AdaptiveLayoutMetrics(
    val screenWidth: Dp,
    val screenHeight: Dp,
    val isCompactPhone: Boolean,
    val isWideScreen: Boolean,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cardSpacing: Dp,
    val bentoColumns: Int,
    val maxContentWidth: Dp
)

@Composable
fun AdaptiveScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable (AdaptiveLayoutMetrics) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val compact = maxWidth < 360.dp
        val wide = maxWidth >= 600.dp
        val metrics = AdaptiveLayoutMetrics(
            screenWidth = maxWidth,
            screenHeight = maxHeight,
            isCompactPhone = compact,
            isWideScreen = wide,
            horizontalPadding = when {
                compact -> 12.dp
                wide -> 24.dp
                else -> 16.dp
            },
            verticalPadding = if (compact) 10.dp else 14.dp,
            cardSpacing = if (compact) 10.dp else 14.dp,
            bentoColumns = when {
                maxWidth >= 840.dp -> 4
                wide -> 3
                else -> 2
            },
            maxContentWidth = if (wide) 860.dp else maxWidth
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = metrics.maxContentWidth),
            contentAlignment = Alignment.TopCenter
        ) {
            content(metrics)
        }
    }
}

/**
 * Full-width glassmorphic modal dialog that replaces narrow default AlertDialogs so chips,
 * headers, and multi-section forms never crush or clip on any Android screen size.
 */
@Composable
fun GlassmorphicModalDialog(
    onDismissRequest: () -> Unit,
    title: String,
    subtitle: String? = null,
    badgeText: String = "۞",
    modifier: Modifier = Modifier,
    dismissButton: @Composable (RowScope.() -> Unit)? = null,
    confirmButton: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val themePalette = LocalIslamicThemePalette.current
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            val maxDialogHeight = (maxHeight * 0.90f).coerceAtLeast(360.dp)
            val isCompact = maxWidth < 360.dp

            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(
                    1.2.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                            themePalette.metallicHighlight.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        )
                    )
                ),
                modifier = modifier
                    .fillMaxWidth(0.96f)
                    .widthIn(max = 560.dp)
                    .heightIn(max = maxDialogHeight)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Ornate Theme-Aware Dialog Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        themePalette.heroStart,
                                        themePalette.heroMid,
                                        themePalette.heroEnd
                                    )
                                )
                            )
                    ) {
                        IslamicGeometricBackground(
                            modifier = Modifier.matchParentSize(),
                            patternColor = Color.White,
                            patternAlpha = 0.08f
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = if (isCompact) 14.dp else 18.dp,
                                    vertical = if (isCompact) 12.dp else 16.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AyahRosetteMedallion(
                                numberText = badgeText,
                                size = if (isCompact) 36.dp else 42.dp,
                                tint = themePalette.metallicHighlight
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = if (isCompact) 18.sp else 20.sp
                                    ),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!subtitle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Adaptive Scrollable Body Area
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .padding(
                                horizontal = if (isCompact) 14.dp else 18.dp,
                                vertical = 12.dp
                            ),
                        content = content
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                    )

                    // Sticky Bottom Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(
                                horizontal = if (isCompact) 14.dp else 18.dp,
                                vertical = 10.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismissButton != null) {
                            dismissButton()
                        }
                        confirmButton()
                    }
                }
            }
        }
    }
}

/**
 * Interactive 2-Column Visual Swatch Grid for selecting between the 4 Islamic Color Themes.
 */
@Composable
fun ThemeSwatchGrid(
    selectedTheme: IslamicColorTheme,
    isBn: Boolean,
    onSelectTheme: (IslamicColorTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val themes = IslamicColorTheme.entries
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        themes.chunked(2).forEach { rowPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPair.forEach { themeItem ->
                    val isSelected = selectedTheme == themeItem
                    val palette = paletteForIslamicTheme(themeItem)
                    val cardShape = RoundedCornerShape(18.dp)

                    Surface(
                        shape = cardShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(cardShape)
                            .clickable { onSelectTheme(themeItem) }
                            .testTag("theme_swatch_${themeItem.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Live Gradient & Swatch Orbs Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                palette.heroStart,
                                                palette.heroMid,
                                                palette.heroEnd
                                            )
                                        )
                                    )
                            ) {
                                IslamicGeometricBackground(
                                    modifier = Modifier.matchParentSize(),
                                    patternColor = Color.White,
                                    patternAlpha = 0.12f
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        palette.swatchPreviewColors.forEach { dotColor ->
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected Theme",
                                            tint = palette.metallicHighlight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = if (isBn) themeItem.titleBn else themeItem.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isBn) themeItem.subtitleBn else themeItem.subtitleEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Quick Theme Palette Picker Dialog accessible directly from the Top App Bar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemePalettePickerDialog(
    selectedColorTheme: IslamicColorTheme,
    selectedThemeMode: ThemePreference,
    strings: BilingualStrings,
    onSelectColorTheme: (IslamicColorTheme) -> Unit,
    onSelectThemeMode: (ThemePreference) -> Unit,
    onDismiss: () -> Unit
) {
    GlassmorphicModalDialog(
        onDismissRequest = onDismiss,
        title = strings.tr("Islamic Theme Studio", "ইসলামিক থিম ও রঙ নির্বাচন"),
        subtitle = strings.tr(
            "Choose from 4 sacred architectural palettes with Light & Dark adaptation",
            "৪টি মনোমুগ্ধকর ইসলামিক থিম এবং লাইট/ডার্ক মোড নির্বাচন করুন"
        ),
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("close_theme_picker_button")
            ) {
                Text(
                    text = strings.tr("Done", "সম্পন্ন"),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = strings.tr("1. Select Color Palette (4 Themes)", "১. কালার থিম নির্বাচন করুন (৪টি থিম)"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeSwatchGrid(
                    selectedTheme = selectedColorTheme,
                    isBn = strings.isBn,
                    onSelectTheme = onSelectColorTheme
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.tr("2. Display Mode (Light / Dark / System)", "২. ডিসপ্লে মোড (লাইট / ডার্ক / সিস্টেম)"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemePreference.entries.forEach { mode ->
                        val modeLabel = when (mode) {
                            ThemePreference.SYSTEM -> strings.tr("System Auto", "সিস্টেম অটো")
                            ThemePreference.LIGHT -> strings.tr("Light Mode", "লাইট মোড")
                            ThemePreference.DARK -> strings.tr("Dark Mode", "ডার্ক মোড")
                        }
                        FilterChip(
                            selected = selectedThemeMode == mode,
                            onClick = { onSelectThemeMode(mode) },
                            label = {
                                Text(
                                    text = modeLabel,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Theme- and time-aware atmospheric sky gradient palette for the Home Hero Card and headers.
 */
@Composable
fun skyGradientForPrayer(
    currentPrayer: DailyPrayerType?,
    nextPrayer: DailyPrayerType,
    themePalette: IslamicThemePalette = LocalIslamicThemePalette.current
): Brush {
    val active = currentPrayer ?: nextPrayer
    val colors = when (active) {
        DailyPrayerType.FAJR, DailyPrayerType.MAGHRIB -> listOf(
            themePalette.heroStart,
            themePalette.heroMid,
            themePalette.heroEnd
        )
        DailyPrayerType.SUNRISE, DailyPrayerType.DHUHR, DailyPrayerType.ASR -> listOf(
            themePalette.heroStart,
            themePalette.heroMid,
            themePalette.heroEnd
        )
        DailyPrayerType.ISHA -> listOf(
            themePalette.dialDeepCore,
            themePalette.heroStart,
            themePalette.heroMid
        )
    }
    return Brush.linearGradient(
        colors = colors,
        start = Offset(0f, 0f),
        end = Offset(1000f, 900f)
    )
}

/**
 * Renders a subtle Islamic 8-pointed star (Rub el Hizb) & Girih geometric tessellation pattern.
 */
@Composable
fun IslamicGeometricBackground(
    modifier: Modifier = Modifier,
    patternColor: Color = Color.White,
    patternAlpha: Float = 0.08f
) {
    Canvas(modifier = modifier) {
        val step = 72.dp.toPx()
        val cols = (size.width / step).toInt() + 2
        val rows = (size.height / step).toInt() + 2
        val strokeColor = patternColor.copy(alpha = patternAlpha)

        for (r in 0..rows) {
            for (c in 0..cols) {
                val cx = c * step + if (r % 2 == 1) step / 2f else 0f
                val cy = r * step
                drawRubElHizbStar(
                    center = Offset(cx, cy),
                    outerRadius = step * 0.34f,
                    color = strokeColor,
                    strokeWidth = 1.4f
                )
            }
        }
    }
}

/**
 * Draws an 8-pointed Islamic Rub el Hizb star rosette on a Canvas DrawScope.
 */
fun DrawScope.drawRubElHizbStar(
    center: Offset,
    outerRadius: Float,
    color: Color,
    strokeWidth: Float = 2f
) {
    val path = Path()
    val points = 16
    val innerRadius = outerRadius * 0.72f
    for (i in 0 until points) {
        val angleRad = (i * (360f / points) - 90f) * (PI / 180.0)
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + (r * cos(angleRad)).toFloat()
        val y = center.y + (r * sin(angleRad)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
    drawCircle(color = color, radius = outerRadius * 0.38f, center = center, style = Stroke(width = strokeWidth * 0.75f))
}

/**
 * Custom 180-degree animated prayer progress arc with glowing orbital marker,
 * dynamically sized via BoxWithConstraints and styled with the active Islamic theme palette.
 */
@Composable
fun PrayerProgressArcGauge(
    progressFraction: Float,
    countdownText: String,
    nextPrayerLabel: String,
    nextPrayerTimeText: String,
    modifier: Modifier = Modifier,
    countdownTestTag: String = "next_prayer_countdown_text"
) {
    val themePalette = LocalIslamicThemePalette.current
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0.06f, 0.96f),
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "prayer_arc_progress"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val isCompact = maxWidth < 340.dp
        val isWide = maxWidth >= 520.dp
        val gaugeHeight = when {
            isCompact -> 130.dp
            isWide -> 164.dp
            else -> 146.dp
        }
        val countdownFontSize = when {
            isCompact -> 26.sp
            isWide -> 36.sp
            else -> 32.sp
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(gaugeHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gaugeHeight)
            ) {
                val arcDiameter = (size.width * 0.78f).coerceAtMost(size.height * 1.76f)
                val arcRadius = arcDiameter / 2f
                val arcLeft = (size.width - arcDiameter) / 2f
                val arcTop = size.height - arcRadius - 10.dp.toPx()
                val arcCenter = Offset(size.width / 2f, arcTop + arcRadius)

                // Outer subtle decorative ring
                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(arcLeft - 10.dp.toPx(), arcTop - 10.dp.toPx()),
                    size = Size(arcDiameter + 20.dp.toPx(), arcDiameter + 20.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Background track arc
                drawArc(
                    color = Color.White.copy(alpha = 0.22f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(arcLeft, arcTop),
                    size = Size(arcDiameter, arcDiameter),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // 5 Waqt milestone ticks along the 180-degree arc
                for (i in 0..4) {
                    val tickAngleDeg = 180f + (i * 45f)
                    val rad = tickAngleDeg * (PI / 180.0)
                    val innerR = arcRadius - 10.dp.toPx()
                    val outerR = arcRadius + 10.dp.toPx()
                    val startPt = Offset(
                        arcCenter.x + (innerR * cos(rad)).toFloat(),
                        arcCenter.y + (innerR * sin(rad)).toFloat()
                    )
                    val endPt = Offset(
                        arcCenter.x + (outerR * cos(rad)).toFloat(),
                        arcCenter.y + (outerR * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.35f),
                        start = startPt,
                        end = endPt,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Active luminous progress arc matching theme palette
                val sweep = 180f * animatedProgress
                drawArc(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            themePalette.metallicAccent,
                            Color.White,
                            themePalette.metallicHighlight
                        )
                    ),
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(arcLeft, arcTop),
                    size = Size(arcDiameter, arcDiameter),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Glowing orbital indicator on arc tip
                val indicatorAngleRad = (180f + sweep) * (PI / 180.0)
                val orbCenter = Offset(
                    arcCenter.x + (arcRadius * cos(indicatorAngleRad)).toFloat(),
                    arcCenter.y + (arcRadius * sin(indicatorAngleRad)).toFloat()
                )
                drawCircle(
                    color = themePalette.metallicHighlight.copy(alpha = 0.38f),
                    radius = 12.dp.toPx(),
                    center = orbCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.5.dp.toPx(),
                    center = orbCenter
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            ) {
                Text(
                    text = nextPrayerLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = themePalette.metallicHighlight,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = countdownText,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = countdownFontSize,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.testTag(countdownTestTag)
                )
                Text(
                    text = nextPrayerTimeText,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Glassmorphic container card with a luminous 1.dp specular gradient border.
 */
@Composable
fun GlassmorphicContainerCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val variantColor = MaterialTheme.colorScheme.surfaceVariant
    val bgBrush = Brush.linearGradient(
        colors = listOf(
            surfaceColor,
            variantColor.copy(alpha = 0.55f)
        )
    )
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            accentColor.copy(alpha = 0.42f),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
            accentColor.copy(alpha = 0.25f)
        )
    )

    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .clip(shape)
        .background(brush = bgBrush, shape = shape)
        .border(BorderStroke(1.dp, borderBrush), shape = shape)
        .let { mod ->
            if (onClick != null) mod.clickable(onClick = onClick) else mod
        }

    Box(modifier = baseModifier, content = content)
}

/**
 * Responsive Bento Card with icon medallion, title, and live subtitle.
 */
@Composable
fun BentoFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicContainerCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        accentColor = accentColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.22f),
                                accentColor.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = accentColor.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(21.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Ornate 8-Pointed Rub el Hizb Rosette Medallion for Quran verse numbers and Surah numbers.
 */
@Composable
fun AyahRosetteMedallion(
    numberText: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val fillTint = tint.copy(alpha = 0.14f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerR = this.size.minDimension * 0.46f
            drawCircle(color = fillTint, radius = outerR * 0.78f, center = center)
            drawRubElHizbStar(
                center = center,
                outerRadius = outerR,
                color = tint,
                strokeWidth = 3.2f
            )
        }
        Text(
            text = numberText,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            fontWeight = FontWeight.Bold,
            color = tint,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Ornate Mushaf-style Surah Cartouche Header with Bismillah banner reactive to active theme.
 */
@Composable
fun MushafSurahHeaderBanner(
    surahNumber: Int,
    surahTitle: String,
    surahArabicName: String,
    revelationAndAyahs: String,
    showBismillah: Boolean,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalIslamicThemePalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        themePalette.heroStart,
                        themePalette.heroMid,
                        themePalette.heroEnd
                    )
                )
            )
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            themePalette.metallicAccent.copy(alpha = 0.7f),
                            themePalette.metallicHighlight.copy(alpha = 0.55f),
                            themePalette.metallicAccent.copy(alpha = 0.7f)
                        )
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        IslamicGeometricBackground(
            modifier = Modifier.matchParentSize(),
            patternColor = Color.White,
            patternAlpha = 0.09f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AyahRosetteMedallion(
                    numberText = "$surahNumber",
                    size = 42.dp,
                    tint = themePalette.metallicHighlight
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = surahTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = revelationAndAyahs,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = surahArabicName,
                    fontFamily = AmiriQuranFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = themePalette.metallicHighlight,
                    maxLines = 1
                )
            }

            if (showBismillah) {
                Surface(
                    color = Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, themePalette.metallicHighlight.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        fontFamily = AmiriQuranFontFamily,
                        fontSize = 22.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
