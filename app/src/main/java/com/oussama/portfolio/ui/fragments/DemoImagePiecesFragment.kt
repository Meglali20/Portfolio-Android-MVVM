package com.oussama.portfolio.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.oussama.portfolio.databinding.FragmentDemoImagePiecesBinding
import com.oussama.portfolio.utils.BitmapUtils
import com.oussama.portfolio.utils.Utils.Companion.dpToPx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DemoImagePiecesFragment : BaseFragment<FragmentDemoImagePiecesBinding>() {
    override fun getViewBinding(): FragmentDemoImagePiecesBinding =
        FragmentDemoImagePiecesBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*lifecycleScope.launch {
            val backgroundBitmap: Bitmap
            val foregroundBitmap: Bitmap
            withContext(Dispatchers.IO) {
                val img1 = "https://tympanus.net/Development/AnimatedImagePieces/img/3d.jpg"
                val img2 = "https://tympanus.net/Development/AnimatedImagePieces/img/4b.jpg"

                backgroundBitmap = requestImage(img1).get()
                foregroundBitmap = requestImage(img2).get()
            }
            withContext(Dispatchers.Main) {
                if (!BitmapUtils.isEmptyBitmap(backgroundBitmap) && !BitmapUtils.isEmptyBitmap(
                        foregroundBitmap
                    )
                )
                    binding.imagePieces.setImages(
                        foregroundImage = foregroundBitmap,
                        backgroundImage = backgroundBitmap, 250.dpToPx(), 350.dpToPx()
                    )
            }
        }*/

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupListeners() {
        super.setupListeners()
        Handler(Looper.getMainLooper()).postDelayed({
        Glide.with(requireContext())
            .asBitmap()
            .listener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.glImageTextureView.setBitmapImage(resource)
                    return false
                }
            })
            .load("https://raw.githubusercontent.com/codrops/ImagePixelLoading/main/img/4.jpg")
            .submit(binding.glImageTextureView.width, binding.glImageTextureView.height)
        }, 400)
        /*binding.textShufflerView.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.textShufflerView.shuffleText(reset = true, withDelay = true)
            }, 2000)
        }*/
        /*binding.imagePieces.setOnTouchListener { view, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.imagePieces.demoAnimateIn()
                    true
                }

                MotionEvent.ACTION_UP -> {
                    binding.imagePieces.demoAnimateOut(event.x, event.y)
                    true
                }

                else -> false
            }
        }*/
    }

    private fun requestImage(url: String): FutureTarget<Bitmap> {
        return Glide.with(requireContext())
            .asBitmap()
            .listener(object : RequestListener<Bitmap?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }
            })
            .load(url)
            .submit()

    }

}