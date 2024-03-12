package com.example.androidcoursework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(private val dao: RecorderDao) : ViewModel() {


    fun insertRecord(record: RecorderDataClass) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(record)
        }
    }

    companion object {
        fun Factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                val application =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return MainViewModel(RecorderDatabase.getInstance(application).audioRecordDao()) as T
            }
        }
    }
}