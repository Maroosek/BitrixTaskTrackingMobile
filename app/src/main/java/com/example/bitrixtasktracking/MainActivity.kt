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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import java.time.ZoneId


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
    @SerializedName("closedDate") val closedDate: String?,
    @SerializedName("chatId") val chatId: Int?,
    @SerializedName("chat_Id") val chatIdAlt: Int?,
    @SerializedName("groupId") val groupId: String?,
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
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

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

                        // Ekran 2: Szczegóły zadania
                        composable(
                            route = "taskDetail/{taskId}",
                            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId") ?: "Brak ID"
                            TaskDetailScreen(taskId = taskId, navController = navController, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

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
    return if (iconPath.startsWith("/")) {
        "https://jenaeuropa.bitrix24.pl$iconPath"
    } else {
        iconPath
    }
}

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

fun formatBitrixDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Brak daty"
    return try {
        val parsedDate = ZonedDateTime.parse(dateString)

        val adjustedDate = parsedDate.withZoneSameInstant(ZoneId.of("Europe/Warsaw"))

        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, 'godz:' HH:mm")
        adjustedDate.format(formatter)
    } catch (e: Exception) {
        dateString
    }
}

fun formatChatMessage(rawText: String?): AnnotatedString {
    if (rawText == null) return buildAnnotatedString { append("[Brak tekstu]") }

    // 1. Usuwanie tagów [USER] i zostawienie samej nazwy
    val userRegex = Regex("\\[USER=\\d+\\](.*?)\\[/USER\\]", RegexOption.IGNORE_CASE)
    val step1Text = rawText.replace(userRegex, "$1")

    // 2. Wyszukiwanie tagów [QUOTE] (flaga (?s) pozwala kropce złapać też znaki nowej linii)
    val quoteRegex = Regex("(?s)\\[QUOTE\\](.*?)\\[/QUOTE\\]")

    return buildAnnotatedString {
        var lastIndex = 0
        val matches = quoteRegex.findAll(step1Text)

        for (match in matches) {
            append(step1Text.substring(lastIndex, match.range.first))

            val quoteContent = match.groupValues[1]
                .replace("[B]", "", ignoreCase = true)
                .replace("[/B]", "", ignoreCase = true)
                .trim()

            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )) {
                append(quoteContent)
            }

            lastIndex = match.range.last + 1
        }

        append(step1Text.substring(lastIndex))
    }
}

