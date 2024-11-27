package com.virtualstudios.extensionfunctions.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.virtualstudios.extensionfunctions.compose.ui.theme.ExtensionFunctionsTheme
import com.virtualstudios.extensionfunctions.core.presentation.ConnectivityViewModel

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExtensionFunctionsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                }
            }
        }
    }
}

@Composable
fun ConnectivityDemo(modifier: Modifier = Modifier) {
    /*val viewModel = viewModel<ConnectivityViewModel> {
        ConnectivityViewModel(
            connectivityObserver = AndroidConnectivityObserver(
                context = applicationContext
            )
        )
    }
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()*/
}