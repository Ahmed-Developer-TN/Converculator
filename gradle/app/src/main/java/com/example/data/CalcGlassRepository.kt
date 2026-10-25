package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CalculatorType
import com.example.model.FavoriteConversion
import com.example.model.HistoryItem
import com.example.model.UnitCategory
import org.json.JSONArray
import org.json.JSONObject

class CalcGlassRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("calc_glass_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_DARK = "theme_dark"
        private const val KEY_HISTORY = "history_items"
        private const val KEY_FAVORITES = "favorite_conversions"
        private const val KEY_LAST_CALC_TYPE = "last_calc_type"
        private const val KEY_LAST_CONVERTER_CAT = "last_converter_cat"
    }

    fun isDarkMode(defaultVal: Boolean): Boolean {
        return prefs.getBoolean(KEY_THEME_DARK, defaultVal)
    }

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_THEME_DARK, isDark).apply()
    }

    fun getLastCalculatorType(): CalculatorType {
        val name = prefs.getString(KEY_LAST_CALC_TYPE, CalculatorType.STANDARD.name)
        return try {
            CalculatorType.valueOf(name ?: CalculatorType.STANDARD.name)
        } catch (e: Exception) {
            CalculatorType.STANDARD
        }
    }

    fun setLastCalculatorType(type: CalculatorType) {
        prefs.edit().putString(KEY_LAST_CALC_TYPE, type.name).apply()
    }

    fun getLastConverterCategory(): UnitCategory {
        val name = prefs.getString(KEY_LAST_CONVERTER_CAT, UnitCategory.LENGTH.name)
        return try {
            UnitCategory.valueOf(name ?: UnitCategory.LENGTH.name)
        } catch (e: Exception) {
            UnitCategory.LENGTH
        }
    }

    fun setLastConverterCategory(category: UnitCategory) {
        prefs.edit().putString(KEY_LAST_CONVERTER_CAT, category.name).apply()
    }

    fun getHistory(): List<HistoryItem> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<HistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        expression = obj.getString("expression"),
                        result = obj.getString("result"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveHistory(items: List<HistoryItem>) {
        val array = JSONArray()
        // Limit to latest 50 items
        items.take(50).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("expression", item.expression)
                put("result", item.result)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun getFavoriteConversions(): List<FavoriteConversion> {
        val jsonStr = prefs.getString(KEY_FAVORITES, null) ?: return defaultFavorites()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<FavoriteConversion>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FavoriteConversion(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        category = UnitCategory.valueOf(obj.getString("category")),
                        fromUnitId = obj.getString("fromUnitId"),
                        toUnitId = obj.getString("toUnitId"),
                        fromUnitName = obj.getString("fromUnitName"),
                        toUnitName = obj.getString("toUnitName")
                    )
                )
            }
            if (list.isEmpty()) defaultFavorites() else list
        } catch (e: Exception) {
            defaultFavorites()
        }
    }

    fun saveFavoriteConversions(favorites: List<FavoriteConversion>) {
        val array = JSONArray()
        favorites.forEach { fav ->
            val obj = JSONObject().apply {
                put("id", fav.id)
                put("category", fav.category.name)
                put("fromUnitId", fav.fromUnitId)
                put("toUnitId", fav.toUnitId)
                put("fromUnitName", fav.fromUnitName)
                put("toUnitName", fav.toUnitName)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_FAVORITES, array.toString()).apply()
    }

    private fun defaultFavorites(): List<FavoriteConversion> {
        return listOf(
            FavoriteConversion(category = UnitCategory.LENGTH, fromUnitId = "km", toUnitId = "mi", fromUnitName = "Kilometer", toUnitName = "Mile"),
            FavoriteConversion(category = UnitCategory.TEMPERATURE, fromUnitId = "c", toUnitId = "f", fromUnitName = "Celsius", toUnitName = "Fahrenheit"),
            FavoriteConversion(category = UnitCategory.MASS, fromUnitId = "kg", toUnitId = "lb", fromUnitName = "Kilogram", toUnitName = "Pound"),
            FavoriteConversion(category = UnitCategory.AREA, fromUnitId = "sq_m", toUnitId = "sq_ft", fromUnitName = "Square meter", toUnitName = "Square foot")
        )
    }
}
