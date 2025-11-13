package com.example.tugaskuliahapp.data


import androidx.room.*


@Dao
interface TugasDao {
    @Query("SELECT * FROM tugas ORDER BY deadline ASC")
    suspend fun getAll(): List<Tugas>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tugas: Tugas)


    @Update
    suspend fun update(tugas: Tugas)


    @Delete
    suspend fun delete(tugas: Tugas)


    @Query("SELECT * FROM tugas WHERE deadline BETWEEN :from AND :to ORDER BY deadline ASC")
    suspend fun getBetween(from: Long, to: Long): List<Tugas>


    @Query("SELECT * FROM tugas WHERE deadline <= :until ORDER BY deadline ASC")
    suspend fun getUntil(until: Long): List<Tugas>
}