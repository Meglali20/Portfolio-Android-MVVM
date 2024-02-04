package com.oussama.portfolio.ui.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator


/**
 * code from https://gist.github.com/vadiole/d5fcc589b3ce170df84fcb1c1e1c4104
 */
@SuppressLint("ClickableViewAccessibility")
object ScalePressEffect : OnTouchListener {
    private const val pressScaleDuration = 150L
    private const val releaseScaleDuration = 100L
    private const val delayBeforeRelease = 50L
    private const val scaleReleased = 1f
    private const val scalePressed = 0.95f
    private val pressInterpolator = AccelerateDecelerateInterpolator()
    private val releaseInterpolator = AccelerateDecelerateInterpolator()

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                v.animate().cancel()
                getPressAnimation(v).start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // A little hack to give a time for press animation to finish
                if (v.animate().interpolator == pressInterpolator) {
                    v.animate()
                        .setDuration(delayBeforeRelease)
                        .withEndAction {
                            getReleaseAnimation(v).start()
                        }
                        .start()
                } else {
                    getReleaseAnimation(v).start()
                }
            }
        }
        return false
    }

    private fun getReleaseAnimation(v: View): ViewPropertyAnimator {
        return v.animate()
            .setStartDelay(0)
            .setInterpolator(releaseInterpolator)
            .scaleX(scaleReleased)
            .scaleY(scaleReleased)
            .setDuration(releaseScaleDuration)
            .withLayer()
    }

    private fun getPressAnimation(view: View): ViewPropertyAnimator {
        return view.animate()
            .setStartDelay(0)
            .setInterpolator(pressInterpolator)
            .scaleX(scalePressed)
            .scaleY(scalePressed)
            .setDuration(pressScaleDuration)
            .withLayer()
            .withEndAction {
                view.animate().interpolator = releaseInterpolator
            }
    }
}