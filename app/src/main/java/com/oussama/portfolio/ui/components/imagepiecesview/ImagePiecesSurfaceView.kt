package com.oussama.portfolio.ui.components.imagepiecesview

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.animation.addListener
import com.oussama.portfolio.ui.components.CubicBezierInterpolator
import com.oussama.portfolio.utils.BitmapUtils
import kotlin.math.roundToInt

class ImagePiecesSurfaceView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback, Runnable {

    private var surfaceHolder: SurfaceHolder = holder
    private var drawingThread: Thread? = null
    private var isRunning = false
    private var columns = 10
    private var rows = 12
    private var originalImage: Bitmap? = null
    private var backgroundImageBitmap: Bitmap? = null
    private var blurredBackgroundImageBitmap: Bitmap? = null
    private var pieces: MutableList<Piece> = ArrayList()
    private val handler = Handler(Looper.getMainLooper())
    private val paint = Paint()
    private var isAnimating = false
    private var animatorSet: AnimatorSet? = null
    private val blurBackgroundAtStart = true
    private var startPosX = 0f
    private var startPosY = 0f
    private var animationContinuationRunnable: Runnable? = null
    private var pieceWidth = 0
    private var pieceHeight = 0

    init {
        surfaceHolder.addCallback(this)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }


    private fun clipBitmap(
        positionX: Int,
        positionY: Int,
        width: Int,
        height: Int,
        resizedBitmap: Bitmap?
    ): Bitmap? {
        if (
            positionX < 0 || positionY < 0 ||
            positionX > resizedBitmap!!.width || positionY > resizedBitmap.height ||
            width > resizedBitmap.width || height > resizedBitmap.height ||
            resizedBitmap.width <= 0 || resizedBitmap.height <= 0
        )
            return null
        return Bitmap.createBitmap(
            resizedBitmap,
            positionX, positionY, width, height
        )
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        /*val backgroundImage = ContextCompat.getDrawable(context, R.drawable.ascii22)
        val image = ContextCompat.getDrawable(context, R.drawable.image222)
        originalImage = image?.toBitmap()
        originalImage = resizeBitmap(originalImage, width, height)
        backgroundImageBitmap = resizeBitmap(backgroundImage?.toBitmap(), width, height)
        createPieces()
        startDrawing()*/
    }

    fun setImages(foregroundImage: Bitmap, backgroundImage: Bitmap) {
        setImages(foregroundImage, backgroundImage, width, height)
    }

    fun setImages(
        foregroundImage: Bitmap,
        backgroundImage: Bitmap,
        resizedWidth: Int,
        resizedHeight: Int
    ) {
        if (BitmapUtils.isEmptyBitmap(foregroundImage) || BitmapUtils.isEmptyBitmap(backgroundImage)) return
        startPosX = (width / 2) - (resizedWidth / 2).toFloat()
        startPosY = (height / 2) - (resizedHeight / 2).toFloat()
        originalImage = resizeBitmap(foregroundImage, resizedWidth, resizedHeight)
        backgroundImageBitmap = resizeBitmap(backgroundImage, resizedWidth, resizedHeight)
        if (isRunning)
            stopDrawing()
        createPieces()
        if (blurBackgroundAtStart) {
            animateUnBlur()
        } else {
            startDrawing()
            postDelayed({
                /*animateIn()
                postDelayed({ animateOut() }, 1200)*/
                demoAnimateInFromCenter()
            }, 800)
        }
    }

    private fun animateUnBlur() {
        blurredBackgroundImageBitmap = BitmapUtils.fastBlur(
            backgroundImageBitmap?.copy(Bitmap.Config.ARGB_8888, true),
            0.5f,
            11f
        )
        startDrawing()
        val animator = ValueAnimator.ofInt(10, 1)
        animator.addUpdateListener {
            blurredBackgroundImageBitmap = BitmapUtils.fastBlur(
                backgroundImageBitmap?.copy(Bitmap.Config.ARGB_8888, true),
                0.5f,
                (it.animatedValue as Int).toFloat()
            )
        }
        animator.addListener(
            onEnd = {
                blurredBackgroundImageBitmap = null
                /*postDelayed({
                    animateIn()
                    postDelayed({ animateOut() }, 1200)
                }, 100)*/
            }
        )
        animator.duration = 1200
        animator.start()
    }

