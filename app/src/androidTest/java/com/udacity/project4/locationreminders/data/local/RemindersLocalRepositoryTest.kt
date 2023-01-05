package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var remDB: RemindersDatabase
    private lateinit var remDao: RemindersDao
    private lateinit var remRepo: RemindersLocalRepository

    @Before
    fun setup() {
        remDB = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remDao = remDB.reminderDao()
        remRepo = RemindersLocalRepository(remDao)
    }

    @After
    fun closeDB() {
        remDB.close()
    }

    @Test
    fun saveReminder_retrieveReminder() = runBlocking {

        //save new reminder
        val rem = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 0.0,
            longitude = 0.0
        )
        //test save and get reminder
        remRepo.saveReminder(rem)
        val result = remRepo.getReminder(rem.id)
        Assert.assertEquals(result is Result.Success, true)
        result as Result.Success
        Assert.assertEquals(result.data.title, rem.title)
        Assert.assertEquals(result.data.description, rem.description)
        Assert.assertEquals(result.data.location, rem.location)
        Assert.assertEquals(result.data.latitude, rem.latitude)
        Assert.assertEquals(result.data.longitude, rem.longitude)
    }

    @Test
    fun deleteAllReminders_getReminders() = runBlocking {
        //save new reminder
        val rem = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 0.0,
            longitude = 0.0
        )
        //test save then delete and get reminders
        remRepo.saveReminder(rem)
        remRepo.deleteAllReminders()
        val result = remRepo.getReminders()
        Assert.assertEquals(result is Result.Success, true)
        result as Result.Success
        Assert.assertEquals(result.data.isEmpty(), true)
    }

    @Test
    fun getReminderById() = runBlocking {
        //save new reminder
        val rem = ReminderDTO(
            title = "title",
            description = "description",
            location = "location",
            latitude = 0.0,
            longitude = 0.0
        )
        //test save then get reminder by id
        remRepo.saveReminder(rem)
        val result = remRepo.getReminder(rem.id)
        Assert.assertEquals(result is Result.Success, true)
        result as Result.Success
        Assert.assertEquals(result.data.title, rem.title)
        Assert.assertEquals(result.data.description, rem.description)
        Assert.assertEquals(result.data.location, rem.location)
        Assert.assertEquals(result.data.latitude, rem.latitude)
        Assert.assertEquals(result.data.longitude, rem.longitude)
    }
}