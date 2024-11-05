package com.oussama.portfolio.ui.components.asciiimage

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.view.MotionEvent
import android.view.animation.AnticipateOvershootInterpolator
import com.oussama.portfolio.BaseApplication
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

class ImageRenderer(
    private var backgroundColor: Int = Color.BLACK
) :
    GLSurfaceView.Renderer {

    private var isPaused: Boolean = false
    private var square: Square? = null
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private var previousX = 0f
    private var previousY = 0f
    private var currentRotationX = 0f
    private var currentRotationY = 0f
    private var isTouching = false
    private var width = 0
    private var height = 0
    private var mouseX = 0f
    private var mouseY = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glFrontFace(GLES20.GL_CCW)
        GLES20.glCullFace(GLES20.GL_BACK)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val bgColorRGBAFloat = convertColorIntToFloatArray(backgroundColor)
        GLES20.glClearColor(
            bgColorRGBAFloat[0],
            bgColorRGBAFloat[1],
            bgColorRGBAFloat[2],
            bgColorRGBAFloat[3]
        )
    }

    private fun convertColorIntToFloatArray(colorInt: Int): FloatArray {
        val red = ((colorInt shr 16) and 0xFF) / 255.0f
        val green = ((colorInt shr 8) and 0xFF) / 255.0f
        val blue = (colorInt and 0xFF) / 255.0f
        val alpha = ((colorInt shr 24) and 0xFF) / 255.0f

        return floatArrayOf(red, green, blue, alpha)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        this.width = width
        this.height = height
        /*  bitmap = ImagePiecesSurfaceView.resizeBitmap(bitmap, width, height)
          square = Square(bitmap)
          square.setResolution(width.toFloat(), height.toFloat())*/
        val aspectRatio: Float = width.toFloat() / height.toFloat()
        val left = -aspectRatio
        val right = aspectRatio
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.5f
        val far = 17f

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
    }

    fun setBitmap(bitmap: Bitmap) {
        isPaused = true
        square?.destroy()
        square = Square(bitmap)
        square?.setResolution(width.toFloat(), height.toFloat())
        isPaused = false
        Timber.i("Image has been set")
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val normalizedX = event.x / width.toFloat()
        val normalizedY = event.y / height.toFloat()

        mouseX = normalizedX * 2 - 1
        mouseY = (1 - normalizedY * 2)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = event.x
                previousY = event.y
                isTouching = true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isTouching) {
                    val deltaX = event.x - previousX
                    val deltaY = event.y - previousY
                    previousX = event.x
                    previousY = event.y

                    rotateCube(deltaX, deltaY)
                }
            }

            MotionEvent.ACTION_UP -> isTouching = false
        }
        if (square != null)
            square!!.onTouch(event.x, event.y, isTouching)
        return true
    }

    private fun rotateCube(deltaX: Float, deltaY: Float) {
        val scaleFactor = 0.5f

        currentRotationY += deltaX * scaleFactor
        currentRotationX += deltaY * scaleFactor
    }

    override fun onDrawFrame(gl: GL10?) {
        if (isPaused || square == null) return
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 3.0f
        val centerX = 0.0f
        val centerY = 0.0f
        val centerZ = 0.0f
        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, currentRotationY, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, currentRotationX, 1f, 0f, 0f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)



        GLES20.glUseProgram(square!!.program)
        square!!.mvpMatrixHandle = GLES20.glGetUniformLocation(square!!.program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(square!!.mvpMatrixHandle, 1, false, mvpMatrix, 0)

        square!!.draw()

    }

    fun paused(isPaused: Boolean) {
        this.isPaused = isPaused
    }

    companion object {
        @JvmStatic
        fun loadFromAsset(context: Context, fileName: String): String {
            val assetManager = context.assets
            try {
                val inputStream = assetManager.open(fileName)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                do {
                    line = bufferedReader.readLine()
                    if (line != null) {
                        stringBuilder.append(line)
                        stringBuilder.append("\n")
                    }
                } while (line != null)
                bufferedReader.close()
                inputStream.close()
                return stringBuilder.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }
        }
    }
}

class Square(bitmap: Bitmap) {


    var program = 0
    var mvpMatrixHandle = 0
    private var textureHandle = 0
    private var initialAlpha = 0f