@Composable
fun UserProfileRow(user: BitrixUser?, roleLabel: String) {
    if (user == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        val avatarUrl = getFullAvatarUrl(user.icon)

        if (avatarUrl.isNotEmpty() && !avatarUrl.endsWith("default_avatar.png")) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar użytkownika",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Domyślny avatar",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

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
            .animateContentSize()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BitrixViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isFetching by viewModel.isFetching.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val tasksList by viewModel.tasksList.collectAsState()

    val usersMap by viewModel.usersMap.collectAsState()
    val selectedUserId by viewModel.selectedUserId.collectAsState()

    val filterOptions = listOf("Wszystkie zadania", "Wszystkie aktywne", "Wybrany użytkownik")


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentFilter) {
        while(true) {
            kotlinx.coroutines.delay(60_000) // Odświeżaj zadania co 60 sekund
            viewModel.fetchData(isAuto = true)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Menu i filtry",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                filterOptions.forEach { option ->
                    NavigationDrawerItem(
                        label = { Text(text = option) },
                        selected = option == currentFilter,
                        onClick = {
                            viewModel.setFilter(option)

                            if (option != "Wybrany użytkownik") {
                                coroutineScope.launch { drawerState.close() }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                if (currentFilter == "Wybrany użytkownik") {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(
                        text = "Wybierz pracownika:",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )

                    var userExpanded by remember { mutableStateOf(false) }
                    val selectedUserName = usersMap[selectedUserId]?.name ?: "Kliknij, aby wybrać"

                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Button(
                            onClick = { userExpanded = true },
                            enabled = !isFetching,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "👤 $selectedUserName")
                        }

                        DropdownMenu(
                            expanded = userExpanded,
                            onDismissRequest = { userExpanded = false },
                            modifier = Modifier.height(300.dp)
                        ) {
                            usersMap.entries.sortedBy { it.value.name }.forEach { (id, profile) ->
                                DropdownMenuItem(
                                    text = { Text(profile.name) },
                                    onClick = {
                                        viewModel.setSelectedUserId(id)
                                        userExpanded = false
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = currentFilter,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Otwórz menu")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.fetchData(isAuto = false) },
                            enabled = !isFetching
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Odśwież")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (statusText.isNotBlank()) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }

                TaskListWithFab(
                    tasksList = tasksList,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TaskCard(task: BitrixTask, onClick: () -> Unit) {
    val (statusText, statusColor) = when (task.status) {
        1 -> "Nowe" to androidx.compose.ui.graphics.Color(0xFF2196F3) // Niebieski
        2 -> "Oczekujące" to androidx.compose.ui.graphics.Color(0xFFFF9800) // Pomarańczowy
        3 -> "W trakcie" to androidx.compose.ui.graphics.Color(0xFF4CAF50) // Zielony
        4 -> "Do kontroli" to androidx.compose.ui.graphics.Color(0xFF9C27B0) // Fioletowy
        5 -> "Zakończone" to androidx.compose.ui.graphics.Color(0xFF757575) // Szary
        6 -> "Odłożone" to androidx.compose.ui.graphics.Color(0xFFF44336) // Czerwony
        else -> "Nieznany (${task.status})" to androidx.compose.ui.graphics.Color.DarkGray
    }

    val groupColor = remember(task.groupId) {
        if (task.groupId.isNullOrEmpty() || task.groupId == "0") {
            androidx.compose.ui.graphics.Color(0xFFE0E0E0) // Delikatny szary, gdy brakuje grupy
        } else {
            val colors = listOf(
                0xFFE57373, 0xFFF06292, 0xFFBA68C8, 0xFF9575CD, 0xFF7986CB,
                0xFF64B5F6, 0xFF4DD0E1, 0xFF4DB6AC, 0xFF81C784, 0xFFAED581,
                0xFFFFD54F, 0xFFFFB74D, 0xFFFF8A65, 0xFFA1887F
            )
            val index = kotlin.math.abs(task.groupId.hashCode()) % colors.size
            androidx.compose.ui.graphics.Color(colors[index])
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = groupColor)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 6.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = task.title ?: "Brak tytułu",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ID: ${task.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))

                androidx.compose.material3.Surface(
                    color = statusColor.copy(alpha = 0.15f), // Lekko przezroczyste tło
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor, // Pełny kolor dla tekstu
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Utworzono: ${if (task.createdAt.isNullOrEmpty()) "Brak danych" else formatBitrixDate(task.createdAt)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Deadline: ${if (task.deadline.isNullOrEmpty()) "Brak" else formatBitrixDate(task.deadline)}",
                style = MaterialTheme.typography.bodySmall
            )

            if (task.status == 5 && !task.closedDate.isNullOrEmpty()) {
                Text(
                    text = "Zakończono: ${formatBitrixDate(task.closedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = androidx.compose.ui.graphics.Color(0xFF388E3C),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(thickness = 0.5.dp, color = androidx.compose.ui.graphics.Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

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
        viewModel.fetchTaskDetails(taskId, isSilent = false)

        while(true) {
            kotlinx.coroutines.delay(10_000) // Odświeżaj co 10 sekund
            viewModel.fetchTaskDetails(taskId, isSilent = true)
        }
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

    // DODANO: Mapowanie statusów liczbowych na tekst w szczegółach
    val mappedStatusText = when (task?.status) {
        1 -> "Nowe"
        2 -> "Oczekujące"
        3 -> "W trakcie"
        4 -> "Do kontroli"
        5 -> "Zakończone"
        6 -> "Odłożone"
        null -> "Brak danych"
        else -> "Nieznany (${task?.status})"
    }

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
                        // ZMIENIONO: Wyświetla zmapowany tekst zamiast samej cyfry
                        Text("Status: $mappedStatusText", style = MaterialTheme.typography.bodyMedium)
                        Text("Czas pracy: ${formatTimeSpent(task?.timeSpent)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Deadline: ${formatBitrixDate(task?.deadline)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Utworzono: ${formatBitrixDate(task?.createdAt)}", style = MaterialTheme.typography.bodyMedium)

                        if (task?.status == 5 && !task?.closedDate.isNullOrEmpty()) {
                            Text(
                                text = "Zakończono: ${formatBitrixDate(task!!.closedDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.ui.graphics.Color(0xFF388E3C),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }

                // Karta z osobami przypisanymi
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Profil zleceniodawcy - funkcja wewnątrz ma już dodany domyślny avatar
                        UserProfileRow(user = task?.creator, roleLabel = "Zleceniodawca")
                        Spacer(modifier = Modifier.height(8.dp))
                        // Profil odpowiedzialnego
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
                                        // LOGIKA CHATU POZOSTAJE JAK BYŁA WCZEŚNIEJ, JEST POPRAWNA
                                        if (avatarUrl.isNotEmpty() && !avatarUrl.endsWith("default_avatar.png")) {
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
                                            // Mały zastępczy avatar na chat
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(androidx.compose.ui.graphics.Color.LightGray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Brak avatara",
                                                    tint = androidx.compose.ui.graphics.Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
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
                                    text = formatChatMessage(message.text),
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
                                    viewModel.sendMessage(taskId, chatId, messageText)
                                    messageText = ""
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
    private val _usersMap = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val usersMap: StateFlow<Map<String, UserProfile>> = _usersMap.asStateFlow()

    val selectedTask: StateFlow<BitrixTask?> = _selectedTask.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    val _statusText = MutableStateFlow("Uruchamianie...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private val _tasksText = MutableStateFlow("")
    val tasksText: StateFlow<String> = _tasksText.asStateFlow()

    private val _currentMode = MutableStateFlow("tasks")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    private val _currentFilter = MutableStateFlow("Pobieranie tasków")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    private val _selectedUserId = MutableStateFlow<String?>(null)
    val selectedUserId: StateFlow<String?> = _selectedUserId.asStateFlow()

    private val _tasksList = MutableStateFlow<List<BitrixTask>>(emptyList())
    val tasksList: StateFlow<List<BitrixTask>> = _tasksList.asStateFlow()

    private val cacheManager = JsonUtil(application)

    private val _timeSummaries = MutableStateFlow<List<UserTimeSummary>>(emptyList())
    val timeSummaries: StateFlow<List<UserTimeSummary>> = _timeSummaries.asStateFlow()

    init {
        loadAllUsers()
        fetchData()
    }

    fun setSelectedUserId(id: String) {
        _selectedUserId.value = id
        fetchData()
    }

    fun setFilter(newFilter: String) {
        _currentFilter.value = newFilter
        fetchData()
    }

    private fun loadAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedData = cacheManager.readUsersList()
            if (cachedData != null) {
                try {
                    val mapType = object : TypeToken<Map<String, UserProfile>>() {}.type
                    _usersMap.value = Gson().fromJson(cachedData, mapType)
                } catch (e: Exception) {
                    println("Błąd odczytu lokalnych użytkowników")
                }
            }

            val allUsers = mutableMapOf<String, UserProfile>()
            var start = 0

            try {
                while (true) {
                    val response = RetrofitClient.api.getUsers(true, start = start)
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

    private fun getCacheKeyForFilter(filterName: String): String {
        return when (filterName) {
            "Wszystkie aktywne" -> "active_tasks"
            "Wybrany użytkownik" -> "user_tasks_${_selectedUserId.value ?: "none"}"
            else -> "all_tasks"
        }
    }


    private fun loadListFromCache(cacheKey: String) {
        val cachedData = cacheManager.readTaskList(cacheKey)
        if (!cachedData.isNullOrEmpty()) {
            try {
                val listType = object : TypeToken<List<BitrixTask>>() {}.type
                val tasks: List<BitrixTask> = Gson().fromJson(cachedData, listType)
                _tasksList.value = tasks
                // Cichy status, jeśli sieć zawiedzie, użytkownik przynajmniej widzi, że ma cache
                _statusText.value = "Pokazuję zapisane dane. Odświeżam w tle..."
            } catch (e: Exception) {
                _tasksList.value = emptyList()
            }
        } else {
            _tasksList.value = emptyList()
            _statusText.value = "Brak danych. Trwa pierwsze pobieranie..."
        }
    }

    fun sendMessage(taskId: String, chatId: Int, text: String) {
        if (text.isBlank()) return

        _statusText.value = "Wysyłanie wiadomości..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = "Użytkownik Marek przekazuje: \n" + text
                var finalchat = "chat" + chatId
                //It is impossible to send message as someone when you are not member of that task
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


    fun fetchTaskDetails(taskId: String, isSilent: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isSilent) {
                _statusText.value = "Pobieranie szczegółów zadania $taskId..."
                // Zabezpieczenie: czyścimy stary widok tylko podczas głównego ładowania
                _selectedTask.value = null
                _chatMessages.value = emptyList()
                _timeSummaries.value = emptyList()
            }

            try {
                val response = RetrofitClient.api.getTaskDetailsRaw(taskId)
                val rawJson = response.string()

                cacheManager.saveTaskDetail(taskId, rawJson)

                val parsedResponse = Gson().fromJson(rawJson, SingleTaskResponse::class.java)
                val taskData = parsedResponse.result.task

                _selectedTask.value = taskData
                if (!isSilent) _statusText.value = "Pobrano szczegóły z sieci!"

                triggerChatFetch(taskData)

            } catch (e: Exception) {
                if (!isSilent) {
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
        val cacheKey = getCacheKeyForFilter(_currentFilter.value)
        loadListFromCache(cacheKey)

        when (_currentFilter.value) {
            "Wszystkie aktywne" -> fetchInProgressTasks(isAuto)
            "Wybrany użytkownik" -> {
                val uid = _selectedUserId.value
                if (uid != null) {
                    fetchTasksForSpecificUser(uid)
                } else {
                    _statusText.value = "Wybierz najpierw użytkownika z bocznego menu."
                    _isFetching.value = false
                }
            }
            else -> fetchStandardTasks(isAuto) // Domyślnie "Pobieranie tasków"
        }
    }

    private fun fetchTasksForSpecificUser(userId: String) {
        val cacheKey = "user_tasks_$userId"
        try {
            _statusText.value = "Lokalne wyszukiwanie zadań pracownika..."

            val allTasksJson = cacheManager.readTaskList("all_tasks")

            if (allTasksJson.isNullOrEmpty()) {
                _statusText.value = "Brak zadań w pamięci. Pobierz najpierw wszystkie zadania."
                _tasksList.value = emptyList()
                return
            }

            val listType = object : TypeToken<List<BitrixTask>>() {}.type
            val allTasks: List<BitrixTask> = Gson().fromJson(allTasksJson, listType)

            val filteredTasks = allTasks.filter { task ->
                task.creator?.id == userId ||
                        task.responsible?.id == userId ||
                        task.accomplicesData?.containsKey(userId) == true
            }.sortedByDescending { it.activity ?: "" }

            _statusText.value = "Znaleziono lokalnie ${filteredTasks.size} zadań."
            _tasksList.value = filteredTasks

            val finalJson = Gson().toJson(filteredTasks)
            cacheManager.saveTaskList(cacheKey, finalJson)

        } catch (e: Exception) {
            _statusText.value = "Błąd lokalnego filtrowania: ${e.message}"
            loadListFromCache(cacheKey)
        } finally {
            _isFetching.value = false
        }
    }

    private suspend fun fetchInProgressTasks(isAuto: Boolean) {
        val cacheKey = "active_tasks"
        try {
            _statusText.value = "Rozpoczynam pobieranie zadań 'W trakcie'..."

            val allTasks = mutableListOf<BitrixTask>()
            var start = 0

            val dateStr = LocalDateTime.now().minusDays(14)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00+01:00"))

            while (true) {
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

            _statusText.value = "Zakończono pobieranie! Razem: ${allTasks.size} zadań."
            _tasksList.value = allTasks.sortedByDescending { it.activity ?: "" }

            val finalJson = Gson().toJson(_tasksList.value)
            cacheManager.saveTaskList(cacheKey, finalJson)
            _tasksText.value = finalJson

        } catch (e: Exception) {
            _statusText.value = "Brak sieci. Ładuję kopię lokalną..."
            loadListFromCache(cacheKey)
            _tasksText.value = "Wyjątek: ${e.javaClass.simpleName}\nTreść: ${e.message}"
        } finally {
            _isFetching.value = false
        }
    }

    // ZMIEŃ TO w BitrixViewModel:
    private suspend fun fetchStandardTasks(isAuto: Boolean) {
        val cacheKey = "all_tasks"
        try {
            if (!isAuto) _statusText.value = "Odświeżanie danych w tle..."

            val allTasks = mutableListOf<BitrixTask>()
            var start = 0

            // Filtrujemy zadania z ostatnich 7 dni dla odświeżania automatycznego
            val dateStr = if (isAuto) {
                LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00+01:00"))
            } else null

            while (true) {
                // Uwaga: Zakładam że getTasks ma opcjonalny parametr activityDate
                val response = RetrofitClient.api.getTasks(start = start, activityDate = dateStr)
                val tasksBatch = response.result.tasks

                if (tasksBatch.isEmpty()) {
                    break
                }

                allTasks.addAll(tasksBatch)
                if (!isAuto) _statusText.value = "Pobrano ${allTasks.size} zadań..."

                val next = response.next
                if (next != null) {
                    start = next
                } else {
                    break
                }
            }

            if (isAuto && _tasksList.value.isNotEmpty()) {
                // TRYB AUTO: Łączymy nowe dane ze starą listą (nadpisujemy zmienione, dodajemy nowe)
                val currentTasks = _tasksList.value.toMutableList()
                val updatedMap = allTasks.associateBy { it.id }

                for (i in currentTasks.indices) {
                    val id = currentTasks[i].id
                    if (updatedMap.containsKey(id)) {
                        currentTasks[i] = updatedMap[id]!!
                    }
                }

                val existingIds = currentTasks.map { it.id }.toSet()
                val newTasks = allTasks.filter { it.id !in existingIds }
                currentTasks.addAll(newTasks)

                currentTasks.sortByDescending { it.activity ?: "" }
                _tasksList.value = currentTasks
            } else {
                // TRYB RĘCZNY: Nadpisujemy wszystko i zapisujemy do pamięci podręczej
                allTasks.sortByDescending { it.activity ?: "" }
                _tasksList.value = allTasks

                val finalJson = Gson().toJson(allTasks)
                cacheManager.saveTaskList(cacheKey, finalJson)
                _tasksText.value = finalJson
                _statusText.value = "Pobrano wszystkie zadania!"
            }

        } catch (e: Exception) {
            if (!isAuto) {
                _statusText.value = "Brak sieci. Ładuję kopię lokalną..."
                loadListFromCache(cacheKey)
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