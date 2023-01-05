package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.CoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = CoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupViewModel() {
        stopKoin()

        remindersRepository = FakeDataSource()

        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), remindersRepository
        )
    }

    @Test
    @Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
    fun loadReminders_loading() {
        mainCoroutineRule.pause()
        remindersListViewModel.loadReminders()

        assert(remindersListViewModel.showLoading.getOrAwaitValue())

        mainCoroutineRule.resume()
        assert(!remindersListViewModel.showLoading.getOrAwaitValue())
    }

    @Test
    @Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
    fun loadReminders_loadingError() {
        mainCoroutineRule.pause()
        remindersRepository.setReturnError(true)
        remindersListViewModel.loadReminders()

        assert(remindersListViewModel.showLoading.getOrAwaitValue())

        mainCoroutineRule.resume()
        assert(!remindersListViewModel.showLoading.getOrAwaitValue())
    }

    @Test
    @Config(sdk = intArrayOf(Build.VERSION_CODES.O_MR1))
    fun loadReminders_noData() {
        mainCoroutineRule.pause()
        remindersRepository.setReturnError(true)
        remindersListViewModel.loadReminders()

        assert(remindersListViewModel.showLoading.getOrAwaitValue())

        mainCoroutineRule.resume()
        assert(!remindersListViewModel.showLoading.getOrAwaitValue())
    }

}