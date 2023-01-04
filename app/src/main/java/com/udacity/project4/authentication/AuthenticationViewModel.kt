package com.udacity.project4.authentication

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.base.BaseViewModel


class AuthenticationViewModel(val app: Application) :
    BaseViewModel(app) {

    private val _signInFlow = MutableLiveData<Boolean>()
    val signInFlow: LiveData<Boolean>
        get() = _signInFlow

    private val _navigateBackToAuth = MutableLiveData<Boolean>()
    val navigateBackToAuth: LiveData<Boolean>
        get() = _navigateBackToAuth

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    init {
        _signInFlow.value = false
        _navigateBackToAuth.value = false
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun logout(context: Context) {
        AuthUI.getInstance()
            .signOut(context)
            .addOnCompleteListener {
                deleteUserAccount(context)
            }
    }

    private fun deleteUserAccount(context: Context) {
        AuthUI.getInstance()
            .delete(context)
            .addOnCompleteListener {
                _navigateBackToAuth.value = true
            }
    }

    fun loginClicked() {
        _signInFlow.value = true
    }

    fun loginFlowDone() {
        _signInFlow.value = false
    }

    fun navigateBackToAuthDone() {
        _navigateBackToAuth.value = false
    }
}