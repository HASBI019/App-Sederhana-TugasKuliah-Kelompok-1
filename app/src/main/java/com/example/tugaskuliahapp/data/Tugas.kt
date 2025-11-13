package com.example.tugaskuliahapp.data


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tugas")
data class Tugas(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mataKuliah: String,
    val deskripsi: String,
    val deadline: Long,
    val status: String,
    val catatan: String
)