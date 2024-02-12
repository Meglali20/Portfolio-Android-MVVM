package com.oussama.portfolio.ui.fragments

import android.view.View
import androidx.fragment.app.viewModels
import com.oussama.portfolio.data.DataError
import com.oussama.portfolio.data.models.ExperienceModel
import com.oussama.portfolio.databinding.FragmentExperienceBinding
import com.oussama.portfolio.ui.components.FileDownloader
import com.oussama.portfolio.ui.receivers.DownloadCompletedReceiver
import com.oussama.portfolio.ui.viewmodels.PortfolioViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class ExperienceFragment : BaseFragment<FragmentExperienceBinding>() {
    override fun getViewBinding() = FragmentExperienceBinding.inflate(layoutInflater)
    private val portfolioViewModel: PortfolioViewModel by viewModels()
    private val downloadCompleteReceiver = DownloadCompletedReceiver()


    override fun setupListeners() {
        super.setupListeners()
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.experienceDescription.onScroll()
            binding.downloadBtn.onScroll()
        }
        binding.loadingLayout.retryBtnLayout.setOnClickListener {
            hideErrorLayout()
            portfolioViewModel.fetchExperience(Locale.getDefault().language)
        }
    }

    override fun registerLiveDataObservers() {
        super.registerLiveDataObservers()
        portfolioViewModel.experienceLiveData.observe(this) { experienceModel ->
            if (experienceModel.data != null) {
                showExperienceContainer()
                displayExperience(experienceModel.data[0])
            } else {
                val errorMessage =
                    DataError.getErrorMessage(requireContext(), experienceModel.errorCode!!)
                Timber.e(
                    "Error fetching aboutMe $errorMessage"
                )
                showErrorLayout(errorMessage)
            }
        }
        portfolioViewModel.fetchExperience(Locale.getDefault().language)
    }

    private fun displayExperience(experienceModel: ExperienceModel) {
        binding.experienceDescription.setText(experienceModel.description, autoAnimate = true)
        binding.downloadBtnLayout.setOnClickListener {
            val fileDownloader = FileDownloader(requireContext())
            val fileUrl = experienceModel.media[0].imageUrl
            fileDownloader.downloadFile(fileUrl)
        }
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

    private fun showExperienceContainer() {
        binding.loadingLayout.root.visibility = View.GONE
        binding.experienceContainer.visibility = View.VISIBLE
    }
}