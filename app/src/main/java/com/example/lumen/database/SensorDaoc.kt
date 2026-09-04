package com.example.lumen.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SensorDao {

    @Query("SELECT * FROM sensors ORDER BY id ASC")
    suspend fun getAllSensors(): List<SensorEntity>

    @Query("SELECT * FROM sensors WHERE id = :sensorId LIMIT 1")
    suspend fun getSensorById(sensorId: Int): SensorEntity?

    @Insert
    suspend fun insertSensor(sensor: SensorEntity)

    @Update
    suspend fun updateSensor(sensor: SensorEntity)

    @Delete
    suspend fun deleteSensor(sensor: SensorEntity)

    @Query("DELETE FROM sensors")
    suspend fun deleteAllSensors()
}