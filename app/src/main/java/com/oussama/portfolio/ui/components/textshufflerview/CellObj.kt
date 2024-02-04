package com.oussama.portfolio.ui.components.textshufflerview

import androidx.compose.ui.text.font.FontFamily


data class CellObj(
    var originalCharacter: Char,
    var currentCharacter: Char = originalCharacter,
    var originalColor: Int,
    var currentColor: Int = originalColor,
    var previousIteration: Int = 0,
    var currentIteration: Int = 0,
    var font: FontFamily? = null,
    var delayed: Boolean = false,
    var animationStartTime: Long  = 0L
)