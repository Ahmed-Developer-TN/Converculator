package com.example.ui.calculator

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.GlassOrangeTop
import com.example.ui.theme.LocalGlassColors
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Composable
fun DateCalcScreen(
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Difference, 1: Add/Subtract

    // Difference mode state
    val cal1 = remember { Calendar.getInstance() }
    val cal2 = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) } }
    var startDate by remember { mutableStateOf(cal1.time) }
    var endDate by remember { mutableStateOf(cal2.time) }

    // Add/Subtract mode state
    var baseDate by remember { mutableStateOf(Calendar.getInstance().time) }
    var isAdd by remember { mutableStateOf(true) }
    var daysDelta by remember { mutableIntStateOf(14) }

    fun showPicker(initialDate: Date, onPicked: (Date) -> Unit) {
        val c = Calendar.getInstance().apply { time = initialDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                }.time
                onPicked(chosen)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Date Difference", "Add / Subtract Days").forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) GlassOrangeTop.copy(alpha = 0.25f) else tokens.surfaceGlass)
                        .border(
                            1.dp,
                            if (isSelected) GlassOrangeTop else tokens.borderLight.copy(alpha = 0.2f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // DIFFERENCE MODE
            val diffMillis = abs(endDate.time - startDate.time)
            val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
            val totalWeeks = totalDays / 7
            val remainingDays = totalDays % 7

            // Calendar difference calculation
            val cStart = Calendar.getInstance().apply { time = minOf(startDate, endDate) }
            val cEnd = Calendar.getInstance().apply { time = maxOf(startDate, endDate) }
            var years = cEnd.get(Calendar.YEAR) - cStart.get(Calendar.YEAR)
            var months = cEnd.get(Calendar.MONTH) - cStart.get(Calendar.MONTH)
            var days = cEnd.get(Calendar.DAY_OF_MONTH) - cStart.get(Calendar.DAY_OF_MONTH)
            if (days < 0) {
                months--
                cEnd.add(Calendar.MONTH, -1)
                days += cEnd.getActualMaximum(Calendar.DAY_OF_MONTH)
            }
            if (months < 0) {
                years--
                months += 12
            }

            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // From Date picker
                    Text("FROM", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceGlass)
                            .clickable { showPicker(startDate) { startDate = it } }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sdf.format(startDate), color = tokens.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.CalendarToday, null, tint = GlassOrangeTop, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // To Date picker
                    Text("TO", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceGlass)
                            .clickable { showPicker(endDate) { endDate = it } }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sdf.format(endDate), color = tokens.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.CalendarToday, null, tint = GlassOrangeTop, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Results Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TOTAL DURATION", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$totalDays days",
                        color = tokens.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = tokens.borderLight.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$years", color = GlassOrangeTop, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Years", color = tokens.textSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$months", color = GlassOrangeTop, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Months", color = tokens.textSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$days", color = GlassOrangeTop, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Days", color = tokens.textSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalWeeks w $remainingDays d", color = tokens.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Weeks", color = tokens.textSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // ADD / SUBTRACT MODE
            val targetCal = Calendar.getInstance().apply {
                time = baseDate
                add(Calendar.DAY_OF_YEAR, if (isAdd) daysDelta else -daysDelta)
            }
            val targetDate = targetCal.time

            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("START DATE", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceGlass)
                            .clickable { showPicker(baseDate) { baseDate = it } }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sdf.format(baseDate), color = tokens.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Default.CalendarToday, null, tint = GlassOrangeTop, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Add / Subtract toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(true to "+ Add Days", false to "− Subtract Days").forEach { (addMode, label) ->
                            val isSelected = isAdd == addMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) GlassOrangeTop.copy(alpha = 0.25f) else tokens.surfaceGlass)
                                    .border(1.dp, if (isSelected) GlassOrangeTop else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { isAdd = addMode }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick day presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(7 to "+1 Wk", 14 to "+2 Wks", 30 to "+30 D", 90 to "+90 D").forEach { (d, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (daysDelta == d) tokens.activePillGlass else tokens.surfaceGlass)
                                    .clickable { daysDelta = d }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = tokens.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stepper / Slider for days
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Days: $daysDelta", color = tokens.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = daysDelta.toFloat(),
                            onValueChange = { daysDelta = it.toInt() },
                            valueRange = 1f..365f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = GlassOrangeTop,
                                activeTrackColor = GlassOrangeTop,
                                inactiveTrackColor = tokens.surfaceGlass
                            )
                        )
                    }
                }
            }

            // Target Result Card
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("RESULTING DATE", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sdf.format(targetDate),
                        color = GlassOrangeTop,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isAdd) "+$daysDelta" else "-$daysDelta"} days from start",
                        color = tokens.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
