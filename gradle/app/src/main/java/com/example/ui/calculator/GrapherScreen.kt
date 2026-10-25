package com.example.ui.calculator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.GlassOrangeTop
import com.example.ui.theme.LocalGlassColors
import com.example.util.LocalHapticManager
import kotlin.math.*

data class GraphEquation(
    val id: String,
    val name: String,
    val formulaLabel: String,
    val color: Color,
    val fn: (Double) -> Double
)

@Composable
fun GrapherScreen(
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current
    val hapticManager = LocalHapticManager.current

    val equations = remember {
        listOf(
            GraphEquation("sin", "Sine", "y = sin(x)", Color(0xFF38BDF8)) { sin(it) },
            GraphEquation("cos", "Cosine", "y = cos(x)", Color(0xFFA855F7)) { cos(it) },
            GraphEquation("parabola", "Parabola", "y = x²", Color(0xFFF97316)) { it * it },
            GraphEquation("cubic", "Cubic", "y = x³ - 3x", Color(0xFF10B981)) { it.pow(3) - 3 * it },
            GraphEquation("sqrt", "Square Root", "y = √x", Color(0xFFEC4899)) { if (it >= 0) sqrt(it) else Double.NaN },
            GraphEquation("reciprocal", "Reciprocal", "y = 1/x", Color(0xFFEAB308)) { if (abs(it) > 0.001) 1.0 / it else Double.NaN },
            GraphEquation("exp", "Exponential", "y = 2^x", Color(0xFF6366F1)) { 2.0.pow(it) }
        )
    }

    var selectedEqId by remember { mutableStateOf("sin") }
    val currentEq = equations.firstOrNull { it.id == selectedEqId } ?: equations[0]

    // Graph scale and pan offset
    var scale by remember { mutableFloatStateOf(45f) } // pixels per unit
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Trace point x in math space
    var traceX by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Equation selection chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(equations) { eq ->
                val isSelected = eq.id == selectedEqId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) eq.color.copy(alpha = 0.25f)
                            else tokens.surfaceGlass
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) eq.color else tokens.borderLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedEqId = eq.id }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = eq.formulaLabel,
                        color = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Trace Info Card
        val traceY = currentEq.fn(traceX.toDouble())
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            cornerRadius = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = currentEq.formulaLabel,
                        color = currentEq.color,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Trace: x = ${"%.2f".format(traceX)}, y = ${if (traceY.isNaN()) "Undefined" else "%.2f".format(traceY)}",
                        color = tokens.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { scale = (scale * 1.25f).coerceAtMost(200f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, "Zoom In", tint = tokens.textPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { scale = (scale / 1.25f).coerceAtLeast(15f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Zoom Out", tint = tokens.textPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            scale = 45f
                            panOffsetX = 0f
                            panOffsetY = 0f
                            traceX = 0f
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, "Reset View", tint = tokens.textMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Interactive Canvas Graph with Pinch-to-Zoom & Pan
        var canvasSize by remember { mutableStateOf(IntSize.Zero) }

        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp),
            cornerRadius = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (scale * zoom).coerceIn(8f, 350f)
                            val w = if (canvasSize.width > 0) canvasSize.width.toFloat() else 400f
                            val h = if (canvasSize.height > 0) canvasSize.height.toFloat() else 400f

                            val originX = w / 2f + panOffsetX
                            val originY = h / 2f + panOffsetY

                            // Adjust pan so zoom centers around the pinch centroid
                            panOffsetX += pan.x + (centroid.x - originX) * (1f - newScale / oldScale)
                            panOffsetY += pan.y + (centroid.y - originY) * (1f - newScale / oldScale)
                            scale = newScale
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val w = if (canvasSize.width > 0) canvasSize.width.toFloat() else 400f
                            val originX = w / 2f + panOffsetX
                            val mathX = (offset.x - originX) / scale
                            traceX = mathX
                            hapticManager?.performTick()
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val w = size.width
                    val h = size.height
                    val originX = w / 2f + panOffsetX
                    val originY = h / 2f + panOffsetY

                    val gridColor = if (tokens.isDark) Color(0x1FFFFFFF) else Color(0x1F000000)
                    val axisColor = if (tokens.isDark) Color(0x66FFFFFF) else Color(0x66000000)

                    // Grid lines (vertical)
                    val leftMathX = floor((-originX) / scale)
                    val rightMathX = ceil((w - originX) / scale)
                    for (gx in leftMathX.toInt()..rightMathX.toInt()) {
                        val px = originX + gx * scale
                        drawLine(
                            color = if (gx == 0) axisColor else gridColor,
                            start = Offset(px, 0f),
                            end = Offset(px, h),
                            strokeWidth = if (gx == 0) 2f else 1f
                        )
                    }

                    // Grid lines (horizontal)
                    val topMathY = ceil(originY / scale)
                    val bottomMathY = floor((originY - h) / scale)
                    for (gy in bottomMathY.toInt()..topMathY.toInt()) {
                        val py = originY - gy * scale
                        drawLine(
                            color = if (gy == 0) axisColor else gridColor,
                            start = Offset(0f, py),
                            end = Offset(w, py),
                            strokeWidth = if (gy == 0) 2f else 1f
                        )
                    }

                    // Plot equation curve
                    val path = Path()
                    var first = true

                    var px = 0f
                    val step = 2f
                    while (px <= w) {
                        val mathX = (px - originX) / scale
                        val mathY = currentEq.fn(mathX.toDouble())

                        if (!mathY.isNaN() && !mathY.isInfinite() && abs(mathY) < 1000.0) {
                            val py = originY - (mathY.toFloat() * scale)
                            if (py in -100f..(h + 100f)) {
                                if (first) {
                                    path.moveTo(px, py)
                                    first = false
                                } else {
                                    path.lineTo(px, py)
                                }
                            } else {
                                first = true
                            }
                        } else {
                            first = true
                        }
                        px += step
                    }

                    drawPath(
                        path = path,
                        color = currentEq.color,
                        style = Stroke(width = 3.5f)
                    )

                    // Draw Trace Point
                    if (!traceY.isNaN() && !traceY.isInfinite()) {
                        val tracePx = originX + traceX * scale
                        val tracePy = originY - (traceY.toFloat() * scale)

                        if (tracePx in 0f..w && tracePy in 0f..h) {
                            // Outer glow
                            drawCircle(
                                color = currentEq.color.copy(alpha = 0.35f),
                                radius = 16f,
                                center = Offset(tracePx, tracePy)
                            )
                            // Inner solid dot
                            drawCircle(
                                color = Color.White,
                                radius = 7f,
                                center = Offset(tracePx, tracePy)
                            )
                            drawCircle(
                                color = currentEq.color,
                                radius = 4.5f,
                                center = Offset(tracePx, tracePy)
                            )
                        }
                    }
                }

                // Interactive gesture indicator badge at top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tokens.surfaceGlass.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Pinch to zoom • Drag to pan",
                        color = tokens.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Trace slider to move trace point along curve
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trace X",
                color = tokens.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Slider(
                value = traceX,
                onValueChange = { traceX = it },
                valueRange = -10f..10f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = currentEq.color,
                    activeTrackColor = currentEq.color,
                    inactiveTrackColor = tokens.surfaceGlass
                )
            )
            Text(
                text = "${"%.1f".format(traceX)}",
                color = tokens.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}
