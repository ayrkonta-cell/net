package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.Modifier
import com.example.data.database.AppDatabase
import com.example.data.repository.NetworkRepository
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NetworkViewModel
import com.example.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Room Local Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NetworkRepository(database.networkHistoryDao())

        // 2. Build the Network Analytics ViewModel
        val factory = ViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[NetworkViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainDashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
