package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalcGlassRepository
import com.example.math.ConverterEngine
import com.example.math.MathEvaluator
import com.example.model.*
import com.example.util.HapticFeedbackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalcGlassViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CalcGlassRepository(application)
    val hapticManager = HapticFeedbackManager(application)

    // Current Main Section: Calculator or Converter
    private val _currentSection = MutableStateFlow(MainSection.CALCULATOR)
    val currentSection: StateFlow<MainSection> = _currentSection.asStateFlow()

    // Calculator Type
    private val _calculatorType = MutableStateFlow(repository.getLastCalculatorType())
    val calculatorType: StateFlow<CalculatorType> = _calculatorType.asStateFlow()

    // Dark/Light Theme
    private val _isDarkMode = MutableStateFlow(repository.isDarkMode(defaultVal = true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Calculator State
    private val _displayExpression = MutableStateFlow("")
    val displayExpression: StateFlow<String> = _displayExpression.asStateFlow()

    private val _displayResult = MutableStateFlow("0")
    val displayResult: StateFlow<String> = _displayResult.asStateFlow()

    private val _angleUnit = MutableStateFlow(AngleUnit.DEG)
    val angleUnit: StateFlow<AngleUnit> = _angleUnit.asStateFlow()

    private val _historyList = MutableStateFlow(repository.getHistory())
    val historyList: StateFlow<List<HistoryItem>> = _historyList.asStateFlow()

    private val _isHistoryOpen = MutableStateFlow(false)
    val isHistoryOpen: StateFlow<Boolean> = _isHistoryOpen.asStateFlow()

    private var lastAns = 0.0
    private var isResultEvaluated = false

    // Converter State
    private val initialCat = repository.getLastConverterCategory()
    private val _converterCategory = MutableStateFlow(initialCat)
    val converterCategory: StateFlow<UnitCategory> = _converterCategory.asStateFlow()

    private val initialUnits = ConverterEngine.unitsByCategory[initialCat] ?: emptyList()
    private val _fromUnit = MutableStateFlow(initialUnits.getOrElse(0) { UnitDefinition("m", "Meter", "m", initialCat) })
    val fromUnit: StateFlow<UnitDefinition> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(initialUnits.getOrElse(1) { initialUnits.firstOrNull() ?: UnitDefinition("km", "Kilometer", "km", initialCat) })
    val toUnit: StateFlow<UnitDefinition> = _toUnit.asStateFlow()

    private val _converterInput = MutableStateFlow("1")
    val converterInput: StateFlow<String> = _converterInput.asStateFlow()

    private val _favoriteConversions = MutableStateFlow(repository.getFavoriteConversions())
    val favoriteConversions: StateFlow<List<FavoriteConversion>> = _favoriteConversions.asStateFlow()

    fun setSection(section: MainSection) {
        _currentSection.value = section
    }

    fun setCalculatorType(type: CalculatorType) {
        _calculatorType.value = type
        repository.setLastCalculatorType(type)
    }

    fun toggleDarkMode() {
        val newDark = !_isDarkMode.value
        _isDarkMode.value = newDark
        repository.setDarkMode(newDark)
    }

    fun toggleAngleUnit() {
        _angleUnit.value = if (_angleUnit.value == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
    }

    fun setHistoryOpen(open: Boolean) {
        _isHistoryOpen.value = open
    }

    // --- Calculator Engine Actions ---

    fun onDigit(digit: String) {
        if (isResultEvaluated) {
            _displayExpression.value = ""
            _displayResult.value = if (digit == "00") "0" else digit
            isResultEvaluated = false
            return
        }

        val current = _displayResult.value
        if (current == "0") {
            _displayResult.value = if (digit == "00") "0" else digit
        } else if (digit == "." && current.endsWith(".")) {
            // ignore duplicate point
        } else if (current.length < 30) {
            _displayResult.value = current + digit
        }
    }

    fun onOperator(op: String) {
        isResultEvaluated = false
        val current = _displayResult.value

        if (op == "%") {
            // Instant percent evaluation
            val expr = if (_displayExpression.value.isEmpty()) current else "${_displayExpression.value} $current"
            when (val res = MathEvaluator.evaluate("$expr %", _angleUnit.value, lastAns)) {
                is MathEvaluator.EvalResult.Success -> {
                    _displayResult.value = res.formatted
                }
                is MathEvaluator.EvalResult.Error -> {}
            }
            return
        }

        if (_displayExpression.value.isEmpty()) {
            _displayExpression.value = "$current $op"
            _displayResult.value = "0"
        } else {
            // Append or chain evaluation
            val fullExpr = "${_displayExpression.value} $current"
            when (val res = MathEvaluator.evaluate(fullExpr, _angleUnit.value, lastAns)) {
                is MathEvaluator.EvalResult.Success -> {
                    _displayExpression.value = "${res.formatted} $op"
                    _displayResult.value = "0"
                }
                is MathEvaluator.EvalResult.Error -> {
                    _displayExpression.value = "$fullExpr $op"
                    _displayResult.value = "0"
                }
            }
        }
    }

    fun onFunction(fn: String) {
        isResultEvaluated = false
        when (fn) {
            "π" -> {
                if (_displayResult.value == "0") _displayResult.value = "π"
                else _displayResult.value += "π"
            }
            "e" -> {
                if (_displayResult.value == "0") _displayResult.value = "e"
                else _displayResult.value += "e"
            }
            "Ans" -> {
                if (_displayResult.value == "0") _displayResult.value = "Ans"
                else _displayResult.value += "Ans"
            }
            "x²" -> {
                _displayResult.value += "²"
            }
            "xʸ" -> {
                _displayResult.value += "^"
            }
            "√", "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln" -> {
                if (_displayResult.value == "0") {
                    _displayResult.value = "$fn("
                } else {
                    _displayResult.value += "$fn("
                }
            }
            "(", ")" -> {
                if (_displayResult.value == "0" && fn == "(") {
                    _displayResult.value = "("
                } else {
                    _displayResult.value += fn
                }
            }
            "!" -> {
                _displayResult.value += "!"
            }
        }
    }

    fun onEquals() {
        val current = _displayResult.value
        val expr = if (_displayExpression.value.isEmpty()) {
            current
        } else {
            "${_displayExpression.value} $current"
        }

        when (val res = MathEvaluator.evaluate(expr, _angleUnit.value, lastAns)) {
            is MathEvaluator.EvalResult.Success -> {
                hapticManager.performCalculationSuccess()
                lastAns = res.value
                val item = HistoryItem(
                    expression = expr,
                    result = res.formatted,
                    timestamp = System.currentTimeMillis()
                )
                val updated = listOf(item) + _historyList.value
                _historyList.value = updated
                repository.saveHistory(updated)

                _displayExpression.value = "$expr ="
                _displayResult.value = res.formatted
                isResultEvaluated = true
            }
            is MathEvaluator.EvalResult.Error -> {
                hapticManager.performErrorAlert()
                _displayResult.value = res.message
                isResultEvaluated = true
            }
        }
    }

    fun onClear() {
        hapticManager.performTick()
        _displayExpression.value = ""
        _displayResult.value = "0"
        isResultEvaluated = false
    }

    fun onBackspace() {
        hapticManager.performKeyTap()
        if (isResultEvaluated) {
            onClear()
            return
        }
        val current = _displayResult.value
        if (current.length > 1) {
            _displayResult.value = current.dropLast(1)
        } else {
            _displayResult.value = "0"
        }
    }

    fun onSelectHistoryItem(item: HistoryItem) {
        _displayExpression.value = item.expression
        _displayResult.value = item.result
        isResultEvaluated = true
    }

    fun onClearHistory() {
        _historyList.value = emptyList()
        repository.saveHistory(emptyList())
    }

    // --- Converter Actions ---

    fun onCategoryChange(cat: UnitCategory) {
        _converterCategory.value = cat
        repository.setLastConverterCategory(cat)
        val units = ConverterEngine.unitsByCategory[cat] ?: emptyList()
        _fromUnit.value = units.getOrElse(0) { UnitDefinition("m", "Meter", "m", cat) }
        _toUnit.value = units.getOrElse(1) { units.firstOrNull() ?: UnitDefinition("km", "Kilometer", "km", cat) }
    }

    fun onFromUnitChange(unit: UnitDefinition) {
        _fromUnit.value = unit
    }

    fun onToUnitChange(unit: UnitDefinition) {
        _toUnit.value = unit
    }

    fun onSwapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
    }

    fun onConverterInputChange(value: String) {
        _converterInput.value = value
    }

    fun onSelectFavorite(fav: FavoriteConversion) {
        val units = ConverterEngine.unitsByCategory[fav.category] ?: return
        val f = units.firstOrNull { it.id == fav.fromUnitId } ?: return
        val t = units.firstOrNull { it.id == fav.toUnitId } ?: return
        _converterCategory.value = fav.category
        repository.setLastConverterCategory(fav.category)
        _fromUnit.value = f
        _toUnit.value = t
    }

    fun onToggleFavorite() {
        val cat = _converterCategory.value
        val from = _fromUnit.value
        val to = _toUnit.value

        val existing = _favoriteConversions.value.firstOrNull {
            it.category == cat && it.fromUnitId == from.id && it.toUnitId == to.id
        }

        val updated = if (existing != null) {
            _favoriteConversions.value.filter { it.id != existing.id }
        } else {
            val newFav = FavoriteConversion(
                category = cat,
                fromUnitId = from.id,
                toUnitId = to.id,
                fromUnitName = from.name,
                toUnitName = to.name
            )
            listOf(newFav) + _favoriteConversions.value
        }
        _favoriteConversions.value = updated
        repository.saveFavoriteConversions(updated)
    }

    fun isFavoriteCurrent(): Boolean {
        val cat = _converterCategory.value
        val from = _fromUnit.value
        val to = _toUnit.value
        return _favoriteConversions.value.any {
            it.category == cat && it.fromUnitId == from.id && it.toUnitId == to.id
        }
    }
}
