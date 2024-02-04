package com.oussama.portfolio.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.oussama.portfolio.BaseApplication


class Network : NetworkConnectivity {
    override fun getNetworkInfo(): NetworkCapabilities? {
        val connectivityManager =
            BaseApplication.INSTANCE.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        return connectivityManager.getNetworkCapabilities(activeNetwork)
    }

    override fun isConnected(): Boolean {
        val networkCapabilities = getNetworkInfo()
        return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}

interface NetworkConnectivity {
    fun getNetworkInfo(): NetworkCapabilities?
    fun isConnected(): Boolean
}