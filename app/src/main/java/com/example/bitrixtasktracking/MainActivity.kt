package com.example.bitrixtasktracking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitrixtasktracking.network.RetrofitClient
import com.example.bitrixtasktracking.ui.theme.BitrixTaskTrackingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Reprezentuje pojedyncze zadanie z Bitrixa
data class BitrixResponse(
    val result: BitrixResult
)

data class BitrixResult(
    val tasks: List<BitrixTask>
)

data class BitrixTask(
    val id: String,
    val title: String,
    val status: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: BitrixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitrixTaskTrackingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: BitrixViewModel, modifier: Modifier = Modifier) {
    val isFetching by viewModel.isFetching.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val tasksText by viewModel.tasksText.collectAsState()
    // DODANO: Nasłuchiwanie obecnego trybu
    val currentMode by viewModel.currentMode.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = statusText)

        Spacer(modifier = Modifier.height(16.dp))

        // POPRAWIONO: Ten przycisk ma pobierać dane
        Button(
            onClick = { viewModel.fetchData(isAuto = false) },
            enabled = !isFetching
        ) {
            Text(text = "Pobierz ręcznie")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // POPRAWIONO: Ten przycisk ma przełączać tryb
        Button(
            onClick = { viewModel.toggleMode() },
            enabled = !isFetching
        ) {
            Text(text = "Przełącz tryb (obecnie: $currentMode)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = tasksText,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        )
    }
}

class BitrixViewModel : ViewModel() {

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    private val _statusText = MutableStateFlow("Oczekuję na akcję...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _tasksText = MutableStateFlow("")
    val tasksText: StateFlow<String> = _tasksText.asStateFlow()

    private val _currentMode = MutableStateFlow("tasks")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    var currentFilter = "Wszystkie"

    fun toggleMode() {
        _currentMode.value = if (_currentMode.value == "tasks") "groups" else "tasks"
    }

    fun fetchData(isAuto: Boolean = false) {
        if (_isFetching.value) return
        _isFetching.value = true

        if (_currentMode.value == "tasks") {
            _statusText.value = if (isAuto) "Automatyczne odświeżanie..." else "Pobieranie zadań..."
            viewModelScope.launch(Dispatchers.IO) {
                bgFetchAllTasks(isAuto)
            }
        } else if (_currentMode.value == "groups") {
            _statusText.value = "Pobieranie grup..."
            viewModelScope.launch(Dispatchers.IO) {
                bgFetchAllGroups(isAuto)
            }
        }
    }

    private suspend fun bgFetchAllTasks(isAuto: Boolean) {
        if (currentFilter == "W trakcie") {
            fetchInProgressTasks(isAuto)
        } else {
            fetchStandardTasks(isAuto)
        }
    }

    private suspend fun fetchInProgressTasks(isAuto: Boolean) {
        delay(2000)
        _statusText.value = "Pobrano zadania 'W trakcie'!"
        _isFetching.value = false
    }

    private suspend fun fetchStandardTasks(isAuto: Boolean) {
        try {
            val response = RetrofitClient.api.getTasks()
            val json = response.string()

            _statusText.value = "Pobrano dane!"
            // POPRAWIONO: Kod poprawnie zapisuje JSON w stanie aplikacji
            _tasksText.value = json

        } catch (e: Exception) {
            _statusText.value = "Błąd komunikacji z Bitrixem"
            // POPRAWIONO: Zamiast "Aaa" wyświetli się konkretna przyczyna błędu
            _tasksText.value = "Wyjątek: ${e.javaClass.simpleName}\nTreść: ${e.message}"
        } finally {
            _isFetching.value = false
        }
    }

    private suspend fun bgFetchAllGroups(isAuto: Boolean) {
        delay(2000)
        _statusText.value = "Pobrano grupy!"
        _isFetching.value = false
    }
}