package com.oussama.portfolio.data.remote

import com.oussama.portfolio.data.ERROR_NETWORK
import com.oussama.portfolio.data.ERROR_NOT_FOUND
import com.oussama.portfolio.data.ERROR_NO_INTERNET_CONNECTION
import com.oussama.portfolio.data.Resource
import com.oussama.portfolio.data.models.AboutMeModel
import com.oussama.portfolio.data.models.ContactModel
import com.oussama.portfolio.data.models.ExperienceModel
import com.oussama.portfolio.data.models.PortfolioModel
import com.oussama.portfolio.utils.NetworkConnectivity
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class RemoteRepository @Inject
constructor(
    private val retrofitClient: RetrofitClient,
    private val networkConnectivity: NetworkConnectivity
) {

    suspend fun fetchAboutMe(lang: String): Resource<AboutMeModel> {
        val firebaseAPIService = retrofitClient.createService(FirebaseAPIService::class.java)
        return when (val response = processCall { firebaseAPIService.fetchAboutMeData(lang) }) {
            is AboutMeModel -> {
                Resource.Success(data = response)
            }

            else -> {
                Resource.DataError(errorCode = (if (response == null) ERROR_NOT_FOUND else response as Int))
            }
        }
    }

    suspend fun fetchPortfolio(lang: String): Resource<PortfolioModel> {
        val firebaseAPIService = retrofitClient.createService(FirebaseAPIService::class.java)
        return when (val response = processCall { firebaseAPIService.fetchPortfolio(lang) }) {
            is PortfolioModel -> {
                Resource.Success(data = response)
            }

            else -> {
                Resource.DataError(errorCode = (if (response == null) ERROR_NOT_FOUND else response as Int))
            }
        }
    }

    suspend fun fetchExperience(lang: String): Resource<ExperienceModel> {
        val firebaseAPIService = retrofitClient.createService(FirebaseAPIService::class.java)
        return when (val response = processCall { firebaseAPIService.fetchExperience(lang) }) {
            is ExperienceModel -> {
                Resource.Success(data = response)
            }

            else -> {
                Resource.DataError(errorCode = (if (response == null) ERROR_NOT_FOUND else response as Int))
            }
        }
    }

    suspend fun fetchContact(): Resource<List<*/*ContactModel*/>> {
        val firebaseAPIService = retrofitClient.createService(FirebaseAPIService::class.java)
        return when (val response = processCall { firebaseAPIService.fetchContact() }) {
            is List<*> -> {
                Resource.Success(data = response) /*as Resource<List<ContactModel>>*/
            }

            else -> {
                Resource.DataError(errorCode = (if (response == null) ERROR_NOT_FOUND else response as Int))
            }
        }
    }

    private suspend fun processCall(responseCall: suspend () -> Response<*>): Any? {
        if (!networkConnectivity.isConnected()) {
            return ERROR_NO_INTERNET_CONNECTION
        }
        return try {
            val response = responseCall.invoke()
            val responseCode = response.code()
            if (response.isSuccessful) {
                response.body()
            } else {
                responseCode
            }
        } catch (e: IOException) {
            ERROR_NETWORK
        }
    }
}