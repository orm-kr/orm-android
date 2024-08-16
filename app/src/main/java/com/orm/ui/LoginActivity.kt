package com.orm.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.orm.BuildConfig
import com.orm.databinding.ActivityLoginBinding
import com.orm.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    companion object {
        const val ADDRESS = "users/login/kakao"
    }

    private val userViewModel: UserViewModel by viewModels()
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var webView: WebView
    private lateinit var btnLogin: ImageButton

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupWebView()
        setupLoginButton()

        clearWebViewCache()
        clearWebViewCookies()

        userViewModel.getAccessToken()
        userViewModel.isTokenLoading.observe(this) { isLoading ->
            if (!isLoading) {
                userViewModel.token.observe(this) { token ->
                    if (!token.isNullOrEmpty()) {
                        navigateToMainActivity()
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        Log.d("LoginActivity", "setupWebView")
        webView = binding.webview.apply {
            settings.domStorageEnabled = true
            settings.javaScriptEnabled = true
            webViewClient = createWebViewClient()
        }
    }

    private fun createWebViewClient() = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            if (url != null && url.startsWith(BuildConfig.BASE_URL + ADDRESS)) {
                userViewModel.loginKakao(url.substringAfter("code="))
                return true
            }
            return false
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?,
        ) {
            handler?.proceed()
        }
    }

    private fun setupLoginButton() {
        btnLogin = binding.btnLogin
        btnLogin.setOnClickListener {
            Log.d("LoginActivity", "clickLoginButton")
            webView.apply {
                loadUrl(BuildConfig.BASE_URL + ADDRESS)
                visibility = WebView.VISIBLE
            }
        }
    }

    private fun navigateToMainActivity() {
        userViewModel.getFirebaseToken()
        startActivity(Intent(this, LauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
        finish()
    }

    private fun clearWebViewCache() {
        webView.clearCache(true)
    }

    private fun clearWebViewCookies() {
        val cookieManager = android.webkit.CookieManager.getInstance()
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

}
