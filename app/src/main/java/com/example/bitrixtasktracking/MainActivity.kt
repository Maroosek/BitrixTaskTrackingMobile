package com.example.bitrixtasktracking

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitrixtasktracking.network.RetrofitClient
import com.example.bitrixtasktracking.ui.theme.BitrixTaskTrackingTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.google.gson.reflect.TypeToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavController

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect


// Reprezentuje pojedyncze zadanie z Bitrixa
data class BitrixResponse(
    @SerializedName("result") val result: BitrixResult,
    @SerializedName("next") val next: Int? = null
)

data class BitrixResult(
    @SerializedName("tasks") val tasks: List<BitrixTask>
)

data class SingleTaskResponse(
    @SerializedName("result") val result: SingleTaskResult
)

data class SingleTaskResult(
    @SerializedName("task") val task: BitrixTask
)

data class BitrixTask(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("realStatus") val realStatus: String?,
    @SerializedName("timeSpentInLogs") val timeSpent: Double?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("activityDate") val activity: String?,
    @SerializedName("createdDate") val createdAt: String?,
    @SerializedName("responsibleId") val responsible: String?,
    @SerializedName("chatId") val chatId: String?,
    @SerializedName("CHAT_ID") val chatIdAlt: String?
)

class MainActivity : ComponentActivity() {

