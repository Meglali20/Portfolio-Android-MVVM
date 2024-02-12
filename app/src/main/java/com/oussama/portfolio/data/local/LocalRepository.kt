package com.oussama.portfolio.data.local

import com.oussama.portfolio.data.models.AboutMeModel
import com.oussama.portfolio.data.models.ContactModel
import com.oussama.portfolio.data.models.ExperienceModel
import com.oussama.portfolio.data.models.PortfolioModel
import com.oussama.portfolio.data.models.ProjectModel
import com.oussama.portfolio.data.local.room.PortfolioDatabase
import com.oussama.portfolio.data.local.room.entities.AboutMeEntity
import com.oussama.portfolio.data.local.room.entities.ContactEntity
import com.oussama.portfolio.data.local.room.entities.ExperienceEntity
import com.oussama.portfolio.data.local.room.entities.PortfolioEntity
import com.oussama.portfolio.data.local.room.entities.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class LocalRepository @Inject constructor(private val portfolioDatabase: PortfolioDatabase) {

    suspend fun insertAboutMe(aboutMeModel: AboutMeModel, lang: String) {
        withContext(Dispatchers.IO) {
            val exitingData = retrieveAboutMe(lang)
            if (exitingData != null) portfolioDatabase.aboutMeDao().deleteAboutMe(exitingData)
            val aboutMeEntity = AboutMeEntity(
                description = aboutMeModel.description,
                lang = lang,
                media = aboutMeModel.media.map { it }
            )
            val insertedRowId = portfolioDatabase.aboutMeDao().insertAboutMe(aboutMeEntity)
            Timber.i("Insertion result : ${(insertedRowId > 0)}")
        }
    }

    fun retrieveAboutMe(lang: String): AboutMeEntity? {
        return portfolioDatabase.aboutMeDao().getAboutMe(lang)
    }

    suspend fun insertPortfolio(portfolioModel: PortfolioModel, lang: String) {
        withContext(Dispatchers.IO) {
            val exitingData = retrievePortfolio(lang)
            if (exitingData != null) portfolioDatabase.portfolioDao().deletePortfolio(exitingData)
            val portfolioEntity = PortfolioEntity(
                description = portfolioModel.description,
                lang = lang,
                projects = portfolioModel.projects.map {
                    ProjectModel(
                        //it.description,
                        title = it.title,
                        icon = it.icon,
                        bannerImage = it.bannerImage
                        //it.media
                    )
                }

            )
            val insertedRowId = portfolioDatabase.portfolioDao().insertPortfolio(portfolioEntity)
            Timber.i("Insertion result : ${(insertedRowId > 0)}")
        }
    }

    fun retrievePortfolio(lang: String): PortfolioEntity? {
        return portfolioDatabase.portfolioDao().getPortfolio(lang)
    }

    private fun insertProject(projectModel: ProjectModel, lang: String) {

        val exitingData = retrieveProject(projectModel.title, lang)
        if (exitingData != null) portfolioDatabase.projectDao().deleteProject(exitingData)
        val portfolioEntity = ProjectEntity(
            title = projectModel.title,
            description = projectModel.description,
            icon = projectModel.icon,
            bannerImage = projectModel.bannerImage,
            preview = projectModel.preview,
            media = projectModel.media,
            lang = lang,
        )
        val insertedRowId = portfolioDatabase.projectDao().insertProject(portfolioEntity)
        Timber.i("Insertion result : ${(insertedRowId > 0)}")

    }

    fun retrieveProject(title: String, lang: String): ProjectEntity? {
        return portfolioDatabase.projectDao().getProject(title, lang)
    }

    suspend fun insertProjects(projects: List<ProjectModel>, lang: String) {
        withContext(Dispatchers.IO) {
            projects.forEach {
                insertProject(it, lang)
            }
        }
    }

    suspend fun insertExperience(experienceModelList: List<ExperienceModel>, lang: String) {
        withContext(Dispatchers.IO) {
            val exitingData = retrieveExperience(lang)
            if (exitingData.isNotEmpty()) portfolioDatabase.experienceDao().deleteExperiences(exitingData)
            val experienceEntity =
                experienceModelList.map { ExperienceEntity(description = it.description, media = it.media, lang = lang) }
            val insertedRows = portfolioDatabase.experienceDao().insertExperiences(experienceEntity).size
            Timber.i("Insertion result : ${(insertedRows > 0)}")
        }
    }

    fun retrieveExperience(lang: String): List<ExperienceEntity> {
        return portfolioDatabase.experienceDao().getExperience(lang)
    }


    suspend fun insertContacts(contactModelList: List<ContactModel>) {
        withContext(Dispatchers.IO) {
            val exitingData = retrieveContacts()
            if (exitingData.isNotEmpty()) portfolioDatabase.contactDao().deleteContacts(exitingData)
            val contactEntity =
                contactModelList.map { ContactEntity(title = it.title, url = it.url) }
            val insertedRows = portfolioDatabase.contactDao().insertContacts(contactEntity).size
            Timber.i("Insertion result : ${(insertedRows > 0)}")
        }
    }

    fun retrieveContacts(): List<ContactEntity> {
        return portfolioDatabase.contactDao().getContacts()
    }
}