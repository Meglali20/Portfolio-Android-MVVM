package com.oussama.portfolio.ui.components.wavestextview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.R
import com.oussama.portfolio.ui.components.drawables.ScanLinesDrawable
import com.oussama.portfolio.ui.components.imagepiecesview.ImagePiecesSurfaceView

class WavesSurfaceView : SurfaceView,
    SurfaceHolder.Callback, Runnable {

    private var surfaceHolder: SurfaceHolder = holder
    private var drawingThread: Thread? = null
    private var isRunning = false
    private var linesText: List<String> = ArrayList()
    private var bannerTextWithReturnToLine: List<String> = ArrayList()
    private var backgroundTextArray: MutableList<LineTextElement> = ArrayList()
    private var bannerTextArray: MutableList<LineTextElement> = ArrayList()
    private var letterWidth: Float = 0f
    private var positionX: Int = 0
    private var positionY: Int = 0
    private var animationStartTime: Double = 0.0
    private var backgroundTextAnimationStartTime: Double = 0.0
    private var lineCount: Int = 0
    private var bannerTextColor: Int = 0
    private var surfaceColor: Int = 0

    private val backgroundTextPaint: Paint = Paint()
    private val bannerPaint: Paint = Paint()
    private lateinit var colors: List<Int>

    private var scanLinesBitmap: Bitmap? = null
    private var filledElements = false

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.WavesSurfaceView, 0, 0)
        bannerTextColor = attributes.getColor(
            R.styleable.WavesSurfaceView_bannerTextColor,
            android.graphics.Color.WHITE
        )
        surfaceColor = attributes.getColor(
            R.styleable.WavesSurfaceView_backgroundColor,
            android.graphics.Color.WHITE
        )

        attributes.recycle()
        surfaceHolder.addCallback(this)
        //setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        initPaint()
    }

    private fun initPaint() {
        /*colors = listOf(
            Color(0xFF61dca3).toArgb(),
            bannerTextColor,
            Color(0xFF61b3dc).toArgb(),
            Color(0xFF2b4539).toArgb()
        )*/
        val defaultCombination = BaseApplication.INSTANCE.colorCombination
        colors = listOf(
            defaultCombination[0],
            bannerTextColor,
            defaultCombination[1],
            defaultCombination[2]
        )
        backgroundTextPaint.apply {
            color = colors.random()
            textSize = 25f
            typeface = ResourcesCompat.getFont(context, R.font.jet_brains_mono_regular)
        }
        bannerPaint.apply {
            color = bannerTextColor
            textSize = 25f
            typeface = ResourcesCompat.getFont(context, R.font.jet_brains_mono_bold)
        }
    }

    override fun surfaceCreated(p0: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if(!filledElements) {
            splitBackgroundText()
            createTextElements()
            val drawable = ScanLinesDrawable()
            scanLinesBitmap = ImagePiecesSurfaceView.resizeBitmap(
                drawable.toBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
                ), width, height
            )
            filledElements = true
        }
        startDrawing()
    }

    private fun createTextElements() {
        backgroundTextArray.clear()
        bannerTextArray.clear()
        val t = (height) / lineCount
        val textToMeasure = "W".repeat(1000)
        val rect = Rect()
        backgroundTextPaint.getTextBounds(textToMeasure, 0, textToMeasure.length, rect)
        val textWidth = backgroundTextPaint.measureText(textToMeasure)//rect.width()
        val r = textWidth / 1000
        letterWidth = ((width - 1) / r)
        for (n in 0 until lineCount) {
            val lineTextElement = LineTextElement("", 5f, n * t + 5f, colors.random())
            backgroundTextArray.add(lineTextElement)
        }
        //backgroundLogoTextArray[0].text = "".padStart(1000, 'W')
        //val r = width / 100
        //backgroundLogoTextArray[0].text = ""
        //Log.e("TAG", "HEIGHT IS $height TEXT ${backgroundLogoTextArray[0].text}")
        // letterWidth = ((width - 1) / r)
        positionX = ((letterWidth * 0.5 - bannerTextWithReturnToLine[0].length / 2) - 1).toInt()
        positionY = (((lineCount * 0.6 - bannerTextWithReturnToLine.size / 2) - 1).toInt())
        for (s in bannerTextWithReturnToLine.indices) {
            /*val paint = Paint().apply {
                color = Color.WHITE
                textSize = 0.9f * t
                textAlign = Paint.Align.LEFT
                fontWeight = Typeface.BOLD // If supported, or adjust for bold
            }*/
            val lineTextElement = LineTextElement(
                "", (5 + (positionX + 1) * r).toFloat(), (s * t + 5 + (positionY + 1) * t).toFloat()
            )
            bannerTextArray.add(lineTextElement)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        drawingThread?.join()
    }


    override fun run() {
        while (isRunning) {
            val canvas = surfaceHolder.lockCanvas() ?: continue
                val currentTimeStamp = System.currentTimeMillis()
                if (animationStartTime == 0.0)
                    animationStartTime = 0.001 * currentTimeStamp

                if (backgroundTextAnimationStartTime == 0.0)
                    backgroundTextAnimationStartTime = 0.001 * currentTimeStamp

                val bannerAnimationDuration = 0.001 * currentTimeStamp - animationStartTime
                val backgroundTextAnimationDuration =
                    0.001 * currentTimeStamp - backgroundTextAnimationStartTime

                val animationDuration1 = run {
                    val e = clampValue((0.5 * (bannerAnimationDuration - 1)).toFloat(), 0f, 1f)
                    if (e < 0.5) {
                        (1 - Math.sqrt(1 - Math.pow(2.0 * e, 2.0))) / 2
                    } else {
                        (Math.sqrt(1 - Math.pow(-2.0 * e + 2, 2.0)) + 1) / 2
                    }
                }

                for (row in 0 until backgroundTextArray.size) {
                    var currentLine = ""
                    var resultLine = ""
                    val verticalPosition = 1 - (2 * row) / backgroundTextArray.size.toDouble()
                    for (col in 0 until letterWidth.toInt()) {
                        val horizontalPosition = (2 * col) / letterWidth.toDouble() - 1
                        val distance =
                            Math.sqrt(horizontalPosition * horizontalPosition + verticalPosition * verticalPosition)
                        val backgroundTextSpeed = 0.1 * backgroundTextAnimationDuration
                        val linearInterpolation = backgroundTextSpeed / Math.max(0.1, distance)
                        val sinValue = Math.sin(linearInterpolation)
                        val cosValue = Math.cos(linearInterpolation)
                        val rotatedHorizontal =
                            horizontalPosition * sinValue - verticalPosition * cosValue
                        val mappedColumn: Int =
                            ((horizontalPosition * cosValue + verticalPosition * sinValue + 1) / 2 * letterWidth).toInt()
                        val mappedRow: Int =
                            ((rotatedHorizontal + 1) / 2 * linesText.size % linesText.size).toInt()

                        // Handle characters for display based on position and boundaries
                        var characterToDisplay =
                            if (mappedColumn < 0 || mappedColumn >= letterWidth || mappedRow < 0 || mappedRow >= backgroundTextArray.size) {
                                " "
                            } else {
                                linesText.getOrElse(mappedRow) { "" }.getOrElse(mappedColumn) { ' ' }
                            }

                        if (row in (positionY + 1) until (positionY + bannerTextWithReturnToLine.size + 1) &&
                            col in (positionX + 1) until (positionX + bannerTextWithReturnToLine[0].length + 1)
                        ) {
                            val offsetColumn = col - positionX - 1
                            val offsetRow = row - positionY - 1
                            val mainLogoCharacter =
                                bannerTextWithReturnToLine.getOrNull(offsetRow)?.getOrNull(offsetColumn)
                                    ?: characterToDisplay
                            val hasLeftNeighbor = bannerTextWithReturnToLine.getOrNull(offsetRow)
                                ?.getOrNull(offsetColumn - 1)?.toString() != " "
                            val hasRightNeighbor = bannerTextWithReturnToLine.getOrNull(offsetRow)
                                ?.getOrNull(offsetColumn + 1)?.toString() != " "

                            if (mainLogoCharacter != " " || hasLeftNeighbor || hasRightNeighbor) {
                                val startCharCode = characterToDisplay.toString().first().code
                                val endCharCode = mainLogoCharacter.toString().first().code
                                resultLine += String(
                                    Character.toChars(
                                        Math.round(
                                            interpolateCharacters(
                                                startCharCode,
                                                endCharCode,
                                                animationDuration1.toFloat()
                                            )
                                        )
                                    )
                                )
                                if (animationDuration1 == 1.0) {
                                    characterToDisplay = " "
                                }
                            } else {
                                resultLine += " "
                            }

                            if (col == positionX + bannerTextWithReturnToLine[0].length) {
                                bannerTextArray.getOrNull(offsetRow)?.text = resultLine
                            }
                        }
                        currentLine += characterToDisplay
                    }
                    backgroundTextArray.getOrNull(row)?.text = currentLine
                }
                //canvas.drawColor(bannerTextColor, PorterDuff.Mode.CLEAR)
                canvas.drawColor(surfaceColor)
                bannerTextArray.forEach { line ->
                    canvas.drawText(line.text, line.x, line.y, bannerPaint)
                }
                backgroundTextArray.forEach { line ->
                    backgroundTextPaint.color = line.color
                    canvas.drawText(line.text, line.x, line.y, backgroundTextPaint)
                }
                if (scanLinesBitmap != null)
                    canvas.drawBitmap(scanLinesBitmap!!, 0f, 0f, null)

            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun splitBannerText(): Pair<List<String>, List<String>> {
        val logo = """
   ____   _    _   _____  _____           __  __           
  / __ \ | |  | | / ____|/ ____|   /\    |  \/  |    /\    
 | |  | || |  | || (___ | (___    /  \   | \  / |   /  \   
 | |  | || |  | | \___ \ \___ \  / /\ \  | |\/| |  / /\ \  
 | |__| || |__| | ____) |____) |/ ____ \ | |  | | / ____ \ 
  \____/  \____/ |_____/|_____//_/    \_\|_|  |_|/_/    \_\
  __  __  ______   _____  _                 _       _____   
 |  \/  ||  ____| / ____|| |         /\    | |     |_   _|  
 | \  / || |__   | |  __ | |        /  \   | |       | |    
 | |\/| ||  __|  | | |_ || |       / /\ \  | |       | |    
 | |  | || |____ | |__| || |____  / ____ \ | |____  _| |_   
 |_|  |_||______| \_____||______|/_/    \_\|______||_____|  
    """.trimIndent()

        /*val logo = """
              __  __ _    _  _                            
             |  \/  (_)__| |(_)___ _  _ _ _ _ _  ___ _  _ 
             | |\/| | / _` || / _ \ || | '_| ' \/ -_) || |
             |_|  |_|_\__,_|/ \___/\_,_|_| |_||_\___|\_, |
                          |__/                       |__/ 
        """.trimIndent()*/

        val horizontalLine = logo.split("\n").map { it.replace("\t", "    ") }
        val verticalLine = horizontalLine.toList()
        //if (verticalLine.isEmpty()) {
        // verticalLine = horizontalLine.toList()
        // }
        return horizontalLine to verticalLine
    }

    private fun splitBackgroundText() {
        linesText = """MainActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
           // Fragment management with support library
            MainFragment mainFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (mainFragment == null) {
                mainFragment = new MainFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mainFragment)
                        .commit();
            }
           // perform terminal prompts with Docker and other tech commands
           docker build -t myapp:latest .
           docker run -d -p 8080:80 myapp
           kubectl apply -f deployment.yaml
           npm install -g @vue/cli
           git clone https://github.com/myapp.git
           composer install
           // Programming languages and syntax
            int[] numbers = {1, 2, 3, 4, 5};
            for (int number : numbers) {
                System.out.println("Number: " + number);
            }
           let greeting = "Hello, World!";
            console.log(greeting);
           function addNumbers(a, b) {
                return a + b;
            }
            let result = addNumbers(5, 3);
           # Python syntax
            def multiply(a, b):
                return a * b
           result = multiply(4, 7)
           // Web development and frameworks
            import Vue from 'vue'
            const app = new Vue({
                el: '#app',
                data: {
                    message: 'Hello, Vue!'
                }
            });
           // Photoshop skills and image manipulation
            const image = loadImage('image.jpg');
            applyFilter(image, 'sepia');
            exportImage(image, 'output.jpg');
        }
    }""".trimIndent().split("\n").map { it.replace("\t", "    ") }

        bannerTextWithReturnToLine = splitBannerText().second

        lineCount = linesText.size
    }

    private fun interpolateCharacters(startChar: Int, endChar: Int, t: Float): Float {
        return startChar.toFloat() * (1 - t) + endChar.toFloat() * t
    }

    private fun clampValue(e: Float, a: Float, t: Float): Float {
        return if (e < a) a else if (e > t) t else e
    }

    private fun startDrawing() {
        isRunning = true
        if(drawingThread != null){
            drawingThread?.join()
        }
        drawingThread = Thread(this)
        drawingThread?.start()
    }

    private fun stopDrawing() {
        isRunning = false
        drawingThread?.join()
    }

    fun paused(isPaused: Boolean) {
        if (isPaused)
            isRunning = false
        else {
           startDrawing()
        }
    }

    private fun resetBanner() {
        animationStartTime = 0.0
    }

    private fun resetBackgroundText() {
        backgroundTextAnimationStartTime = 0.0
    }

    fun reset() {
        stopDrawing()
        resetBanner()
        resetBackgroundText()
        //splitBackgroundText()
        startDrawing()
    }
}