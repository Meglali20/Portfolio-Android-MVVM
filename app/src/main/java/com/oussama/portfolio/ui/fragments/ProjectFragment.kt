package com.oussama.portfolio.ui.fragments

import android.animation.Animator
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.oussama.portfolio.BaseApplication
import com.oussama.portfolio.R
import com.oussama.portfolio.data.DataError
import com.oussama.portfolio.data.models.ProjectModel
import com.oussama.portfolio.databinding.FragmentProjectBinding
import com.oussama.portfolio.ui.adapters.MediaAdapter
import com.oussama.portfolio.ui.viewmodels.PortfolioViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale


@AndroidEntryPoint
class ProjectFragment : BaseFragment<FragmentProjectBinding>() {
    override fun getViewBinding() = FragmentProjectBinding.inflate(layoutInflater)

    private val portfolioViewModel: PortfolioViewModel by viewModels()
    private lateinit var mediaAdapter: MediaAdapter
    private var projectTitle: String? = null
    private var id: String? = null
    private var viewPropertyAnimator: ViewPropertyAnimator? = null
    private var projectModel: ProjectModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectTitle = it.getString("projectTitle")
            id = it.getString("id")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        mediaAdapter = MediaAdapter()
        binding.recyclerView.adapter = mediaAdapter
    }


    override fun registerLiveDataObservers() {
        portfolioViewModel.projectLiveData.observe(viewLifecycleOwner) { portfolioModel ->
            if(portfolioModel.data != null) {
                projectModel = portfolioModel.data
                showProjectContainer()
                displayProject(portfolioModel.data)
            }
            else{
                val errorMessage =
                    DataError.getErrorMessage(requireContext(), portfolioModel.errorCode!!)
                Timber.e(
                    "Error fetching aboutMe $errorMessage"
                )
                showErrorLayout(errorMessage)
            }
        }
        portfolioViewModel.retrieveProject(projectTitle!!, Locale.getDefault().language)
    }

    private fun displayProject(projectModel: ProjectModel) {
        Glide.with(requireContext()).load(projectModel.icon).into(binding.projectIcon)
        binding.projectTitle.setText(projectModel.title, autoAnimate = true)
        mediaAdapter.addMedias(projectModel.media)
        Timber.e("proj desc ${projectModel.description}")
        binding.projectDescription.setText(projectModel.description, autoAnimate = true)
        if (projectModel.preview.isNotEmpty()) {
            binding.previewBtn.setText(getString(R.string.livePreviewBtn), autoAnimate = true)
            binding.previewBtn.textColor = BaseApplication.INSTANCE.colorCombination[0]
            animatePreviewIndicator(binding.previewIndicator)
            binding.previewBtnLayout.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(projectModel.preview)))
            }
        } else {
            binding.previewBtn.setText(
                getString(R.string.livePreviewUnavailable),
                autoAnimate = true
            )
            val color = ContextCompat.getColor(requireContext(), R.color.grey)
            binding.previewBtn.textColor = color
            binding.previewBtnLayout.background = null
            binding.previewIndicator.backgroundTintList = ColorStateList.valueOf(color)
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

    private fun showProjectContainer() {
        binding.loadingLayout.root.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE
    }

    private fun animatePreviewIndicator(previewIndicator: View) {
        viewPropertyAnimator = previewIndicator.animate()
            .alpha(if (previewIndicator.alpha == 0f) 1f else 0f)
            .setDuration(800)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {

                }

                override fun onAnimationEnd(p0: Animator) {
                    animatePreviewIndicator(previewIndicator)
                }

                override fun onAnimationCancel(p0: Animator) {

                }

                override fun onAnimationRepeat(p0: Animator) {

                }
            })
        viewPropertyAnimator?.start()
    }

    override fun setupListeners() {
        /*binding.appBarLayout.addOnOffsetChangedListener { _, _ ->
            binding.portfolioDescription.onScroll()
        }*/
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.projectTitle.onScroll()
            binding.previewBtn.onScroll()
            binding.projectDescription.onScroll()
        }

        binding.loadingLayout.retryBtnLayout.setOnClickListener {
            hideErrorLayout()
            portfolioViewModel.fetchPortfolio(Locale.getDefault().language)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPropertyAnimator?.cancel()
        viewPropertyAnimator?.setListener(null)
        viewPropertyAnimator = null
    }
}