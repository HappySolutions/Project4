package com.udacity.project4.base

import androidx.appcompat.app.AppCompatActivity
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

// Sealed class used with the live data to navigate between the activities
sealed class IntentCommand {
    data class ToReminderActivity(val from: AppCompatActivity, val to: Class<RemindersActivity>) :
        IntentCommand()

    data class ToReminderDescriptionActivity(
        val from: AppCompatActivity, val to: Class<ReminderDescriptionActivity>,
        val item: ReminderDataItem
    ) : IntentCommand()

    data class BackTo(val activity: Class<AuthenticationActivity>) : IntentCommand()
}