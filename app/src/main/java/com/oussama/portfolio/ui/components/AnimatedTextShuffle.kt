package com.oussama.portfolio.ui.components

import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oussama.portfolio.R
import com.oussama.portfolio.ui.components.textshufflerview.CellObj
import com.oussama.portfolio.utils.dummyText
import com.oussama.portfolio.utils.lettersAndSymbols
import kotlinx.coroutines.launch
import timber.log.Timber




@Composable
fun Cell(currentIteration: Int, originalCharacter: Char, originalColor: Int, font: FontFamily) {
    val char = if (currentIteration >= 7 || originalCharacter == ' ') originalCharacter.toString() else lettersAndSymbols.random()
    val colors = arrayOf(Color(0xFF61dca3).toArgb(), Color(0xFF61b3dc).toArgb(), Color(0xFF2b4539).toArgb())
    val color = if (currentIteration >= 7 ||  originalCharacter == ' ') originalColor else colors.random()
    val alpha: Float by animateFloatAsState(if (currentIteration == 0) 0f else 1f)
    Text(
        text = char.toString(),
        modifier = Modifier.alpha(alpha),
        color = Color(color),
        fontSize = 16.sp,
        fontFamily = font,
    )
}

@Composable
fun Line(
    lineIndex: Int,
    cells: List<CellObj>,
    fontFamily: FontFamily,
    animate: Boolean
) {
    Row {
        cells.forEachIndexed { index, cell ->

            val animatedValue by animateIntAsState(
                targetValue = if(animate) 0 else 7,
                animationSpec = tween(durationMillis = 1500,
                    delayMillis = index*50
                ),

                label = "" // Change the duration here (in milliseconds)
            )
            val currentIteration = mutableIntStateOf(animatedValue)
            Cell(currentIteration.intValue, cell.originalCharacter, cell.originalColor, fontFamily)
        }
    }
}

@Composable
fun TextDisplay(text: String, font: FontFamily, animate: Boolean = false) {
    val screenWidth = remember { mutableStateOf(0.dp) }
    val lines = remember { mutableStateListOf<List<CellObj>>() }
    val wordWidths = remember { mutableMapOf<String, Int>() }
    val coroutineScope = rememberCoroutineScope()
    val textMeasurer = rememberTextMeasurer()
    val spaceWidth = textMeasurer.measure(
        text = " ",
        style = TextStyle(
            color = Color.Red,
            fontSize = 16.sp,
            fontFamily = font,
        )
    ).size.width
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                screenWidth.value = Dp(coordinates.size.width.toFloat())
            }
    ) {
        val words = text.split(" ")

        LaunchedEffect(text) {
            coroutineScope.launch {
                words.forEach { word ->
                    val textLayoutResult = textMeasurer.measure(
                        text = word,
                        style = TextStyle(
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontFamily = font,
                        )
                    )
                    val wordWidth = textLayoutResult.size.width
                    wordWidths[word] = wordWidth
                }

                val maxWidth = screenWidth.value.value
                var currentLineWidth = 0
                var numberOfSpaces = 0
                val currentLine = mutableListOf<CellObj>()

                for (wordIndex in words.indices) {
                    val word = words[wordIndex]
                    val wordWidth = wordWidths[word] ?: 0

                    val nextWordWidth = if (wordIndex + 1 < words.size) {
                        val nextWord = words[wordIndex + 1]
                        wordWidths[nextWord] ?: 0
                    } else {
                        0
                    }


                    var isLastWordInLine = false
                    if (currentLineWidth + wordWidth + (numberOfSpaces * spaceWidth)> maxWidth) {
                        if (currentLine.isNotEmpty()) {
                            lines.add(currentLine.toList())
                            currentLine.clear()
                            currentLineWidth = 0
                            numberOfSpaces = 0
                        }
                    }

                    if (currentLineWidth + wordWidth + nextWordWidth + (numberOfSpaces * spaceWidth)> maxWidth)
                        isLastWordInLine = true

                    word.forEach { char ->
                      //  val cell = CellObj(originalCharacter = char, originalColor = Color.White, currentIteration = 0, font = font)
                        //currentLine.add(cell) // Default color is White
                    }

                    //if(!isLastWordInLine)
                        //currentLine.add(CellObj(' ', Color.White, font = font)) // Add space after the word
                    currentLineWidth += wordWidth
                    numberOfSpaces++
                }

                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toList())
                }
            }
        }

        Column {
            lines.forEachIndexed { index, line ->
                val an = mutableStateOf(animate)
                Line(lineIndex = index, cells = line,fontFamily = font, an.value)
            }
        }
    }
}


