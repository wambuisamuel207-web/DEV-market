package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.DevMarketDatabase
import com.example.data.repository.DevMarketRepository
import com.example.ui.DevMarketApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.DevMarketViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appScope = CoroutineScope(Dispatchers.Default)
        val database = DevMarketDatabase.getDatabase(applicationContext, appScope)
        val repository = DevMarketRepository(database.devMarketDao())

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DevMarketViewModel(repository) as T
            }
        }

        setContent {
            val viewModel: DevMarketViewModel = viewModel(factory = factory)
            val uiState = viewModel.uiState.value
            val isDark = viewModel.uiState.collectAsState()

            MyApplicationTheme(darkTheme = isDark.value.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DevMarketApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name! Welcome to DevMarket Escrow Platform.",
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
