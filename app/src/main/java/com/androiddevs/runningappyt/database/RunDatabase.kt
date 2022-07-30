package com.androiddevs.runningappyt.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDAO

    // WOULD DO IF WE HAD NOT USED DAGGER
//    companion object {
//        @Volatile
//        private var instance: RunDatabase? = null
//        private val LOCK = Any()
//
//        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
//            instance ?: createDatabase(context).also {
//                instance = it
//            }
//        }
//
//        private fun createDatabase(context: Context) = Room.databaseBuilder(
//            context,
//            RunDatabase::class.java,
//            "run_db.db"
//        ).fallbackToDestructiveMigration().build()
//    }
}