package com.example.model

enum class MainSection(val label: String) {
    CALCULATOR("Calculator"),
    CONVERTER("Converter")
}

enum class CalculatorType(val displayName: String, val iconName: String) {
    STANDARD("Standard", "calculate"),
    SCIENTIFIC("Scientific", "science"),
    GRAPHER("Grapher", "show_chart"),
    PROGRAMMER("Programmer", "terminal"),
    DATE_CALC("Date calculation", "event"),
    DISCOUNT("Percentage / Discount", "percent"),
    TIP_SPLIT("Tip & Bill split", "receipt_long")
}

enum class AngleUnit {
    DEG, RAD
}

enum class WordSize(val label: String, val bits: Int, val mask: Long) {
    QWORD("64-bit", 64, -1L),
    DWORD("32-bit", 32, 0xFFFFFFFFL),
    WORD("16-bit", 16, 0xFFFFL),
    BYTE("8-bit", 8, 0xFFL)
}

data class HistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
