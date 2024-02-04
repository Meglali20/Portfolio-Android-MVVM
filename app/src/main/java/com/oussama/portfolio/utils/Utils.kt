package com.oussama.portfolio.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.RectF
import android.util.TypedValue


const val THEME_DATASTORE_KEY = "name"
const val LOCALE_DATASTORE_KEY = "lang"
const val COLORSCHEME_DATASTORE_KEY = "color_scheme"
const val COLORSCHEME_DYNAMIC = -1
const val COLORSCHEME_RANDOM = 0
const val BASE_URL = "https://portfolio-3d6d7-default-rtdb.europe-west1.firebasedatabase.app/"
val lettersAndSymbols = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '!', '@', '#', '$', '&', '*', '(', ')', '-', '_', '+', '=', '/', '[', ']', '{', '}', ';', ':', '<', '>', ',', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
const val dummyText = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."

class Utils {
    companion object {

        @JvmStatic
        fun isNightMode(context: Context): Boolean{
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightMode == Configuration.UI_MODE_NIGHT_YES
        }

        @JvmStatic
        fun lerp(a: RectF, b: RectF, f: Float, to: RectF?) {
            to?.set(
                lerp(a.left, b.left, f),
                lerp(a.top, b.top, f),
                lerp(a.right, b.right, f),
                lerp(a.bottom, b.bottom, f)
            )
        }

        @JvmStatic
        fun lerp(a: Float, b: Float, f: Float): Float {
            return a + f * (b - a)
        }

        fun lerp(a: Int, b: Int, f: Float): Int {
            return (a + f * (b - a)).toInt()
        }

        fun Float.dpToPx(): Float {
            return this * Resources.getSystem().displayMetrics.density
        }

        fun Int.dpToPx(): Int {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun Float.spToPx(): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                this,
                Resources.getSystem().displayMetrics
            )
        }

        @JvmStatic
        fun Int.spToPx(): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                this.toFloat(),
                Resources.getSystem().displayMetrics
            ).toInt()
        }
    }

}