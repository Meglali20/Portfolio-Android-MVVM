package com.oussama.portfolio.data

import android.content.Context
import com.oussama.portfolio.R

object DataError {
    @JvmStatic
    fun getErrorMessage(context: Context, code: Int): String {
        return when (code) {
            ERROR_NO_INTERNET_CONNECTION -> context.getString(R.string.errorNoInternet)
            ERROR_NETWORK -> context.getString(R.string.errorNetwork)
            ERROR_NOT_FOUND -> context.getString(R.string.errorNotFound)
            ERROR_DEFAULT -> context.getString(R.string.errorDefault)
            else -> context.getString(R.string.errorDefault)
        }
    }
}

const val ERROR_NO_INTERNET_CONNECTION = -1
const val ERROR_NETWORK = -2
const val ERROR_DEFAULT = -3
const val ERROR_NOT_FOUND = -4