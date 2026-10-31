package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.example.data.repository.ArishRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.ArishMessengerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: ArishRepository
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = ArishRepository(this)
        authViewModel = AuthViewModel(repository)

        setContent {
            val isDark = isSystemInDarkTheme()

            DisposableEffect(Unit) {
                onDispose {
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.updatePresence(false)
                    }
                }
            }

            ArishMessengerTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        repository = repository
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.updatePresence(true)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::repository.isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.updatePresence(false)
            }
        }
    }
}
