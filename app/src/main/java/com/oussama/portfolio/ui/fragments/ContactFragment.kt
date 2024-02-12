package com.oussama.portfolio.ui.fragments

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.oussama.portfolio.R
import com.oussama.portfolio.data.DataError
import com.oussama.portfolio.data.models.ContactModel
import com.oussama.portfolio.databinding.FragmentContactBinding
import com.oussama.portfolio.ui.viewmodels.PortfolioViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class ContactFragment : BaseFragment<FragmentContactBinding>() {
    override fun getViewBinding() = FragmentContactBinding.inflate(layoutInflater)
    private val portfolioViewModel: PortfolioViewModel by viewModels()
    private lateinit var contactModelList: List<ContactModel>

    override fun setupListeners() {
        super.setupListeners()
        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.descText.onScroll()
            binding.callBtn.onScroll()
            binding.mailBtn.onScroll()
            binding.linkedinBtn.onScroll()
            binding.githubBtn.onScroll()
        }
        binding.callBtnLayout.setOnClickListener {
            val phoneNUmber = contactModelList.find { it.title.lowercase() == "phone" }?.url
            if (phoneNUmber != null)
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNUmber")))
        }
        binding.mailBtnLayout.setOnClickListener {
            val mail = contactModelList.find { it.title.lowercase() == "mail" }?.url ?: return@setOnClickListener

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, mail)
            }

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else
                Toast.makeText(requireContext(), getString(R.string.errorMailAppNotFound), Toast.LENGTH_SHORT).show()
        }
        binding.linkedinBtnLayout.setOnClickListener {
            val url = contactModelList.find { it.title.lowercase() == "linkedin" }?.url
            if (url != null)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        binding.githubBtnLayout.setOnClickListener {
            val url = contactModelList.find { it.title.lowercase() == "github" }?.url
            if (url != null)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        binding.loadingLayout.retryBtnLayout.setOnClickListener {
            hideErrorLayout()
            portfolioViewModel.fetchContact()
        }
    }

    override fun registerLiveDataObservers() {
        super.registerLiveDataObservers()
        portfolioViewModel.contactLiveData.observe(viewLifecycleOwner) { contactModelList ->
            if (!contactModelList.data.isNullOrEmpty()) {
                this.contactModelList = contactModelList.data
                showContactContainer()
                showContactInformation()
            } else {
                val errorMessage =
                    DataError.getErrorMessage(requireContext(), contactModelList.errorCode!!)
                Timber.e(
                    "Error fetching contact $errorMessage"
                )
                showErrorLayout(errorMessage)
            }
        }
        portfolioViewModel.fetchContact()
    }

    private fun showContactInformation() {
        val mail = contactModelList.find { it.title.lowercase() == "mail" }?.url
        val phoneNUmber = contactModelList.find { it.title.lowercase() == "phone" }?.url
        val linkedinUrl = contactModelList.find { it.title.lowercase() == "linkedin" }?.url
        val githubUrl = contactModelList.find { it.title.lowercase() == "github" }?.url
        if (mail == null)
            binding.mailBtnLayout.visibility = View.GONE
        else {
            binding.mailBtnLayout.visibility = View.VISIBLE
            binding.mailBtn.setText(mail, autoAnimate = true)
        }
        if (phoneNUmber == null)
            binding.callBtnLayout.visibility = View.GONE
        else {
            binding.callBtnLayout.visibility = View.VISIBLE
            binding.callBtn.setText(phoneNUmber, autoAnimate = true)
        }
        if (linkedinUrl == null)
            binding.linkedinBtnLayout.visibility = View.GONE
        else {
            binding.linkedinBtnLayout.visibility = View.VISIBLE
            binding.linkedinBtn.setText(linkedinUrl.replace("(https?://)?(www\\.)?".toRegex(), ""), autoAnimate = true)
        }
        if (githubUrl == null)
            binding.githubBtnLayout.visibility = View.GONE
        else {
            binding.githubBtnLayout.visibility = View.VISIBLE
            binding.githubBtn.setText(githubUrl.replace("(https?://)?(www\\.)?".toRegex(), ""), autoAnimate = true)
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

    private fun showContactContainer() {
        binding.loadingLayout.root.visibility = View.GONE
        binding.scrollView.visibility = View.VISIBLE
    }
}