// ISSUE drawingScope for the canvas is being called TWICE on each recomposition
@Composable
fun Shuffler(text: String, font: FontFamily, currentIteration: Int, typeface: Typeface?) {
    val screenWidth = remember { mutableStateOf(0.dp) }
    val lines = remember { mutableStateListOf<List<CellObj>>() }
    val colors = remember {
        arrayOf(Color(0xFF61dca3).toArgb(), Color(0xFF61b3dc).toArgb(), Color(0xFF2b4539).toArgb())
    }
    val textMeasurer = rememberTextMeasurer()
    val spaceWidth = remember {
        textMeasurer.measure(
            text = " ",
            style = TextStyle(
                color = Color.Red,
                fontSize = 16.sp,
                fontFamily = font,
            )
        ).size.width
    }

    // Cache for text measurements
    val cachedMeasurements = remember { mutableMapOf<String, TextLayoutResult>() }

    // Measure frequently used texts only once
    fun measureTextOnce(text: String): TextLayoutResult {
        return cachedMeasurements[text]
            ?: textMeasurer.measure(
                text = text,
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontFamily = font,
                )
            ).also { cachedMeasurements[text] = it }
    }

    // Precompute word widths
    val words = text.split(" ")
    val wordWidths = remember { mutableMapOf<String, Int>() }
    for (word in words) {
        val textLayoutResult = measureTextOnce(word)
        wordWidths[word] = textLayoutResult.size.width.toInt()
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            Timber.e("GLOBAL POSITION !!")
            screenWidth.value = Dp(coordinates.size.width.toFloat())
            //val words = text.split(" ")

            val maxWidth = screenWidth.value.value
            var currentLineWidth = 0
            var numberOfSpaces = 0
            val currentLine = mutableListOf<CellObj>()

            for (wordIndex in words.indices) {
                val word = words[wordIndex]
                val wordWidth = wordWidths[word] ?: 0

                val nextWordWidth = if (wordIndex + 1 < words.size) {
                    val nextWord = words[wordIndex + 1]
                    wordWidths[nextWord] ?: 0
                } else {
                    0
                }
                var isLastWordInLine = false
                if (currentLineWidth + wordWidth + (numberOfSpaces * spaceWidth) > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine.toList())
                        currentLine.clear()
                        currentLineWidth = 0
                        numberOfSpaces = 0
                    }
                }

                if (currentLineWidth + wordWidth + nextWordWidth + (numberOfSpaces * spaceWidth) > maxWidth)
                    isLastWordInLine = true

                word.forEach { char ->
                   /* val cell = CellObj(
                        originalCharacter = char,
                        originalColor = Color.White,
                        currentIteration = 0,
                        font = font
                    )
                    currentLine.add(cell) // Default color is White*/
                }

                /*if (!isLastWordInLine)
                    currentLine.add(
                        CellObj(
                            ' ',
                            Color.White,
                            font = font
                        )
                    ) */// Add space after the word
                currentLineWidth += wordWidth
                numberOfSpaces++
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toList())
            }
        }
    ) {
        // Drawing logic inside the Canvas drawing scope
        Timber.e("Drawing logic ")
        lines.forEachIndexed { lineIndex, line ->
            //Timber.e("LINE INDEX IS $lineIndex")
            line.forEachIndexed { cellIndex, cellObj ->
                val originalCharacter = cellObj.originalCharacter
                val currentIteration1 = cellObj.currentIteration
                val char = if (currentIteration1 >= 7 || originalCharacter == ' ') originalCharacter else lettersAndSymbols.random()
                val color = if (currentIteration1 >= 7 || originalCharacter == ' ') cellObj.originalColor else colors.random()

                val textToDraw = char.toString()
                val textMeasurerResult = measureTextOnce(textToDraw)

                val x = (cellIndex * textMeasurerResult.size.width).toFloat()
                val y = lineIndex * 16.sp.toPx() + 5f

                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        textToDraw,
                        x,
                        y,
                        Paint().asFrameworkPaint().apply {
                            this.color = color
                            this.textSize = 16.sp.toPx()
                            this.typeface = typeface
                            // Set other paint properties if needed
                        }
                    )
                }
            }
        }
    }
}






@Preview
@Composable
fun PreviewTextDisplay() {
    val fontFamily = FontFamily(
        Font(R.font.jet_brains_mono_regular, FontWeight.Light)
    )
    Shuffler(dummyText, fontFamily, 0, null)
}
