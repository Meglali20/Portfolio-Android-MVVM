package com.oussama.portfolio.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oussama.portfolio.data.DataStoreRepository
import com.oussama.portfolio.utils.LOCALE_DATASTORE_KEY
import com.oussama.portfolio.utils.THEME_DATASTORE_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    fun saveTheme(value: Int) {
        viewModelScope.launch {
            repository.putInt(THEME_DATASTORE_KEY, value)
        }
    }

    fun getTheme(): Int? = runBlocking {
        repository.getInt(THEME_DATASTORE_KEY)
    }

    fun saveLocale(value: String) {
        viewModelScope.launch {
            repository.putString(LOCALE_DATASTORE_KEY, value)
        }
    }

    fun getLocale(): String? = runBlocking {
        repository.getString(LOCALE_DATASTORE_KEY)
    }

}