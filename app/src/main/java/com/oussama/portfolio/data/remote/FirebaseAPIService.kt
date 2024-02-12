package com.oussama.portfolio.data.remote

import com.oussama.portfolio.data.models.AboutMeModel
import com.oussama.portfolio.data.models.ContactModel
import com.oussama.portfolio.data.models.ExperienceModel
import com.oussama.portfolio.data.models.PortfolioModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface FirebaseAPIService {
    @GET("portfolio/{lang}/about-me.json")
    suspend fun fetchAboutMeData(@Path("lang") language: String): Response<AboutMeModel>

    @GET("portfolio/{lang}/portfolio.json")
    suspend fun fetchPortfolio(@Path("lang") language: String): Response<PortfolioModel>

    @GET("portfolio/{lang}/experience.json")
    suspend fun fetchExperience(@Path("lang") language: String): Response<List<ExperienceModel>>

    @GET("portfolio/contact.json")
    suspend fun fetchContact(): Response<List<ContactModel>>
}