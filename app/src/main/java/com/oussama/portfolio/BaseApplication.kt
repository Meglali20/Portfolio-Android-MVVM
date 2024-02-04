package com.oussama.portfolio

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.google.android.material.color.DynamicColors
import com.oussama.portfolio.data.DataStoreRepository
import com.oussama.portfolio.utils.COLORSCHEME_DATASTORE_KEY
import com.oussama.portfolio.utils.COLORSCHEME_RANDOM
import com.oussama.portfolio.utils.LOCALE_DATASTORE_KEY
import com.oussama.portfolio.utils.THEME_DATASTORE_KEY
import com.oussama.portfolio.utils.Utils
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application() {

    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    var colorCombination: List<Int> = pickRandomColorCombination()
    var configChanged = false

    override fun onCreate() {
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        super.onCreate()
        INSTANCE = this
        runBlocking {
            applyAppConfig()
        }
    }

    private suspend fun applyAppConfig() {
        val savedTheme = dataStoreRepository.getInt(THEME_DATASTORE_KEY)
        val savedLocale = dataStoreRepository.getString(LOCALE_DATASTORE_KEY)
        val savedColorScheme = dataStoreRepository.getInt(COLORSCHEME_DATASTORE_KEY)
        Timber.e("Default theme: $savedTheme locale: $savedLocale color scheme $savedColorScheme")
        if (savedTheme == null)
            dataStoreRepository.putInt(
                THEME_DATASTORE_KEY,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        else
            applyTheme(savedTheme)
        if (savedLocale == null) {
            val locale2Save = when (Locale.getDefault().displayLanguage) {
                "fr" -> Locale.FRANCE.language
                else -> Locale.UK.language
            }
            dataStoreRepository.putString(
                LOCALE_DATASTORE_KEY,
                locale2Save
            )
        } /*else
            applyLocale(savedLocale)*/

        if (savedColorScheme == null)
            dataStoreRepository.putInt(COLORSCHEME_DATASTORE_KEY, COLORSCHEME_RANDOM)

    }

    private fun applyTheme(theme: Int) {
        AppCompatDelegate.setDefaultNightMode(theme)
    }

    private fun pickRandomColorCombination(): List<Int> {
        val colorCombinations = listOf(
            listOf(Color(0xFF61DCA3), Color(0xFF2B4539), Color(0xFF61B3DC)),
            listOf(Color(0xFF63DC82), Color(0xFF4284F4), Color(0xFFD1EEFE)),
            listOf(Color(0xFFFF8333), Color(0xFF635DFF), Color(0xFF63DC82)),
            listOf(Color(0xFFF8A488), Color(0xFFE88463), Color(0xFF624C5B)),
            listOf(Color(0xFF62A2AC), Color(0xFF404E7C), Color(0xFFE94B3C)),
            listOf(Color(0xFFFF6B35), Color(0xFF181E24), Color(0xFF6D83A3)),
            listOf(Color(0xFFDB6A58), Color(0xFFEBD43F), Color(0xFF6B4226)),
            listOf(Color(0xFF8D230F), Color(0xFF480F0C), Color(0xFF8697B8)),
            listOf(Color(0xFF004E89), Color(0xFFF5B700), Color(0xFF5A5755)),
            listOf(Color(0xFFC2D4DB), Color(0xFF134074), Color(0xFFFFDB58)),
            listOf(Color(0xFF8564D8), Color(0xFF6C757D), Color(0xFFBFC0C0)),
            listOf(Color(0xFF99B898), Color(0xFF3D405B), Color(0xFFFFE66D)),
            listOf(Color(0xFFFDCB3D), Color(0xFF172A3A), Color(0xFFD9BF77)),
            listOf(Color(0xFFFC766A), Color(0xFF5B84B1), Color(0xFFFDCB3D)),
            listOf(Color(0xFF5C74E8), Color(0xFF0F2C91), Color(0xFFD1D9F8)),
            listOf(Color(0xFFE67A0E), Color(0xFF303841), Color(0xFF95A3B3))
        )
        val chosenCombination = colorCombinations.random()
        return listOf(
            chosenCombination[0].toArgb(),
            chosenCombination[1].toArgb(),
            chosenCombination[2].toArgb()
        )
    }

    @SuppressLint("ResourceType")
    fun updateColorCombination(theme: Resources.Theme, styleResId: Int) {
        val typedArray = theme.obtainStyledAttributes(
            styleResId,
            intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryInverse, R.attr.colorTertiary)
        )
        val colorPrimary = typedArray.getColor(0, 0)
        val colorSecondary = typedArray.getColor(1, 0)
        val colorTertiary = typedArray.getColor(2, 0)
        typedArray.recycle()
        colorCombination = listOf(colorPrimary, colorSecondary, colorTertiary)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun updateToDynamicColorCombination() {
        val isNightMode = Utils.isNightMode(this)
        val colorPrimary =
            ContextCompat.getColor(
                this,
                if (isNightMode) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
            )
        val colorSecondary =
            ContextCompat.getColor(
                this,
                if (isNightMode) android.R.color.system_accent2_200 else android.R.color.system_accent2_600
            )
        val colorTertiary =
            ContextCompat.getColor(
                this,
                if (isNightMode) android.R.color.system_accent3_200 else android.R.color.system_accent3_600
            )
        colorCombination = listOf(colorPrimary, colorSecondary, colorTertiary)
    }

    companion object {
        lateinit var INSTANCE: BaseApplication
    }
}