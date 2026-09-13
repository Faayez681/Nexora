package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.PulseDatabase
import com.example.data.repository.PulseRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PulseViewModel
import com.example.viewmodel.PulseViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = PulseDatabase.getDatabase(applicationContext)
        val repository = PulseRepository(database.pulseDao())
        val factory = PulseViewModelFactory(repository)

        setContent {
            val viewModel: PulseViewModel = viewModel(factory = factory)
            val themeMode by viewModel.selectedThemeMode.collectAsStateWithLifecycle()
            val accentIndex by viewModel.accentColorIndex.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = themeMode, accentIndex = accentIndex) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Pulse Messenger $name", modifier = modifier)
}
