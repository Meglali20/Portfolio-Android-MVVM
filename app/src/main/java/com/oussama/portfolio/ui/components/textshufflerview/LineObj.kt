package com.oussama.portfolio.ui.components.textshufflerview

data class LineObj(
    var linePosition: Int = -0,
    var currentIteration: Int = 0,
    var previousIteration: Int = 0,
    var lastTimeAnimationPlayed: Long = 0,
    var isVisible: Boolean = false,
    var isAnimationFinished: Boolean = false,
    var cellsList: List<CellObj> = ArrayList(),
    var animationStartTime: Long  = -1L
)