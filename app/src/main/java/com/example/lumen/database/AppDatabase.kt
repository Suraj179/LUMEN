package com.example.lumen.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LightEntity::class,
        SensorEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lightDao(): LightDao

    abstract fun sensorDao(): SensorDao
}