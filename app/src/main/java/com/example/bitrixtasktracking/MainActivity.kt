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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton

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
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.Send
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.IconButton

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding


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
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("timeSpentInLogs") val timeSpent: Double?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("activityDate") val activity: String?,
    @SerializedName("createdDate") val createdAt: String?,
    @SerializedName("chatId") val chatId: Int?, // Zmiana na Int
    @SerializedName("chat_Id") val chatIdAlt: Int?, // Zmiana na Int
    @SerializedName("creator") val creator: BitrixUser?,
    @SerializedName("responsible") val responsible: BitrixUser?,
    @SerializedName("accomplicesData") val accomplicesData: Map<String, BitrixUser>?

)

data class BitrixUser(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("workPosition") val workPosition: String?
)

data class ChatResponse(
    @SerializedName("result") val result: ChatResult?
)

data class ChatResult(
    @SerializedName("messages") val messages: List<ChatMessage>?
)

data class ChatMessage(
    @SerializedName("id") val id: Int?,
    @SerializedName("text") val text: String?,
    @SerializedName("author_id") val authorId: String?,
    @SerializedName("date") val date: String?
)

data class UsersResponse(
    @SerializedName("result") val result: List<BitrixGlobalUser>?,
    @SerializedName("next") val next: Int?
)

data class BitrixGlobalUser(
    @SerializedName("ID") val id: String?,
    @SerializedName("NAME") val name: String?,
    @SerializedName("LAST_NAME") val lastName: String?,
    @SerializedName("PERSONAL_PHOTO") val photo: String?
)

// Klasa pomocnicza do budowania sesji
data class TrackerSession(
    val start: ZonedDateTime,
    var stop: ZonedDateTime? = null
)

// Model danych dla pojedynczego użytkownika
data class UserTimeSummary(
    val userName: String,
    val totalSeconds: Double,
    val todaySeconds: Double,
    val isActive: Boolean,
    val activeStartDt: ZonedDateTime?
)

