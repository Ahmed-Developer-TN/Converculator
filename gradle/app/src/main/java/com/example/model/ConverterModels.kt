package com.example.model

enum class UnitCategory(val displayName: String, val icon: String) {
    AREA("Area", "crop_square"),
    ENERGY("Energy", "bolt"),
    LENGTH("Length", "straighten"),
    MASS("Mass", "scale"),
    POWER("Power", "electric_bolt"),
    PRESSURE("Pressure", "speed"),
    TEMPERATURE("Temperature", "thermostat"),
    VELOCITY("Velocity", "navigation"),
    VOLUME("Volume", "water_drop")
}

data class UnitDefinition(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    // Factor relative to base unit for linear categories
    val toBaseFactor: Double = 1.0
)

data class FavoriteConversion(
    val id: String = java.util.UUID.randomUUID().toString(),
    val category: UnitCategory,
    val fromUnitId: String,
    val toUnitId: String,
    val fromUnitName: String,
    val toUnitName: String
)
