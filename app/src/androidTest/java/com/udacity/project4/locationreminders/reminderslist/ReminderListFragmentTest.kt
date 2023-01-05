package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest()  {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.
    private lateinit var remDataSource: ReminderDataSource

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        stopKoin()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single {
                AuthenticationViewModel(get())
            }
            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(get())
            }
        }
        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
        remDataSource = get()
        runBlocking {
            remDataSource.deleteAllReminders()
        }
    }

    @Test
    fun clickAddRemBtn_navToSaveRemFragment() = runBlockingTest {
        runBlocking {

            // GIVEN - On the home screen
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            val navController = Mockito.mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            // WHEN - Click on the "+" button
            onView(withId(R.id.addReminderFAB)).perform(click())

            // THEN - Verify that we navigate to the add screen
            verify(navController).navigate(
                ReminderListFragmentDirections.toSaveReminder()
            )
        }
    }

    @Test
    fun saveRem_displayInUI() = runBlockingTest {
        runBlocking {
            val insertedRem = ReminderDTO(
                "test title",
                "test description",
                "test location",
                50.00,
                50.00,
                "testingId"
            )
            remDataSource.saveReminder(
                insertedRem
            )
            launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

            onView(withText(insertedRem.title)).check(matches(isDisplayed()))
            onView(withText(insertedRem.description)).check(matches(isDisplayed()))
            onView(withText(insertedRem.location)).check(matches(isDisplayed()))
        }
    }
}