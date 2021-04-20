package com.example.mobile_spotter.ext

val VERSION_REGEX = "[0-9]+(\\.[0-9]+)*".toRegex()
val VERSION_SYMBOLS = "([^\\d.])".toRegex()

/**
 * Сравнивает значения версий в формате String
 * @param first первая версия
 * @param second вторая версия
 * @return true, если первая версия равна или новее, чем вторая
 * @return false, если первая версия старше, чем вторая
 * @return null, если по какой-то причине не удалось распарсить значения
 */
fun splitAndCompareVersion(first: String, second: String): Int {
    // Откидываем буквы и прочие непонятные символы
    val firstFormatted = first.replace(VERSION_SYMBOLS, "")
    val secondFormatted = second.replace(VERSION_SYMBOLS, "")

    // Проверка, что версия пришла и значение соответсвует формату
    if (firstFormatted.isNotEmpty() && secondFormatted.isNotEmpty() &&
            firstFormatted.matches(VERSION_REGEX) && secondFormatted.matches(VERSION_REGEX)) {
        val firstParts = firstFormatted.split(".")
        val secondParts = secondFormatted.split(".")
        val length: Int = kotlin.math.max(firstParts.size, secondParts.size)
        for (i in 0 until length) {
            val firstPart = if (i < firstParts.size) firstParts[i].toInt() else 0
            val secondPart = if (i < secondParts.size) secondParts[i].toInt() else 0
            if (firstPart < secondPart) return -1
            if (firstPart > secondPart) return 1
        }
        return 1
    } else return 0
}