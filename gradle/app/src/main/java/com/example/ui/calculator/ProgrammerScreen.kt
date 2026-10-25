package com.example.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WordSize
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.GlassOrangeTop
import com.example.ui.theme.LocalGlassColors

enum class Radix(val base: Int, val label: String) {
    HEX(16, "HEX"),
    DEC(10, "DEC"),
    OCT(8, "OCT"),
    BIN(2, "BIN")
}

@Composable
fun ProgrammerScreen(
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current

    var activeRadix by remember { mutableStateOf(Radix.DEC) }
    var wordSize by remember { mutableStateOf(WordSize.QWORD) }

    var currentValue by remember { mutableLongStateOf(0L) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var operandValue by remember { mutableLongStateOf(0L) }
    var isNewEntry by remember { mutableStateOf(true) }

    fun maskValue(v: Long): Long {
        return when (wordSize) {
            WordSize.QWORD -> v
            WordSize.DWORD -> v and 0xFFFFFFFFL
            WordSize.WORD -> v and 0xFFFFL
            WordSize.BYTE -> v and 0xFFL
        }
    }

    val hexStr = remember(currentValue, wordSize) {
        val masked = maskValue(currentValue)
        val padLen = when (wordSize) {
            WordSize.QWORD -> 16
            WordSize.DWORD -> 8
            WordSize.WORD -> 4
            WordSize.BYTE -> 2
        }
        java.lang.Long.toHexString(masked).uppercase().padStart(padLen, '0')
    }

    val decStr = remember(currentValue, wordSize) {
        maskValue(currentValue).toString()
    }

    val octStr = remember(currentValue, wordSize) {
        val masked = maskValue(currentValue)
        java.lang.Long.toOctalString(masked)
    }

    val binStr = remember(currentValue, wordSize) {
        val masked = maskValue(currentValue)
        val raw = java.lang.Long.toBinaryString(masked)
        val bitCount = wordSize.bits
        val padded = raw.padStart(bitCount, '0')
        // Group in 4s
        padded.chunked(4).joinToString(" ")
    }

    fun handleDigit(d: Char) {
        val digitVal = d.toString().toLongOrNull(activeRadix.base) ?: return
        if (isNewEntry) {
            currentValue = maskValue(digitVal)
            isNewEntry = false
        } else {
            val newVal = maskValue(currentValue * activeRadix.base + digitVal)
            currentValue = newVal
        }
    }

    fun handleOp(op: String) {
        when (op) {
            "NOT" -> {
                currentValue = maskValue(currentValue.inv())
            }
            "LSH" -> {
                currentValue = maskValue(currentValue shl 1)
            }
            "RSH" -> {
                currentValue = maskValue(currentValue ushr 1)
            }
            "AND", "OR", "XOR" -> {
                operandValue = currentValue
                pendingOp = op
                isNewEntry = true
            }
            "=" -> {
                pendingOp?.let {
                    currentValue = when (it) {
                        "AND" -> maskValue(operandValue and currentValue)
                        "OR" -> maskValue(operandValue or currentValue)
                        "XOR" -> maskValue(operandValue xor currentValue)
                        else -> currentValue
                    }
                    pendingOp = null
                    isNewEntry = true
                }
            }
            "AC" -> {
                currentValue = 0L
                operandValue = 0L
                pendingOp = null
                isNewEntry = true
            }
            "DEL" -> {
                if (!isNewEntry && currentValue != 0L) {
                    val str = when (activeRadix) {
                        Radix.HEX -> java.lang.Long.toHexString(currentValue)
                        Radix.DEC -> currentValue.toString()
                        Radix.OCT -> java.lang.Long.toOctalString(currentValue)
                        Radix.BIN -> java.lang.Long.toBinaryString(currentValue)
                    }
                    if (str.length > 1) {
                        val sub = str.substring(0, str.length - 1)
                        currentValue = maskValue(sub.toLongOrNull(activeRadix.base) ?: 0L)
                    } else {
                        currentValue = 0L
                        isNewEntry = true
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Word Size selector (QWORD, DWORD, WORD, BYTE)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WordSize.values().forEach { ws ->
                val isSelected = wordSize == ws
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GlassOrangeTop.copy(alpha = 0.25f) else tokens.surfaceGlass)
                        .border(
                            1.dp,
                            if (isSelected) GlassOrangeTop else tokens.borderLight.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { wordSize = ws }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ws.label,
                        color = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4 Bases Multi-Display Card
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                listOf(
                    Triple(Radix.HEX, "HEX", hexStr),
                    Triple(Radix.DEC, "DEC", decStr),
                    Triple(Radix.OCT, "OCT", octStr),
                    Triple(Radix.BIN, "BIN", binStr)
                ).forEach { (radix, label, value) ->
                    val isSelected = activeRadix == radix
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) GlassOrangeTop.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { activeRadix = radix }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) GlassOrangeTop else tokens.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = value,
                            color = if (isSelected) tokens.textPrimary else tokens.textSecondary,
                            fontSize = if (radix == Radix.BIN) 11.sp else 15.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Bitwise operators row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("AND", "OR", "XOR", "NOT", "LSH", "RSH").forEach { op ->
                LiquidGlassButton(
                    onClick = { handleOp(op) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    isOperator = true
                ) {
                    Text(
                        text = op,
                        color = tokens.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Keypad Grid for Programmer Mode
        val rows = listOf(
            listOf("D", "E", "F", "AC"),
            listOf("A", "B", "C", "DEL"),
            listOf("7", "8", "9", "="),
            listOf("4", "5", "6", "0"),
            listOf("1", "2", "3", "00")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 78.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        val isHexAlpha = key in listOf("A", "B", "C", "D", "E", "F")
                        val isOctInvalid = key in listOf("8", "9") && activeRadix == Radix.OCT
                        val isBinInvalid = key !in listOf("0", "1", "AC", "DEL", "=") && activeRadix == Radix.BIN
                        val isEnabled = when {
                            isHexAlpha -> activeRadix == Radix.HEX
                            isOctInvalid -> false
                            isBinInvalid -> false
                            else -> true
                        }

                        val isEquals = key == "="
                        val isSpecial = key in listOf("AC", "DEL")

                        LiquidGlassButton(
                            onClick = {
                                if (isEnabled) {
                                    when (key) {
                                        "AC", "DEL", "=" -> handleOp(key)
                                        "00" -> {
                                            handleDigit('0')
                                            handleDigit('0')
                                        }
                                        else -> handleDigit(key[0])
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            isEquals = isEquals,
                            isOperator = isSpecial,
                            isFunction = isHexAlpha
                        ) {
                            Text(
                                text = key,
                                color = if (isEnabled) {
                                    if (isEquals) Color.White else tokens.textPrimary
                                } else {
                                    tokens.textMuted.copy(alpha = 0.3f)
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
