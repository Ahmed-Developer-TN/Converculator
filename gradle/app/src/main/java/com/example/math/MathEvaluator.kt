package com.example.math

import com.example.model.AngleUnit
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

object MathEvaluator {

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    private val dfSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        val absVal = abs(value)
        if (absVal != 0.0 && (absVal >= 1e12 || absVal < 1e-6)) {
            val dfScientific = DecimalFormat("0.######E0", dfSymbols)
            return dfScientific.format(value)
        }

        val df = DecimalFormat("#,##0.########", dfSymbols)
        val formatted = df.format(value)
        return formatted
    }

    fun evaluate(rawExpr: String, angleUnit: AngleUnit, lastAnswer: Double = 0.0): EvalResult {
        if (rawExpr.isBlank()) return EvalResult.Success(0.0, "0")

        return try {
            val normalized = preprocess(rawExpr, lastAnswer)
            val tokens = tokenize(normalized)
            val parser = Parser(tokens, angleUnit)
            val result = parser.parseExpression()

            if (result.isNaN()) {
                EvalResult.Error("Undefined")
            } else if (result.isInfinite()) {
                EvalResult.Error("Cannot divide by 0")
            } else {
                EvalResult.Success(result, formatNumber(result))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "Math error")
        } catch (e: Exception) {
            EvalResult.Error("Invalid format")
        }
    }

    private fun preprocess(expr: String, lastAnswer: Double): String {
        var s = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", Math.PI.toString())
            .replace("Ans", lastAnswer.toString())
            .replace("e", Math.E.toString())

        // Replace ² with ^2
        s = s.replace("²", "^2")

        // Handle implicit multiplication like 2(3) -> 2*(3) or (2)(3) -> (2)*(3)
        s = s.replace(Regex("(\\d|\\))\\s*\\("), "$1*(")
        s = s.replace(Regex("(\\))\\s*(\\d)"), "$1*$2")
        s = s.replace(Regex("(\\d)\\s*(sin|cos|tan|asin|acos|atan|log|ln|√)"), "$1*$2")

        return s
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val n = expr.length

        while (i < n) {
            val c = expr[i]
            if (c.isWhitespace()) {
                i++
                continue
            }

            if (c in "+-*/^%()!√") {
                tokens.add(c.toString())
                i++
            } else if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < n && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || expr[i] == 'e')) {
                    // Check if 'e' is exponent or constant
                    if ((expr[i] == 'E' || expr[i] == 'e') && i + 1 < n && (expr[i + 1] == '+' || expr[i + 1] == '-' || expr[i + 1].isDigit())) {
                        sb.append(expr[i])
                        i++
                        if (i < n && (expr[i] == '+' || expr[i] == '-')) {
                            sb.append(expr[i])
                            i++
                        }
                    } else if (expr[i] == 'E' || expr[i] == 'e') {
                        break
                    } else {
                        sb.append(expr[i])
                        i++
                    }
                }
                tokens.add(sb.toString())
            } else if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < n && expr[i].isLetter()) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
            } else {
                i++
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<String>, private val angleUnit: AngleUnit) {
        private var pos = 0

        private fun peek(): String? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): String = tokens[pos++]

        fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < tokens.size) {
                val op = peek() ?: break
                if (op == "+" || op == "-") {
                    consume()
                    val right = parseTerm()
                    value = if (op == "+") value + right else value - right
                } else {
                    break
                }
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (pos < tokens.size) {
                val op = peek() ?: break
                if (op == "*" || op == "/" || op == "%") {
                    consume()
                    val right = parseFactor()
                    value = when (op) {
                        "*" -> value * right
                        "/" -> {
                            if (right == 0.0) throw ArithmeticException("Cannot divide by 0")
                            value / right
                        }
                        "%" -> value * (right / 100.0)
                        else -> value
                    }
                } else {
                    break
                }
            }
            return value
        }

        private fun parseFactor(): Double {
            val left = parseUnary()
            if (pos < tokens.size && peek() == "^") {
                consume()
                val right = parseFactor() // right-associative
                return left.pow(right)
            }
            return left
        }

        private fun parseUnary(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")
            if (token == "+") {
                consume()
                return parseUnary()
            }
            if (token == "-") {
                consume()
                return -parseUnary()
            }
            if (token == "√") {
                consume()
                val arg = parseFactor()
                if (arg < 0) throw ArithmeticException("Domain error")
                return sqrt(arg)
            }
            var value = parsePostfix()
            // Check for factorial !
            while (pos < tokens.size && peek() == "!") {
                consume()
                value = factorial(value)
            }
            return value
        }

        private fun parsePostfix(): Double {
            val token = peek() ?: throw IllegalArgumentException("Unexpected end of expression")

            when (token) {
                "sin" -> {
                    consume()
                    val arg = parseArgument()
                    val rad = if (angleUnit == AngleUnit.DEG) Math.toRadians(arg) else arg
                    return sin(rad)
                }
                "cos" -> {
                    consume()
                    val arg = parseArgument()
                    val rad = if (angleUnit == AngleUnit.DEG) Math.toRadians(arg) else arg
                    return cos(rad)
                }
                "tan" -> {
                    consume()
                    val arg = parseArgument()
                    val rad = if (angleUnit == AngleUnit.DEG) Math.toRadians(arg) else arg
                    val c = cos(rad)
                    if (abs(c) < 1e-15) throw ArithmeticException("Domain error")
                    return tan(rad)
                }
                "asin" -> {
                    consume()
                    val arg = parseArgument()
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                    val rad = asin(arg)
                    return if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
                }
                "acos" -> {
                    consume()
                    val arg = parseArgument()
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                    val rad = acos(arg)
                    return if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
                }
                "atan" -> {
                    consume()
                    val arg = parseArgument()
                    val rad = atan(arg)
                    return if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
                }
                "log" -> {
                    consume()
                    val arg = parseArgument()
                    if (arg <= 0) throw ArithmeticException("Domain error")
                    return log10(arg)
                }
                "ln" -> {
                    consume()
                    val arg = parseArgument()
                    if (arg <= 0) throw ArithmeticException("Domain error")
                    return ln(arg)
                }
                "(" -> {
                    consume()
                    val inner = parseExpression()
                    if (peek() == ")") {
                        consume()
                    }
                    return inner
                }
                else -> {
                    val numStr = consume()
                    return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                }
            }
        }

        private fun parseArgument(): Double {
            if (peek() == "(") {
                consume()
                val inner = parseExpression()
                if (peek() == ")") consume()
                return inner
            }
            return parseFactor()
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || floor(n) != n || n > 170) throw ArithmeticException("Invalid factorial")
            var res = 1.0
            for (i in 2..n.toInt()) {
                res *= i
            }
            return res
        }
    }
}
