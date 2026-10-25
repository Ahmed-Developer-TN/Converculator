package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MainSection
import com.example.ui.calculator.CalculatorScreen
import com.example.ui.calculator.HistorySheet
import com.example.ui.components.DynamicLiquidBackground
import com.example.ui.components.FloatingLiquidNavBar
import com.example.ui.converter.ConverterScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CalcGlassViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CalcGlassViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
            val calculatorType by viewModel.calculatorType.collectAsStateWithLifecycle()
            val displayExpression by viewModel.displayExpression.collectAsStateWithLifecycle()
            val displayResult by viewModel.displayResult.collectAsStateWithLifecycle()
            val angleUnit by viewModel.angleUnit.collectAsStateWithLifecycle()
            val historyList by viewModel.historyList.collectAsStateWithLifecycle()
            val isHistoryOpen by viewModel.isHistoryOpen.collectAsStateWithLifecycle()

            // Converter states
            val converterCategory by viewModel.converterCategory.collectAsStateWithLifecycle()
            val fromUnit by viewModel.fromUnit.collectAsStateWithLifecycle()
            val toUnit by viewModel.toUnit.collectAsStateWithLifecycle()
            val converterInput by viewModel.converterInput.collectAsStateWithLifecycle()
            val favoriteConversions by viewModel.favoriteConversions.collectAsStateWithLifecycle()

            CompositionLocalProvider(com.example.util.LocalHapticManager provides viewModel.hapticManager) {
                MyApplicationTheme(darkTheme = isDarkMode) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                    // Ambient living liquid background with slow-drifting gradient blobs
                    DynamicLiquidBackground(isDark = isDarkMode)

                    // Main App Content (Constrained width for tablets/foldables, fluid for mobile)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 540.dp)
                    ) {
                        // Smooth directional slide, fade, and scale with spring easing between Calculator and Converter
                        AnimatedContent(
                            targetState = currentSection,
                            transitionSpec = {
                                if (targetState == MainSection.CONVERTER) {
                                    // Moving forward to Converter (slides in from right to left)
                                    (slideInHorizontally(
                                        initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 380f)
                                    ) + fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300)))
                                        .togetherWith(
                                            slideOutHorizontally(
                                                targetOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() },
                                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 380f)
                                            ) + fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(220))
                                        )
                                } else {
                                    // Moving back to Calculator (slides in from left to right)
                                    (slideInHorizontally(
                                        initialOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() },
                                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 380f)
                                    ) + fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300)))
                                        .togetherWith(
                                            slideOutHorizontally(
                                                targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() },
                                                animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.82f, stiffness = 380f)
                                            ) + fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(220))
                                        )
                                }
                            },
                            label = "sectionTransition",
                            modifier = Modifier.fillMaxSize()
                        ) { section ->
                            when (section) {
                                MainSection.CALCULATOR -> {
                                    CalculatorScreen(
                                        currentType = calculatorType,
                                        onTypeChange = { viewModel.setCalculatorType(it) },
                                        displayExpression = displayExpression,
                                        displayResult = displayResult,
                                        angleUnit = angleUnit,
                                        onAngleUnitToggle = { viewModel.toggleAngleUnit() },
                                        onDigit = { viewModel.onDigit(it) },
                                        onOperator = { viewModel.onOperator(it) },
                                        onFunction = { viewModel.onFunction(it) },
                                        onEquals = { viewModel.onEquals() },
                                        onClear = { viewModel.onClear() },
                                        onBackspace = { viewModel.onBackspace() },
                                        onOpenHistory = { viewModel.setHistoryOpen(true) },
                                        isDark = isDarkMode,
                                        onToggleDark = { viewModel.toggleDarkMode() }
                                    )
                                }
                                MainSection.CONVERTER -> {
                                    ConverterScreen(
                                        currentCategory = converterCategory,
                                        onCategoryChange = { viewModel.onCategoryChange(it) },
                                        fromUnit = fromUnit,
                                        toUnit = toUnit,
                                        onFromUnitChange = { viewModel.onFromUnitChange(it) },
                                        onToUnitChange = { viewModel.onToUnitChange(it) },
                                        onSwapUnits = { viewModel.onSwapUnits() },
                                        inputValue = converterInput,
                                        onInputValueChange = { viewModel.onConverterInputChange(it) },
                                        favorites = favoriteConversions,
                                        onSelectFavorite = { viewModel.onSelectFavorite(it) },
                                        onToggleFavorite = { viewModel.onToggleFavorite() },
                                        isFavoriteCurrent = viewModel.isFavoriteCurrent()
                                    )
                                }
                            }
                        }

                        // Floating Liquid Glass Navigation Menu (fixed at bottom center)
                        FloatingLiquidNavBar(
                            currentSection = currentSection,
                            onSectionSelected = { viewModel.setSection(it) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 14.dp)
                        )
                    }

                    // Calculation History Slide-up Sheet
                    if (isHistoryOpen) {
                        HistorySheet(
                            historyItems = historyList,
                            onSelectEntry = { viewModel.onSelectHistoryItem(it) },
                            onClearHistory = { viewModel.onClearHistory() },
                            onDismiss = { viewModel.setHistoryOpen(false) }
                        )
                    }
                }
            }
        }
    }
}
}
