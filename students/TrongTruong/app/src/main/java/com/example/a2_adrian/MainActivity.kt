package com.example.a2_adrian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.a2_adrian.nav.AppNavHost
import com.example.a2_adrian.ui.theme.A2AdrianTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A2AdrianTheme(darkTheme = true, dynamicColor = false) {
                val navController = rememberNavController()
                Scaffold(containerColor = MaterialTheme.colorScheme.background) {
                    AppNavHost(navController)
                }
            }
        }
    }
}
