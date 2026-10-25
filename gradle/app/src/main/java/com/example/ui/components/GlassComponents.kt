package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MainSection
import com.example.ui.theme.*
import com.example.util.HapticFeedbackManager
import com.example.util.HapticFeedbackType
import com.example.util.LocalHapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ambient background with slow-moving organic liquid gradient blobs.
 */
@Composable
fun DynamicLiquidBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_blobs")

    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t1"
    )

    val t2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t2"
    )

    val baseBgColor by animateColorAsState(
        targetValue = if (isDark) DarkBackground else LightBackground,
        animationSpec = tween(500),
        label = "bgColor"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = baseBgColor)

        val w = size.width
        val h = size.height

        // Orb 1: Violet/Indigo blob
        val orb1X = w * 0.35f + cos(t1) * (w * 0.25f)
        val orb1Y = h * 0.25f + sin(t1) * (h * 0.15f)
        val orb1Radius = w * 0.65f
        val orb1Color = if (isDark) BlobIndigo.copy(alpha = 0.28f) else BlobBlue.copy(alpha = 0.22f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orb1Color, orb1Color.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(orb1X, orb1Y),
                radius = orb1Radius
            ),
            radius = orb1Radius,
            center = Offset(orb1X, orb1Y)
        )

        // Orb 2: Cyan/Teal blob
        val orb2X = w * 0.75f - sin(t2) * (w * 0.25f)
        val orb2Y = h * 0.60f + cos(t2) * (h * 0.20f)
        val orb2Radius = w * 0.55f
        val orb2Color = if (isDark) BlobCyan.copy(alpha = 0.22f) else BlobCyan.copy(alpha = 0.18f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orb2Color, orb2Color.copy(alpha = 0.06f), Color.Transparent),
                center = Offset(orb2X, orb2Y),
                radius = orb2Radius
            ),
            radius = orb2Radius,
            center = Offset(orb2X, orb2Y)
        )

        // Orb 3: Amber/Orange accent blob
        val orb3X = w * 0.25f + sin(t1 * 0.7f) * (w * 0.20f)
        val orb3Y = h * 0.80f - cos(t2 * 0.8f) * (h * 0.15f)
        val orb3Radius = w * 0.45f
        val orb3Color = if (isDark) BlobAmber.copy(alpha = 0.18f) else BlobAmber.copy(alpha = 0.15f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orb3Color, orb3Color.copy(alpha = 0.04f), Color.Transparent),
                center = Offset(orb3X, orb3Y),
                radius = orb3Radius
            ),
            radius = orb3Radius,
            center = Offset(orb3X, orb3Y)
        )

        // Orb 4: Soft violet/pink blob
        val orb4X = w * 0.85f + cos(t2) * (w * 0.15f)
        val orb4Y = h * 0.18f + sin(t1 * 1.2f) * (h * 0.12f)
        val orb4Radius = w * 0.5f
        val orb4Color = if (isDark) BlobViolet.copy(alpha = 0.25f) else BlobPink.copy(alpha = 0.16f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(orb4Color, Color.Transparent),
                center = Offset(orb4X, orb4Y),
                radius = orb4Radius
            ),
            radius = orb4Radius,
            center = Offset(orb4X, orb4Y)
        )
    }
}

