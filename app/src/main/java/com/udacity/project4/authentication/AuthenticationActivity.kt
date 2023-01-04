package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.base.IntentCommand
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.android.ext.android.inject

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    val _viewModel: AuthenticationViewModel by inject()
    private lateinit var binding: ActivityAuthenticationBinding

    companion object {
        private const val SIGN_IN_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_authentication
        )

        binding.viewModel = _viewModel

        setupObservers()
    }

    private fun setupObservers() {
        _viewModel.signInFlow.observe(this) { launchSignInFlow ->
            if (launchSignInFlow) {
                launchSignInFlow()
                _viewModel.loginFlowDone()
            }
        }

        _viewModel.authenticationState.observe(this) { authenticationState ->
            if (authenticationState == AuthenticationViewModel.AuthenticationState.AUTHENTICATED &&
                _viewModel.navigateBackToAuth.value == false
            ) {
                navigateToRemindersActivity()
            }
        }

        _viewModel.navigateBackToAuth.observe(this) { navigateToAuth ->
            if (navigateToAuth) {
                _viewModel.navigateBackToAuthDone()
            }
        }

        _viewModel.intentCommand.observe(this) { command ->
            if (command is IntentCommand.ToReminderActivity) {
                val intent = Intent(command.from, command.to)
                startActivity(intent)
            }
        }
    }
    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.FirebaseTheme)
                .setLogo(R.drawable.map)
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                navigateToRemindersActivity()
            } else {
                Log.i("TAG", response?.error?.errorCode.toString())
            }
        }
    }

    private fun navigateToRemindersActivity() {
        _viewModel.intentCommand.postValue(
            IntentCommand.ToReminderActivity(this, RemindersActivity::class.java)
        )
    }
}
