package com.danilkinkin.buckwheat.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import kotlin.math.min

private fun getAnnotatedString(
    value: String,
    hintAfterIndex: Int,
    hintColor: Color,
): AnnotatedString {
    val builder = AnnotatedString.Builder(value)
    builder.addStyle(SpanStyle(color = hintColor), hintAfterIndex, value.length)
    return builder.toAnnotatedString()
}

private fun calcOffset(before: String, after: String, position: Int): Int {
    var offset = 0

    for (i in 0 until position) {
        while (i < before.length && i + offset < after.length && (before[i] != after[i + offset])) {
            offset += 1
        }
    }


    return offset
}

private fun visualTransformationAsCurrency(
    input: AnnotatedString,
    currency: ExtendCurrency,
    hintColor: Color,
): TransformedText {
    val output = prettyCandyCanes(
        input.text.ifEmpty { "0" }.toBigDecimal(),
        currency,
        maximumFractionDigits = 2,
        minimumFractionDigits = 0,
    ) + (if (input.text.lastOrNull() == '.') "." else "")
    val fixed = tryConvertStringToNumber(input.text)

    val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return (offset + calcOffset(input.text, output, offset)).coerceIn(0, output.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            return (offset - calcOffset(input.text, output, offset)).coerceIn(0, output.length)
        }
    }

    return TransformedText(
        getAnnotatedString(
            output + fixed.third,
            output.length,
            hintColor,
        ),
        offsetTranslator,
    )
}

fun visualTransformationAsCurrency(
    currency: ExtendCurrency,
    hintColor: Color,
): ((input: AnnotatedString) -> TransformedText) {
    return {
        visualTransformationAsCurrency(it, currency, hintColor)
    }
}

fun isNumber(char: Char): Boolean {
    return try {
        char.toString().toInt(); true
    } catch (e: Exception) {
        false
    }
}

fun Triple<String, String, String>.join(third: Boolean = true): String = this.first + this.second + if (third) this.third else ""

fun tryConvertStringToNumber(input: String): Triple<String, String, String> {
    val afterDot = input.dropWhile { it != '.' }
    val beforeDot = input.substring(0, input.length - afterDot.length)

    val start = beforeDot.filter { isNumber(it) }.dropWhile { it == '0' }
    val hintStart = if (start.isEmpty()) "0" else ""
    val end = afterDot.filter { isNumber(it) }
    var hintEnd = ""
    if (end.isEmpty() && input.lastOrNull() == '.') {
        hintEnd = "0"
    }
    val middle = if (end.isNotEmpty() || (input.lastOrNull() == '.')) {
        "."
    } else {
        ""
    }

    return Triple(
        hintStart,
        "$start$middle${end.substring(0, min(2, end.length))}",
        hintEnd,
    )
}