/**
 * OxygenOS 16 / iOS Liquid Glass Button:
 * - Perfectly circular (or rounded)
 * - Translucent gradient fill
 * - 1px inner highlight border (light top-left, soft dark bottom-right)
 * - Specular sheen across top edge
 * - Spring scale-down on press
 * - Radial touch glow following exact touch point
 * - Haptic feedback on tap
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEquals: Boolean = false,
    isOperator: Boolean = false,
    isFunction: Boolean = false,
    hapticType: HapticFeedbackType? = null,
    testTag: String = "",
    content: @Composable () -> Unit
) {
    val tokens = LocalGlassColors.current
    val view = LocalView.current
    val hapticManager = LocalHapticManager.current
    val coroutineScope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var touchGlowAlpha by remember { mutableFloatStateOf(0f) }

    // Spring scale animation
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btnScale"
    )

    // Background color / brush
    val defaultFill = when {
        isEquals -> null
        isOperator -> tokens.operatorGlass
        isFunction -> tokens.functionGlass
        else -> tokens.surfaceGlass
    }

    val baseModifier = if (testTag.isNotEmpty()) modifier.testTag(testTag) else modifier

    Box(
        modifier = baseModifier
            .scale(scaleAnim)
            .shadow(
                elevation = if (isEquals) 12.dp else if (tokens.isDark) 4.dp else 6.dp,
                shape = CircleShape,
                ambientColor = if (isEquals) GlassOrangeGlow else Color.Black.copy(alpha = 0.2f),
                spotColor = if (isEquals) GlassOrangeTop else Color.Black.copy(alpha = 0.3f)
            )
            .clip(CircleShape)
            .drawBehind {
                val w = size.width
                val h = size.height

                // Fill background
                if (isEquals) {
                    drawRect(tokens.equalsBrush)
                    // Soft orange inner glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(w * 0.5f, h * 0.35f),
                            radius = w * 0.6f
                        )
                    )
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                defaultFill ?: tokens.surfaceGlass,
                                (defaultFill ?: tokens.surfaceGlass).copy(alpha = (defaultFill ?: tokens.surfaceGlass).alpha * 0.75f)
                            )
                        )
                    )
                }

                // Specular sheen across top-half
                val sheenBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (tokens.isDark) 0.22f else 0.45f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.55f
                )
                drawCircle(
                    brush = sheenBrush,
                    radius = w * 0.48f,
                    center = Offset(w * 0.5f, h * 0.15f)
                )

                // Dynamic radial touch glow at touch point
                touchPosition?.let { touch ->
                    if (touchGlowAlpha > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = touchGlowAlpha * 0.45f),
                                    if (isEquals) GlassOrangeTop.copy(alpha = touchGlowAlpha * 0.35f)
                                    else Color.White.copy(alpha = touchGlowAlpha * 0.15f),
                                    Color.Transparent
                                ),
                                center = touch,
                                radius = w * 0.8f
                            ),
                            center = touch,
                            radius = w * 0.8f
                        )
                    }
                }

                // 1px inner border: top-left light highlight, bottom-right dark edge
                val borderHighlightBrush = Brush.linearGradient(
                    colors = listOf(
                        tokens.borderLight,
                        tokens.borderLight.copy(alpha = 0.3f),
                        tokens.borderDark
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
                drawCircle(
                    brush = borderHighlightBrush,
                    radius = (w / 2f) - 1f,
                    style = Stroke(width = 1.5f)
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        touchPosition = offset
                        isPressed = true
                        touchGlowAlpha = 1f
                        val feedback = hapticType ?: when {
                            isEquals -> HapticFeedbackType.CALCULATION_SUCCESS
                            isOperator -> HapticFeedbackType.OPERATOR_TAP
                            isFunction -> HapticFeedbackType.TICK
                            else -> HapticFeedbackType.KEY_TAP
                        }
                        hapticManager?.perform(feedback)
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        } catch (e: Exception) {
                            // ignore
                        }
                        tryAwaitRelease()
                        isPressed = false
                        coroutineScope.launch {
                            val anim = Animatable(1f)
                            anim.animateTo(0f, tween(250)) {
                                touchGlowAlpha = this.value
                            }
                            touchPosition = null
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Liquid Glass Card container with frosted backdrop styling.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalGlassColors.current

    Box(
        modifier = modifier
            .shadow(
                elevation = if (tokens.isDark) 8.dp else 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tokens.surfaceGlass,
                        tokens.surfaceGlass.copy(alpha = tokens.surfaceGlass.alpha * 0.8f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        tokens.borderLight,
                        tokens.borderLight.copy(alpha = 0.2f),
                        tokens.borderDark
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .drawBehind {
                // Subtle top specular highlight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (tokens.isDark) 0.12f else 0.3f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = 40.dp.toPx()
                    )
                )
            },
        content = content
    )
}

/**
 * Pill-shaped Dark / Light Mode Toggle:
 * Light: light sky blue pill with label "Light" and glowing yellow SUN knob on right.
 * Dark: dark navy pill with crescent moon icon and label "Dark Mode" and white/pale MOON sphere knob with subtle craters on right.
 */
