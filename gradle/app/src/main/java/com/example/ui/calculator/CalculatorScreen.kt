package com.example.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AngleUnit
import com.example.model.CalculatorType
import com.example.model.HistoryItem
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.ThemeTogglePill
import com.example.ui.theme.GlassOrangeTop
import com.example.ui.theme.LocalGlassColors

@Composable
fun CalculatorScreen(
    currentType: CalculatorType,
    onTypeChange: (CalculatorType) -> Unit,
    displayExpression: String,
    displayResult: String,
    angleUnit: AngleUnit,
    onAngleUnitToggle: () -> Unit,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onFunction: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onOpenHistory: () -> Unit,
    isDark: Boolean,
    onToggleDark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current
    var isDropdownOpen by remember { mutableStateOf(false) }
    var is2ndFunction by remember { mutableStateOf(false) }

    // Blinking cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    val hapticManager = com.example.util.LocalHapticManager.current

    // Swipe left/right on display to delete last digit
    var totalDrag by remember { mutableFloatStateOf(0f) }
    val draggableState = rememberDraggableState { delta ->
        totalDrag += delta
        if (totalDrag > 50f || totalDrag < -50f) {
            hapticManager?.performKeyTap()
            onBackspace()
            totalDrag = 0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Calculator Type Dropdown trigger
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(tokens.surfaceGlass)
                        .border(1.dp, tokens.borderLight.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .clickable {
                            hapticManager?.performTick()
                            isDropdownOpen = !isDropdownOpen
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentType.displayName,
                        color = tokens.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Select Type",
                        tint = tokens.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Glass Dropdown Menu
                DropdownMenu(
                    expanded = isDropdownOpen,
                    onDismissRequest = { isDropdownOpen = false },
                    modifier = Modifier
                        .background(if (tokens.isDark) Color(0xFF0F172A).copy(alpha = 0.95f) else Color(0xFFF8FAFC).copy(alpha = 0.95f))
                        .border(1.dp, tokens.borderLight.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    CalculatorType.values().forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = type.displayName,
                                    color = if (type == currentType) GlassOrangeTop else tokens.textPrimary,
                                    fontWeight = if (type == currentType) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                hapticManager?.performTick()
                                onTypeChange(type)
                                isDropdownOpen = false
                            }
                        )
                    }
                }
            }

            // Right side: History button & Theme toggle pill
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(tokens.surfaceGlass)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = tokens.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                ThemeTogglePill(
                    isDark = isDark,
                    onToggle = onToggleDark
                )
            }
        }

        // Sub-screen routing based on Calculator Type
        when (currentType) {
            CalculatorType.GRAPHER -> {
                GrapherScreen()
            }
            CalculatorType.PROGRAMMER -> {
                ProgrammerScreen()
            }
            CalculatorType.DATE_CALC -> {
                DateCalcScreen()
            }
            CalculatorType.DISCOUNT -> {
                DiscountScreen()
            }
            CalculatorType.TIP_SPLIT -> {
                TipSplitScreen()
            }
            CalculatorType.STANDARD, CalculatorType.SCIENTIFIC -> {
                // STANDARD & SCIENTIFIC DISPLAY AND KEYPAD
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Display Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (currentType == CalculatorType.SCIENTIFIC) 0.65f else 0.85f)
                            .draggable(
                                state = draggableState,
                                orientation = Orientation.Horizontal,
                                onDragStopped = { totalDrag = 0f }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Smaller expression line above result
                            if (displayExpression.isNotEmpty()) {
                                Text(
                                    text = displayExpression,
                                    color = tokens.textSecondary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 2,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            // Large right-aligned result with auto-shrinking font and blinking cursor
                            val resultText = displayResult.ifEmpty { "0" }
                            val fontSize = when {
                                resultText.length > 14 -> 32.sp
                                resultText.length > 10 -> 40.sp
                                resultText.length > 7 -> 50.sp
                                else -> 60.sp
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = resultText,
                                    color = tokens.textPrimary,
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.End,
                                    maxLines = 1
                                )
                                // Orange blinking cursor
                                Box(
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .width(3.dp)
                                        .height((fontSize.value * 0.85).dp)
                                        .background(GlassOrangeTop.copy(alpha = cursorAlpha))
                                )
                            }
                        }
                    }

                    // Scientific functions panel (if scientific mode is active)
                    if (currentType == CalculatorType.SCIENTIFIC) {
                        ScientificFunctionRow(
                            angleUnit = angleUnit,
                            is2nd = is2ndFunction,
                            onToggleAngleUnit = onAngleUnitToggle,
                            onToggle2nd = { is2ndFunction = !is2ndFunction },
                            onFunction = onFunction
                        )
                    }

                    // Standard Keypad Layout:
                    // AC | % | ⌫ | ÷
                    // 7  | 8 | 9 | ×
                    // 4  | 5 | 6 | −
                    // 1  | 2 | 3 | +
                    // 00 | 0 | . | =
                    StandardKeypadGrid(
                        onDigit = onDigit,
                        onOperator = onOperator,
                        onEquals = onEquals,
                        onClear = onClear,
                        onBackspace = onBackspace,
                        onPercent = { onOperator("%") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.5f)
                            .padding(bottom = 80.dp) // Leave margin for floating navbar
                    )
                }
            }
        }
    }
}