    private val squareVertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f, // bottom left
        1.0f, -1.0f, 0.0f,  // bottom right
        -1.0f, 1.0f, 0.0f,  // top left
        1.0f, 1.0f, 0.0f    // top right
    )

    private val textureCoordinates = floatArrayOf(
        0.0f, 1.0f, // bottom left
        1.0f, 1.0f, // bottom right
        0.0f, 0.0f, // top left
        1.0f, 0.0f  // top right
    )

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private var isTouching = false
    private var width = 0f
    private var height = 0f
    private var startRangeX = 0f
    private var endRangeX = width
    private var startRangeY = 0f
    private var endRangeY = 0.2f * height
    private var speedFactorX = 5f
    private var speedFactorY = 5f
    private var xTouchCoordinates = -1f
    private var yTouchCoordinates = -1f
    private var previousEndRangeX = -1f
    private var previousEndRangeY = -1f
    private val maxTouchSize = 250f
    private var touchSize = maxTouchSize / 2 //250f
    private val minRangeSkippingValue = 0.75f
    private val maxRangeSkippingValue = 0.9f
    private var rangeSkippingValue = maxRangeSkippingValue

    init {

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, ImageRenderer.loadFromAsset(BaseApplication.INSTANCE, "shaders/vertex_shader.glsl"))
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, ImageRenderer.loadFromAsset(BaseApplication.INSTANCE, "shaders/fragment_shader.glsl"))

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        val byteBuffer = ByteBuffer.allocateDirect(squareVertices.size * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        vertexBuffer = byteBuffer.asFloatBuffer()
        vertexBuffer.put(squareVertices)
        vertexBuffer.position(0)

        val textureCordsByteBuffer = ByteBuffer.allocateDirect(textureCoordinates.size * 4)
        textureCordsByteBuffer.order(ByteOrder.nativeOrder())
        textureBuffer = textureCordsByteBuffer.asFloatBuffer()
        textureBuffer.put(textureCoordinates)
        textureBuffer.position(0)

        textureHandle = loadTexture(bitmap)
    }


    private fun loadTexture(bitmap: Bitmap): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        if (textureIds[0] == 0) {
            return 0
        }


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()

        return textureIds[0]
    }

    fun setResolution(resolutionX: Float, resolutionY: Float) {
        width = resolutionX
        height = resolutionY
        startRangeX = -touchSize
        endRangeX = touchSize
        startRangeY = 0f // 15% of the height for start range in Y-axis
        endRangeY = touchSize * 2 // 0.15f * height // 30% of the height for end range in Y-axis
    }

    fun onTouch(x: Float, y: Float, isTouching: Boolean) {
        this.isTouching = isTouching
        if (isTouching) {
            if (previousEndRangeX == -1f || previousEndRangeY == -1f) {
                previousEndRangeX = endRangeX
                previousEndRangeY = endRangeY
                animateBubble(x, y)
            } else {
                startRangeX = x - touchSize
                endRangeX = x + touchSize
                startRangeY = y - touchSize * 2
                endRangeY = y
            }
        } else {
            narrowBubble(x, y)
            previousEndRangeX = -1f
            previousEndRangeY = -1f
        }
        xTouchCoordinates = if (isTouching) x else -1f
        yTouchCoordinates = if (isTouching) y else -1f
    }

    private fun animateBubble(toX: Float, toY: Float) {
        val animatorSet = AnimatorSet()
        val translateXAnimator = ValueAnimator.ofFloat(endRangeX - touchSize, toX)
        translateXAnimator.addUpdateListener {
            startRangeX = it.animatedValue as Float - touchSize
            endRangeX = it.animatedValue as Float + touchSize
        }
        val translateYAnimator = ValueAnimator.ofFloat(endRangeY, toY)
        translateYAnimator.addUpdateListener {
            startRangeY = (it.animatedValue as Float) - touchSize * 2
            endRangeY = it.animatedValue as Float
        }
        val touchSizeAnimator = ValueAnimator.ofFloat(touchSize, maxTouchSize)
        touchSizeAnimator.addUpdateListener {
            touchSize = it.animatedValue as Float
        }

        val rangeSkippingValueAnimator =
            ValueAnimator.ofFloat(rangeSkippingValue, minRangeSkippingValue)
        rangeSkippingValueAnimator.addUpdateListener {
            rangeSkippingValue = it.animatedValue as Float
        }
        val animators = mutableListOf<Animator>()
        animators.add(translateXAnimator)
        animators.add(translateYAnimator)
        animators.add(touchSizeAnimator)
        animators.add(rangeSkippingValueAnimator)
        animatorSet.apply {
            interpolator = AnticipateOvershootInterpolator()
            playTogether(animators)
            duration = 600
            start()
        }
    }

    private fun narrowBubble(x: Float, y: Float) {
        val animatorSet = AnimatorSet()
        val touchAreaAnimator = ValueAnimator.ofFloat(touchSize, maxTouchSize / 2)
        touchAreaAnimator.addUpdateListener {
            touchSize = it.animatedValue as Float
            startRangeX = x - touchSize
            endRangeX = x + touchSize
            startRangeY = y - touchSize * 2
            endRangeY = y
        }
        val rangeSkippingValueAnimator =
            ValueAnimator.ofFloat(rangeSkippingValue, maxRangeSkippingValue)
        rangeSkippingValueAnimator.addUpdateListener {
            rangeSkippingValue = it.animatedValue as Float
        }
        val animators = mutableListOf<Animator>()
        animators.add(touchAreaAnimator)
        animators.add(rangeSkippingValueAnimator)
        animatorSet.apply {
            interpolator = AnticipateOvershootInterpolator()
            playTogether(animators)
            duration = 600
            start()
        }
    }


    fun draw() {
        GLES20.glVertexAttribPointer(
            0, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer
        )
        GLES20.glEnableVertexAttribArray(0)

        GLES20.glVertexAttribPointer(
            1, 2, GLES20.GL_FLOAT, false, 0, textureBuffer
        )
        GLES20.glEnableVertexAttribArray(1)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)


        val initialAlphaHandle = GLES20.glGetUniformLocation(program, "initialAlpha")
        GLES20.glUniform1f(initialAlphaHandle, initialAlpha)

        if (initialAlpha < 1f)
            initialAlpha += 0.005f


        val resolutionHandle = GLES20.glGetUniformLocation(program, "u_resolution")
        GLES20.glUniform2f(resolutionHandle, width, height)

        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "u_texture0"), 0)

        val animatedXHandle = GLES20.glGetUniformLocation(program, "startRangeX")
        GLES20.glUniform1f(animatedXHandle, startRangeX)

        val animatedYHandle = GLES20.glGetUniformLocation(program, "startRangeY")
        GLES20.glUniform1f(animatedYHandle, startRangeY)

        val endRangeXHandle = GLES20.glGetUniformLocation(program, "endRangeX")
        GLES20.glUniform1f(endRangeXHandle, endRangeX)

        val endRangeYHandle = GLES20.glGetUniformLocation(program, "endRangeY")
        GLES20.glUniform1f(endRangeYHandle, endRangeY)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)


        val rangeSkippingValueHandle = GLES20.glGetUniformLocation(program, "rangeSkippingValue")
        GLES20.glUniform1f(rangeSkippingValueHandle, rangeSkippingValue)

        val isTouchingHandle = GLES20.glGetUniformLocation(program, "isTouching")
        if (!isTouching) {
            if ((endRangeX >= width || startRangeX <= 0)) {
                speedFactorX = if (endRangeX >= width) abs(speedFactorX) * -1 else abs(speedFactorX)
            }
            if ((endRangeY >= height || startRangeY <= 0)) {
                speedFactorY =
                    if (endRangeY >= height) abs(speedFactorY) * -1 else abs(speedFactorY)
            }

            startRangeX += speedFactorX
            endRangeX += speedFactorX
            startRangeY += speedFactorY
            endRangeY += speedFactorY
            //GLES20.glUniform1f(rangeSkippingValueHandle, 0.95f)
            GLES20.glUniform1i(isTouchingHandle, 0)
        } else {
            //GLES20.glUniform1f(rangeSkippingValueHandle, 0.8f)
            GLES20.glUniform1i(isTouchingHandle, 1)
        }

        GLES20.glDisableVertexAttribArray(0)
        GLES20.glDisableVertexAttribArray(1)
    }

    private fun loadShader(type: Int, shaderSrc: String): Int {
        val compiled = IntArray(1)
        val shader: Int = GLES20.glCreateShader(type)
        if (shader == 0) {
            return 0
        }
        GLES20.glShaderSource(shader, shaderSrc)
        GLES20.glCompileShader(shader)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            throw RuntimeException(
                "Could not compile program: "
                        + GLES20.glGetShaderInfoLog(shader) + " " + shaderSrc
            )
        }
        return shader
    }

    fun destroy() {
        GLES20.glDeleteProgram(program)
    }


}
