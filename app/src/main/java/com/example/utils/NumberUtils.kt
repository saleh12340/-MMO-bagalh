package com.example.utils

import java.math.BigDecimal

object NumberUtils {
    /**
     * Converts Eastern Arabic numerals (٠-٩) and Persian numerals to Western Arabic digits (0-9).
     * Replaces Arabic decimal separators with standard dot.
     */
    fun normalizeNumberString(input: String): String {
        val sb = StringBuilder()
        for (ch in input.trim()) {
            when (ch) {
                '٠', '۰' -> sb.append('0')
                '١', '۱' -> sb.append('1')
                '٢', '۲' -> sb.append('2')
                '٣', '۳' -> sb.append('3')
                '٤', '۴' -> sb.append('4')
                '٥', '۵' -> sb.append('5')
                '٦', '۶' -> sb.append('6')
                '٧', '۷' -> sb.append('7')
                '٨', '۸' -> sb.append('8')
                '٩', '۹' -> sb.append('9')
                '٫', ',' -> sb.append('.')
                '.' -> sb.append('.')
                '-' -> sb.append('-')
                in '0'..'9' -> sb.append(ch)
                else -> {
                    // Ignore non-numeric noise
                }
            }
        }
        val result = sb.toString()
        // If multiple dots, keep only the first dot
        val firstDot = result.indexOf('.')
        return if (firstDot != -1) {
            val before = result.substring(0, firstDot + 1)
            val after = result.substring(firstDot + 1).replace(".", "")
            before + after
        } else {
            result
        }
    }

    fun parseBigDecimal(input: String?, default: BigDecimal = BigDecimal.ZERO): BigDecimal {
        if (input.isNullOrBlank()) return default
        return try {
            val cleaned = normalizeNumberString(input)
            if (cleaned.isBlank() || cleaned == "." || cleaned == "-") {
                default
            } else {
                BigDecimal(cleaned)
            }
        } catch (_: Exception) {
            default
        }
    }

    fun parseBigDecimalOrNull(input: String?): BigDecimal? {
        if (input.isNullOrBlank()) return null
        return try {
            val cleaned = normalizeNumberString(input)
            if (cleaned.isBlank() || cleaned == "." || cleaned == "-") null
            else BigDecimal(cleaned)
        } catch (_: Exception) {
            null
        }
    }
}

fun String?.toSafeBigDecimal(default: BigDecimal = BigDecimal.ZERO): BigDecimal {
    return NumberUtils.parseBigDecimal(this, default)
}
