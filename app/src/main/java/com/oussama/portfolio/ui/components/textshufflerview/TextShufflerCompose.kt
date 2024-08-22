package com.oussama.portfolio.ui.components.textshufflerview

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.oussama.portfolio.utils.dummyText
import com.oussama.portfolio.utils.lettersAndSymbols
import kotlin.math.min


@Preview(showBackground = true)
@Composable
fun TextShufflerPreview() {
    TextShufflerCompose(text = "$dummyText $dummyText $dummyText $dummyText")
}

@Composable
fun TextShufflerCompose(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Int = Color.Black.toArgb(),
    textSize: Float = 40f,
    typeface: Typeface? = Typeface.MONOSPACE,
    colors: List<Int> = listOf(
        Color(0xFF61dca3).toArgb(),
        Color(0xFF61b3dc).toArgb(),
        Color(0xFF2b4539).toArgb()
    ),
    shuffleText: (@Composable (shuffleText: (reset: Boolean, withDelay: Boolean) -> Unit) -> Unit)? = null
) {


    val lines = remember { mutableListOf<LineObj>() }
    val cachedMeasurements = remember { mutableMapOf<String, Float>() }
    var composableWidth by remember { mutableFloatStateOf(0f) }
    var composableHeight by remember { mutableFloatStateOf(0f) }
    var oldComposableWidth by remember { mutableFloatStateOf(-1f) }
    var oldComposableHeight by remember { mutableFloatStateOf(-1f) }
    var linesHeight by remember { mutableFloatStateOf(0f) }
    var isSingleLine by remember { mutableStateOf(false) }
    var recompositionHandler by remember { mutableIntStateOf(0) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var locationOnScreen by remember { mutableStateOf(Offset.Zero) }
    var delayCells by remember { mutableStateOf(false) }

    val textPaint = Paint().apply {
        color = textColor
        this.textSize = textSize
        this.typeface = typeface
    }

    val fontMetrics = textPaint.fontMetrics
    val lineHeight = fontMetrics.descent - fontMetrics.ascent
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels
    val inPreviewMode = LocalInspectionMode.current
    fun resetIterationForLine(line: LineObj) {
        line.currentIteration = 0
        line.previousIteration = -1
        line.isAnimationFinished = false
        line.animationStartTime = -1L
        line.lastTimeAnimationPlayed = -1
        line.cellsList.forEach {
            it.currentIteration = 0
            it.previousIteration = -1
            it.delayed = false
            it.currentCharacter = ' '
        }
    }

    fun reset() {
        lines.forEach { line ->
            resetIterationForLine(line)
        }
    }

    fun isLineAnimated(line: LineObj, currentTimeMillis: Long): Boolean {
        return (currentTimeMillis < (line.lastTimeAnimationPlayed + TextShufflerView.ANIMATION_SLEEP_TIME))
    }

    fun isLineAnimationInProgress(line: LineObj): Boolean {
        return line.animationStartTime > 0 && line.lastTimeAnimationPlayed <= 0
    }

    fun isLineVisible(y: Int): Boolean {
        //if (!inScrollView) return true
        val lineBottom = ((y) + locationOnScreen.y).toInt()
        val lineVisibleVertically =
            lineBottom in 0..screenHeight
        return lineVisibleVertically
    }

    fun filterLinesToDraw() {
        val currentTimestamp = System.currentTimeMillis()
        lines.forEachIndexed { lineIndex, lineObj ->
            val lineY = lineIndex * lineHeight + lineHeight
            val isLineAnimated =
                isLineAnimated(lineObj, currentTimestamp) || isLineAnimationInProgress(lineObj)
            lineObj.isVisible = isLineVisible(lineY.toInt()) || isLineAnimated
            if (!isLineAnimated /*&& !lineObj.isVisible*/) {
                resetIterationForLine(lineObj)
            }
        }
    }

    fun shuffleText(reset: Boolean, withDelay: Boolean) {
        if (reset) reset()
        delayCells = withDelay
        filterLinesToDraw()
        recompositionHandler++
    }

    Column(modifier = modifier) {
        if (shuffleText != null) {
            shuffleText { reset, withDelay ->
                shuffleText(reset = reset, withDelay = withDelay)
            }
        }
        /*Text(text = "Location X${locationOnScreen.x} Location Y${locationOnScreen.y}  Preview? $inPreviewMode Composable width $composableWidth number of lines ${lines.size}")
        Slider(
            value = iterationValue.toFloat(),
            onValueChange = { iterationValue = it.roundToInt() },
            valueRange = 0f..26f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Button(
            onClick = {
                shuffleText(true)
            }
        ) {
            Text(
                text = "Reset"
            )
        }*/
        Canvas(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .height(linesHeight.pxToDp())
                .onGloballyPositioned {
                    locationOnScreen = Offset(it.positionInWindow().x, it.positionInWindow().y)
                }
                .onSizeChanged { newSize ->
                    canvasSize = newSize
                    composableWidth = newSize.width.toFloat()
                    composableHeight = newSize.height.toFloat()
                    if (oldComposableWidth != composableWidth || oldComposableHeight != composableHeight) {
                        splitText(text, textPaint, lines, cachedMeasurements, composableWidth)
                        isSingleLine = lines.size == 1
                        linesHeight = (lines.size * lineHeight + lineHeight)
                        oldComposableWidth = composableWidth
                        oldComposableHeight = composableHeight
                    }
                }
        ) {
            var recompositionNeeded = false

            drawIntoCanvas { canvas ->
                recompositionHandler.let {
                    var numberOfChars = 0
                    var drw = ""
                    for ((lineIndex, line) in lines.withIndex()) {
                        val lineY = lineIndex * lineHeight + lineHeight
                        if (!line.isVisible && !inPreviewMode) continue
                        if (lineIndex > 0) {
                            numberOfChars++
                            drw += " "
                        }

                        if (line.animationStartTime <= 0 && line.currentIteration != TextShufflerView.MAX_ITERATIONS) {
                            line.animationStartTime = System.currentTimeMillis()
                        }
                        if (!delayCells)
                            prepareLineForDrawing(delayCells, lineIndex, line)
                        line.cellsList.forEachIndexed { cellIndex, cellObj ->
                            if (!delayCells) {
                                cellObj.currentIteration = line.currentIteration
                            } else {
                                if (cellObj.currentIteration < TextShufflerView.MAX_ITERATIONS)
                                    prepareCellForDrawing(
                                        delayCells,
                                        lineIndex,
                                        line,
                                        cellIndex,
                                        cellObj
                                    )
                            }

                            val originalCharacter = cellObj.originalCharacter
                            numberOfChars++
                            drw += originalCharacter
                            if (originalCharacter == ' ') return@forEachIndexed

                            val currentIteration =
                                if (inPreviewMode) (if (cellIndex < line.cellsList.size / 2) TextShufflerView.MAX_ITERATIONS else 3) else cellObj.currentIteration

                            val alpha = if (delayCells) {
                                if (currentIteration == 0)
                                    0
                                else if (currentIteration in 1..5)
                                    120
                                else
                                    255
                            } else {
                                255
                            }


                            if (alpha == 0) return@forEachIndexed

                            val previousIteration = cellObj.previousIteration
                            val char: Char
                            val color: Int
                            if (previousIteration == currentIteration) {
                                char = cellObj.currentCharacter
                                color = cellObj.currentColor
                            } else {
                                char =
                                    if (currentIteration >= TextShufflerView.MAX_ITERATIONS) originalCharacter else lettersAndSymbols.random()
                                color =
                                    if (currentIteration >= TextShufflerView.MAX_ITERATIONS) cellObj.originalColor else colors.random()
                            }


                            val charWidth = textPaint.measureText(char.toString())
                            val x = cellIndex * charWidth
                            textPaint.color = color
                            textPaint.alpha = alpha
                            canvas.nativeCanvas.drawText(char.toString(), x, lineY, textPaint)

                            cellObj.currentCharacter = char
                            cellObj.currentColor = color
                            cellObj.previousIteration = cellObj.currentIteration
                            line.previousIteration = line.currentIteration
                        }
                        val lineFullyAnimated =
                            if (delayCells) line.cellsList.all { it.currentIteration == TextShufflerView.MAX_ITERATIONS } else line.currentIteration == TextShufflerView.MAX_ITERATIONS
                        if (lineFullyAnimated) {
                            line.isAnimationFinished = true
                            line.lastTimeAnimationPlayed = System.currentTimeMillis()
                            line.animationStartTime = -1L
                        } else
                            recompositionNeeded = true
                    }
                    if (recompositionNeeded)
                        recompositionHandler++
                }
            }


        }
    }


}

@Composable
internal fun TextUnit.spToPx(): Float {
    return this.value * LocalDensity.current.fontScale
}

@Composable
internal fun Float.pxToDp(): Dp {
    return (this / LocalDensity.current.density).dp
}

private fun splitText(
    text: String,
    textPaint: Paint,
    lines: MutableList<LineObj>,
    cachedMeasurements: MutableMap<String, Float>,
    composableWidth: Float
) {
    cachedMeasurements.clear()
    lines.clear()
    val words = text.split(" ")
        .onEach { word ->
            cachedMeasurements[word] ?: textPaint.measureText(word)
                .also { cachedMeasurements[word] = it }
        }
    var currentLineWidth = 0f
    var numberOfSpaces = 0
    val currentLine = mutableListOf<CellObj>()
    val spaceWidth = textPaint.measureText(" ")
    var linesCounter = 0
    for (wordIndex in words.indices) {
        val word = words[wordIndex]
        if (word == "\n") {
            val line = LineObj(linePosition = linesCounter, cellsList = currentLine.toList())
            lines.add(line)
            linesCounter++
            currentLine.clear()
            currentLineWidth = 0f
            numberOfSpaces = 0
            continue
        }
        val wordWidth = cachedMeasurements[word] ?: 0f

        val nextWordWidth = if (wordIndex + 1 < words.size) {
            val nextWord = words[wordIndex + 1]
            (cachedMeasurements[nextWord]?.plus(spaceWidth)) ?: 0f
        } else {
            0f
        }
        var isLastWordInLine: Boolean
        if (currentLineWidth + wordWidth + (numberOfSpaces * spaceWidth) > composableWidth) {
            if (currentLine.isNotEmpty()) {
                val line =
                    LineObj(linePosition = linesCounter, cellsList = currentLine.toList())
                lines.add(line)
                linesCounter++
                currentLine.clear()
                currentLineWidth = 0f
                numberOfSpaces = 0
            }
        }

        var isNextWordBreakLine = false
        if (wordIndex + 1 < words.size) {
            val nextWord = words[wordIndex + 1]
            isNextWordBreakLine = (nextWord == "\n")
        }

        isLastWordInLine = (wordIndex + 1 >= words.size)
        if (currentLineWidth + wordWidth + nextWordWidth + (numberOfSpaces * spaceWidth) > composableWidth || isNextWordBreakLine)
            isLastWordInLine = true

        word.forEach { char ->
            val cell = CellObj(
                originalCharacter = char,
                originalColor = textPaint.color,
                currentIteration = 0,
                font = null
            )
            currentLine.add(cell)
        }

        if (!isLastWordInLine)
            currentLine.add(
                CellObj(
                    ' ',
                    originalColor = textPaint.color,
                    font = null
                )
            ) // Add space after the word
        currentLineWidth += wordWidth
        numberOfSpaces++
    }

    if (currentLine.isNotEmpty()) {
        val line = LineObj(cellsList = currentLine.toList())
        lines.add(line)
    }
}


private fun calculateIterationValue(
    maxIterations: Int,
    animationStartTime: Long,
    animationDuration: Long
): Int {
    val currentTime = System.currentTimeMillis()
    val elapsedTime = currentTime - animationStartTime
    if (elapsedTime >= animationDuration) {
        return maxIterations
    }
    val fractionElapsed = elapsedTime.toFloat() / animationDuration.toFloat()

    return (fractionElapsed * maxIterations).toInt()
}

private fun prepareLineForDrawing(withDelay: Boolean, lineIndex: Int, line: LineObj) {
    val delay = if (withDelay) min(5, lineIndex) * 150L else 0
    line.currentIteration = calculateIterationValue(
        TextShufflerView.MAX_ITERATIONS,
        line.animationStartTime + delay,
        TextShufflerView.SHORT_ANIMATION
    )
}

private fun prepareCellForDrawing(
    delayCells: Boolean,
    lineIndex: Int,
    line: LineObj,
    cellIndex: Int,
    cellObj: CellObj
) {
    val delayValue = if (delayCells) (((lineIndex + 1) + cellIndex) * 50L) else 0
    if (delayCells && !cellObj.delayed) {
        val currentTime = System.currentTimeMillis()
        if (currentTime < line.animationStartTime + delayValue) {
            cellObj.currentIteration = 0
            return
        } else {
            cellObj.delayed = true
        }
    }
    cellObj.currentIteration = calculateIterationValue(
        TextShufflerView.MAX_ITERATIONS,
        line.animationStartTime + delayValue,
        TextShufflerView.LONG_ANIMATION
    )
}