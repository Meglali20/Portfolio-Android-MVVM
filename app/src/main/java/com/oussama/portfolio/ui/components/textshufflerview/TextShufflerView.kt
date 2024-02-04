package com.oussama.portfolio.ui.components.textshufflerview

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.R
import com.oussama.portfolio.utils.Utils.Companion.spToPx
import com.oussama.portfolio.utils.dummyText
import com.oussama.portfolio.utils.lettersAndSymbols
import java.util.regex.Matcher
import java.util.regex.Pattern


class TextShufflerView : View {
    companion object {
        private const val MAX_ITERATIONS = 25
        private const val ANIMATION_SLEEP_TIME = 2000
    }

    private var textSize: Int = 0
    private var text: String = dummyText

    private var fontId: Int = 0
    private var screenWidth: Int = 0
    private var linesHeight: Int = 0
    private lateinit var colors: List<Int>
    private val paint = Paint()
    private var spaceWidth: Float = 0f
    private val cachedMeasurements: HashMap<String, Float> = HashMap()
    private var lineHeight: Float = 0f
    private var lines: ArrayList<LineObj> = arrayListOf()
    private var isAnimating = false
    private var isSingleLine = false
    private var isTextSet = false
    private var textAllCaps = false
    private val animator = ValueAnimator.ofInt(0, MAX_ITERATIONS)
    private var autoAnimate = false
    private var inScrollView = true
    private val viewLocationOnScreen = IntArray(2)
    var textColor: Int = Color.Blue.toArgb()
        set(value) {
            field = value
            invalidate()
        }


    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        if (attrs == null)
            return
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.TextShufflerView, defStyleAttr, 0)
        val defaultText = attributes.getText(R.styleable.TextShufflerView_android_text) ?: ""
        textSize =
            attributes.getDimensionPixelSize(R.styleable.TextShufflerView_android_textSize, 0)
        textAllCaps =
            attributes.getBoolean(R.styleable.TextShufflerView_android_textAllCaps, textAllCaps)
        autoAnimate = attributes.getBoolean(R.styleable.TextShufflerView_autoAnimate, autoAnimate)
        inScrollView =
            attributes.getBoolean(R.styleable.TextShufflerView_inScrollView, inScrollView)
        textColor = attributes.getColor(
            R.styleable.TextShufflerView_android_textColor,
            Color(0xFFFFFFFF).toArgb()
        )
        fontId = attributes.getResourceId(
            R.styleable.TextShufflerView_android_fontFamily,
            R.font.jet_brains_mono_regular
        )
        attributes.recycle()
        if (!TextUtils.isEmpty(defaultText)) text = defaultText.toString()

        colors =
            listOf(
                Color(0xFF61dca3).toArgb(),
                Color(0xFF61b3dc).toArgb(),
                Color(0xFF2b4539).toArgb()
            )

        if (!isInEditMode)
            colors = BaseApplication.INSTANCE.colorCombination


        applyPaintOptions()

    }


    private fun applyPaintOptions() {
        textSize = if (textSize == 0) 16.spToPx() else textSize
        paint.typeface = ResourcesCompat.getFont(context, fontId)
        paint.textSize = textSize.toFloat()
        spaceWidth = paint.measureText(" ")
        val fontMetrics = paint.fontMetrics
        lineHeight = fontMetrics.descent - fontMetrics.ascent

    }

    private fun measureWordOnce(word: String): Float {
        return cachedMeasurements[word] ?: paint.measureText(word)
            .also { cachedMeasurements[word] = it }
    }


    fun setText(text: String, autoAnimate: Boolean = false, animateWithDelay: Boolean = true) {
        val pattern: Pattern = Pattern.compile("\\s*\\\\n\\s*")
        val matcher: Matcher = pattern.matcher(text)
        this.text = matcher.replaceAll(" \\\\n ").replace("\\n", "\n")
        if (textAllCaps)
            this.text = this.text.uppercase()
        splitText()
        requestLayout()
        if (autoAnimate) {
            postDelayed({
                shuffleText(true, animateWithDelay)
            }, 250)

        }
    }

    /**
     * Split the whole text into lines and lines into cells
     * the max number of cells in a line will be set according to the width of the view
     *
     */
    private fun splitText() {
        cachedMeasurements.clear()
        lines.clear()
        val words = text.split(" ")
            .onEach { word ->
                measureWordOnce(word)
            }
        val maxWidth = screenWidth
        var currentLineWidth = 0f
        var numberOfSpaces = 0
        val currentLine = mutableListOf<CellObj>()

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
            if (currentLineWidth + wordWidth + (numberOfSpaces * spaceWidth) > maxWidth) {
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
            if (currentLineWidth + wordWidth + nextWordWidth + (numberOfSpaces * spaceWidth) > maxWidth || isNextWordBreakLine)
                isLastWordInLine = true

            word.forEach { char ->
                val cell = CellObj(
                    originalCharacter = char,
                    originalColor = textColor,
                    currentIteration = 0,
                    font = null
                )
                currentLine.add(cell)
            }

            if (!isLastWordInLine)
                currentLine.add(
                    CellObj(
                        ' ',
                        originalColor = textColor,
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
        isSingleLine = lines.size == 1
        linesHeight = (lines.size * lineHeight + lineHeight).toInt()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Get the suggested dimensions provided by the parent
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        screenWidth = widthSize

        if (!isTextSet) {
            setText(text, autoAnimate = autoAnimate)
            isTextSet = true
        }

        // Include padding in desired width and height
        val desiredWidth = screenWidth - paddingLeft - paddingRight
        val desiredHeight = linesHeight - paddingTop - paddingBottom

        // Resolve the width and height based on the mode (e.g., EXACTLY, AT_MOST, UNSPECIFIED)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        // Handle width
        val finalWidth: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize // Parent has specified an exact size
            MeasureSpec.AT_MOST -> Math.min(
                desiredWidth,
                widthSize
            ) // Parent has provided a maximum size, take the smaller of the two
            else -> desiredWidth // No specific size, use the desired size
        }

        // Handle height
        val finalHeight: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize // Parent has specified an exact size
            MeasureSpec.AT_MOST -> Math.min(
                desiredHeight,
                heightSize
            ) // Parent has provided a maximum size, take the smaller of the two
            else -> desiredHeight // No specific size, use the desired size
        }

        // Set the final dimensions for your custom view
        if (isSingleLine) {
            var lineText = ""
            lines[0].cellsList.forEach {
                lineText += it.originalCharacter
            }
            val lineWidth = paint.measureText(lineText)
            setMeasuredDimension(
                (lineWidth + paddingLeft + paddingRight).toInt(),
                MeasureSpec.getSize((finalHeight + paddingTop + paddingBottom - lineHeight / 2).toInt())
            )
        } else {
            setMeasuredDimension(
                MeasureSpec.getSize((finalWidth + paddingLeft + paddingRight)),
                MeasureSpec.getSize((finalHeight + paddingTop + paddingBottom - lineHeight / 2).toInt())
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        var numberOfChars = 0
        var drw = ""
        for ((lineIndex, line) in lines.withIndex()) {
            val lineY = lineIndex * lineHeight + lineHeight
            if (!line.isVisible && !isInEditMode) {
                var lineText = ""
                line.cellsList.forEach {
                    lineText += it.originalCharacter
                }
                continue
            }
            if (lineIndex > 0) {
                numberOfChars++
                drw += " "
            }
            line.cellsList.forEachIndexed { cellIndex, cellObj ->
                val originalCharacter = cellObj.originalCharacter
                numberOfChars++
                drw += originalCharacter
                if (originalCharacter == ' ') return@forEachIndexed

                val currentIteration =
                    if (isInEditMode) (if(cellIndex < line.cellsList.size / 2) MAX_ITERATIONS else 3) else cellObj.currentIteration
                val alpha = if (currentIteration == 0) 0 else 255
                if (alpha == 0) return@forEachIndexed

                /*val char =
                    if (currentIteration >= MAX_ITERATIONS) originalCharacter else lettersAndSymbols.random()
                val color =
                    if (currentIteration >= MAX_ITERATIONS) cellObj.originalColor else colors.random()*/


                val previousIteration = if (isInEditMode) currentIteration else cellObj.previousIteration
                val char: Char
                val color: Int
                if(previousIteration == currentIteration) {
                    char = cellObj.currentCharacter
                    color = cellObj.currentColor
                }
                else {
                    char =
                        if (currentIteration >= MAX_ITERATIONS) originalCharacter else lettersAndSymbols.random()
                    color = if (currentIteration >= MAX_ITERATIONS) cellObj.originalColor else colors.random()
                }

                val charWidth = paint.measureText(char.toString())
                val x = cellIndex * charWidth

                paint.color = color
                if (!isInEditMode) {
                    paint.alpha = alpha
                }
                canvas.drawText(char.toString(), x, lineY, paint)

                cellObj.currentCharacter = char
                cellObj.currentColor = color
                cellObj.previousIteration = cellObj.currentIteration
                line.previousIteration = line.currentIteration
            }
        }
    }




    /**
     * we set visibility to false lines that are not visible and that has not been animated yet
     * */
    private fun filterLinesToDraw() {
        val currentTimestamp = System.currentTimeMillis()
        lines.forEachIndexed { lineIndex, lineObj ->
            val lineY = lineIndex * lineHeight + lineHeight
            val isLineAnimated = isLineAnimated(lineObj, currentTimestamp)
            lineObj.isVisible = isLineVisible(0, lineY.toInt())
            if (!isLineAnimated && !lineObj.isVisible) {
                resetIterationForLine(lineObj)
            }
        }
    }

    fun onScroll() {
        shuffleText(reset = false, withDelay = true)
    }



    fun shuffleText(reset: Boolean, withDelay: Boolean) {
        if (reset) reset()
        filterLinesToDraw()
        if (isAnimating) return

        val numberOfLinesToAnimate = lines.filter { it.isVisible }
            .flatMap { line ->
                line.cellsList.filter { it.currentIteration < MAX_ITERATIONS }
            }.size

        // If there's nothing to animate, invalidate to trigger a redraw
        if (numberOfLinesToAnimate == 0) {
            invalidate()
            return
        }
        //val animator = ValueAnimator.ofInt(0, MAX_ITERATIONS)
        animator.duration = 800
        val staggerDuration = 50L

        animator.removeAllUpdateListeners()
        animator.removeAllListeners()

        //val startTime = System.currentTimeMillis()
        animator.addUpdateListener { //valueAnimator ->
            //val iteration = valueAnimator.animatedValue as Int

            lines.filter {
                it.isVisible
            }.forEachIndexed { lineIndex, line ->
                line.cellsList
                    .filter { it.currentIteration < MAX_ITERATIONS }
                    .forEachIndexed { cellIndex, cellObj ->

                        if (withDelay) {
                            if (!cellObj.delayed) {
                                val delayValue = ((lineIndex+1) + cellIndex) * staggerDuration
                                postDelayed({
                                    cellObj.currentIteration = cellObj.currentIteration + 1
                                    if (cellObj.currentIteration == MAX_ITERATIONS) {
                                        line.lastTimeAnimationPlayed = System.currentTimeMillis()
                                        line.isAnimationFinished = true
                                    }
                                    invalidate()
                                }, delayValue)
                                cellObj.delayed = true
                            } else {
                                if (cellObj.currentIteration > 0) {
                                    cellObj.currentIteration = cellObj.currentIteration + 1
                                    if (cellObj.currentIteration == MAX_ITERATIONS) {
                                        line.lastTimeAnimationPlayed = System.currentTimeMillis()
                                        line.isAnimationFinished = true
                                    }
                                    invalidate()
                                }
                            }
                        }
                       /* val elapsedTime = System.currentTimeMillis() - startTime
                        if (withDelay) {
                            val delayValue = ((lineIndex) + cellIndex) * staggerDuration

                            if (elapsedTime >= delayValue) {
                                cellObj.currentIteration = cellObj.currentIteration + 1
                                if (cellObj.currentIteration == MAX_ITERATIONS) {
                                    line.lastTimeAnimationPlayed = System.currentTimeMillis()
                                    line.isAnimationFinished = true
                                }
                                invalidate()
                            }
                        }*/ else {
                            cellObj.currentIteration = cellObj.currentIteration + 1
                            if (cellObj.currentIteration == MAX_ITERATIONS) {
                                line.lastTimeAnimationPlayed = System.currentTimeMillis()
                                line.isAnimationFinished = true
                            }
                            invalidate()
                        }
                    }
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                isAnimating = true
            }

            override fun onAnimationEnd(p0: Animator) {
                isAnimating = false
                checkCompletedIterationForAllLines()
            }

            override fun onAnimationCancel(p0: Animator) {
            }

            override fun onAnimationRepeat(p0: Animator) {
            }
        })
        animator.start()
    }

    private fun checkCompletedIterationForAllLines() {
        var shouldContinueAnimation = false
        lines.filter { it.isVisible }.forEach { line ->
            val unfinishedCells = line.cellsList.count { it.currentIteration < MAX_ITERATIONS }
            if (unfinishedCells > 0) {
                shouldContinueAnimation = true
                return@forEach
            }
        }
        if (shouldContinueAnimation) {
            shuffleText(reset = false, withDelay = true)
        }
    }


    private fun isLineAnimated(line: LineObj, currentTimeMillis: Long): Boolean {
        return (currentTimeMillis < (line.lastTimeAnimationPlayed + ANIMATION_SLEEP_TIME))
    }


    private fun isLineVisible(x: Int, y: Int): Boolean {
        if (!inScrollView) return true

        getLocationOnScreen(viewLocationOnScreen)
        val lineBottom = ((y) + viewLocationOnScreen[1])
        val lineVisibleVertically = lineBottom in 0..resources.displayMetrics.heightPixels
        val lineVisibleHorizontally = x in 0..resources.displayMetrics.widthPixels
        return lineVisibleVertically //&& lineVisibleHorizontally
    }

    private fun reset() {
        lines.forEach { line ->
            line.currentIteration = 0
            line.previousIteration = 0
            line.isAnimationFinished = false
            line.lastTimeAnimationPlayed = -1
            line.cellsList.forEach {
                it.currentIteration = 0
                it.previousIteration = 0
                it.delayed = false
                it.currentCharacter = ' '
            }
        }
    }


    private fun resetIterationForLine(line: LineObj) {
        line.isAnimationFinished = false
        line.animationStartTime = -1L
        line.lastTimeAnimationPlayed = -1
        line.cellsList.forEach {
            it.delayed = false
            it.currentIteration = 0
            it.previousIteration = 0
        }
    }


    fun paused(isPaused: Boolean) {
        if (isPaused) animator.pause()
        else animator.resume()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.removeAllUpdateListeners()
        animator.removeAllListeners()
        animator.cancel()
    }

}
