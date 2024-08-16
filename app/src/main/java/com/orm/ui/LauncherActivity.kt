package com.orm.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.orm.util.NetworkUtils
import com.orm.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        userViewModel.isTokenLoading.observe(this) { isTokenLoading ->
            if (!isTokenLoading) {
                handleToken(userViewModel.token.value)
            }
        }
    }

    private fun handleToken(token: String?) {
        val isNetworkAvailable = NetworkUtils.isNetworkAvailable(this)

        if (token.isNullOrEmpty()) {
            Log.d("LauncherActivity", "token is null or empty")
            navigateToActivity(LoginActivity::class.java)
        } else {
            Log.d("LauncherActivity", "token is not null or empty")
            if (isNetworkAvailable) {
                userViewModel.loginAuto()
            }
            navigateToActivity(MainActivity::class.java)
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        startActivity(Intent(this, activityClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }
}
