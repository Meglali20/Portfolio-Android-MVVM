package com.oussama.portfolio.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.data.DataError
import com.oussama.portfolio.data.models.AboutMeModel
import com.oussama.portfolio.databinding.FragmentHomeBinding
import com.oussama.portfolio.ui.viewmodels.PortfolioViewModel
import com.oussama.portfolio.utils.BitmapUtils
import com.oussama.portfolio.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), View.OnTouchListener {
    override fun getViewBinding() = FragmentHomeBinding.inflate(layoutInflater)
    private val portfolioViewModel: PortfolioViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun setupListeners() {
        binding.imagePieces.setOnTouchListener(this)
        binding.wavesSurfaceView.setOnTouchListener(this)
    }

    override fun registerLiveDataObservers() {
        super.registerLiveDataObservers()
        portfolioViewModel.aboutMeLiveData.observe(viewLifecycleOwner) { aboutMeModel ->
            if (aboutMeModel.data != null) {
                lifecycleScope.launch {
                    loadImages(aboutMeModel.data)
                }
            } else {
                Timber.e(
                    "Error fetching aboutMe ${
                        DataError.getErrorMessage(
                            requireContext(),
                            aboutMeModel.errorCode!!
                        )
                    } "
                )
                Toast.makeText(
                    requireContext(),
                    DataError.getErrorMessage(requireContext(), aboutMeModel.errorCode),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        portfolioViewModel.fetchAboutMe(Locale.getDefault().language)
    }

    private suspend fun loadImages(aboutMeModel: AboutMeModel) {
        var backgroundBitmap: Bitmap?
        var foregroundBitmap: Bitmap?
        withContext(Dispatchers.IO) {
            while (true) {
                if (BaseApplication.INSTANCE.networkConnectivity.isConnected()) {
                    try {
                        backgroundBitmap =
                            requestImage(aboutMeModel.media[if (Utils.isNightMode(requireContext())) 1 else 2].imageUrl)
                        foregroundBitmap = requestImage(aboutMeModel.media[0].imageUrl)
                        break
                    } catch (_: Exception) {
                        Timber.e("Image loading failed, retrying in 5 seconds...")
                    }
                } else
                    Timber.e("No internet connection, retrying in 5 seconds...")
                delay(5000)
            }
        }

        withContext(Dispatchers.Main) {
            if (foregroundBitmap != null && backgroundBitmap != null)
                if (!BitmapUtils.isEmptyBitmap(backgroundBitmap) && !BitmapUtils.isEmptyBitmap(
                        foregroundBitmap
                    )
                ) {
                    binding.imagePieces.setImages(
                        foregroundImage = foregroundBitmap!!,
                        backgroundImage = backgroundBitmap!!
                    )
                }
        }
    }

    private fun requestImage(url: String): Bitmap {
        return Glide.with(requireContext())
            .asBitmap()
            .load(url)
            .submit().get()
    }


    override fun onFragmentVisibilityChanged(isVisible: Boolean) {
        //binding.wavesSurfaceView.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.imagePieces.paused(!isVisible)
        binding.wavesSurfaceView.paused(!isVisible)
    }

    override fun onPause() {
        super.onPause()
        binding.imagePieces.paused(true)
        binding.wavesSurfaceView.paused(true)
    }

    override fun onDestroyView() {
        binding.imagePieces.clear()
        super.onDestroyView()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                binding.imagePieces.animateIn()
                //binding.wavesSurfaceView.resetBackgroundText()
                true
            }

            MotionEvent.ACTION_UP -> {
                binding.imagePieces.animateOut()
                //binding.wavesSurfaceView.resetBanner()
                true
            }

            else -> false
        }
    }

}