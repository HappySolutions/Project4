package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    //provide testing to the SaveReminderView and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: ReminderDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun initTheRpoAndViewMode() {
        stopKoin()

        remindersLocalRepository = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersLocalRepository
        )
    }

    @Test
    @Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
    fun validateReminderCaseInvalid() {
        // test invalid reminder
        val reminder = ReminderDataItem(
            title = null,
            description = "Desc",
            location = "loc",
            latitude = 1.2,
            longitude = 1.3,
            id = "someid"
        )

        // test validate reminder
        val result = saveReminderViewModel.validateEnteredData(reminder)

        // result when reminder is invalid
        assertThat(result, `is`(false))
    }

    @Test
    @Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
    fun validateReminderCaseValid() {
        // test invalid reminder
        val reminder = ReminderDataItem(
            title = "title",
            description = "Desc",
            location = "loc",
            latitude = 1.2,
            longitude = 1.3,
            id = "someid"
        )

        // test validate reminder
        val result = saveReminderViewModel.validateEnteredData(reminder)

        // result when reminder is invalid
        assertThat(result, `is`(true))
    }
}