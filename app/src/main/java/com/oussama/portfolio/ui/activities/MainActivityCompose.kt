package com.oussama.portfolio.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oussama.portfolio.R
import com.oussama.portfolio.ui.theme.PortfolioTheme

class MainActivityCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PortfolioTheme (darkTheme = true, true){
                // A surface container using the 'background' color from the theme
                R.layout.activity_main
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