@Composable
fun ScientificFunctionRow(
    angleUnit: AngleUnit,
    is2nd: Boolean,
    onToggleAngleUnit: () -> Unit,
    onToggle2nd: () -> Unit,
    onFunction: (String) -> Unit
) {
    val tokens = LocalGlassColors.current

    val row1 = if (is2nd) {
        listOf("2nd", "asin", "acos", "atan", "xʸ")
    } else {
        listOf("2nd", "sin", "cos", "tan", "x²")
    }

    val row2 = listOf(
        if (angleUnit == AngleUnit.DEG) "DEG" else "RAD",
        "log", "ln", "√", "!"
    )

    val row3 = listOf("π", "e", "(", ")", "Ans")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(row1, row2, row3).forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowKeys.forEach { key ->
                    val isAccent = (key == "2nd" && is2nd) || (key in listOf("DEG", "RAD"))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAccent) GlassOrangeTop.copy(alpha = 0.25f)
                                else tokens.surfaceGlass
                            )
                            .border(
                                1.dp,
                                if (isAccent) GlassOrangeTop else tokens.borderLight.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                when (key) {
                                    "2nd" -> onToggle2nd()
                                    "DEG", "RAD" -> onToggleAngleUnit()
                                    else -> onFunction(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            color = if (isAccent) GlassOrangeTop else tokens.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StandardKeypadGrid(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onPercent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current

    val keyRows = listOf(
        listOf("AC", "%", "⌫", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("00", "0", ".", "=")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keyRows.forEach { rowKeys ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowKeys.forEach { key ->
                    val isEquals = key == "="
                    val isOperator = key in listOf("÷", "×", "−", "+")
                    val isFunction = key in listOf("AC", "%", "⌫")

                    LiquidGlassButton(
                        onClick = {
                            when (key) {
                                "AC" -> onClear()
                                "%" -> onPercent()
                                "⌫" -> onBackspace()
                                "÷", "×", "−", "+" -> onOperator(key)
                                "=" -> onEquals()
                                else -> onDigit(key)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        isEquals = isEquals,
                        isOperator = isOperator,
                        isFunction = isFunction,
                        testTag = "key_$key"
                    ) {
                        Text(
                            text = key,
                            color = when {
                                isEquals -> Color.White
                                isOperator -> if (tokens.isDark) GlassOrangeTop else Color(0xFFEA580C)
                                else -> tokens.textPrimary
                            },
                            fontSize = when (key) {
                                "AC", "00" -> 22.sp
                                "⌫" -> 24.sp
                                "÷", "×", "−", "+", "=" -> 30.sp
                                else -> 28.sp
                            },
                            fontWeight = if (isEquals || isOperator) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
