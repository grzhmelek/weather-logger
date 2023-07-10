package com.grzhmelek.weatherlogger

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.grzhmelek.weatherlogger.database.WeatherDatabase
import com.grzhmelek.weatherlogger.database.WeatherDatabaseDao
import com.grzhmelek.weatherlogger.database.WeatherResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WeatherDatabaseTest {

    private lateinit var weatherDatabaseDao: WeatherDatabaseDao
    private lateinit var db: WeatherDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java)
            // Allowing main thread queries for testing only
            .allowMainThreadQueries()
            .build()
        weatherDatabaseDao = db.weatherDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndClear() {
        insert()
        weatherDatabaseDao.clear()
        val weatherHistory = getHistory()
        assertEquals(weatherHistory.size, 0)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHistory() {
        insert()
        val weatherHistory = getHistory()
        assertEquals(weatherHistory.size, 1)
    }

    @Test
    @Throws(Exception::class)
    fun insertUpdateAndGetItem() {
        val id = insert()
        val weatherResult = weatherDatabaseDao.getWeatherItem(id)
        weatherResult.date = -1
        weatherDatabaseDao.update(weatherResult)
        val updatedItem = weatherDatabaseDao.getWeatherItem(id)
        assertEquals(updatedItem.date, -1L)
    }

    private fun insert(): Long {
        return weatherDatabaseDao.insert(WeatherResult())
    }

    private fun getHistory(): List<WeatherResult> {
        return weatherDatabaseDao.getWeatherHistory()
    }
}

