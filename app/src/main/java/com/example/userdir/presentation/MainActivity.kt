package com.example.userdir.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.userdir.presentation.navigation.UserDirNavigation
import com.example.userdir.presentation.theme.UserDirectoryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserDirectoryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserDirNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}