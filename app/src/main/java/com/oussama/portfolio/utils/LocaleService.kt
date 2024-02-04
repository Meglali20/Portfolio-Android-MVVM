package com.oussama.portfolio.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleService {

    fun updateBaseContextLocale(context: Context): Context {
       // val language = getLanguageFromPreferences(context)
        val locale = Locale("fr")
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResourcesLocale(context, locale)
            return updateResourcesLocaleLegacy(context, locale)
        }
        return updateResourcesLocaleLegacy(context, locale)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResourcesLocale(
        context: Context,
        locale: Locale
    ): Context? {
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
       // updateAppTheme(context)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLocaleLegacy(
        context: Context,
        locale: Locale
    ): Context {
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
        //updateAppTheme(context)
        return context
    }
}