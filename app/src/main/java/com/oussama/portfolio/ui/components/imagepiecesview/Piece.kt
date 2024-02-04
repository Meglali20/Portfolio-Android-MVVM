package com.oussama.portfolio.ui.components.imagepiecesview

import android.graphics.Bitmap

class Piece(val row: Int, val column: Int, var image: Bitmap?) {
    var positionX: Int = 0
    var positionY: Int = 0
    var translateX: Int = 0
    var translateY: Int = 0
    var delayTranslateX: Long = 0
    var durationTranslateX: Long = 400
    var fromTranslateX: Int = 0
    var fromTranslateY: Int = 0
    var toTranslateX: Int = 0
    var toTranslateY: Int = 0
    var delayTranslateY: Long = 0
    var durationTranslateY: Long = 400
    var rotation: Int = 0
    var fromRotation: Int = 0
    var toRotation: Int = 0
    var delayRotation: Long = 0
    var durationRotation: Long = 300
    var alpha: Int = 0
    var fromAlpha: Int = 0
    var toAlpha: Int = 0
    var delayAlpha: Long = 0
    var durationAlpha: Long = 300
    var isTranslationComplete = false
    var isOpacityComplete = false
    var isRotationComplete = false
}

