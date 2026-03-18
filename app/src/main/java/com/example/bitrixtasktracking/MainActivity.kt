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
import java.time.LocalDate
import java.time.Instant
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.unit.sp
import java.time.temporal.ChronoUnit
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider


sealed class DateFilter {
    object All : DateFilter()
    object Today : DateFilter()
    object Yesterday : DateFilter()
    data class SingleDate(val date: LocalDate) : DateFilter()
    data class DateRange(val from: LocalDate, val to: LocalDate) : DateFilter()
}

enum class StatusFilter(val label: String) {
    ALL("Wszystkie"),
    ACTIVE("Aktywne"),
    COMPLETED("Zakończone")
}

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

data class TrackerSession(
    val start: ZonedDateTime,
    var stop: ZonedDateTime? = null
)

data class UserTimeSummary(
    val userName: String,
    val totalSeconds: Double,
    val todaySeconds: Double,
    val isActive: Boolean,
    val activeStartDt: ZonedDateTime?,
    val dailyBreakdown: Map<LocalDate, Double> = emptyMap()  // data → sekundy
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
                        composable("taskList") {
                            MainScreen(viewModel = viewModel, navController = navController)
                        }
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

fun DateFilter.label(): String = when (this) {
    is DateFilter.All -> "Wszystkie daty"
    is DateFilter.Today -> "Dzisiaj"
    is DateFilter.Yesterday -> "Wczoraj"
    is DateFilter.SingleDate -> "Data: ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
    is DateFilter.DateRange -> "${from.format(DateTimeFormatter.ofPattern("dd.MM"))} – ${to.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
}

fun matchesDateFilter(activity: String?, filter: DateFilter): Boolean {
    if (filter is DateFilter.All) return true
    if (activity.isNullOrEmpty()) return false
    return try {
        val taskDate = ZonedDateTime.parse(activity)
            .withZoneSameInstant(ZoneId.of("Europe/Warsaw"))
            .toLocalDate()
        val today = LocalDate.now(ZoneId.of("Europe/Warsaw"))
        when (filter) {
            is DateFilter.Today -> taskDate == today
            is DateFilter.Yesterday -> taskDate == today.minusDays(1)
            is DateFilter.SingleDate -> taskDate == filter.date
            is DateFilter.DateRange -> !taskDate.isBefore(filter.from) && !taskDate.isAfter(filter.to)
            else -> true
        }
    } catch (e: Exception) { false }
}

fun formatActivityAgo(activityDate: String?): String {
    if (activityDate.isNullOrEmpty()) return "Brak aktywności"
    return try {
        val taskDate = ZonedDateTime.parse(activityDate)
            .withZoneSameInstant(ZoneId.of("Europe/Warsaw"))
            .toLocalDate()
        val today = LocalDate.now(ZoneId.of("Europe/Warsaw"))

        val daysBetween = ChronoUnit.DAYS.between(taskDate, today)

        when {
            daysBetween == 0L -> "Dzisiaj"
            daysBetween == 1L -> "Wczoraj"
            daysBetween > 1L -> "$daysBetween dni temu"
            true -> "W przyszłości"
            else -> "Brak danych"
        }
    } catch (e: Exception) {
        "Nieznana data"
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
    return if (iconPath.startsWith("/")) "https://jenaeuropa.bitrix24.pl$iconPath" else iconPath
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
        val dt = try { ZonedDateTime.parse(rawDate) } catch (e: Exception) { continue }

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
                    if (sess.stop == null) { sess.stop = dt; break }
                }
                openStarts.remove(user)
            }
            finishMatch != null -> {
                for (user in openStarts.keys.toList()) {
                    val sessions = userSessions[user] ?: continue
                    for (sess in sessions.reversed()) {
                        if (sess.stop == null) { sess.stop = dt; break }
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
        val dailyMap = mutableMapOf<LocalDate, Double>()

        for (sess in sessions) {
            if (sess.stop == null) {
                isActive = true; activeStart = sess.start
            } else {
                val elapsed = java.time.Duration.between(sess.start, sess.stop).seconds.toDouble()
                val clamped = maxOf(0.0, elapsed)
                totalElapsed += clamped

                // Przypisujemy czas do dnia startu sesji (uproszczone, wystarczające dla 1-dniowych sesji)
                val sessionDay = sess.start
                    .withZoneSameInstant(ZoneId.of("Europe/Warsaw")).toLocalDate()
                dailyMap[sessionDay] = (dailyMap[sessionDay] ?: 0.0) + clamped

                if (sess.start.toLocalDate() == todayDate || sess.stop!!.toLocalDate() == todayDate) {
                    todayElapsed += clamped
                }
            }
        }
        summaries.add(UserTimeSummary(user, totalElapsed, todayElapsed, isActive, activeStart, dailyMap))
    }
    return summaries
}

fun formatBitrixDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Brak daty"
    return try {
        val parsedDate = ZonedDateTime.parse(dateString)
        val adjustedDate = parsedDate.withZoneSameInstant(ZoneId.of("Europe/Warsaw"))
        adjustedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy, 'godz:' HH:mm"))
    } catch (e: Exception) { dateString }
}

fun formatChatMessage(rawText: String?): AnnotatedString {
    if (rawText == null) return buildAnnotatedString { append("[Brak tekstu]") }

    val userRegex = Regex("\\[USER=\\d+\\](.*?)\\[/USER\\]", RegexOption.IGNORE_CASE)
    val timestampRegex = Regex("\\[TIMESTAMP=(\\d+)\\s+FORMAT=([A-Z_]+)\\]", RegexOption.IGNORE_CASE)
    val quoteRegex = Regex("(?s)\\[QUOTE\\](.*?)\\[/QUOTE\\]", RegexOption.IGNORE_CASE)
    val urlRegex = Regex("\\[URL(?:=(.*?))?\\](.*?)\\[/URL\\]", RegexOption.IGNORE_CASE)

    var step1Text = rawText.replace(userRegex, "$1")

    val matchResults = timestampRegex.findAll(step1Text).toList()
    for (match in matchResults) {
        val ts = match.groupValues[1].toLongOrNull()
        val formatType = match.groupValues[2]

        if (ts != null) {
            val date = Instant.ofEpochSecond(ts).atZone(ZoneId.of("Europe/Warsaw"))
            val formattedDate = when (formatType) {
                "LONG_DATE_FORMAT" -> date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", java.util.Locale("pl")))
                "SHORT_DATE_FORMAT" -> date.format(DateTimeFormatter.ofPattern("HH:mm"))
                else -> date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            }
            step1Text = step1Text.replace(match.value, formattedDate)
        }
    }

    return buildAnnotatedString {

        fun appendWithUrls(text: String, isItalic: Boolean = false) {
            val combinedRegex = Regex(
                """(?i)\[B](.*?)\[/B]|\[URL(?:=(.*?))?](.*?)\[/URL]""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            )
            var lastIndex = 0
            for (match in combinedRegex.findAll(text)) {
                append(text.substring(lastIndex, match.range.first))

                if (match.value.startsWith("[B", ignoreCase = true)) {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                        )
                    ) {
                        append(match.groupValues[1])
                    }
                } else {
                    val matchedUrl = match.groupValues[2]
                    val linkText = match.groupValues[3]
                    val finalUrl = matchedUrl.ifEmpty { linkText }

                    val linkStyle = SpanStyle(
                        color = androidx.compose.ui.graphics.Color(0xFF1E88E5),
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
                    )
                    pushLink(
                        androidx.compose.ui.text.LinkAnnotation.Url(
                            url = finalUrl,
                            styles = androidx.compose.ui.text.TextLinkStyles(style = linkStyle)
                        )
                    )
                    append(linkText)
                    pop()
                }
                lastIndex = match.range.last + 1
            }
            append(text.substring(lastIndex))
        }

        var lastIndex = 0
        for (match in quoteRegex.findAll(step1Text)) {
            appendWithUrls(step1Text.substring(lastIndex, match.range.first))

            val quoteContent = match.groupValues[1]
                .replace("[B]", "", ignoreCase = true)
                .replace("[/B]", "", ignoreCase = true).trim()

            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {

                appendWithUrls(quoteContent, isItalic = true)
            }
            lastIndex = match.range.last + 1
        }

        appendWithUrls(step1Text.substring(lastIndex))
    }
}

@Composable
fun ShimmerTaskCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_transition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 6.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(24.dp).clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)))
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.4f)))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp).clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)))
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)))
        }
    }
}

@Composable
fun formatDescriptionText(rawText: String): AnnotatedString {
    val quoteRegex = Regex("(?s)\\[QUOTE\\](.*?)\\[/QUOTE\\]")
    val nameRegex = Regex("(?s)\\[B\\](.*?)\\[/B\\]")

    val quoteBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val quoteTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    return buildAnnotatedString {
        var lastIndex = 0
        for (match in quoteRegex.findAll(rawText)) {
            append(rawText.substring(lastIndex, match.range.first))

            val rawQuoteContent = match.groupValues[1].trim()
            val nameMatch = nameRegex.find(rawQuoteContent)

            val currentText = toAnnotatedString().text
            if (currentText.isNotEmpty() && !currentText.endsWith("\n")) {
                append("\n")
            }

            withStyle(
                SpanStyle(
                    background = quoteBackgroundColor,
                    color = quoteTextColor,
                    fontStyle = FontStyle.Italic
                )
            ) {

                if (nameMatch != null) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(nameMatch.groupValues[1] + " ")
                    }
                    val remainingText = rawQuoteContent.removeRange(nameMatch.range).trim()
                    if (remainingText.isNotEmpty()) {
                        append("\n" + remainingText.replace("\n", "\n"))
                    }
                } else {
                    append(rawQuoteContent.replace("\n", "\n"))
                }
                append(" ")
            }

            append("\n")
            lastIndex = match.range.last + 1
        }

        append(rawText.substring(lastIndex).trimStart())
    }
}

@Composable
fun UserProfileRow(user: BitrixUser?, roleLabel: String) {
    if (user == null) return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        val avatarUrl = getFullAvatarUrl(user.icon)
        if (avatarUrl.isNotEmpty() && !avatarUrl.endsWith("default_avatar.png")) {
            AsyncImage(
                model = avatarUrl, contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(28.dp))
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
    val formattedDescription = formatDescriptionText(description)

    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Text(text = "Opis zadania:", style = MaterialTheme.typography.titleMedium)
        Text(
            text = formattedDescription,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isExpanded) Int.MAX_VALUE else maxLinesCollapsed,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (it.hasVisualOverflow) showReadMoreButton = true },
            modifier = Modifier.padding(top = 4.dp)
        )
        if (showReadMoreButton) {
            Text(
                text = if (isExpanded) "Zwiń opis" else "Czytaj dalej...",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, // Zmiana koloru na systemowy
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 8.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
fun AccomplicesRow(accomplices: Map<String, BitrixUser>?) {
    if (accomplices.isNullOrEmpty()) return
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(
            text = "Uczestnicy (${accomplices.size}):",
            style = MaterialTheme.typography.labelMedium,
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            items(accomplices.values.toList()) { user ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val avatarUrl = getFullAvatarUrl(user.icon)
                    if (avatarUrl.isNotEmpty() && !avatarUrl.endsWith("default_avatar.png")) {
                        AsyncImage(model = avatarUrl, contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(36.dp).clip(CircleShape))
                    } else {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.name?.substringBefore(" ") ?: "Nieznany",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleDatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.of("Europe/Warsaw")).toLocalDate()
                    onDateSelected(date)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onRangeSelected: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val startMillis = state.selectedStartDateMillis
                val endMillis = state.selectedEndDateMillis
                if (startMillis != null && endMillis != null) {
                    val from = Instant.ofEpochMilli(startMillis)
                        .atZone(ZoneId.of("Europe/Warsaw")).toLocalDate()
                    val to = Instant.ofEpochMilli(endMillis)
                        .atZone(ZoneId.of("Europe/Warsaw")).toLocalDate()
                    onRangeSelected(from, to)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    ) {
        DateRangePicker(state = state, modifier = Modifier.padding(top = 16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserFilterSection(
    dateFilter: DateFilter,
    statusFilter: StatusFilter,
    onDateFilterChange: (DateFilter) -> Unit,
    onStatusFilterChange: (StatusFilter) -> Unit
) {
    var showSingleDatePicker by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }

    if (showSingleDatePicker) {
        SingleDatePickerDialog(
            onDateSelected = { date ->
                onDateFilterChange(DateFilter.SingleDate(date))
                showSingleDatePicker = false
            },
            onDismiss = { showSingleDatePicker = false }
        )
    }

    if (showRangePicker) {
        DateRangePickerDialog(
            onRangeSelected = { from, to ->
                onDateFilterChange(DateFilter.DateRange(from, to))
                showRangePicker = false
            },
            onDismiss = { showRangePicker = false }
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        Text(
            text = "STATUS ZADANIA",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatusFilter.entries.forEach { filter ->
                FilterChip(
                    selected = statusFilter == filter,
                    onClick = { onStatusFilterChange(filter) },
                    label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        Text(
            text = "DATA AKTYWNOŚCI",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Szybkie presety w jednym rzędzie
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = dateFilter is DateFilter.All,
                onClick = { onDateFilterChange(DateFilter.All) },
                label = { Text("Wszystkie", style = MaterialTheme.typography.labelMedium) }
            )
            FilterChip(
                selected = dateFilter is DateFilter.Today,
                onClick = { onDateFilterChange(DateFilter.Today) },
                label = { Text("Dzisiaj", style = MaterialTheme.typography.labelMedium) }
            )
            FilterChip(
                selected = dateFilter is DateFilter.Yesterday,
                onClick = { onDateFilterChange(DateFilter.Yesterday) },
                label = { Text("Wczoraj", style = MaterialTheme.typography.labelMedium) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = dateFilter is DateFilter.SingleDate,
                onClick = { showSingleDatePicker = true },
                label = {
                    Text(
                        text = if (dateFilter is DateFilter.SingleDate)
                            dateFilter.label() else "Wybierz datę",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = dateFilter is DateFilter.DateRange,
                onClick = { showRangePicker = true },
                label = {
                    Text(
                        text = if (dateFilter is DateFilter.DateRange)
                            dateFilter.label() else "Zakres dat",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Aktywny filtr — podgląd
        if (dateFilter !is DateFilter.All || statusFilter != StatusFilter.ALL) {
            Spacer(modifier = Modifier.height(8.dp))
            val parts = mutableListOf<String>()
            if (statusFilter != StatusFilter.ALL) parts.add(statusFilter.label)
            if (dateFilter !is DateFilter.All) parts.add(dateFilter.label())
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aktywne filtry: ${parts.joinToString(" · ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
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
    val dateFilter by viewModel.dateFilter.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    val filterOptions = listOf("Wszystkie zadania", "Wszystkie aktywne", "Wybrany użytkownik")

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val searchQuery by viewModel.searchQuery.collectAsState()

    // Lokalne filtrowanie listy zadań na podstawie wpisanej frazy
    val filteredTasks = remember(tasksList, searchQuery) {
        if (searchQuery.isBlank()) {
            tasksList
        } else {
            tasksList.filter {
                it.title?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    LaunchedEffect(currentFilter) {
        while (true) {
            delay(60_000)
            viewModel.fetchData(isAuto = true)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Menu i filtry",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
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
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )

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
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedUserId != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerDefaults.color
                            )
                            UserFilterSection(
                                dateFilter = dateFilter,
                                statusFilter = statusFilter,
                                onDateFilterChange = { viewModel.setDateFilter(it) },
                                onStatusFilterChange = { viewModel.setStatusFilter(it) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { coroutineScope.launch { drawerState.close() } },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) { Text("Zastosuj i zamknij") }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentFilter,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            // Pokaż aktywne filtry w podtytule TopBar
                            if (currentFilter == "Wybrany użytkownik") {
                                val parts = mutableListOf<String>()
                                if (statusFilter != StatusFilter.ALL) parts.add(statusFilter.label)
                                if (dateFilter !is DateFilter.All) parts.add(dateFilter.label())
                                if (parts.isNotEmpty()) {
                                    Text(
                                        text = parts.joinToString(" · "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Otwórz menu")
                        }
                    },
                    actions = {
                        // Guzik "Wyczyść" widoczny wszędzie, poza "Wszystkie zadania"
                        if (currentFilter != "Wszystkie zadania") {
                            IconButton(
                                onClick = {
                                    viewModel.setFilter("Wszystkie zadania")
                                    viewModel.setSearchQuery("")
                                },
                                enabled = !isFetching
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Wyczyść filtry")
                            }
                        }

                        IconButton(
                            onClick = { viewModel.fetchData(isAuto = false) },
                            enabled = !isFetching
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isFetching) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Szukaj zadania po nazwie...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Wyczyść wyszukiwanie")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (filteredTasks.isEmpty() && isFetching) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        userScrollEnabled = false
                    ) {
                        items(6) {
                            ShimmerTaskCard()
                        }
                    }
                } else if (filteredTasks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Brak zadań pasujących do: '$searchQuery'" else "Brak zadań do wyświetlenia.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    TaskListWithFab(tasksList = filteredTasks, navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TaskCard(task: BitrixTask, onClick: () -> Unit) {
    val (statusText, statusColor) = when (task.status) {
        1 -> "Nowe" to androidx.compose.ui.graphics.Color(0xFF2196F3)
        2 -> "Oczekujące" to androidx.compose.ui.graphics.Color(0xFFFF9800)
        3 -> "W trakcie" to androidx.compose.ui.graphics.Color(0xFF4CAF50)
        4 -> "Do kontroli" to androidx.compose.ui.graphics.Color(0xFF9C27B0)
        5 -> "Zakończone" to androidx.compose.ui.graphics.Color(0xFF757575)
        6 -> "Odłożone" to androidx.compose.ui.graphics.Color(0xFFF44336)
        else -> "Nieznany (${task.status})" to androidx.compose.ui.graphics.Color.DarkGray
    }

    val activityText = formatActivityAgo(task.activity)
    val activityColor = when (activityText) {
        "Dzisiaj" -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Zielony
        "Wczoraj" -> androidx.compose.ui.graphics.Color(0xFFFBC02D) // Ciemnożółty
        else -> androidx.compose.ui.graphics.Color.Black // Czarny dla starszych dat
    }

    val groupColor = remember(task.groupId) {
        if (task.groupId.isNullOrEmpty() || task.groupId == "0") {
            androidx.compose.ui.graphics.Color(0xFFE0E0E0)
        } else {
            val colors = listOf(
                0xFFE57373L, 0xFFF06292L, 0xFFBA68C8L, 0xFF9575CDL, 0xFF7986CBL,
                0xFF64B5F6L, 0xFF4DD0E1L, 0xFF4DB6ACL, 0xFF81C784L, 0xFFAED581L,
                0xFFFFD54FL, 0xFFFFB74DL, 0xFFFF8A65L, 0xFFA1887FL
            )
            val index = kotlin.math.abs(task.groupId.hashCode()) % colors.size
            androidx.compose.ui.graphics.Color(colors[index])
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = groupColor)
    ) {
        Column(
            modifier = Modifier.padding(start = 6.dp).fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface).padding(16.dp)
        ) {
            Text(text = task.title ?: "Brak tytułu", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ID: ${task.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                androidx.compose.material3.Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText, color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aktywność: $activityText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = activityColor
            )
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
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                Modifier,
                thickness = 0.5.dp,
                color = androidx.compose.ui.graphics.Color.LightGray
            )
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
    var currentTime by remember { mutableStateOf(ZonedDateTime.now()) }
    var showDetails by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) { delay(1000); currentTime = ZonedDateTime.now() }
    }

    // Oblicz live-dodatek raz, żeby używać go i w nagłówku i w szczegółach
    val liveExtras = remember(summaries, currentTime) {
        summaries.associate { summary ->
            val extra = if (summary.isActive && summary.activeStartDt != null)
                maxOf(0.0, java.time.Duration.between(summary.activeStartDt, currentTime).seconds.toDouble())
            else 0.0
            summary.userName to extra
        }
    }
    val totalLiveAdditionalTime = liveExtras.values.sum()
    val grandTotal = (baseTaskTime ?: 0.0) + totalLiveAdditionalTime

    val today = LocalDate.now(ZoneId.of("Europe/Warsaw"))
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏱ Raport czasu pracy", style = MaterialTheme.typography.titleMedium)
                if (summaries.isNotEmpty()) {
                    TextButton(onClick = { showDetails = !showDetails }) {
                        Text(
                            text = if (showDetails) "Zwiń szczegóły ▲" else "Szczegóły ▼",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            if (summaries.isEmpty() && (baseTaskTime == null || baseTaskTime <= 0.0)) {
                Text("Brak historii czasu.", style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.Gray)
            } else {

                summaries.forEach { summary ->
                    val extra = liveExtras[summary.userName] ?: 0.0
                    val displayTotal = summary.totalSeconds + extra
                    val displayToday = summary.todaySeconds + extra
                    val todayStr = if (displayToday > 0) " (dziś: ${formatTimeSpentLive(displayToday)})" else ""
                    val activeMarker = if (summary.isActive) " 🔴" else ""

                    Text(
                        text = "👤 ${summary.userName}  ${formatTimeSpentLive(displayTotal)}$todayStr$activeMarker",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (summary.isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (totalLiveAdditionalTime > 0) {
                    Text(
                        text = "Razem: ${formatTimeSpentLive(baseTaskTime)} + ${formatTimeSpentLive(totalLiveAdditionalTime)} = ${formatTimeSpentLive(grandTotal)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFF1565C0)
                    )
                } else {
                    Text(
                        text = "Razem: ${formatTimeSpentLive(grandTotal)}",
                        style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = showDetails) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 10.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )

                        summaries.forEach { summary ->
                            val extra = liveExtras[summary.userName] ?: 0.0

                            val allDays = summary.dailyBreakdown.toMutableMap()
                            if (summary.isActive && extra > 0) {
                                allDays[today] = (allDays[today] ?: 0.0) + extra
                            }

                            if (allDays.isEmpty()) return@forEach

                            Text(
                                text = "👤 ${summary.userName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            val sortedDays = allDays.entries.sortedByDescending { it.key }

                            sortedDays.forEach { (date, seconds) ->
                                val isToday = date == today
                                val isYesterday = date == today.minusDays(1)
                                val dateLabel = when {
                                    isToday -> "Dzisiaj (${date.format(dateFormatter)})"
                                    isYesterday -> "Wczoraj (${date.format(dateFormatter)})"
                                    else -> date.format(dateFormatter)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val maxDaySeconds = allDays.values.maxOrNull() ?: 1.0
                                        val barFraction = (seconds / maxDaySeconds).toFloat().coerceIn(0.05f, 1f)
                                        Box(
                                            modifier = Modifier
                                                .width((barFraction * 40).dp)
                                                .height(8.dp)
                                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                                .background(
                                                    if (isToday) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = dateLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isToday) MaterialTheme.colorScheme.primary
                                            else androidx.compose.ui.graphics.Color.DarkGray,
                                            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                    Text(
                                        text = formatTimeSpent(seconds),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isToday) MaterialTheme.colorScheme.primary
                                        else androidx.compose.ui.graphics.Color.DarkGray
                                    )
                                }
                            }

                            val userTotalWithLive = summary.totalSeconds + extra
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                                thickness = DividerDefaults.Thickness,
                                color = androidx.compose.ui.graphics.Color.LightGray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Suma",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.Gray
                                )
                                Text(
                                    text = formatTimeSpent(userTotalWithLive),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.Gray
                                )
                            }
                        }
                    }
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
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
        ) {
            items(tasksList) { task ->
                TaskCard(task = task, onClick = {
                    if (task.id != null) navController.navigate("taskDetail/${task.id}")
                    else viewModel._statusText.value = "To zadanie nie ma ID!"
                })
            }
        }
        AnimatedVisibility(
            visible = showScrollToTop, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Wróć na górę")
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
        while (true) {
            delay(10_000)
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

    val (mappedStatusText, statusColor) = when (task?.status) {
        1 -> "Nowe" to androidx.compose.ui.graphics.Color(0xFF2196F3)
        2 -> "Oczekujące" to androidx.compose.ui.graphics.Color(0xFFFF9800)
        3 -> "W trakcie" to androidx.compose.ui.graphics.Color(0xFF4CAF50)
        4 -> "Do kontroli" to androidx.compose.ui.graphics.Color(0xFF9C27B0)
        5 -> "Zakończone" to androidx.compose.ui.graphics.Color(0xFF757575)
        6 -> "Odłożone" to androidx.compose.ui.graphics.Color(0xFFF44336)
        null -> "Brak danych" to androidx.compose.ui.graphics.Color.DarkGray
        else -> "Nieznany (${task?.status})" to androidx.compose.ui.graphics.Color.DarkGray
    }

    val groupColor = remember(task?.groupId) {
        if (task?.groupId.isNullOrEmpty() || task?.groupId == "0") {
            androidx.compose.ui.graphics.Color.Unspecified
        } else {
            val colors = listOf(
                0xFFE57373L, 0xFFF06292L, 0xFFBA68C8L, 0xFF9575CDL, 0xFF7986CBL,
                0xFF64B5F6L, 0xFF4DD0E1L, 0xFF4DB6ACL, 0xFF81C784L, 0xFFAED581L,
                0xFFFFD54FL, 0xFFFFB74DL, 0xFFFF8A65L, 0xFFA1887FL
            )
            val index = kotlin.math.abs(task!!.groupId.hashCode()) % colors.size
            androidx.compose.ui.graphics.Color(colors[index])
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
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
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (groupColor == androidx.compose.ui.graphics.Color.Unspecified)
                            MaterialTheme.colorScheme.surfaceVariant
                        else groupColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 6.dp) // Pasek koloru grupy po lewej stronie
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Text("ID: ${task?.id}", style = MaterialTheme.typography.bodyMedium)

                        // Ostylowany status
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Status: ", style = MaterialTheme.typography.bodyMedium)
                            androidx.compose.material3.Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = mappedStatusText,
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text("Czas pracy: ${formatTimeSpent(task?.timeSpent)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Utworzono: ${formatBitrixDate(task?.createdAt)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Deadline: ${formatBitrixDate(task?.deadline)}", style = MaterialTheme.typography.bodyMedium)

                        if (task?.status == 5 && !task?.closedDate.isNullOrEmpty()) {
                            Text(
                                text = "Zakończono: ${formatBitrixDate(task!!.closedDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = androidx.compose.ui.graphics.Color(0xFF388E3C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
                if (timeSummaries.isNotEmpty() || (task?.timeSpent != null && task!!.timeSpent!! > 0.0)) {
                    TimeTrackerSection(summaries = timeSummaries, baseTaskTime = task?.timeSpent)
                }
                if (!task?.description.isNullOrEmpty()) {
                    ExpandableDescription(description = task!!.description!!)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (chatMessages.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystem) androidx.compose.ui.graphics.Color(0xFFFFF9C4)
                                else androidx.compose.ui.graphics.Color(0xFFF0F0F0)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (avatarUrl.isNotEmpty() && !avatarUrl.endsWith("default_avatar.png")) {
                                            AsyncImage(
                                                model = avatarUrl, contentDescription = "Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(24.dp).clip(CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        } else if (!isSystem) {
                                            Box(
                                                modifier = Modifier.size(24.dp).clip(CircleShape)
                                                    .background(androidx.compose.ui.graphics.Color.LightGray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null,
                                                    tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(
                                            text = authorName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSystem) androidx.compose.ui.graphics.Color.DarkGray
                                            else androidx.compose.ui.graphics.Color.Black
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
                                    fontStyle = if (isSystem) FontStyle.Italic else FontStyle.Normal
                                )
                            }
                        }
                    }
                } else if (task?.chatId != null || task?.chatIdAlt != null) {
                    Text("Trwa pobieranie wiadomości...", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = statusText, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
        }

        if (task != null && (task?.chatId != null || task?.chatIdAlt != null)) {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj wiadomość")
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp).navigationBarsPadding().imePadding()
                ) {
                    Text("Nowa wiadomość", style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = messageText, onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Wpisz treść...") }, maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                val chatId = task?.chatId ?: task?.chatIdAlt
                                if (chatId != null && messageText.isNotBlank()) {
                                    viewModel.sendMessage(taskId, chatId, messageText)
                                    messageText = ""
                                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) showBottomSheet = false
                                    }
                                }
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Wyślij",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}

class BitrixViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedTask = MutableStateFlow<BitrixTask?>(null)
    val selectedTask: StateFlow<BitrixTask?> = _selectedTask.asStateFlow()

    private val _usersMap = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val usersMap: StateFlow<Map<String, UserProfile>> = _usersMap.asStateFlow()

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

    private val _currentFilter = MutableStateFlow("Wszystkie zadania")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()

    private val _selectedUserId = MutableStateFlow<String?>(null)
    val selectedUserId: StateFlow<String?> = _selectedUserId.asStateFlow()

    private val _tasksList = MutableStateFlow<List<BitrixTask>>(emptyList())
    val tasksList: StateFlow<List<BitrixTask>> = _tasksList.asStateFlow()

    private val _dateFilter = MutableStateFlow<DateFilter>(DateFilter.All)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

    private val cacheManager = JsonUtil(application)

    private val _timeSummaries = MutableStateFlow<List<UserTimeSummary>>(emptyList())
    val timeSummaries: StateFlow<List<UserTimeSummary>> = _timeSummaries.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

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
        if (newFilter != "Wybrany użytkownik") {
            _dateFilter.value = DateFilter.All
            _statusFilter.value = StatusFilter.ALL
        }
        fetchData()
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
        if (_currentFilter.value == "Wybrany użytkownik") fetchData()
    }

    fun setStatusFilter(filter: StatusFilter) {
        _statusFilter.value = filter
        if (_currentFilter.value == "Wybrany użytkownik") fetchData()
    }

    private fun loadAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            val cachedData = cacheManager.readUsersList()
            if (cachedData != null) {
                try {
                    val mapType = object : TypeToken<Map<String, UserProfile>>() {}.type
                    _usersMap.value = Gson().fromJson(cachedData, mapType)
                } catch (e: Exception) { println("Błąd odczytu lokalnych użytkowników") }
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
                    if (response.next != null) start = response.next else break
                }
                if (allUsers.isNotEmpty()) {
                    _usersMap.value = allUsers
                    cacheManager.saveUsersList(Gson().toJson(allUsers))
                }
            } catch (e: Exception) { println("Błąd pobierania użytkowników: ${e.message}") }
        }
    }

    private fun getCacheKeyForFilter(filterName: String): String = when (filterName) {
        "Wszystkie aktywne" -> "active_tasks"
        "Wybrany użytkownik" -> "user_tasks_${_selectedUserId.value ?: "none"}"
        else -> "all_tasks"
    }

    private fun loadListFromCache(cacheKey: String) {
        val cachedData = cacheManager.readTaskList(cacheKey)
        if (!cachedData.isNullOrEmpty()) {
            try {
                val listType = object : TypeToken<List<BitrixTask>>() {}.type
                _tasksList.value = Gson().fromJson(cachedData, listType)
                _statusText.value = "Pokazuję zapisane dane. Odświeżam w tle..."
            } catch (e: Exception) { _tasksList.value = emptyList() }
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
                val finalText = "[B]Użytkownik Marek przekazuje:[/B] \n \n $text"
                val finalChat = "chat$chatId"
                RetrofitClient.api.sendMessage(finalChat, finalText, "Y")
                delay(1000)
                _statusText.value = "Wiadomość wysłana!"
                refreshChat(chatId)
            } catch (e: Exception) {
                _statusText.value = "Błąd wysyłania: ${e.message}"
            }
        }
    }

    private suspend fun refreshChat(chatId: Int) {
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
                cacheManager.saveChat(chatId.toString(), Gson().toJson(messages))
                _chatMessages.value = messages
                _timeSummaries.value = calculateTimeSummaries(messages)
            }
        } catch (e: Exception) {
            _statusText.value = "Błąd odświeżania czatu: ${e.message}"
        }
    }

    fun fetchTaskDetails(taskId: String, isSilent: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!isSilent) {
                _statusText.value = "Pobieranie szczegółów zadania $taskId..."
                _selectedTask.value = null
                _chatMessages.value = emptyList()
                _timeSummaries.value = emptyList()
            }
            try {
                val response = RetrofitClient.api.getTaskDetailsRaw(taskId)
                val rawJson = response.string()
                cacheManager.saveTaskDetail(taskId, rawJson)
                val taskData = Gson().fromJson(rawJson, SingleTaskResponse::class.java).result.task
                _selectedTask.value = taskData
                if (!isSilent) _statusText.value = "Pobrano szczegóły z sieci!"
                triggerChatFetch(taskData)
            } catch (e: Exception) {
                if (!isSilent) {
                    val cachedJson = cacheManager.readTaskDetail(taskId)
                    if (cachedJson != null) {
                        val taskData = Gson().fromJson(cachedJson, SingleTaskResponse::class.java).result.task
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
        val chatId = task.chatId ?: task.chatIdAlt
        if (chatId != null) {
            viewModelScope.launch(Dispatchers.IO) { bgFetchMessages(task, chatId) }
        }
    }

    private suspend fun bgFetchMessages(task: BitrixTask, chatId: Int) {
        try {
            val cachedChatJson = cacheManager.readChat(chatId.toString())

            if (!cachedChatJson.isNullOrEmpty()) {
                val listType = object : TypeToken<List<ChatMessage>>() {}.type
                val cachedMessages: List<ChatMessage> = Gson().fromJson(cachedChatJson, listType)

                _chatMessages.value = cachedMessages
                _timeSummaries.value = calculateTimeSummaries(cachedMessages)
            }
        } catch (e: Exception) {
            println("Błąd odczytu lokalnego czatu: ${e.message}")
        }

        val messages = mutableListOf<ChatMessage>()
        var lastId: Int? = null
        try {
            while (true) {
                val response = RetrofitClient.api.getChatMessages(dialogId = "chat$chatId", lastId = lastId)
                val fetched = response.result?.messages ?: emptyList()
                if (fetched.isEmpty()) break

                messages.addAll(fetched)
                val validIds = fetched.mapNotNull { it.id }
                if (validIds.isEmpty()) break

                lastId = validIds.minOrNull()
                delay(500)
            }

            if (messages.isNotEmpty()) {
                cacheManager.saveChat(chatId.toString(), Gson().toJson(messages))
                _chatMessages.value = messages
                _timeSummaries.value = calculateTimeSummaries(messages)
            }
        } catch (e: Exception) {
            println("Błąd pobierania wiadomości z sieci: ${e.message}")
        }
    }
    fun fetchData(isAuto: Boolean = false) {
        if (_isFetching.value) return
        _isFetching.value = true
        if (_currentMode.value == "tasks") {
            _statusText.value = if (isAuto) "Automatyczne odświeżanie..." else "Pobieranie zadań..."
            viewModelScope.launch(Dispatchers.IO) { bgFetchAllTasks(isAuto) }
        } else if (_currentMode.value == "groups") {
            _statusText.value = "Pobieranie grup..."
            viewModelScope.launch(Dispatchers.IO) { bgFetchAllGroups(isAuto) }
        }
    }

    private suspend fun bgFetchAllTasks(isAuto: Boolean) {
        val cacheKey = getCacheKeyForFilter(_currentFilter.value)
        loadListFromCache(cacheKey)
        when (_currentFilter.value) {
            "Wszystkie aktywne" -> fetchInProgressTasks(isAuto)
            "Wybrany użytkownik" -> {
                val uid = _selectedUserId.value
                if (uid != null) fetchTasksForSpecificUser(uid)
                else {
                    _statusText.value = "Wybierz najpierw użytkownika z bocznego menu."
                    _isFetching.value = false
                }
            }
            else -> fetchStandardTasks(isAuto)
        }
    }

    private fun fetchTasksForSpecificUser(userId: String) {
        val cacheKey = "user_tasks_$userId"
        try {
            _statusText.value = "Filtrowanie zadań pracownika..."
            val allTasksJson = cacheManager.readTaskList("all_tasks")
            if (allTasksJson.isNullOrEmpty()) {
                _statusText.value = "Brak zadań w pamięci. Pobierz najpierw wszystkie zadania."
                _tasksList.value = emptyList()
                return
            }

            val listType = object : TypeToken<List<BitrixTask>>() {}.type
            val allTasks: List<BitrixTask> = Gson().fromJson(allTasksJson, listType)

            val currentDateFilter = _dateFilter.value
            val currentStatusFilter = _statusFilter.value

            val filteredTasks = allTasks.filter { task ->
                // Filtr użytkownika
                val matchesUser = task.creator?.id == userId ||
                        task.responsible?.id == userId ||
                        task.accomplicesData?.containsKey(userId) == true

                // Filtr statusu
                val matchesStatus = when (currentStatusFilter) {
                    StatusFilter.ALL -> true
                    StatusFilter.ACTIVE -> task.status != null && task.status != 5 && task.status != 6
                    StatusFilter.COMPLETED -> task.status == 5
                }

                // Filtr daty aktywności
                val matchesDate = matchesDateFilter(task.activity, currentDateFilter)

                matchesUser && matchesStatus && matchesDate
            }.sortedByDescending { it.activity ?: "" }

            val filterDesc = buildString {
                if (currentStatusFilter != StatusFilter.ALL) append(currentStatusFilter.label)
                if (currentDateFilter !is DateFilter.All) {
                    if (isNotEmpty()) append(", ")
                    append(currentDateFilter.label())
                }
            }

            _statusText.value = if (filterDesc.isNotEmpty())
                "Znaleziono ${filteredTasks.size} zadań ($filterDesc)"
            else
                "Znaleziono ${filteredTasks.size} zadań."

            _tasksList.value = filteredTasks
            cacheManager.saveTaskList(cacheKey, Gson().toJson(filteredTasks))

        } catch (e: Exception) {
            _statusText.value = "Błąd filtrowania: ${e.message}"
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
                val response = RetrofitClient.api.getTasks(start = start, realStatus = 3, activityDate = dateStr)
                val tasksBatch = response.result.tasks
                if (tasksBatch.isEmpty()) break
                allTasks.addAll(tasksBatch)
                _statusText.value = "Pobrano ${allTasks.size} zadań 'W trakcie'..."
                if (response.next != null) start = response.next else break
            }

            _statusText.value = "Zakończono pobieranie! Razem: ${allTasks.size} zadań."
            _tasksList.value = allTasks.sortedByDescending { it.activity ?: "" }
            cacheManager.saveTaskList(cacheKey, Gson().toJson(_tasksList.value))

        } catch (e: Exception) {
            _statusText.value = "Brak sieci. Ładuję kopię lokalną..."
            loadListFromCache(cacheKey)
        } finally {
            _isFetching.value = false
        }
    }

    private suspend fun fetchStandardTasks(isAuto: Boolean) {
        val cacheKey = "all_tasks"
        try {
            if (!isAuto) _statusText.value = "Odświeżanie danych w tle..."
            val allTasks = mutableListOf<BitrixTask>()
            var start = 0
            val dateStr = if (isAuto) {
                LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00+01:00"))
            } else null

            while (true) {
                val response = RetrofitClient.api.getTasks(start = start, activityDate = dateStr)
                val tasksBatch = response.result.tasks
                if (tasksBatch.isEmpty()) break
                allTasks.addAll(tasksBatch)
                if (!isAuto) _statusText.value = "Pobrano ${allTasks.size} zadań..."
                if (response.next != null) start = response.next else break
            }

            if (isAuto && _tasksList.value.isNotEmpty()) {
                val currentTasks = _tasksList.value.toMutableList()
                val updatedMap = allTasks.associateBy { it.id }
                for (i in currentTasks.indices) {
                    val id = currentTasks[i].id
                    if (updatedMap.containsKey(id)) currentTasks[i] = updatedMap[id]!!
                }
                val existingIds = currentTasks.map { it.id }.toSet()
                currentTasks.addAll(allTasks.filter { it.id !in existingIds })
                currentTasks.sortByDescending { it.activity ?: "" }
                _tasksList.value = currentTasks

                val finalJson = Gson().toJson(currentTasks)
                cacheManager.saveTaskList(cacheKey, finalJson)
                _tasksText.value = finalJson

            } else {
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