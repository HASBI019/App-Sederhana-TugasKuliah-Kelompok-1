package com.example.tugaskuliahapp.data


import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Tugas::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tugasDao(): TugasDao
}