@Composable
fun ThemeTogglePill(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val hapticManager = LocalHapticManager.current
    val pillBgColor by animateColorAsState(
        targetValue = if (isDark) PillDarkBg else PillLightBg,
        animationSpec = tween(400),
        label = "pillBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFFE2E8F0) else Color(0xFF0369A1),
        animationSpec = tween(400),
        label = "pillText"
    )

    // Animated knob offset
    val knobPosition by animateFloatAsState(
        targetValue = if (isDark) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "knobPos"
    )

    val knobRotation by animateFloatAsState(
        targetValue = if (isDark) 360f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "knobRot"
    )

    Box(
        modifier = modifier
            .width(136.dp)
            .height(42.dp)
            .shadow(6.dp, RoundedCornerShape(21.dp))
            .clip(RoundedCornerShape(21.dp))
            .background(pillBgColor)
            .border(
                width = 1.dp,
                color = if (isDark) Color(0x33FFFFFF) else Color(0x66FFFFFF),
                shape = RoundedCornerShape(21.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    hapticManager?.performTick()
                    onToggle()
                }
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Label on left/center
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isDark) Arrangement.Start else Arrangement.End
        ) {
            if (isDark) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Dark",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Light",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Sliding knob (Moon or Sun sphere)
        val pillWidthPx = with(density) { 136.dp.toPx() }
        val knobSizePx = with(density) { 32.dp.toPx() }
        val maxOffsetPx = pillWidthPx - knobSizePx - with(density) { 12.dp.toPx() }
        val currentOffsetPx = if (isDark) maxOffsetPx * knobPosition else maxOffsetPx * (1f - knobPosition)

        Box(
            modifier = Modifier
                .offset(x = with(density) { currentOffsetPx.toDp() })
                .size(32.dp)
                .shadow(
                    elevation = if (isDark) 4.dp else 6.dp,
                    shape = CircleShape,
                    ambientColor = if (isDark) Color.Black else SunGlow.copy(alpha = 0.5f),
                    spotColor = if (isDark) Color.White.copy(alpha = 0.2f) else SunGlow
                )
                .clip(CircleShape)
                .scale(0.96f)
                .drawBehind {
                    val r = size.width / 2f
                    if (isDark) {
                        // Moon sphere with craters
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, MoonSphere, Color(0xFFCBD5E1)),
                                center = Offset(r * 0.7f, r * 0.7f),
                                radius = r * 1.1f
                            )
                        )
                        // Moon craters
                        drawCircle(
                            color = MoonCrater.copy(alpha = 0.65f),
                            radius = r * 0.22f,
                            center = Offset(r * 0.65f, r * 0.6f)
                        )
                        drawCircle(
                            color = MoonCrater.copy(alpha = 0.5f),
                            radius = r * 0.16f,
                            center = Offset(r * 1.25f, r * 1.1f)
                        )
                        drawCircle(
                            color = MoonCrater.copy(alpha = 0.4f),
                            radius = r * 0.14f,
                            center = Offset(r * 1.1f, r * 0.5f)
                        )
                    } else {
                        // Sun sphere with radiant gradient & soft glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF9C4), SunCenter, SunGlow, Color(0xFFF59E0B)),
                                center = Offset(r * 0.8f, r * 0.8f),
                                radius = r * 1.1f
                            )
                        )
                    }
                }
        )
    }
}

/**
 * Floating Liquid Glass Navigation Menu:
 * Fixed at bottom center, pill-shaped, with smooth sliding morphing liquid indicator.
 */
@Composable
fun FloatingLiquidNavBar(
    currentSection: MainSection,
    onSectionSelected: (MainSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current
    val hapticManager = LocalHapticManager.current

    val tabWidth = 138.dp
    val tabHeight = 46.dp
    val totalTabs = 2

    val targetProgress = if (currentSection == MainSection.CALCULATOR) 0f else 1f

    val animProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 320f
        ),
        label = "liquidIndicatorProgress"
    )

    // Liquid stretch calculation: expands horizontally while traveling
    val travelVelocity = kotlin.math.sin(animProgress * Math.PI.toFloat()).coerceAtLeast(0f)
    val extraStretch = (travelVelocity * 14f).dp

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        tokens.navGlassBg,
                        tokens.navGlassBg.copy(alpha = tokens.navGlassBg.alpha * 0.85f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        tokens.borderLight,
                        tokens.borderLight.copy(alpha = 0.25f),
                        tokens.borderDark
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(6.dp)
    ) {
        // Container of the tabs
        Box(
            modifier = Modifier
                .width(tabWidth * totalTabs)
                .height(tabHeight)
        ) {
            // Sliding Liquid Glass Pill Indicator
            val indicatorX = (tabWidth * animProgress) - (extraStretch / 2f)
            Box(
                modifier = Modifier
                    .offset(x = indicatorX.coerceAtLeast(0.dp))
                    .width(tabWidth + extraStretch)
                    .height(tabHeight)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(23.dp),
                        ambientColor = Color.Black.copy(alpha = 0.2f),
                        spotColor = if (tokens.isDark) GlassOrangeTop.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f)
                    )
                    .clip(RoundedCornerShape(23.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tokens.activePillGlass,
                                tokens.activePillGlass.copy(alpha = tokens.activePillGlass.alpha * 0.72f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                tokens.borderLight,
                                tokens.borderLight.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(23.dp)
                    )
                    .drawBehind {
                        // Subtle specular highlight on top of the liquid pill
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (tokens.isDark) 0.2f else 0.4f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.5f
                            )
                        )
                    }
            )

            // Two interactive Tab buttons overlaid on top
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainSection.values().forEach { section ->
                    val isSelected = currentSection == section

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (tokens.isDark) GlassOrangeTop else GlassOrangeBottom
                        } else {
                            tokens.textMuted
                        },
                        animationSpec = tween(250),
                        label = "tabIconColor"
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                        animationSpec = tween(250),
                        label = "tabTextColor"
                    )

                    val scaleAnim by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.96f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "tabScale"
                    )

                    Box(
                        modifier = Modifier
                            .width(tabWidth)
                            .height(tabHeight)
                            .scale(scaleAnim)
                            .clip(RoundedCornerShape(23.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (currentSection != section) {
                                        hapticManager?.performTick()
                                        onSectionSelected(section)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = when (section) {
                                    MainSection.CALCULATOR -> Icons.Default.Calculate
                                    MainSection.CONVERTER -> Icons.Default.SyncAlt
                                },
                                contentDescription = section.label,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = section.label,
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
