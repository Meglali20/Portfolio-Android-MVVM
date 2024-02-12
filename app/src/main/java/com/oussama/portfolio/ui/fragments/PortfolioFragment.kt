package com.oussama.portfolio.ui.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.transition.Slide
import com.oussama.portfolio.R
import com.oussama.portfolio.data.DataError
import com.oussama.portfolio.databinding.FragmentPortfolioBinding
import com.oussama.portfolio.ui.adapters.ProjectsAdapter
import com.oussama.portfolio.ui.components.ProjectClickListener
import com.oussama.portfolio.ui.viewmodels.PortfolioViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale


@AndroidEntryPoint
class PortfolioFragment : BaseFragment<FragmentPortfolioBinding>() {
    override fun getViewBinding() = FragmentPortfolioBinding.inflate(layoutInflater)

    private val portfolioViewModel: PortfolioViewModel by viewModels()
    private lateinit var projectsAdapter: ProjectsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerview()
    }

    private fun setupRecyclerview() {
        projectsAdapter = ProjectsAdapter()
        projectsAdapter.setProjectClickListener(object : ProjectClickListener {
            override fun onClick(projectTitle: String, imageView: ImageView) {
                showProjectFragment(projectTitle, imageView)
            }
        })
        binding.recyclerView.adapter = projectsAdapter
        //projectsAdapter.addRecyclerViewScrollListener()
    }

    override fun registerLiveDataObservers() {
        portfolioViewModel.portfolioLiveData.observe(viewLifecycleOwner) { portfolioModel ->
            if (portfolioModel.data != null) {
                showPortfolioContainer()
                binding.portfolioDescription.setText(
                    portfolioModel.data.description,
                    autoAnimate = true,
                    animateWithDelay = true
                )
                projectsAdapter.addProjects(portfolioModel.data.projects)
            } else {
                val errorMessage =
                    DataError.getErrorMessage(requireContext(), portfolioModel.errorCode!!)
                Timber.e(
                    "Error fetching aboutMe $errorMessage"
                )
                showErrorLayout(errorMessage)
            }
        }
        portfolioViewModel.fetchPortfolio(Locale.getDefault().language)

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

    private fun showPortfolioContainer() {
        binding.loadingLayout.root.visibility = View.GONE
        binding.portfolioContainer.visibility = View.VISIBLE
    }

    override fun setupListeners() {
        /*binding.appBarLayout.addOnOffsetChangedListener { _, _ ->
            binding.portfolioDescription.onScroll()
        }*/
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.portfolioDescription.onScroll()
            projectsAdapter.onParentScrolled()
        }
        binding.loadingLayout.retryBtnLayout.setOnClickListener {
            hideErrorLayout()
            portfolioViewModel.fetchPortfolio(Locale.getDefault().language)
        }

    }

    private fun showProjectFragment(projectTitle: String, imageView: ImageView) {
        val projectFragment = ProjectFragment()
        val bundle = Bundle()
        bundle.putString("projectTitle", projectTitle)
        projectFragment.arguments = bundle
        val slideTransition = Slide(Gravity.BOTTOM)
        slideTransition.setDuration(200)
        projectFragment.enterTransition = slideTransition
        requireActivity().supportFragmentManager
            .commit {
                //setCustomAnimations(...)
                setReorderingAllowed(true)
                replace(R.id.fullFragmentContainer, projectFragment)
                addToBackStack(null)
            }


    }

    override fun onFragmentVisibilityChanged(isVisible: Boolean) {
        binding.portfolioDescription.paused(!isVisible)
    }

}