package com.oussama.portfolio.ui.fragments

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.oussama.portfolio.data.DataError
import com.oussama.portfolio.databinding.FragmentAboutBinding
import com.oussama.portfolio.ui.viewmodels.PortfolioViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding>() {
    override fun getViewBinding() = FragmentAboutBinding.inflate(layoutInflater)
    private val portfolioViewModel: PortfolioViewModel by viewModels()


    override fun setupListeners() {
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.textShufflerView.onScroll()
        }
        binding.loadingLayout.retryBtnLayout.setOnClickListener {
            hideErrorLayout()
            portfolioViewModel.fetchAboutMe(Locale.getDefault().language)
        }
    }

    override fun registerLiveDataObservers() {
        super.registerLiveDataObservers()
        portfolioViewModel.aboutMeLiveData.observe(viewLifecycleOwner) { aboutMeModel ->
            if (aboutMeModel.data != null) {
                binding.textShufflerView.setText(aboutMeModel.data.description, autoAnimate = true)
                showAboutMeContainer()
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
                                binding.glImageTextureView.alpha = 1f
                                return false
                            }
                        })
                        .load(aboutMeModel.data.media[3].imageUrl)
                        .submit(binding.glImageTextureView.width, binding.glImageTextureView.height)
                }, 400)
            } else {
                val errorMessage =
                    DataError.getErrorMessage(requireContext(), aboutMeModel.errorCode!!)
                Timber.e(
                    "Error fetching aboutMe $errorMessage"
                )
                showErrorLayout(errorMessage)
            }

        }
        portfolioViewModel.fetchAboutMe(Locale.getDefault().language)
    }

    private fun showErrorLayout(errorMessage: String) {
        binding.loadingLayout.loadingIndicator.visibility = View.GONE
        binding.loadingLayout.errorContainer.visibility = View.VISIBLE
        binding.loadingLayout.errorText.setText(errorMessage, autoAnimate = true)
        binding.loadingLayout.root.visibility = View.VISIBLE
    }

    private fun hideErrorLayout() {
        binding.loadingLayout.loadingIndicator.visibility = View.VISIBLE
        binding.loadingLayout.errorContainer.visibility = View.GONE
        binding.loadingLayout.root.visibility = View.VISIBLE
    }

    private fun showAboutMeContainer() {
        binding.loadingLayout.root.visibility = View.GONE
        binding.aboutMeContainer.visibility = View.VISIBLE
    }


    override fun onFragmentVisibilityChanged(isVisible: Boolean) {
        binding.glImageTextureView.paused(!isVisible)
    }
}