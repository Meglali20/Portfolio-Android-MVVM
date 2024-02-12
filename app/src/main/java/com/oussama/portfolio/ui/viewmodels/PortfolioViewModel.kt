package com.oussama.portfolio.ui.viewmodels

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama.portfolio.data.MainRepository
import com.oussama.portfolio.data.Resource
import com.oussama.portfolio.data.models.AboutMeModel
import com.oussama.portfolio.data.models.ContactModel
import com.oussama.portfolio.data.models.ExperienceModel
import com.oussama.portfolio.data.models.PortfolioModel
import com.oussama.portfolio.data.models.ProjectModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortfolioViewModel @Inject constructor(private val mainRepository: MainRepository) :
    ViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val aboutMeLiveDataPrivate = MutableLiveData<Resource<AboutMeModel>>()
    val aboutMeLiveData: LiveData<Resource<AboutMeModel>> get() = aboutMeLiveDataPrivate

    private val experienceLiveDataPrivate = MutableLiveData<Resource<List<ExperienceModel>>>()
    val experienceLiveData: LiveData<Resource<List<ExperienceModel>>> get() = experienceLiveDataPrivate

    private val portfolioLiveDataPrivate = MutableLiveData<Resource<PortfolioModel>>()
    val portfolioLiveData: LiveData<Resource<PortfolioModel>> get() = portfolioLiveDataPrivate

    private val projectLiveDataPrivate = MutableLiveData<Resource<ProjectModel>>()
    val projectLiveData: LiveData<Resource<ProjectModel>> get() = projectLiveDataPrivate

    private val contactLiveDataPrivate = MutableLiveData<Resource<List<ContactModel>>>()
    val contactLiveData: LiveData<Resource<List<ContactModel>>> get() = contactLiveDataPrivate

    fun fetchAboutMe(lang: String) {
        viewModelScope.launch {
            aboutMeLiveDataPrivate.value = mainRepository.fetchAboutMe(lang)
        }
    }

    fun fetchExperience(lang: String) {
        viewModelScope.launch {
            experienceLiveDataPrivate.value = mainRepository.fetchExperience(lang)
        }
    }

    fun fetchPortfolio(lang: String) {
        viewModelScope.launch {
            portfolioLiveDataPrivate.value = mainRepository.fetchPortfolio(lang)
        }
    }

    fun retrieveProject(title: String, lang: String) {
        viewModelScope.launch {
            projectLiveDataPrivate.value = mainRepository.retrieveProject(title, lang)
        }
    }

    fun fetchContact() {
        viewModelScope.launch {
            contactLiveDataPrivate.value = mainRepository.fetchContact()
        }
    }

}