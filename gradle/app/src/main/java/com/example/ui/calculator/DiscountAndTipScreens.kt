package com.example.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.GlassOrangeTop
import com.example.ui.theme.LocalGlassColors
import java.text.DecimalFormat
import kotlin.math.ceil

@Composable
fun DiscountScreen(
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current
    val df = remember { DecimalFormat("#,##0.00") }

    var originalPriceStr by remember { mutableStateOf("100") }
    var discountPercent by remember { mutableFloatStateOf(20f) }
    var taxPercent by remember { mutableFloatStateOf(8f) }

    val originalPrice = originalPriceStr.toDoubleOrNull() ?: 0.0
    val discountAmount = originalPrice * (discountPercent / 100.0)
    val discountedPrice = (originalPrice - discountAmount).coerceAtLeast(0.0)
    val taxAmount = discountedPrice * (taxPercent / 100.0)
    val finalPrice = discountedPrice + taxAmount

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Price & inputs card
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ORIGINAL PRICE ($)", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = originalPriceStr,
                    onValueChange = { originalPriceStr = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = tokens.textPrimary,
                        unfocusedTextColor = tokens.textPrimary,
                        focusedBorderColor = GlassOrangeTop,
                        unfocusedBorderColor = tokens.borderLight.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Discount presets
                Text("DISCOUNT: ${discountPercent.toInt()}%", color = tokens.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10f, 15f, 20f, 25f, 50f).forEach { pct ->
                        val isSelected = discountPercent == pct
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GlassOrangeTop.copy(alpha = 0.25f) else tokens.surfaceGlass)
                                .border(1.dp, if (isSelected) GlassOrangeTop else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { discountPercent = pct }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${pct.toInt()}%", color = tokens.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Slider(
                    value = discountPercent,
                    onValueChange = { discountPercent = it },
                    valueRange = 0f..90f,
                    colors = SliderDefaults.colors(
                        thumbColor = GlassOrangeTop,
                        activeTrackColor = GlassOrangeTop,
                        inactiveTrackColor = tokens.surfaceGlass
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tax percentage slider
                Text("TAX RATE: ${"%.1f".format(taxPercent)}%", color = tokens.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = taxPercent,
                    onValueChange = { taxPercent = it },
                    valueRange = 0f..30f,
                    colors = SliderDefaults.colors(
                        thumbColor = tokens.textSecondary,
                        activeTrackColor = tokens.textSecondary,
                        inactiveTrackColor = tokens.surfaceGlass
                    )
                )
            }
        }

        // Summary result card
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
                Text("FINAL PRICE", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$${df.format(finalPrice)}",
                    color = GlassOrangeTop,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = tokens.borderLight.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("You Save", color = tokens.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "-$${df.format(discountAmount)}",
                            color = Color(0xFF10B981),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tax Amount", color = tokens.textSecondary, fontSize = 12.sp)
                        Text(
                            text = "+$${df.format(taxAmount)}",
                            color = tokens.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TipSplitScreen(
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current
    val df = remember { DecimalFormat("#,##0.00") }

    var billStr by remember { mutableStateOf("85.00") }
    var tipPercent by remember { mutableFloatStateOf(18f) }
    var peopleCount by remember { mutableIntStateOf(3) }
    var roundUp by remember { mutableStateOf(false) }

    val rawBill = billStr.toDoubleOrNull() ?: 0.0
    val rawTip = rawBill * (tipPercent / 100.0)
    val rawTotal = rawBill + rawTip

    val finalTotal = if (roundUp) ceil(rawTotal) else rawTotal
    val actualTip = (finalTotal - rawBill).coerceAtLeast(0.0)
    val perPerson = if (peopleCount > 0) finalTotal / peopleCount else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Bill Input & Tip Controls Card
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BILL AMOUNT ($)", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = billStr,
                    onValueChange = { billStr = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = tokens.textPrimary,
                        unfocusedTextColor = tokens.textPrimary,
                        focusedBorderColor = GlassOrangeTop,
                        unfocusedBorderColor = tokens.borderLight.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tip Presets
                Text("TIP PERCENTAGE: ${tipPercent.toInt()}%", color = tokens.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10f, 15f, 18f, 20f, 25f).forEach { pct ->
                        val isSelected = tipPercent == pct
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GlassOrangeTop.copy(alpha = 0.25f) else tokens.surfaceGlass)
                                .border(1.dp, if (isSelected) GlassOrangeTop else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { tipPercent = pct }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${pct.toInt()}%", color = tokens.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // People Split Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Split Between", color = tokens.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tokens.surfaceGlass)
                                .clickable { if (peopleCount > 1) peopleCount-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, null, tint = tokens.textPrimary, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "$peopleCount people",
                            color = tokens.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tokens.surfaceGlass)
                                .clickable { if (peopleCount < 50) peopleCount++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = tokens.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Round Up Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Round Up Total", color = tokens.textSecondary, fontSize = 13.sp)
                    Switch(
                        checked = roundUp,
                        onCheckedChange = { roundUp = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GlassOrangeTop
                        )
                    )
                }
            }
        }

        // Summary Card: Per Person & Total
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
                Text("PER PERSON", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$${df.format(perPerson)}",
                    color = GlassOrangeTop,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = tokens.borderLight.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Tip", color = tokens.textSecondary, fontSize = 12.sp)
                        Text("$${df.format(actualTip)}", color = tokens.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Bill", color = tokens.textSecondary, fontSize = 12.sp)
                        Text("$${df.format(finalTotal)}", color = tokens.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
