package com.udacity.project4.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
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
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun setupObservers() {
        TODO("Not yet implemented")
    }
}
