package com.oussama.portfolio.data

import com.oussama.portfolio.data.local.LocalRepository
import com.oussama.portfolio.data.local.room.entities.AboutMeEntity
import com.oussama.portfolio.data.local.room.entities.ContactEntity
import com.oussama.portfolio.data.local.room.entities.ExperienceEntity
import com.oussama.portfolio.data.local.room.entities.PortfolioEntity
import com.oussama.portfolio.data.local.room.entities.ProjectEntity
import com.oussama.portfolio.data.models.AboutMeModel
import com.oussama.portfolio.data.models.ContactModel
import com.oussama.portfolio.data.models.ExperienceModel
import com.oussama.portfolio.data.models.PortfolioModel
import com.oussama.portfolio.data.models.ProjectModel
import com.oussama.portfolio.data.remote.RemoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
) {

    suspend fun fetchAboutMe(lang: String): Resource<AboutMeModel> {
        val existingData: AboutMeEntity? = withContext(Dispatchers.IO) {
            return@withContext localRepository.retrieveAboutMe(lang)
        }
        if (existingData != null) {
            Timber.i("Data already found in cache... will serve cache data")
            val aboutMeModel = AboutMeModel()
            aboutMeModel.description = existingData.description
            aboutMeModel.media = existingData.media//.map { MediaModel(it) }
            return Resource.Success(data = aboutMeModel)
        }
        Timber.w("No data cached for locale $lang requesting from remote repository")

        val result: Resource<AboutMeModel> = remoteRepository.fetchAboutMe(lang)
        if (result.data != null)
            localRepository.insertAboutMe(result.data, lang)

        return result
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun fetchExperience(lang: String): Resource<List<ExperienceModel>> {
        val existingData: List<ExperienceEntity> = withContext(Dispatchers.IO) {
            return@withContext localRepository.retrieveExperience(lang)
        }
        if (existingData.isNotEmpty()) {
            Timber.i("Data already found in cache... will serve cache data")
            val experienceModel = existingData.map{ ExperienceModel(description = it.description, media = it.media)}
            return Resource.Success(data = experienceModel)
        }
        Timber.w("No data cached for locale $lang requesting from remote repository")

        val result: Resource<List<ExperienceModel>> = remoteRepository.fetchExperience(lang) as Resource<List<ExperienceModel>>
        if (result.data != null) {
            localRepository.insertExperience(result.data, lang)
        }
        return result
    }

    suspend fun fetchPortfolio(lang: String): Resource<PortfolioModel> {
        val existingData: PortfolioEntity? = withContext(Dispatchers.IO) {
            return@withContext localRepository.retrievePortfolio(lang)
        }
        if (existingData != null) {
            Timber.i("Data already found in cache... will serve cache data")
            val portfolioModel = PortfolioModel()
            portfolioModel.description = existingData.description
            portfolioModel.projects = existingData.projects.map {
                ProjectModel(
                    description = it.description,
                    title = it.title,
                    icon = it.icon,
                    bannerImage = it.bannerImage,
                    media = it.media
                )
            }
            return Resource.Success(data = portfolioModel)
        }
        Timber.w("No data cached for locale $lang requesting from remote repository")

        val result: Resource<PortfolioModel> = remoteRepository.fetchPortfolio(lang)
        if (result.data != null) {
            localRepository.insertPortfolio(result.data, lang)
            localRepository.insertProjects(result.data.projects, lang)
        }

        return result
    }

    suspend fun retrieveProject(title: String, lang: String): Resource<ProjectModel> {
        val existingData: ProjectEntity? = withContext(Dispatchers.IO) {
            return@withContext localRepository.retrieveProject(title, lang)
        }
        if (existingData != null) {
            Timber.i("Data already found in cache... will serve cache data")
            val projectModel = ProjectModel(
                title = existingData.title,
                description = existingData.description,
                icon = existingData.icon,
                bannerImage = existingData.bannerImage,
                preview = existingData.preview,
                media = existingData.media
            )

            return Resource.Success(data = projectModel)
        }
        Timber.w("No data cached for locale $lang requesting from remote repository")

        return Resource.DataError(0)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun fetchContact(): Resource<List<ContactModel>> {
        val existingData: List<ContactEntity> = withContext(Dispatchers.IO) {
            return@withContext localRepository.retrieveContacts()
        }
        if (existingData.isNotEmpty()) {
            Timber.i("Data already found in cache... will serve cache data")
            val experienceModel = existingData.map { ContactModel(title = it.title ,url = it.url) }
            return Resource.Success(data = experienceModel)
        }
        Timber.w("No data cached requesting from remote repository")

        val result: Resource<List<ContactModel>> = remoteRepository.fetchContact() as Resource<List<ContactModel>>
        if (result.data != null) {
            localRepository.insertContacts(result.data)
        }
        return result
    }
}