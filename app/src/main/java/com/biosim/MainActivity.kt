package com.biosim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.biosim.navigation.AppNavHost
import com.biosim.ui.theme.BiosimTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BiosimTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // AppNavHost maneja toda la navegación de la app
                    AppNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}