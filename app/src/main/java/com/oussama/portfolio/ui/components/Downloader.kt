package com.oussama.portfolio.ui.components

interface Downloader {
    fun downloadFile(url: String): Long
}