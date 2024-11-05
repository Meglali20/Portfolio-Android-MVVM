package com.oussama.portfolio.ui.activities

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.oussama.portfolio.ui.components.asciiimage.AsciiImage
import com.oussama.portfolio.ui.theme.PortfolioTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*setContentView(R.layout.test_layout)
        val textShufflerView = findViewById<TextShufflerView>(R.id.shufflerView)
        textShufflerView.setOnClickListener {
            textShufflerView.shuffleText(reset = true, withDelay = true)
        }
        textShufflerView.setText(dummyText)
        textShufflerView.shuffleText(reset = true, withDelay = true)*/
        setContent {
            val context = LocalContext.current
            var image by remember { mutableStateOf<Bitmap?>(null) }
            PortfolioTheme(darkTheme = true, true) {
                // A surface container using the 'background' color from the theme
                // R.layout.activity_main
                Box(modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxSize(), contentAlignment = Alignment.Center) {
                    val scrollState = rememberScrollState()
                    var shuffleTextCallback: (reset: Boolean, withDelay: Boolean) -> Unit =
                        { _, _ -> }
                    /*LaunchedEffect(scrollState.value) {
                        shuffleTextCallback(false, true)
                    }
                    LaunchedEffect(key1 = false) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            shuffleTextCallback( true,  true)
                        }, 50)
                    }*/
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            shuffleTextCallback(true, true)
                        }) {
                        /*TextShufflerCompose(
                            modifier = Modifier
                                .fillMaxSize(),
                            typeface = ResourcesCompat.getFont(LocalContext.current, R.font.jet_brains_mono_bold),
                            textSize = 16.spToPx().toFloat(),
                            text = dummyText,
                            shuffleText = { shuffleTextCallback = it }
                        )*/
                        LaunchedEffect(Unit) {
                                image = withContext(Dispatchers.IO) {
                                    Glide.with(context)
                                        .asBitmap()
                                        .load("https://tympanus.net/Development/DeveloperDesignerPageLayout/img/normal.jpg")
                                        .submit().get()
                                }
                        }

                       if(image != null)
                            AsciiImage(image!!)

                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val modifier1 = Modifier.background(
        Color.Red)
        .then(Modifier.padding(5.dp))
        .then(Modifier.wrapContentHeight())
        .then(Modifier.wrapContentWidth())
    Text(
        text = "$name Hello $name!",
        color = Color.White,
        modifier = modifier1
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PortfolioTheme {
        Greeting("Android")
    }
}