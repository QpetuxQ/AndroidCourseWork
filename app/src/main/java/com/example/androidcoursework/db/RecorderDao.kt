package com.example.androidcoursework

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RecorderDao {
    @Query("SELECT*FROM audioRecords")
    fun getAll(): LiveData<List<RecorderDataClass>>

    @Insert
    fun insert(vararg recorderDataClass: RecorderDataClass)

    @Delete
    fun delete(recorderDataClass: RecorderDataClass)

    @Delete
    fun delete(recorderDataClasses: List<RecorderDataClass>)

    @Update
    fun update(recorderDataClass: RecorderDataClass)

    @Query("DELETE FROM audioRecords")
    suspend fun deleteAll()

}