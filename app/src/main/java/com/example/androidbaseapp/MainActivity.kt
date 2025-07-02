package com.example.androidbaseapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.androidbaseapp.presentation.navigation.AndroidBaseNavigation
import com.example.androidbaseapp.ui.theme.AndroidBaseAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidBaseAppTheme {
                AndroidBaseNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}