    private fun createPieces() {
        pieces.clear()
        val initialAlpha = 0
        val initialTranslateX = 0
        val initialTranslateY = 0
        val bitmapWidth = originalImage?.width ?: 0
        val bitmapHeight = originalImage?.height ?: 0
        pieceWidth = (bitmapWidth / columns).toDouble().roundToInt()
        pieceHeight = (bitmapHeight / rows).toDouble().roundToInt()
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val piece = Piece(row, column, null)
                val positionX = column * pieceWidth
                piece.positionX = positionX
                val positionY = pieceHeight * row
                piece.positionY = positionY
                piece.translateX = initialTranslateX
                piece.translateY = initialTranslateY
                piece.alpha = initialAlpha
                val pieceImage: Bitmap? =
                    clipBitmap(positionX, positionY, pieceWidth, pieceHeight, originalImage)
                piece.image = pieceImage
                pieces.add(piece)
            }
        }
    }

    private fun startDrawing() {
        isRunning = true
        drawingThread = Thread(this)
        drawingThread?.start()
    }

    private fun stopDrawing() {
        isRunning = false
        drawingThread?.join()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopDrawing()
        animatorSet?.cancel()
        animatorSet?.removeAllListeners()
        animatorSet = null
    }


    override fun run() {
        while (isRunning) {
            val canvas = surfaceHolder.lockCanvas() ?: continue
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            if (blurredBackgroundImageBitmap != null) {
                canvas.drawBitmap(blurredBackgroundImageBitmap!!, startPosX, startPosY, null)
                surfaceHolder.unlockCanvasAndPost(canvas)
                continue
            }
            if (backgroundImageBitmap != null)
                canvas.drawBitmap(backgroundImageBitmap!!, startPosX, startPosY, null)
            if (pieces.isEmpty()) {
                surfaceHolder.unlockCanvasAndPost(canvas)
                continue
            }
            pieces.forEach { piece ->
                val left = startPosX + piece.positionX.toFloat() + piece.translateX
                val top = startPosY + piece.positionY.toFloat() + piece.translateY
                //if (piece.rotation != 0) {
                canvas.save()
                canvas.rotate(
                    piece.rotation.toFloat(),
                    left + (piece.image?.width ?: 0) / 2f,
                    top + (piece.image?.height ?: 0) / 2f
                )

                // }
                paint.alpha = piece.alpha

                canvas.drawBitmap(
                    piece.image!!,
                    left,
                    top,
                    paint
                )
                //if (piece.rotation != 0)
                canvas.restore()

            }
            if (!isAnimating) {
                handler.post { animatePieces() }
            }

            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun animatePieces() {
        if (isAnimating || pieces.isEmpty()) return

        animatorSet?.cancel()
        animatorSet?.removeAllListeners()
        animatorSet = AnimatorSet()

        val animators = mutableListOf<Animator>()

        for (piece in pieces) {

            val translateXAnimator = if (piece.translateX != piece.toTranslateX) {
                ObjectAnimator.ofInt(
                    piece,
                    "translateX",
                    piece.translateX,
                    piece.toTranslateX
                ).apply {
                    duration = piece.durationTranslateX
                    startDelay = piece.delayTranslateX
                }
            } else null

            val translateYAnimator = if (piece.translateY != piece.toTranslateY) {
                ObjectAnimator.ofInt(
                    piece,
                    "translateY",
                    piece.translateY,
                    piece.toTranslateY
                ).apply {
                    duration = piece.durationTranslateY
                    startDelay = piece.delayTranslateY
                }
            } else null

            val alphaAnimator = if (piece.alpha != piece.toAlpha) {
                ObjectAnimator.ofInt(
                    piece,
                    "alpha",
                    piece.alpha,
                    piece.toAlpha
                ).apply {
                    duration = piece.durationAlpha
                    startDelay = piece.delayAlpha
                }
            } else null

            val rotationAnimator = if (piece.rotation != piece.toRotation) {
                ObjectAnimator.ofInt(
                    piece,
                    "rotation",
                    piece.rotation,
                    piece.toRotation
                ).apply {
                    duration = piece.durationRotation
                    startDelay = piece.delayRotation
                }
            } else null

            animators.addAll(
                listOfNotNull(
                    translateXAnimator,
                    translateYAnimator,
                    alphaAnimator,
                    rotationAnimator
                )
            )
        }

        animatorSet?.apply {
            playTogether(animators)
            //duration = 400
            interpolator =
                CubicBezierInterpolator(
                    0.3,
                    1.0,
                    0.3,
                    1.0
                )
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {
                    isAnimating = true
                }

                override fun onAnimationEnd(p0: Animator) {
                    isAnimating = false
                    val allPiecesCompleted = pieces.all {
                        it.translateX == it.toTranslateX &&
                                it.translateY == it.toTranslateY &&
                                it.alpha == it.toAlpha &&
                                it.rotation == it.toRotation
                    }
                    if (allPiecesCompleted)
                        stopDrawing()
                    animationContinuationRunnable?.run()
                }

                override fun onAnimationCancel(p0: Animator) {

                }

                override fun onAnimationRepeat(p0: Animator) {

                }
            })
            start()
        }
    }


    fun animateIn() {
        pieces.forEach { piece ->
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX = 0
            piece.durationTranslateX = 50
            piece.delayTranslateX =
                ((rows - piece.row) * (0..150).random()).toLong()// (piece.row * (0..150).random()).toLong()

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY = 0
            piece.durationTranslateY = 50
            piece.delayTranslateY =
                ((rows - piece.row) * (0..150).random()).toLong()//(piece.row * (0..150).random()).toLong()

            piece.fromAlpha = piece.alpha
            piece.toAlpha = 180
            piece.durationAlpha = 300
            piece.delayAlpha = (piece.delayTranslateX + piece.durationTranslateX
                    + piece.delayTranslateY + piece.durationTranslateY) / 2

            piece.fromRotation = piece.rotation
            piece.toRotation = 0
            piece.durationRotation = 300
            piece.delayRotation = (piece.delayTranslateX + piece.durationTranslateX
                    + piece.delayTranslateY + piece.durationTranslateY) / 2

            piece.isTranslationComplete = false
            piece.isOpacityComplete = false
            piece.isRotationComplete = false
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun animateOut() {
        val maxDelayX = pieces.maxOfOrNull { it.row } ?: 0
        val maxDelayY = pieces.maxOfOrNull { it.row } ?: 0

        pieces.forEachIndexed { _, piece ->
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX = (-50..50).random()
            piece.durationTranslateX = 200
            piece.delayTranslateX = (maxDelayX - piece.row) * (0..150).random()
                .toLong()

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY = (-800..-200).random()
            piece.durationTranslateY = 200
            piece.delayTranslateY = (maxDelayY - piece.row) * (0..150).random()
                .toLong()
            piece.fromAlpha = piece.alpha
            piece.toAlpha = 0
            piece.durationAlpha = 800
            piece.delayAlpha = piece.delayTranslateY

            piece.fromRotation = piece.rotation
            piece.toRotation = (-90..90).random()
            piece.durationRotation = 800
            piece.delayRotation = piece.delayTranslateY
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun demoAnimateIn() {
        pieces.forEach { piece ->
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX = 0
            piece.delayTranslateX = ((columns - piece.column) * (0..100).random()).toLong()
            piece.durationTranslateX = 300

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY = 0
            piece.delayTranslateY = piece.delayTranslateX
            piece.durationTranslateY = 300

            piece.fromRotation = piece.rotation
            piece.toRotation = 0

            piece.fromAlpha = piece.alpha
            piece.toAlpha = 180
            piece.durationAlpha = 300
            piece.delayAlpha = ((columns - piece.column) * (0..150).random()).toLong()

            piece.isTranslationComplete = false
            piece.isOpacityComplete = false
            piece.isRotationComplete = false
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun demoAnimateOut(x: Float, y: Float) {
        val isTouchInLeftSide = x < width / 2
        val isToucheInMiddleHorizontally = x in (width * 0.3)..(width * 0.7)
        val isToucheInMiddleVertically = y in (height * 0.7)..(height * 0.3)
        val isTouchInTopSide = y < height / 2
        pieces.forEachIndexed { _, piece ->
            piece.fromTranslateX = piece.translateX
            if (isToucheInMiddleHorizontally && isToucheInMiddleVertically)
                piece.toTranslateX =
                    if (isTouchInLeftSide) (20..40).random() else (-40..-20).random()
            else
                piece.toTranslateX =
                    if (isTouchInLeftSide) (200..400).random() else (-400..-200).random()

            piece.durationTranslateX = 400
            piece.delayTranslateX = 0

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY =
                if (!isTouchInTopSide) (-400..-200).random() else (200..400).random()
            //(-400..-200).random()
            piece.durationTranslateY = 400
            piece.delayTranslateY = 0
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        animationContinuationRunnable =
            Runnable { demoAnimateOut2(x, y) }
        startDrawing()
    }

    fun demoAnimateOut2(xTouch: Float, yTouch: Float) {
        val isTouchInLeftSide = xTouch < width / 2
        val isToucheInMiddleHorizontally = xTouch in (width * 0.3)..(width * 0.7)
        val isToucheInMiddleVertically = yTouch in (height * 0.3)..(height * 0.7)
        val isTouchInTopSide = yTouch < height / 2
        pieces.forEachIndexed { _, piece ->
            piece.fromTranslateX = piece.translateX
            if (isToucheInMiddleHorizontally && isToucheInMiddleVertically) {
                val x = (width / 2) - startPosX.toInt()
                piece.toTranslateX = (-pieceWidth * piece.column) + (x - 200..x + 200).random()
            } else
                piece.toTranslateX = if (isTouchInLeftSide) -width else width
            //-pieceWidth * piece.column
            piece.durationTranslateX = 800
            piece.delayTranslateX = 0

            val y = (height / 2) - startPosY.toInt()
            piece.fromTranslateY = piece.translateY
            if (isToucheInMiddleHorizontally && isToucheInMiddleVertically)
                piece.toTranslateY = if (isTouchInTopSide) -height else height
            else
                piece.toTranslateY = (-pieceHeight * piece.row) + (y - 200..y + 200).random()
            piece.durationTranslateY = 800
            piece.delayTranslateY = 0
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }

        animationContinuationRunnable = null
        startDrawing()
    }

    fun demoEntryOut() {
        pieces.forEachIndexed { _, piece ->
            val delay = calculateDelayAccordingOutToCenter(piece.column, piece.row)
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX =
                if (piece.column < columns / 2) (-400..0).random() else (0..400).random()
            piece.delayTranslateX = delay
            piece.durationTranslateX = 800

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY =
                if (piece.row < rows / 2) (-400..0).random() else (0..400).random()
            piece.delayTranslateY = delay
            piece.durationTranslateY = 800

            piece.fromAlpha = piece.alpha
            piece.toAlpha = 0
            piece.delayAlpha = delay
            piece.durationAlpha = 800
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun demoEntryIn() {
        pieces.forEachIndexed { _, piece ->
            val delay = calculateDelayAccordingToCenter(piece.column, piece.row)
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX = 0
            piece.delayTranslateX = delay
            piece.durationTranslateX = 800

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY = 0
            piece.delayTranslateY = delay
            piece.durationTranslateY = 800

            piece.fromAlpha = piece.alpha
            piece.toAlpha = 255
            piece.delayAlpha = delay
            piece.durationAlpha = 800
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun demoSlicesOut(){
        if(columns > 6 ){
            columns = 6
            if(rows > 1) rows = 1
            createPieces()
        }
        pieces.forEachIndexed { _, piece ->
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX = 0

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY = if(piece.column % 2 == 0) height else -height
            piece.durationTranslateY = 1600
            piece.delayTranslateY = 100

        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun demoSlicesIn(){
        if(columns > 6 ){
            columns = 6
            if(rows > 1) rows = 1
            createPieces()
        }
        pieces.forEachIndexed { _, piece ->
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX = 0

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY = 0
            piece.durationTranslateY = 800
            piece.delayTranslateY = 0

            piece.fromAlpha = piece.alpha
            piece.toAlpha = 255
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    private fun calculateDelayAccordingToCenter(column: Int, row: Int): Long {
        val x1 =  width / 2
        val y1 =  height / 2
        val x2 = (startPosX+(pieceWidth * column)) + pieceWidth / 2
        val y2 = (startPosY + (pieceHeight * row)) + pieceHeight / 2
        val dist = Math.sqrt(Math.pow((x2 - x1).toDouble(), 2.0) + Math.pow((y2 - y1).toDouble(), 2.0))
        val maxDist = Math.sqrt(Math.pow((x1).toDouble(), 2.0) + Math.pow((y1).toDouble(), 2.0))
        val maxDelay = 300.0

        return (-1 * maxDelay / maxDist * dist + maxDelay).toLong()
    }

    private fun calculateDelayAccordingOutToCenter(column: Int, row: Int): Long {
        val x1 =  width / 2
        val y1 =  height / 2
        val x2 = (startPosX+(pieceWidth * column)) + pieceWidth / 2
        val y2 = (startPosY + (pieceHeight * row)) + pieceHeight / 2
        val dist = Math.sqrt(Math.pow((x2 - x1).toDouble(), 2.0) + Math.pow((y2 - y1).toDouble(), 2.0))
        val maxDist = Math.sqrt(Math.pow((x1).toDouble(), 2.0) + Math.pow((y1).toDouble(), 2.0))
        val maxDelay = 300.0

        return (maxDelay/maxDist*dist).toLong()
    }

    fun demoAnimateInFromCenter() {
        pieces.forEach { piece ->

            val delay = calculateDelayAccordingOutToCenter(piece.column, piece.row) * 4

            piece.fromRotation = piece.rotation
            piece.toRotation = 0

            piece.fromAlpha = piece.alpha
            piece.toAlpha = 255
            piece.durationAlpha = 1500
            piece.delayAlpha = delay

            piece.isTranslationComplete = false
            piece.isOpacityComplete = false
            piece.isRotationComplete = false
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun demoAnimateOutToCenter() {
        pieces.forEach { piece ->
            val delay = calculateDelayAccordingToCenter(piece.column, piece.row) * 4
            piece.fromRotation = piece.rotation
            piece.toRotation = 0

            piece.fromAlpha = piece.alpha
            piece.toAlpha = 0
            piece.durationAlpha = 1500
            piece.delayAlpha = delay
                //((maxDistanceFromCenter - distanceFromCenter) * maxDelay / maxDistanceFromCenter).toLong()

            piece.isTranslationComplete = false
            piece.isOpacityComplete = false
            piece.isRotationComplete = false
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }
        startDrawing()
    }

    fun demoAnimateOut3() {
        pieces.forEachIndexed { _, piece ->
            piece.fromTranslateX = piece.translateX
            piece.toTranslateX = -pieceWidth * piece.column
            piece.durationTranslateX = 800
            piece.delayTranslateX = 0

            piece.fromTranslateY = piece.translateY
            piece.toTranslateY = -pieceHeight * piece.row
            piece.durationTranslateY = 800
            piece.delayTranslateY = 0
        }
        if (isAnimating) {
            animatorSet?.cancel()
            isAnimating = false
        }

        animationContinuationRunnable = null
        startDrawing()
    }

    fun paused(isPaused: Boolean) {
        if (isPaused) {
            if (isAnimating) animatorSet?.cancel()
            isAnimating = false
            stopDrawing()
        } else {
            animateOut()
        }
    }

    private fun releaseBitmaps() {
        originalImage?.recycle()
        backgroundImageBitmap?.recycle()
        blurredBackgroundImageBitmap?.recycle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clear()
    }

    fun clear() {
        stopDrawing()
        animatorSet?.cancel()
        animatorSet?.removeAllListeners()
        animatorSet = null
        releaseBitmaps()
        pieces.clear()
        surfaceHolder.surface.release()
    }

    companion object {
        @JvmStatic
        fun resizeBitmap(bitmap: Bitmap?, maxWidth: Int, maxHeight: Int): Bitmap {
            if (bitmap == null) {
                throw IllegalArgumentException("Bitmap should not be null.")
            }
            var newWidth = bitmap.width
            var newHeight = bitmap.height
            val aspectRatio: Float = if (newHeight != 0) {
                newWidth.toFloat() / newHeight.toFloat()
            } else {
                0f
            }

            if (newWidth > newHeight) {
                newWidth = maxWidth
                newHeight = if (aspectRatio.isFinite() && aspectRatio > 0) {
                    (newWidth / aspectRatio).toInt()
                } else {
                    maxHeight
                }
            } else {
                newHeight = maxHeight
                newWidth = if (aspectRatio.isFinite() && aspectRatio > 0) {
                    (newHeight * aspectRatio).toInt()
                } else {
                    maxWidth
                }
            }

            if (newWidth <= 0 || newHeight <= 0)
                return bitmap


            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
    }

}