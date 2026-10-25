package com.example.math

import com.example.model.UnitCategory
import com.example.model.UnitDefinition
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object ConverterEngine {

    private val dfSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    // Units map by Category
    val unitsByCategory: Map<UnitCategory, List<UnitDefinition>> = mapOf(
        UnitCategory.AREA to listOf(
            UnitDefinition("sq_m", "Square meter", "m²", UnitCategory.AREA, 1.0),
            UnitDefinition("sq_km", "Square kilometer", "km²", UnitCategory.AREA, 1_000_000.0),
            UnitDefinition("sq_cm", "Square centimeter", "cm²", UnitCategory.AREA, 0.0001),
            UnitDefinition("sq_mm", "Square millimeter", "mm²", UnitCategory.AREA, 0.000001),
            UnitDefinition("hectare", "Hectare", "ha", UnitCategory.AREA, 10_000.0),
            UnitDefinition("acre", "Acre", "ac", UnitCategory.AREA, 4046.8564224),
            UnitDefinition("sq_mi", "Square mile", "mi²", UnitCategory.AREA, 2_589_988.110336),
            UnitDefinition("sq_yd", "Square yard", "yd²", UnitCategory.AREA, 0.83612736),
            UnitDefinition("sq_ft", "Square foot", "ft²", UnitCategory.AREA, 0.09290304),
            UnitDefinition("sq_in", "Square inch", "in²", UnitCategory.AREA, 0.00064516)
        ),
        UnitCategory.ENERGY to listOf(
            UnitDefinition("j", "Joule", "J", UnitCategory.ENERGY, 1.0),
            UnitDefinition("kj", "Kilojoule", "kJ", UnitCategory.ENERGY, 1000.0),
            UnitDefinition("cal", "Calorie", "cal", UnitCategory.ENERGY, 4.184),
            UnitDefinition("kcal", "Kilocalorie", "kcal", UnitCategory.ENERGY, 4184.0),
            UnitDefinition("wh", "Watt-hour", "Wh", UnitCategory.ENERGY, 3600.0),
            UnitDefinition("kwh", "Kilowatt-hour", "kWh", UnitCategory.ENERGY, 3_600_000.0),
            UnitDefinition("ev", "Electronvolt", "eV", UnitCategory.ENERGY, 1.602176634e-19),
            UnitDefinition("btu", "BTU", "BTU", UnitCategory.ENERGY, 1055.05585),
            UnitDefinition("ft_lb", "Foot-pound", "ft⋅lbf", UnitCategory.ENERGY, 1.355818)
        ),
        UnitCategory.LENGTH to listOf(
            UnitDefinition("m", "Meter", "m", UnitCategory.LENGTH, 1.0),
            UnitDefinition("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0),
            UnitDefinition("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01),
            UnitDefinition("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001),
            UnitDefinition("um", "Micrometer", "µm", UnitCategory.LENGTH, 1e-6),
            UnitDefinition("nm", "Nanometer", "nm", UnitCategory.LENGTH, 1e-9),
            UnitDefinition("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344),
            UnitDefinition("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144),
            UnitDefinition("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048),
            UnitDefinition("in", "Inch", "in", UnitCategory.LENGTH, 0.0254),
            UnitDefinition("nmi", "Nautical mile", "NM", UnitCategory.LENGTH, 1852.0)
        ),
        UnitCategory.MASS to listOf(
            UnitDefinition("kg", "Kilogram", "kg", UnitCategory.MASS, 1.0),
            UnitDefinition("g", "Gram", "g", UnitCategory.MASS, 0.001),
            UnitDefinition("mg", "Milligram", "mg", UnitCategory.MASS, 0.000001),
            UnitDefinition("t", "Metric ton", "t", UnitCategory.MASS, 1000.0),
            UnitDefinition("lb", "Pound", "lb", UnitCategory.MASS, 0.45359237),
            UnitDefinition("oz", "Ounce", "oz", UnitCategory.MASS, 0.028349523125),
            UnitDefinition("st", "Stone", "st", UnitCategory.MASS, 6.35029318),
            UnitDefinition("ct", "Carat", "ct", UnitCategory.MASS, 0.0002)
        ),
        UnitCategory.POWER to listOf(
            UnitDefinition("w", "Watt", "W", UnitCategory.POWER, 1.0),
            UnitDefinition("kw", "Kilowatt", "kW", UnitCategory.POWER, 1000.0),
            UnitDefinition("mw", "Megawatt", "MW", UnitCategory.POWER, 1_000_000.0),
            UnitDefinition("hp_mech", "Horsepower (mechanical)", "hp", UnitCategory.POWER, 745.69987),
            UnitDefinition("hp_metric", "Horsepower (metric)", "PS", UnitCategory.POWER, 735.49875),
            UnitDefinition("btu_h", "BTU/hour", "BTU/h", UnitCategory.POWER, 0.293071),
            UnitDefinition("ft_lb_min", "Foot-pound/minute", "ft⋅lb/min", UnitCategory.POWER, 0.022597)
        ),
        UnitCategory.PRESSURE to listOf(
            UnitDefinition("pa", "Pascal", "Pa", UnitCategory.PRESSURE, 1.0),
            UnitDefinition("kpa", "Kilopascal", "kPa", UnitCategory.PRESSURE, 1000.0),
            UnitDefinition("bar", "Bar", "bar", UnitCategory.PRESSURE, 100_000.0),
            UnitDefinition("mbar", "Millibar", "mbar", UnitCategory.PRESSURE, 100.0),
            UnitDefinition("atm", "Atmosphere", "atm", UnitCategory.PRESSURE, 101_325.0),
            UnitDefinition("psi", "PSI", "psi", UnitCategory.PRESSURE, 6894.757),
            UnitDefinition("mmhg", "mmHg", "mmHg", UnitCategory.PRESSURE, 133.322),
            UnitDefinition("torr", "Torr", "Torr", UnitCategory.PRESSURE, 133.322368)
        ),
        UnitCategory.TEMPERATURE to listOf(
            UnitDefinition("c", "Celsius", "°C", UnitCategory.TEMPERATURE),
            UnitDefinition("f", "Fahrenheit", "°F", UnitCategory.TEMPERATURE),
            UnitDefinition("k", "Kelvin", "K", UnitCategory.TEMPERATURE),
            UnitDefinition("r", "Rankine", "°R", UnitCategory.TEMPERATURE)
        ),
        UnitCategory.VELOCITY to listOf(
            UnitDefinition("mps", "Meter per second", "m/s", UnitCategory.VELOCITY, 1.0),
            UnitDefinition("kmh", "Kilometer per hour", "km/h", UnitCategory.VELOCITY, 1.0 / 3.6),
            UnitDefinition("mph", "Mile per hour", "mph", UnitCategory.VELOCITY, 0.44704),
            UnitDefinition("knot", "Knot", "kn", UnitCategory.VELOCITY, 0.514444),
            UnitDefinition("fps", "Foot per second", "ft/s", UnitCategory.VELOCITY, 0.3048),
            UnitDefinition("mach", "Mach", "Ma", UnitCategory.VELOCITY, 340.29) // at sea level, 15°C
        ),
        UnitCategory.VOLUME to listOf(
            UnitDefinition("l", "Liter", "L", UnitCategory.VOLUME, 0.001),
            UnitDefinition("ml", "Milliliter", "mL", UnitCategory.VOLUME, 0.000001),
            UnitDefinition("cu_m", "Cubic meter", "m³", UnitCategory.VOLUME, 1.0),
            UnitDefinition("cu_cm", "Cubic centimeter", "cm³", UnitCategory.VOLUME, 0.000001),
            UnitDefinition("gal_us", "Gallon (US)", "gal US", UnitCategory.VOLUME, 0.003785411784),
            UnitDefinition("gal_uk", "Gallon (UK)", "gal UK", UnitCategory.VOLUME, 0.00454609),
            UnitDefinition("qt", "Quart", "qt", UnitCategory.VOLUME, 0.000946352946),
            UnitDefinition("pt", "Pint", "pt", UnitCategory.VOLUME, 0.000473176473),
            UnitDefinition("cup", "Cup", "cup", UnitCategory.VOLUME, 0.0002365882365),
            UnitDefinition("fl_oz", "Fluid ounce", "fl oz", UnitCategory.VOLUME, 0.0000295735295625),
            UnitDefinition("tbsp", "Tablespoon", "tbsp", UnitCategory.VOLUME, 0.0000147867647813),
            UnitDefinition("tsp", "Teaspoon", "tsp", UnitCategory.VOLUME, 0.00000492892159375),
            UnitDefinition("cu_ft", "Cubic foot", "ft³", UnitCategory.VOLUME, 0.028316846592),
            UnitDefinition("cu_in", "Cubic inch", "in³", UnitCategory.VOLUME, 0.000016387064)
        )
    )

    fun convert(value: Double, from: UnitDefinition, to: UnitDefinition): Double {
        if (from.id == to.id) return value

        if (from.category == UnitCategory.TEMPERATURE) {
            return convertTemperature(value, from.id, to.id)
        }

        // Base unit conversion
        val baseValue = value * from.toBaseFactor
        return baseValue / to.toBaseFactor
    }

    private fun convertTemperature(value: Double, fromId: String, toId: String): Double {
        // Convert to Celsius first
        val celsius = when (fromId) {
            "c" -> value
            "f" -> (value - 32.0) * 5.0 / 9.0
            "k" -> value - 273.15
            "r" -> (value - 491.67) * 5.0 / 9.0
            else -> value
        }

        // Convert Celsius to Target
        return when (toId) {
            "c" -> celsius
            "f" -> (celsius * 9.0 / 5.0) + 32.0
            "k" -> celsius + 273.15
            "r" -> (celsius + 273.15) * 9.0 / 5.0
            else -> celsius
        }
    }

    fun getFormulaDescription(from: UnitDefinition, to: UnitDefinition): String {
        if (from.id == to.id) return "1 ${from.symbol} = 1 ${to.symbol}"

        if (from.category == UnitCategory.TEMPERATURE) {
            return when {
                from.id == "c" && to.id == "f" -> "(°C × 9/5) + 32 = °F"
                from.id == "f" && to.id == "c" -> "(°F − 32) × 5/9 = °C"
                from.id == "c" && to.id == "k" -> "°C + 273.15 = K"
                from.id == "k" && to.id == "c" -> "K − 273.15 = °C"
                else -> {
                    val oneConverted = convert(1.0, from, to)
                    "1 ${from.symbol} ≈ ${formatResult(oneConverted)} ${to.symbol}"
                }
            }
        }

        val factor = convert(1.0, from, to)
        return "1 ${from.symbol} = ${formatResult(factor)} ${to.symbol}"
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "0"
        if (value.isInfinite()) return "∞"

        val absVal = abs(value)
        if (absVal != 0.0 && (absVal >= 1e9 || absVal < 1e-4)) {
            val df = DecimalFormat("0.#####E0", dfSymbols)
            return df.format(value)
        }

        val df = DecimalFormat("#,##0.######", dfSymbols)
        return df.format(value)
    }
}
