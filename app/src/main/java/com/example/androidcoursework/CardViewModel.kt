package com.example.androidcoursework

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CardViewModel(private val dao: RecorderDao) : ViewModel() {
    val records: LiveData<List<RecorderDataClass>> = dao.getAll()


    fun deleteRecords(recordsToDelete: List<RecorderDataClass>) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(recordsToDelete)
        }
    }

    fun updateRecord(record: RecorderDataClass, inputText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(
                RecorderDataClass(
                    record.id,
                    inputText,
                    record.filepath,
                    record.timestamp,
                    record.duration,
                    record.latitude,
                    record.longitude
                )
            )
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
                return CardViewModel(
                    RecorderDatabase.getInstance(application).audioRecordDao()
                ) as T
            }
        }
    }
}