    private val viewModel: BitrixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BitrixTaskTrackingTheme {
                // DODANO: Kontroler nawigacji
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // DODANO: NavHost zarządza tym, który ekran jest obecnie wyświetlany
                    NavHost(
                        navController = navController,
                        startDestination = "taskList",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Ekran 1: Główna lista
                        composable("taskList") {
                            MainScreen(
                                viewModel = viewModel,
                                navController = navController // Przekazujemy kontroler w dół
                            )
                        }

                        // Ekran 2: Szczegóły zadania (z dynamicznym parametrem {taskId})
                        composable(
                            route = "taskDetail/{taskId}",
                            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // Wyciągamy przekazane ID z argumentów
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: "Brak ID"

                            // Wywołujemy nowy ekran
                            TaskDetailScreen(taskId = taskId, navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: BitrixViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isFetching by viewModel.isFetching.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val tasksText by viewModel.tasksText.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()

    // DODANO: Pobieramy listę tasków z ViewModelu
    val tasksList by viewModel.tasksList.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("Pobieranie tasków", "Wszystkie aktywne", "Surowe")

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = statusText)
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                Button(onClick = { expanded = true }, enabled = !isFetching) {
                    Text(text = "Tryb testowy: $currentFilter")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.setFilter(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { viewModel.fetchData(isAuto = false) }, enabled = !isFetching) {
                Text(text = "Pobierz ręcznie")
            }
        }

        // DODANO: Logika wyświetlania danych
        if (currentFilter == "Surowe") {
            // Pokazujemy surowy JSON tylko dla tego trybu
            Text(
                text = tasksText,
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            )
        } else {
            // Pokazujemy ładną listę kafelków
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(tasksList) { task ->
                    TaskCard(task = task, onClick = {
                        if (task.id != null) {
                            navController.navigate("taskDetail/${task.id}")
                        } else {
                            viewModel._statusText.value = "To zadanie nie ma ID!"
                        }
                    })
                }
            }
        }
    }
}

// DODANO: Nowy komponent - pojedynczy kafelek zadania
@Composable
fun TaskCard(task: BitrixTask, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }, // To sprawia, że cały kafelek reaguje na kliknięcie
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title ?: "Brak tytułu", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "ID: ${task.id} | Status: ${task.status}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Text(text = "Deadline: ${task.deadline ?: "Brak"}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: BitrixViewModel // Musisz podać viewModel w wywołaniu z NavHost!
) {
    // To wywoła się tylko raz, gdy ekran zostanie załadowany
    LaunchedEffect(key1 = taskId) {
        viewModel.fetchTaskDetails(taskId)
    }

    val task by viewModel.selectedTask.collectAsState()
    val statusText by viewModel.statusText.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Dodany przycisk powrotu na górę
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = { navController.popBackStack() }) {
                Text("< Wróć")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (task == null) {
            // Jeśli obiekt jest pusty, pokazujemy status (Ładowanie lub Błąd)
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = statusText)
        } else {
            // Właściwy interfejs szczegółów (odpowiednik _build_task_window_ui)
            Text(
                text = task?.title ?: "Brak nazwy",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID: ${task?.id}")
                    Text("Status: ${task?.status}")
                    Text("Czas pracy: ${(task?.timeSpent ?: 0.0) / 60} h")
                    Text("Deadline: ${task?.deadline ?: "Nieokreślony"}")
                    Text("Odpowiedzialny: ${task?.responsible ?: "Brak"}")

                    val chatIdToDisplay = task?.chatId ?: task?.chatIdAlt
                    if (chatIdToDisplay != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Powiązany czat: $chatIdToDisplay",
                            color = androidx.compose.ui.graphics.Color.Blue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = statusText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

class BitrixViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedTask = MutableStateFlow<BitrixTask?>(null)

    val selectedTask: StateFlow<BitrixTask?> = _selectedTask.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    val _statusText = MutableStateFlow("Oczekuję na akcję...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _tasksText = MutableStateFlow("")
    val tasksText: StateFlow<String> = _tasksText.asStateFlow()

    private val _currentMode = MutableStateFlow("tasks")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    private val _currentFilter = MutableStateFlow("Pobieranie tasków")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    private val _tasksList = MutableStateFlow<List<BitrixTask>>(emptyList())

    val tasksList: StateFlow<List<BitrixTask>> = _tasksList.asStateFlow()

    private val cacheManager = JsonUtil(application)

    init {
        checkJson()
    }

    fun setFilter(newFilter: String) {
        _currentFilter.value = newFilter
    }

    fun checkJson() {
        val cachedData = cacheManager.readJson()
        if (cachedData != null && _tasksList.value.isEmpty()) {
            _statusText.value = "Wyświetlam dane z pamięci podręcznej..."
            _tasksText.value = cachedData // Zostawiamy dla trybu "Surowe"

            // DODANO: Zamiana zapisanego JSONa z powrotem na listę obiektów
            try {
                val listType = object : TypeToken<List<BitrixTask>>() {}.type
                val tasks: List<BitrixTask> = Gson().fromJson(cachedData, listType)
                _tasksList.value = tasks
            } catch (e: Exception) {
                // Obsługa błędu, jeśli JSON w cache nie pasuje do modelu
            }
        }
    }

    fun fetchTaskDetails(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _statusText.value = "Pobieranie szczegółów zadania $taskId..."
            // Zabezpieczenie: czyścimy stary widok przed załadowaniem nowego
            _selectedTask.value = null

            try {
                // 1. Próba pobrania z API
                val response = RetrofitClient.api.getTaskDetails(taskId)
                val taskData = response.result.task

                // 2. Zapis do cache (odpowiednik json.dump)
                val finalJson = Gson().toJson(taskData)
                cacheManager.saveTaskDetail(taskId, finalJson)

                // 3. Wysłanie danych do interfejsu
                _selectedTask.value = taskData
                _statusText.value = "Pobrano szczegóły z sieci!"

                // 4. Tutaj uruchamiamy wątek wiadomości (jak w Pythonie)
                triggerChatFetch(taskData)

            } catch (e: Exception) {
                // 5. Błąd API - próbujemy czytać z lokalnego pliku (odpowiednik bloku except)
                val cachedJson = cacheManager.readTaskDetail(taskId)
                if (cachedJson != null) {
                    val taskData = Gson().fromJson(cachedJson, BitrixTask::class.java)
                    _selectedTask.value = taskData
                    _statusText.value = "Brak sieci. Wczytano lokalną kopię."

                    triggerChatFetch(taskData)
                } else {
                    // Brak i internetu, i pliku (odpowiednik messagebox.showerror)
                    _statusText.value = "Błąd: Brak internetu i brak lokalnej kopii zadania."
                }
            }
        }
    }

    private fun triggerChatFetch(task: BitrixTask) {
        // Zabezpieczenie przed brakiem ID lub inną wielkością liter
        val chatId = task.chatId ?: task.chatIdAlt
        if (chatId != null) {
            // Zamiast threading.Thread odpalamy nową korutynę w tle
            viewModelScope.launch(Dispatchers.IO) {
                bgFetchMessages(task, chatId)
            }
        }
    }

    private suspend fun bgFetchMessages(task: BitrixTask, chatId: String) {
        // Tutaj w przyszłości dodasz logikę pobierania wiadomości chatu
        println("Rozpoczęto pobieranie chatu $chatId dla zadania ${task.id}")
    }


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
        // POPRAWIONO: Wywołanie odpowiedniej funkcji na podstawie wybranego filtra
        when (_currentFilter.value) {
            "Wszystkie aktywne" -> fetchInProgressTasks(isAuto)
            "Surowe" -> fetchStandardTasksRaw(isAuto)
            else -> fetchStandardTasks(isAuto) // Domyślnie "Pobieranie tasków"
        }
    }

    private suspend fun fetchInProgressTasks(isAuto: Boolean) {
        try {
            _statusText.value = "Rozpoczynam pobieranie zadań 'W trakcie'..."

            val allTasks = mutableListOf<BitrixTask>()
            var start = 0

            val dateStr = LocalDateTime.now().minusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00+01:00"))

            while (true) {
                // Wywołanie API z dynamicznymi parametrami
                val response = RetrofitClient.api.getTasks(
                    start = start,
                    realStatus = 3,
                    activityDate = dateStr
                )

                val tasksBatch = response.result.tasks

                if (tasksBatch.isEmpty()) {
                    break
                }

                allTasks.addAll(tasksBatch)
                _statusText.value = "Pobrano ${allTasks.size} zadań 'W trakcie'..."

                val next = response.next
                if (next != null) {
                    start = next
                } else {
                    break
                }
            }

            val finalJson = Gson().toJson(allTasks)
            cacheManager.saveJson(finalJson)

            _statusText.value = "Zakończono pobieranie! Razem: ${allTasks.size} zadań."
            _tasksText.value = finalJson

            _tasksList.value = allTasks

        } catch (e: Exception) {
            _statusText.value = "Błąd komunikacji z API"
            _tasksText.value = "Wyjątek: ${e.javaClass.simpleName}\nTreść: ${e.message}"
        } finally {
            _isFetching.value = false
        }
    }

    private suspend fun fetchStandardTasks(isAuto: Boolean) {
        try {
            _statusText.value = "Rozpoczynam pobieranie wszystkich zadań..."

            val allTasks = mutableListOf<BitrixTask>()
            var start = 0

            // Pętla pobierająca paczki po 50 elementów
            while (true) {
                val response = RetrofitClient.api.getTasks(start = start)
                val tasksBatch = response.result.tasks

                if (tasksBatch.isEmpty()) {
                    break
                }

                allTasks.addAll(tasksBatch)
                _statusText.value = "Pobrano ${allTasks.size} zadań..."

                val next = response.next
                if (next != null) {
                    start = next
                } else {
                    break
                }
            }

            // DODANO: Odwrócenie całej listy w miejscu (najnowsze trafiają na indeks 0)
            allTasks.reverse()

            val fetchedTasksStandard = Gson().toJson(allTasks)
            cacheManager.saveJson(fetchedTasksStandard)

            _statusText.value = "Pobrano wszystkie zadania!"
            _tasksText.value = fetchedTasksStandard // Możesz usunąć budowanie StringBuildera!
            // DODANO: Zapisujemy listę do stanu
            _tasksList.value = allTasks

            // Budujemy tekst do wyświetlenia na ekranie dla celów testowych
            val stringBuilder = StringBuilder()
            stringBuilder.append("Łącznie pobrano: ${allTasks.size} zadań\n\n")

            allTasks.forEach { task ->
                // POPRAWIONO: Bezpieczna konwersja String na Int przed dzieleniem
                val timeSpent = task.timeSpent?.div(60)

                stringBuilder.append("ID: ${task.id} | ${task.title}\n")
                stringBuilder.append("Status: ${task.status} | Deadline: ${task.deadline}\n")
                stringBuilder.append("Czas: ${timeSpent} h\n")
                stringBuilder.append("Aktywność: ${task.activity}\n")
                stringBuilder.append("Utworzono: ${task.createdAt}\n")
                stringBuilder.append("Odpowiedzialny: ${task.responsible}\n")
                stringBuilder.append("----------------------------\n")
            }

            _statusText.value = "Pobrano wszystkie zadania!"
            _tasksText.value = stringBuilder.toString()

            // Cache zapisze listę już w odwróconej, poprawnej kolejności
            val fetchedTasks = Gson().toJson(allTasks)
            cacheManager.saveJson(fetchedTasks)

        } catch (e: Exception) {
            _statusText.value = "Błąd pobierania"
            _tasksText.value = "Wyjątek: ${e.javaClass.simpleName}\nTreść: ${e.message}"
        } finally {
            _isFetching.value = false
        }
    }

    private suspend fun fetchStandardTasksRaw(isAuto: Boolean) {
        try {
            // Najpierw próbujemy pokazać dane z cache, żeby użytkownik nie czekał
            val cachedData = cacheManager.readJson()
            if (cachedData != null && _tasksText.value.isEmpty()) {
                _statusText.value = "Wyświetlam dane z pamięci podręcznej..."
                _tasksText.value = cachedData
            }

            // Pobieramy świeże dane z webhooka
            _statusText.value = "Pobieranie świeżych danych..."
            val response = RetrofitClient.api.getTasksRaw()
            val json = response.string()

            // Zapisujemy nowy JSON do pliku (nadpisujemy stary)
            cacheManager.saveJson(json)

            _statusText.value = "Pobrano i zapisano dane!"
            _tasksText.value = json

        } catch (e: Exception) {
            // Jeśli nie ma internetu, a mamy cache, poinformuj o tym
            val cachedData = cacheManager.readJson()
            if (cachedData != null) {
                _statusText.value = "Brak sieci. Pokazuję ostatnio zapisane dane."
                _tasksText.value = cachedData
            } else {
                _statusText.value = "Błąd komunikacji z Bitrixem i brak danych w cache."
                _tasksText.value = "Wyjątek: ${e.javaClass.simpleName}\nTreść: ${e.message}"
            }
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