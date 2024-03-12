package com.example.androidcoursework

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecorderDataClass::class], version = 6)
abstract class RecorderDatabase : RoomDatabase() {
    abstract fun audioRecordDao(): RecorderDao

    companion object {
        private var recorderDatabase: RecorderDatabase? = null
        fun getInstance(context: Context): RecorderDatabase {
            synchronized(this) {
                var databaseInstance = recorderDatabase
                if (databaseInstance == null) {
                    databaseInstance = Room.databaseBuilder(
                        context, RecorderDatabase::class.java, "recorderDatabase"
                    ).fallbackToDestructiveMigration().build()
                    recorderDatabase = databaseInstance
                }
                return databaseInstance
            }
        }
    }

}