data class UserProfile(
    val name: String,
    val photoUrl: String?
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
// Formatyzer czasu na żywo (np. 01:25:10)
fun formatTimeSpentLive(seconds: Double?): String {
    if (seconds == null || seconds <= 0.0) return "00:00:00"
    val totalSeconds = seconds.toLong()
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}

fun getFullAvatarUrl(iconPath: String?): String {
    if (iconPath.isNullOrEmpty()) return ""
    // Zmień "https://twojadomena.bitrix24.pl" na swój właściwy adres
    return if (iconPath.startsWith("/")) {
        "https://jenaeuropa.bitrix24.pl$iconPath"
    } else {
        iconPath
    }
}



// Przelicza sekundy na czytelny format (np. "5h 19m")
fun formatTimeSpent(seconds: Double?): String {
    if (seconds == null || seconds <= 0.0) return "0h 0m"
    val totalMinutes = (seconds / 60).toLong()
    val hours = totalMinutes / 60
    val mins = totalMinutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

private fun calculateTimeSummaries(messages: List<ChatMessage>): List<UserTimeSummary> {
    val userSessions = mutableMapOf<String, MutableList<TrackerSession>>()
    val openStarts = mutableMapOf<String, ZonedDateTime>()

    val startRegex = Regex("\\[USER=\\d+\\](.*?)\\[/USER\\]\\s+włączył[a]?\\s+śledzenie\\s+czasu", RegexOption.IGNORE_CASE)
    val stopRegex = Regex("\\[USER=\\d+\\](.*?)\\[/USER\\]\\s+wyłączył[a]?\\s+śledzenie\\s+czasu", RegexOption.IGNORE_CASE)
    val finishRegex = Regex("(ukończył|zakończył)[a]?\\s+zadanie", RegexOption.IGNORE_CASE)

    val todayDate = ZonedDateTime.now().toLocalDate()

    // Przetwarzanie od najstarszych do najnowszych wiadomości
    for (msg in messages.reversed()) {
        val text = msg.text ?: continue
        val rawDate = msg.date ?: continue

        val dt = try {
            ZonedDateTime.parse(rawDate)
        } catch (e: Exception) { continue }

        val startMatch = startRegex.find(text)
        val stopMatch = stopRegex.find(text)
        val finishMatch = finishRegex.find(text)

        when {
            startMatch != null -> {
                val user = startMatch.groupValues[1].trim()
                openStarts[user] = dt
                userSessions.getOrPut(user) { mutableListOf() }.add(TrackerSession(dt))
            }
            stopMatch != null -> {
                val user = stopMatch.groupValues[1].trim()
                val sessions = userSessions[user] ?: continue
                for (sess in sessions.reversed()) {
                    if (sess.stop == null) {
                        sess.stop = dt
                        break
                    }
                }
                openStarts.remove(user)
            }
            finishMatch != null -> {
                for (user in openStarts.keys.toList()) {
                    val sessions = userSessions[user] ?: continue
                    for (sess in sessions.reversed()) {
                        if (sess.stop == null) {
                            sess.stop = dt
                            break
                        }
                    }
                }
                openStarts.clear()
            }
        }
    }

    val summaries = mutableListOf<UserTimeSummary>()
    for ((user, sessions) in userSessions) {
        if (sessions.isEmpty()) continue

        var totalElapsed = 0.0
        var todayElapsed = 0.0
        var isActive = false
        var activeStart: ZonedDateTime? = null

        for (sess in sessions) {
            if (sess.stop == null) {
                isActive = true
                activeStart = sess.start
            } else {
                val elapsed = java.time.Duration.between(sess.start, sess.stop).seconds.toDouble()
                totalElapsed += maxOf(0.0, elapsed)

                if (sess.start.toLocalDate() == todayDate || sess.stop!!.toLocalDate() == todayDate) {
                    todayElapsed += maxOf(0.0, elapsed)
                }
            }
        }

        summaries.add(
            UserTimeSummary(
                userName = user,
                totalSeconds = totalElapsed,
                todaySeconds = todayElapsed,
                isActive = isActive,
                activeStartDt = activeStart
            )
        )
    }
    return summaries
}

// Formatuje datę ISO z Bitrixa na polski format
fun formatBitrixDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Brak daty"
    return try {
        val parsedDate = ZonedDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        parsedDate.format(formatter)
    } catch (e: Exception) {
        dateString // W razie błędu zwraca oryginalny tekst
    }
}

@Composable
fun UserProfileRow(user: BitrixUser?, roleLabel: String) {
    if (user == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        AsyncImage(
            model = getFullAvatarUrl(user.icon),
            contentDescription = "Avatar użytkownika",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = roleLabel, style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.Gray)
            Text(text = user.name ?: "Nieznany", style = MaterialTheme.typography.bodyMedium)
            if (!user.workPosition.isNullOrEmpty()) {
                Text(text = user.workPosition, style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
            }
        }
    }
}

@Composable
fun ExpandableDescription(description: String, maxLinesCollapsed: Int = 7) {
    var isExpanded by remember { mutableStateOf(false) }
    var showReadMoreButton by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize() // Płynna animacja rozwijania/zwijania
    ) {
        Text(
            text = "Opis zadania:",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isExpanded) Int.MAX_VALUE else maxLinesCollapsed,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.hasVisualOverflow) {
                    showReadMoreButton = true
                }
            },
            modifier = Modifier.padding(top = 4.dp)
        )

        // Pokazujemy przycisk tylko jeśli tekst jest za długi
        if (showReadMoreButton) {
            Text(
                text = if (isExpanded) "Zwiń opis" else "Czytaj dalej...",
                style = MaterialTheme.typography.labelMedium,
                color = androidx.compose.ui.graphics.Color(0xFF1565C0), // Ładny niebieski kolor linku
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 8.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun AccomplicesRow(accomplices: Map<String, BitrixUser>?) {
    // Jeśli lista jest pusta lub null, nic nie rysujemy
    if (accomplices.isNullOrEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(
            text = "Uczestnicy (${accomplices.size}):",
            style = MaterialTheme.typography.labelMedium,
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Pozioma lista zapobiegająca wydłużaniu ekranu w dół
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(accomplices.values.toList()) { user ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val avatarUrl = getFullAvatarUrl(user.icon)

                    if (avatarUrl.isNotEmpty() && !avatarUrl.endsWith("default_avatar.png")) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        // Zastępczy, domyślny avatar (zamiast strzałki)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Domyślny avatar",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Wyświetlamy tylko pierwsze imię/słowo, by oszczędzić miejsce
                    Text(
                        text = user.name?.substringBefore(" ") ?: "Nieznany",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
    val filterOptions = listOf("Pobieranie tasków", "Wszystkie aktywne")

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
//        if (currentFilter == "Surowe") {
//            // Pokazujemy surowy JSON tylko dla tego trybu
//            Text(
//                text = tasksText,
//                modifier = Modifier
//                    .padding(16.dp)
//                    .verticalScroll(rememberScrollState())
//            )
//        } else {
            // Czysto i elegancko wstrzykujemy wyizolowany komponent
            TaskListWithFab(
                tasksList = tasksList,
                navController = navController,
                viewModel = viewModel
            )
        //}
    }
}


// DODANO: Nowy komponent - pojedynczy kafelek zadania
// Zaktualizowany komponent - pojedynczy kafelek zadania
@Composable
fun TaskCard(task: BitrixTask, onClick: () -> Unit) {
    // Mapowanie statusów liczbowych na tekst
    val statusText = when (task.status) {
        1 -> "Nowe"
        2 -> "Oczekujące"
        3 -> "W trakcie"
        4 -> "Do kontroli"
        5 -> "Zakończone"
        6 -> "Odłożone"
        else -> "Nieznany (${task.status})"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tytuł
            Text(
                text = task.title ?: "Brak tytułu",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ID i zmapowany Status
            Text(
                text = "ID: ${task.id} | Status: $statusText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Daty
            Text(
                text = "Utworzono: ${task.createdAt ?: "Brak danych"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Deadline: ${task.deadline ?: "Brak"}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(thickness = 0.5.dp, color = androidx.compose.ui.graphics.Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            // Osoby przypisane
            Text(
                text = "Zleceniodawca: ${task.creator?.name ?: "Nieznany"}",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.DarkGray
            )
            Text(
                text = "Odpowiedzialny: ${task.responsible?.name ?: "Nieznany"}",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.DarkGray
            )
//            Text(
//                text = "Uczestnicy: ${task.accomplices?.name ?: "Nieznany"}",
//                style = MaterialTheme.typography.bodySmall,
//                color = androidx.compose.ui.graphics.Color.DarkGray
//            )
        }
    }
}

@Composable
fun TimeTrackerSection(summaries: List<UserTimeSummary>, baseTaskTime: Double?) {
    // Odpowiada za cykliczne odświeżanie interfejsu (Live Ticker)
    var currentTime by remember { mutableStateOf(ZonedDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = ZonedDateTime.now()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD)) // Lekko niebieskie tło jak raport
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("⏱ Raport czasu pracy", style = MaterialTheme.typography.titleMedium)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (summaries.isEmpty() && (baseTaskTime == null || baseTaskTime <= 0.0)) {
                Text("Brak historii czasu.", style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.Gray)
            } else {
                var totalLiveAdditionalTime = 0.0

                summaries.forEach { summary ->
                    var displayTotal = summary.totalSeconds
                    var displayToday = summary.todaySeconds

                    if (summary.isActive && summary.activeStartDt != null) {
                        val activeElapsed = java.time.Duration.between(summary.activeStartDt, currentTime).seconds.toDouble()
                        val elapsedToAdd = maxOf(0.0, activeElapsed)

                        displayTotal += elapsedToAdd
                        displayToday += elapsedToAdd
                        totalLiveAdditionalTime += elapsedToAdd
                    }

                    val todayStr = if (displayToday > 0) " (dziś: ${formatTimeSpentLive(displayToday)})" else ""
                    val activeMarker = if (summary.isActive) " 🔴 aktywny" else ""

                    Text(
                        text = "👤 ${summary.userName}  ${formatTimeSpentLive(displayTotal)}$todayStr$activeMarker",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (summary.isActive) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                val grandTotal = (baseTaskTime ?: 0.0) + totalLiveAdditionalTime

                if (totalLiveAdditionalTime > 0) {
                    Text(
                        text = "Razem: ${formatTimeSpentLive(baseTaskTime)} + ${formatTimeSpentLive(totalLiveAdditionalTime)} = ${formatTimeSpentLive(grandTotal)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF1565C0)
                    )
                } else {
                    Text(
                        text = "Razem: ${formatTimeSpentLive(grandTotal)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TaskListWithFab(
    tasksList: List<BitrixTask>,
    navController: NavController,
    viewModel: BitrixViewModel
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
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

        // Tutaj kompilator bez problemu użyje standardowego AnimatedVisibility,
        // bo nie jest uwięziony wewnątrz ColumnScope.
        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Wróć na górę"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: BitrixViewModel
) {
    LaunchedEffect(key1 = taskId) {
        viewModel.fetchTaskDetails(taskId)
    }

    val task by viewModel.selectedTask.collectAsState()
    val statusText by viewModel.statusText.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val usersMap by viewModel.usersMap.collectAsState()
    val timeSummaries by viewModel.timeSummaries.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Zawijamy cały ekran w Box, aby móc umieścić pływające przyciski nad treścią
    Box(modifier = Modifier.fillMaxSize()) {

        // Główna, przewijana zawartość
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Puste miejsce na samej górze, aby przycisk "Wróć" nie zasłaniał tytułu
            Spacer(modifier = Modifier.height(72.dp))

            if (task == null) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = statusText)
            } else {
                Text(
                    text = task?.title ?: "Brak nazwy",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Karta ze szczegółami zadania
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ID: ${task?.id}", style = MaterialTheme.typography.bodyMedium)
                        Text("Status: ${task?.status}", style = MaterialTheme.typography.bodyMedium)
                        Text("Czas pracy: ${formatTimeSpent(task?.timeSpent)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Deadline: ${formatBitrixDate(task?.deadline)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Utworzono: ${formatBitrixDate(task?.createdAt)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Karta z osobami przypisanymi
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        UserProfileRow(user = task?.creator, roleLabel = "Zleceniodawca")
                        Spacer(modifier = Modifier.height(8.dp))
                        UserProfileRow(user = task?.responsible, roleLabel = "Odpowiedzialny")

                        AccomplicesRow(accomplices = task?.accomplicesData)
                    }
                }

                // Raport czasu
                if (timeSummaries.isNotEmpty() || (task?.timeSpent != null && task!!.timeSpent!! > 0.0)) {
                    TimeTrackerSection(
                        summaries = timeSummaries,
                        baseTaskTime = task?.timeSpent
                    )
                }

                // Opis
                if (!task?.description.isNullOrEmpty()) {
                    ExpandableDescription(description = task!!.description!!)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Czat
                if (chatMessages.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        text = "Czat zadania (${chatMessages.size} wiadomości)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    chatMessages.forEach { message ->
                        val isSystem = message.authorId == "0"
                        val authorProfile = usersMap[message.authorId]
                        val authorName = if (isSystem) "System" else (authorProfile?.name ?: "Nieznany ID: ${message.authorId}")
                        val avatarUrl = if (isSystem) "" else getFullAvatarUrl(authorProfile?.photoUrl)
                        val messageDate = formatBitrixDate(message.date)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystem) androidx.compose.ui.graphics.Color(0xFFFFF9C4) else androidx.compose.ui.graphics.Color(0xFFF0F0F0)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (avatarUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = avatarUrl,
                                                contentDescription = "Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        } else if (!isSystem) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Brak avatara",
                                                modifier = Modifier.size(24.dp).clip(CircleShape),
                                                tint = androidx.compose.ui.graphics.Color.Gray
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        Text(
                                            text = authorName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSystem) androidx.compose.ui.graphics.Color.DarkGray else androidx.compose.ui.graphics.Color.Black
                                        )
                                    }

                                    Text(
                                        text = messageDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = androidx.compose.ui.graphics.Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = message.text ?: "[Brak tekstu]",
                                    style = if (isSystem) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                    fontStyle = if (isSystem) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                )
                            }
                        }
                    }
                } else if (task?.chatId != null || task?.chatIdAlt != null) {
                    Text("Trwa pobieranie wiadomości...", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = statusText, style = MaterialTheme.typography.bodySmall)

                // Dodatkowe puste miejsce na dole, by przycisk "+" nie przysłonił czatu
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Delikatniejszy kolor
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wróć")
        }


        if (task != null && (task?.chatId != null || task?.chatIdAlt != null)) {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj wiadomość")
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                // Upewnia się, że klawiatura ładnie przesuwa okienko do góry
                //windowInsets = WindowInsets.ime
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Text(
                        text = "Nowa wiadomość",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Wpisz treść...") },
                            maxLines = 4 // Pozwala na wpisanie dłuższego tekstu przed włączeniem scrolla
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Przycisk wysyłania okrągły
                        IconButton(
                            onClick = {
                                val chatId = task?.chatId ?: task?.chatIdAlt
                                if (chatId != null && messageText.isNotBlank()) {
                                    // 1. Wysyłamy wiadomość
                                    viewModel.sendMessage(taskId, chatId, messageText)
                                    // 2. Czyścimy pole
                                    messageText = ""
                                    // 3. Chowamy okienko z animacją
                                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showBottomSheet = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Wyślij",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

class BitrixViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedTask = MutableStateFlow<BitrixTask?>(null)

    // Słownik wszystkich użytkowników: kluczem jest ID
    private val _usersMap = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val usersMap: StateFlow<Map<String, UserProfile>> = _usersMap.asStateFlow()

    val selectedTask: StateFlow<BitrixTask?> = _selectedTask.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

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

    private val _timeSummaries = MutableStateFlow<List<UserTimeSummary>>(emptyList())
    val timeSummaries: StateFlow<List<UserTimeSummary>> = _timeSummaries.asStateFlow()

    init {
        checkJson()
        loadAllUsers()
    }

    fun setFilter(newFilter: String) {
        _currentFilter.value = newFilter
    }

    private fun loadAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Najpierw czytamy z pamięci (jak w Pythonie self.users_map)
            val cachedData = cacheManager.readUsersList()
            if (cachedData != null) {
                try {
                    val mapType = object : TypeToken<Map<String, UserProfile>>() {}.type
                    _usersMap.value = Gson().fromJson(cachedData, mapType)
                } catch (e: Exception) {
                    println("Błąd odczytu lokalnych użytkowników")
                }
            }

            // 2. Pobieramy świeże dane w pętli (odpowiednik fetch_all_users)
            val allUsers = mutableMapOf<String, UserProfile>()
            var start = 0

            try {
                while (true) {
                    val response = RetrofitClient.api.getUsers(start = start)
                    val usersBatch = response.result ?: break

                    for (u in usersBatch) {
                        val uid = u.id ?: continue
                        val fullName = "${u.name ?: ""} ${u.lastName ?: ""}".trim()
                        allUsers[uid] = UserProfile(fullName, u.photo)
                    }

                    if (response.next != null) {
                        start = response.next
                    } else {
                        break
                    }
                }

                if (allUsers.isNotEmpty()) {
                    _usersMap.value = allUsers
                    val finalJson = Gson().toJson(allUsers)
                    cacheManager.saveUsersList(finalJson)
                }

            } catch (e: Exception) {
                println("Błąd pobierania użytkowników: ${e.message}")
            }
        }
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

    fun sendMessage(taskId: String, chatId: Int, text: String) {
        if (text.isBlank()) return

        _statusText.value = "Wysyłanie wiadomości..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = "Użytkownik Marek przekazuje: \n" + text
                var finalchat = "chat" + chatId
                //It is possible to send message as someone else when changed system to N and provide different webhook url
                RetrofitClient.api.sendMessage(finalchat, text, "Y")

                delay(1000) // Symulacja opóźnienia sieci
                _statusText.value = "Wiadomość wysłana!"

                // Odświeżamy zadanie, żeby pobrać nową wiadomość z serwera
                fetchTaskDetails(taskId)
            } catch (e: Exception) {
                _statusText.value = "Błąd wysyłania: ${e.message}"
            }
        }
    }

    fun fetchTaskDetails(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _statusText.value = "Pobieranie szczegółów zadania $taskId..."
            // Zabezpieczenie: czyścimy stary widok przed załadowaniem nowego
            _selectedTask.value = null
            _chatMessages.value = emptyList() // Czyścimy stary czat!
            _timeSummaries.value = emptyList() // <-- DODANE

            try {
                val response = RetrofitClient.api.getTaskDetailsRaw(taskId)
                val rawJson = response.string()

                cacheManager.saveTaskDetail(taskId, rawJson)

                val parsedResponse = Gson().fromJson(rawJson, SingleTaskResponse::class.java)
                val taskData = parsedResponse.result.task

                _selectedTask.value = taskData
                _statusText.value = "Pobrano szczegóły z sieci!"

                // Po prostu wywołujemy pobieranie, bez przypisywania (to funkcja asynchroniczna)
                triggerChatFetch(taskData)

            } catch (e: Exception) {
                val cachedJson = cacheManager.readTaskDetail(taskId)
                if (cachedJson != null) {
                    val parsedResponse = Gson().fromJson(cachedJson, SingleTaskResponse::class.java)
                    val taskData = parsedResponse.result.task

                    _selectedTask.value = taskData
                    _statusText.value = "Brak sieci. Wczytano lokalną kopię."

                    triggerChatFetch(taskData)
                } else {
                    _statusText.value = "Błąd: Brak internetu i brak lokalnej kopii zadania."
                }
            }
        }
    }

    private fun triggerChatFetch(task: BitrixTask) {
        // Zabezpieczenie przed brakiem ID
        val chatId = task.chatId ?: task.chatIdAlt
        if (chatId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                bgFetchMessages(task, chatId)
            }
        }
    }

    private suspend fun bgFetchMessages(task: BitrixTask, chatId: Int) {
        val messages = mutableListOf<ChatMessage>()
        var lastId: Int? = null

        try {
            while (true) {
                val response = RetrofitClient.api.getChatMessages(
                    dialogId = "chat$chatId",
                    lastId = lastId
                )

                val fetched = response.result?.messages ?: emptyList()
                if (fetched.isEmpty()) break

                messages.addAll(fetched)

                val validIds = fetched.mapNotNull { it.id }
                if (validIds.isEmpty()) break

                lastId = validIds.minOrNull()
                delay(500)
            }

            if (messages.isNotEmpty()) {
                val finalJson = Gson().toJson(messages)
                cacheManager.saveChat(chatId.toString(), finalJson)

                println("Pobrano ${messages.size} wiadomości dla czatu $chatId")

                // DODANO: Przekazanie wiadomości do interfejsu!
                _chatMessages.value = messages
                _timeSummaries.value = calculateTimeSummaries(messages)
            }

        } catch (e: Exception) {
            println("Błąd pobierania wiadomości: ${e.message}")
        }
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
            //"Surowe" -> fetchStandardTasksRaw(isAuto)
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

            _tasksList.value = allTasks.reversed()

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

//    private suspend fun fetchStandardTasksRaw(isAuto: Boolean) {
//        try {
//            // Najpierw próbujemy pokazać dane z cache, żeby użytkownik nie czekał
//            val cachedData = cacheManager.readJson()
//            if (cachedData != null && _tasksText.value.isEmpty()) {
//                _statusText.value = "Wyświetlam dane z pamięci podręcznej..."
//                _tasksText.value = cachedData
//            }
//
//            // Pobieramy świeże dane z webhooka
//            _statusText.value = "Pobieranie świeżych danych..."
//            val response = RetrofitClient.api.getTasksRaw()
//            val json = response.string()
//
//            // Zapisujemy nowy JSON do pliku (nadpisujemy stary)
//            cacheManager.saveJson(json)
//
//            _statusText.value = "Pobrano i zapisano dane!"
//            _tasksText.value = json
//
//        } catch (e: Exception) {
//            // Jeśli nie ma internetu, a mamy cache, poinformuj o tym
//            val cachedData = cacheManager.readJson()
//            if (cachedData != null) {
//                _statusText.value = "Brak sieci. Pokazuję ostatnio zapisane dane."
//                _tasksText.value = cachedData
//            } else {
//                _statusText.value = "Błąd komunikacji z Bitrixem i brak danych w cache."
//                _tasksText.value = "Wyjątek: ${e.javaClass.simpleName}\nTreść: ${e.message}"
//            }
//        } finally {
//            _isFetching.value = false
//        }
//    }

    private suspend fun bgFetchAllGroups(isAuto: Boolean) {
        delay(2000)
        _statusText.value = "Pobrano grupy!"
        _isFetching.value = false
    }
}