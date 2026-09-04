package com.example.lumen.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LightDao {

    @Query("SELECT * FROM lights ORDER BY id ASC")
    suspend fun getAllLights(): List<LightEntity>

    @Query("SELECT * FROM lights WHERE id = :lightId LIMIT 1")
    suspend fun getLightById(lightId: Int): LightEntity?

    @Insert
    suspend fun insertLight(light: LightEntity)

    @Update
    suspend fun updateLight(light: LightEntity)

    @Delete
    suspend fun deleteLight(light: LightEntity)

    @Query("DELETE FROM lights")
    suspend fun deleteAllLights()
}