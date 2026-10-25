package com.example.ui.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ConverterEngine
import com.example.model.FavoriteConversion
import com.example.model.UnitCategory
import com.example.model.UnitDefinition
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.GlassOrangeTop
import com.example.ui.theme.LocalGlassColors

@Composable
fun ConverterScreen(
    currentCategory: UnitCategory,
    onCategoryChange: (UnitCategory) -> Unit,
    fromUnit: UnitDefinition,
    toUnit: UnitDefinition,
    onFromUnitChange: (UnitDefinition) -> Unit,
    onToUnitChange: (UnitDefinition) -> Unit,
    onSwapUnits: () -> Unit,
    inputValue: String,
    onInputValueChange: (String) -> Unit,
    favorites: List<FavoriteConversion>,
    onSelectFavorite: (FavoriteConversion) -> Unit,
    onToggleFavorite: () -> Unit,
    isFavoriteCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassColors.current
    val context = LocalContext.current
    val hapticManager = com.example.util.LocalHapticManager.current

    var isCategoryDropdownOpen by remember { mutableStateOf(false) }
    var isFromUnitSheetOpen by remember { mutableStateOf(false) }
    var isToUnitSheetOpen by remember { mutableStateOf(false) }

    // Swap button rotation animation
    var swapRotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = swapRotation,
        animationSpec = spring(stiffness = 300f),
        label = "swapRotation"
    )

    // Calculate converted result
    val numInput = inputValue.toDoubleOrNull() ?: 0.0
    val convertedResult = remember(numInput, fromUnit, toUnit) {
        ConverterEngine.convert(numInput, fromUnit, toUnit)
    }
    val formattedResult = remember(convertedResult) {
        ConverterEngine.formatResult(convertedResult)
    }

    val formulaText = remember(fromUnit, toUnit) {
        ConverterEngine.getFormulaDescription(fromUnit, toUnit)
    }

    fun handleKeypad(key: String) {
        when (key) {
            "AC" -> onInputValueChange("0")
            "⌫" -> {
                if (inputValue.length > 1) {
                    onInputValueChange(inputValue.dropLast(1))
                } else {
                    onInputValueChange("0")
                }
            }
            "±" -> {
                if (inputValue.startsWith("-")) {
                    onInputValueChange(inputValue.removePrefix("-"))
                } else if (inputValue != "0") {
                    onInputValueChange("-$inputValue")
                }
            }
            "." -> {
                if (!inputValue.contains(".")) {
                    onInputValueChange("$inputValue.")
                }
            }
            else -> {
                if (inputValue == "0") {
                    onInputValueChange(key)
                } else if (inputValue.length < 15) {
                    onInputValueChange(inputValue + key)
                }
            }
        }
    }

    fun copyToClipboard(text: String) {
        hapticManager?.performCalculationSuccess()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Converted Value", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $text to clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Top category selector bar & favorite toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Dropdown
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(tokens.surfaceGlass)
                        .border(1.dp, tokens.borderLight.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                        .clickable { isCategoryDropdownOpen = !isCategoryDropdownOpen }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = GlassOrangeTop,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentCategory.displayName,
                        color = tokens.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isCategoryDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Select Category",
                        tint = tokens.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                var categorySearchQuery by remember { mutableStateOf("") }

                DropdownMenu(
                    expanded = isCategoryDropdownOpen,
                    onDismissRequest = {
                        isCategoryDropdownOpen = false
                        categorySearchQuery = ""
                    },
                    modifier = Modifier
                        .width(300.dp)
                        .heightIn(max = 440.dp)
                        .background(if (tokens.isDark) Color(0xFF0F172A).copy(alpha = 0.98f) else Color(0xFFF8FAFC).copy(alpha = 0.98f))
                        .border(1.dp, tokens.borderLight.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(vertical = 4.dp)
                ) {
                    // Search Input Field
                    OutlinedTextField(
                        value = categorySearchQuery,
                        onValueChange = { categorySearchQuery = it },
                        placeholder = { Text("Search unit or category...", color = tokens.textMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = tokens.textMuted, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (categorySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { categorySearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = tokens.textMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = tokens.textPrimary,
                            unfocusedTextColor = tokens.textPrimary,
                            focusedBorderColor = GlassOrangeTop,
                            unfocusedBorderColor = tokens.borderLight.copy(alpha = 0.25f),
                            focusedContainerColor = tokens.surfaceGlass,
                            unfocusedContainerColor = tokens.surfaceGlass
                        )
                    )

                    HorizontalDivider(color = tokens.borderLight.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

                    if (categorySearchQuery.isBlank()) {
                        Text(
                            text = "CATEGORIES",
                            color = tokens.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        UnitCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat.displayName,
                                            color = if (cat == currentCategory) GlassOrangeTop else tokens.textPrimary,
                                            fontWeight = if (cat == currentCategory) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }
                                },
                                onClick = {
                                    hapticManager?.performTick()
                                    onCategoryChange(cat)
                                    isCategoryDropdownOpen = false
                                }
                            )
                        }
                    } else {
                        val q = categorySearchQuery.trim().lowercase()
                        val matchingUnits = remember(q) {
                            ConverterEngine.unitsByCategory.flatMap { (cat, units) ->
                                units.filter {
                                    it.name.lowercase().contains(q) ||
                                    it.symbol.lowercase().contains(q) ||
                                    cat.displayName.lowercase().contains(q)
                                }.map { cat to it }
                            }
                        }

                        if (matchingUnits.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No units found", color = tokens.textMuted, fontSize = 13.sp)
                            }
                        } else {
                            matchingUnits.take(25).forEach { (cat, unit) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = unit.name,
                                                    color = tokens.textPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${unit.symbol} • ${cat.displayName}",
                                                    color = tokens.textMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(GlassOrangeTop.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(cat.displayName, color = GlassOrangeTop, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    },
                                    onClick = {
                                        hapticManager?.performTick()
                                        onCategoryChange(cat)
                                        onFromUnitChange(unit)
                                        isCategoryDropdownOpen = false
                                        categorySearchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Favorite star button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tokens.surfaceGlass)
            ) {
                Icon(
                    imageVector = if (isFavoriteCurrent) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Toggle Favorite",
                    tint = if (isFavoriteCurrent) GlassOrangeTop else tokens.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Quick Favorites Row
        if (favorites.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { fav ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.surfaceGlass)
                            .border(1.dp, tokens.borderLight.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .clickable { onSelectFavorite(fav) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${fav.fromUnitName} → ${fav.toUnitName}",
                            color = tokens.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Two Big Glass Cards (FROM & TO) with animated Swap Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FROM CARD
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FROM", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = inputValue,
                            color = tokens.textPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // Unit Picker Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(tokens.surfaceGlass)
                            .border(1.dp, tokens.borderLight.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable { isFromUnitSheetOpen = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${fromUnit.name} (${fromUnit.symbol})",
                            color = GlassOrangeTop,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, null, tint = GlassOrangeTop, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Animated Swap Button
            Box(
                modifier = Modifier
                    .offset(y = (-6).dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tokens.activePillGlass)
                    .border(1.2.dp, tokens.borderLight, CircleShape)
                    .clickable {
                        hapticManager?.performOperatorTap()
                        swapRotation += 180f
                        onSwapUnits()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap Units",
                    tint = tokens.textPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(animatedRotation)
                )
            }

            // TO CARD
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-12).dp),
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TO", color = tokens.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formattedResult,
                            color = GlassOrangeTop,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }

                    // Unit Picker Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(tokens.surfaceGlass)
                            .border(1.dp, tokens.borderLight.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .clickable { isToUnitSheetOpen = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${toUnit.name} (${toUnit.symbol})",
                            color = tokens.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, null, tint = tokens.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Formula & Copy Button bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-6).dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formulaText,
                color = tokens.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = { copyToClipboard(formattedResult) }
            ) {
                Icon(Icons.Default.ContentCopy, null, tint = GlassOrangeTop, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy", color = GlassOrangeTop, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // On-screen glass numeric keypad for converter
        val converterKeypadRows = listOf(
            listOf("7", "8", "9", "⌫"),
            listOf("4", "5", "6", "AC"),
            listOf("1", "2", "3", "±"),
            listOf(".", "0", "00", "")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 80.dp), // margin for bottom nav
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            converterKeypadRows.forEach { rowKeys ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowKeys.forEach { key ->
                        if (key.isNotEmpty()) {
                            LiquidGlassButton(
                                onClick = { handleKeypad(key) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                isOperator = key in listOf("AC", "⌫", "±"),
                                isFunction = key == "."
                            ) {
                                Text(
                                    text = key,
                                    color = tokens.textPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // FROM Unit Picker Bottom Sheet
    if (isFromUnitSheetOpen) {
        UnitSelectionSheet(
            category = currentCategory,
            selectedUnit = fromUnit,
            onSelect = {
                onFromUnitChange(it)
                isFromUnitSheetOpen = false
            },
            onDismiss = { isFromUnitSheetOpen = false }
        )
    }

    // TO Unit Picker Bottom Sheet
    if (isToUnitSheetOpen) {
        UnitSelectionSheet(
            category = currentCategory,
            selectedUnit = toUnit,
            onSelect = {
                onToUnitChange(it)
                isToUnitSheetOpen = false
            },
            onDismiss = { isToUnitSheetOpen = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitSelectionSheet(
    category: UnitCategory,
    selectedUnit: UnitDefinition,
    onSelect: (UnitDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalGlassColors.current
    val unitList = ConverterEngine.unitsByCategory[category] ?: emptyList()

    var sheetSearchQuery by remember { mutableStateOf("") }
    val filteredUnits = remember(unitList, sheetSearchQuery) {
        if (sheetSearchQuery.isBlank()) unitList
        else {
            val q = sheetSearchQuery.trim().lowercase()
            unitList.filter {
                it.name.lowercase().contains(q) || it.symbol.lowercase().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (tokens.isDark) Color(0xFF0F172A).copy(alpha = 0.94f) else Color(0xFFF8FAFC).copy(alpha = 0.96f),
        dragHandle = { BottomSheetDefaults.DragHandle(color = tokens.textMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select ${category.displayName} Unit",
                color = tokens.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search filter field
            OutlinedTextField(
                value = sheetSearchQuery,
                onValueChange = { sheetSearchQuery = it },
                placeholder = { Text("Filter ${category.displayName.lowercase()} units...", color = tokens.textMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = tokens.textMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (sheetSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { sheetSearchQuery = "" }) {
                            Icon(Icons.Default.Close, null, tint = tokens.textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = tokens.textPrimary,
                    unfocusedTextColor = tokens.textPrimary,
                    focusedBorderColor = GlassOrangeTop,
                    unfocusedBorderColor = tokens.borderLight.copy(alpha = 0.25f),
                    focusedContainerColor = tokens.surfaceGlass,
                    unfocusedContainerColor = tokens.surfaceGlass
                )
            )

            if (filteredUnits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching units found", color = tokens.textMuted, fontSize = 14.sp)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredUnits) { unitDef ->
                        val isSelected = unitDef.id == selectedUnit.id
                        val hapticManager = com.example.util.LocalHapticManager.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GlassOrangeTop.copy(alpha = 0.2f) else tokens.surfaceGlass)
                                .clickable {
                                    hapticManager?.performTick()
                                    onSelect(unitDef)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = unitDef.name,
                                color = if (isSelected) GlassOrangeTop else tokens.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = unitDef.symbol,
                                color = tokens.textSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
