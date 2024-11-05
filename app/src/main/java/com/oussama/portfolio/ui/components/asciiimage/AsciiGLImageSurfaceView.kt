package com.oussama.portfolio.ui.components.asciiimage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.oussama.portfolio.R
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

@SuppressLint("ClickableViewAccessibility")
class AsciiGLImageSurfaceView : GLSurfaceView {
    private val imageRenderer: ImageRenderer

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        setEGLContextClientVersion(3)
        setEGLContextFactory(ContextFactory())
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        //holder.setFormat(PixelFormat.TRANSLUCENT);
        //setZOrderOnTop(true)
        //setZOrderMediaOverlay(true)
        //setBackgroundColor(Color.TRANSPARENT);

        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.GLImageSurfaceView, 0, 0)
        val backgroundColor = attributes.getColor(
            R.styleable.GLImageSurfaceView_backgroundColor,
            Color.WHITE
        )

        attributes.recycle()


        imageRenderer = ImageRenderer(backgroundColor)
        setRenderer(imageRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            imageRenderer.onTouchEvent(event)
        }

    }

    fun paused(isPaused: Boolean) {
        imageRenderer.paused(isPaused)
    }

    fun setBitmapImage(bitmap: Bitmap){
        queueEvent {
            imageRenderer.setBitmap(bitmap)
            //setZOrderOnTop(false)
        }
    }


    private class ContextFactory : EGLContextFactory {
        override fun createContext(
            egl: EGL10,
            display: EGLDisplay?,
            eglConfig: EGLConfig?
        ): EGLContext {
            val attrib_list = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
            return egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list)
        }

        override fun destroyContext(egl: EGL10, display: EGLDisplay?, context: EGLContext?) {
            egl.eglDestroyContext(display, context)
        }

        companion object {
            private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
        }
    }

}