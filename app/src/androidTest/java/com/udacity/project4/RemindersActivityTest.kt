package com.udacity.project4

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.isToast
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() = runBlocking {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            viewModel {
                AuthenticationViewModel(
                    appContext
                )
            }
            viewModel {
                SelectLocationViewModel(
                    appContext
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun testAddAReminder_noLocationSelected_snackbarWithErrorMessageAppears(): Unit = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("title2"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("description2"))
        onView(withId(R.id.saveReminder)).perform(click())

        val snackbarMessage = appContext.getString(R.string.select_location)
        onView(withText(snackbarMessage))
            .check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun testClickingOnListItem_opensRemindersDescriptionActivity(): Unit = runBlocking {
        val reminder1 = ReminderDTO(
            "title1", "description1", "location1",
            11.111, 11.112
        )
        repository.saveReminder(reminder1)

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(
            withId(R.id.reminderssRecyclerView)
        )
            .check(matches(hasDescendant(withText("title1"))))
            .perform(click())

        onView(withId(R.id.reminderssRecyclerView)).perform(
            RecyclerViewChildActions.Companion.actionOnChild(
                click(), R.id.reminderCardView
            )
        )

        onView(withId(R.id.rem_title))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rem_description))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rem_location))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rem_latitude))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rem_longitude))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun testAddAReminder_reminderListAppears() = runBlocking {
        val reminder1 = ReminderDTO(
            "title1", "description1", "location1",
            11.111, 11.112
        )
        repository.saveReminder(reminder1)
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(replaceText("title2"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("description2"))

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withId(R.id.myMap)).check(matches(isDisplayed()))
        onView(withId(R.id.myMap)).perform(click())

        delay(2000)
        onView(withId(R.id.save_loc_btton)).perform(click())

        delay(2000)

        onView(withId(R.id.saveReminder)).perform(click())

        delay(2000)

        onView(withText(R.string.reminder_saved)).inRoot(isToast()).check(matches(isDisplayed()))

        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText("title2"))))
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText("description2"))))
        activityScenario.close()
    }
}