package com.muhammadali.udemy.jetpack.room.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

/**
 * Created by Muhammad Ali on 17-May-20.
 * Email muhammad.ali9385@gmail.com
 */
@Database(entities = arrayOf(DogBreed::class), version = 1)
abstract class DogDatabase : RoomDatabase() {
    abstract fun dogDao(): DogDao

    companion object {
        @Volatile
        private var instance: DogDatabase? = null
        private var lock = Any()


        operator fun invoke(context: Context) = instance ?: kotlin.synchronized(lock) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, DogDatabase::class.java, "DogDatabase")
                .build()
    }
}