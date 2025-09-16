package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.*
import kotlin.random.Random

data class Participant(
    val name: String,
    var score: Int = 0,
    val color: Color
)

@Composable
@Preview
fun App() {
    var participants by remember { mutableStateOf(listOf<Participant>()) }
    var wheelParticipants by remember { mutableStateOf(listOf<Participant>()) }
    var currentWinner by remember { mutableStateOf<Participant?>(null) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinTime by remember { mutableStateOf("3") }
    var angle by remember { mutableStateOf(0f) }
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }

    var showAddPlayerInput by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val leaderboardManager = remember { LeaderboardManager() }

    val colors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow,
        Color.Cyan, Color.Magenta, Color.Gray, Color(0xFFFFA500),
        Color(0xFFFF1493), Color(0xFF9370DB), Color(0xFF20B2AA), Color(0xFFFF6347)
    )

    // Радужные цвета для превью
    val rainbowColors = listOf(
        Color(0xFFFF0000), // Красный
        Color(0xFFFF8000), // Оранжевый
        Color(0xFFFFFF00), // Желтый
        Color(0xFF80FF00), // Желто-зеленый
        Color(0xFF00FF00), // Зеленый
        Color(0xFF00FF80), // Зелено-голубой
        Color(0xFF00FFFF), // Голубой
        Color(0xFF0080FF), // Сине-голубой
        Color(0xFF0000FF), // Синий
        Color(0xFF8000FF), // Сине-фиолетовый
        Color(0xFFFF00FF), // Фиолетовый
        Color(0xFFFF0080)  // Красно-фиолетовый
    )

    fun addPlayer(name: String) {
        if (name.isNotBlank()) {
            val newParticipant = Participant(
                name = name.trim(),
                score = 0,
                color = colors[participants.size % colors.size]
            )
            participants = participants + newParticipant
            wheelParticipants = participants.toList()
            newPlayerName = ""
            showAddPlayerInput = false
        }
    }

    // Функция для определения победителя по текущему углу колеса
    fun getWinnerByAngle(currentAngle: Float, participants: List<Participant>): Participant? {
        if (participants.isEmpty()) return null

        val sliceAngle = 360f / participants.size

        // Отладочная информация
        println("=== Debug Winner Selection ===")
        println("Current angle: $currentAngle")
        println("Participants order:")
        participants.forEachIndexed { index, participant ->
            val startAngle = index * sliceAngle
            val endAngle = startAngle + sliceAngle
            println("  [$index] ${participant.name} (${participant.color}) - sector: ${startAngle}° to ${endAngle}°")
        }

        // Указатель находится наверху (270 градусов в системе Canvas, но мы считаем как 0)
        // Поскольку колесо вращается по часовой стрелке, нужно инвертировать угол
        val pointerAngle = 270f // Указатель наверху
        val relativeAngle = (pointerAngle - currentAngle + 360f) % 360f

        println("Pointer looking at angle: $relativeAngle")

        val winnerIndex = (relativeAngle / sliceAngle).toInt() % participants.size
        val winner = participants[winnerIndex]

        println("Winner index: $winnerIndex")
        println("Winner: ${winner.name} (${winner.color})")
        println("===============================")

        return winner
    }

    // Загружаем лидерборд при запуске
    LaunchedEffect(Unit) {
        leaderboard = leaderboardManager.getLeaderboard()
        println("Loaded leaderboard on start: $leaderboard") // Для отладки
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Левая часть - управление игроками
            Column(
                modifier = Modifier.weight(0.4f).padding(end = 16.dp)
            ) {
                Text("Игроки", fontSize = 20.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Список игроков
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(participants) { participant ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(20.dp).background(participant.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(participant.name, fontSize = 16.sp)
                                }
                                Text(
                                    "Очки: ${participant.score}",
                                    fontSize = 14.sp,
                                    color = Color.Blue
                                )
                            }
                        }
                    }

                    // Кнопка/поле добавления игрока
                    item {
                        if (showAddPlayerInput) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(modifier = Modifier.weight(1f)) {
                                    BasicTextField(
                                        value = newPlayerName,
                                        onValueChange = { newPlayerName = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                            .background(Color.White),
                                        decorationBox = { innerTextField ->
                                            if (newPlayerName.isEmpty()) {
                                                Text("Введите имя игрока...", color = Color.Gray)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                                Button(
                                    onClick = { addPlayer(newPlayerName) },
                                    modifier = Modifier.size(48.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", fontSize = 20.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showAddPlayerInput = true }
                                ) {
                                    Text(
                                        "Добавить игрока...",
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                }
                                Button(
                                    onClick = { showAddPlayerInput = true },
                                    modifier = Modifier.size(48.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    // Кнопка "Очистить" под полем добавления
                    item {
                        Button(
                            onClick = {
                                participants = emptyList()
                                wheelParticipants = emptyList()
                                currentWinner = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Очистить список")
                        }
                    }
                }
            }

            // Центральная часть - игра
            Column(
                modifier = Modifier.weight(0.8f).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Rainbow Wheel of Fortune", fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
                    // Колесо
                    Canvas(modifier = Modifier.size(300.dp)) {
                        val radius = size.minDimension / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // Используем игроков или радужные цвета для превью
                        val displayParticipants = wheelParticipants.ifEmpty {
                            rainbowColors.mapIndexed { index, color ->
                                Participant("Player${index + 1}", 0, color)
                            }
                        }

                        val sliceAngle = 360f / displayParticipants.size

                        rotate(angle, pivot = center) {
                            displayParticipants.forEachIndexed { index, participant ->
                                val startAngle = index * sliceAngle
                                val baseColor = participant.color

                                // Отладка: выводим информацию о рисовании секторов
                                if (wheelParticipants.isNotEmpty()) {
                                    println("Drawing sector [$index]: ${participant.name} at ${startAngle}° (color: $baseColor)")
                                }

                                // Создаем более светлый и более темный оттенки
                                val lightColor = Color(
                                    red = (baseColor.red + 0.3f).coerceIn(0f, 1f),
                                    green = (baseColor.green + 0.3f).coerceIn(0f, 1f),
                                    blue = (baseColor.blue + 0.3f).coerceIn(0f, 1f),
                                    alpha = baseColor.alpha
                                )

                                val darkColor = Color(
                                    red = (baseColor.red * 0.4f).coerceIn(0f, 1f),
                                    green = (baseColor.green * 0.4f).coerceIn(0f, 1f),
                                    blue = (baseColor.blue * 0.4f).coerceIn(0f, 1f),
                                    alpha = baseColor.alpha
                                )

                                // Создаем угловой градиент с парами (позиция, цвет)
                                val gradient = Brush.sweepGradient(
                                    0f to lightColor,
                                    0.25f to baseColor,
                                    0.5f to darkColor,
                                    0.75f to baseColor,
                                    1f to lightColor,
                                    center = center
                                )

                                drawArc(
                                    brush = gradient,
                                    startAngle = startAngle,
                                    sweepAngle = sliceAngle,
                                    useCenter = true,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2)
                                )

                                // Добавляем тонкую белую границу между секторами
                                if (displayParticipants.size > 1) {
                                    val startX = center.x + radius * cos(startAngle * PI / 180).toFloat()
                                    val startY = center.y + radius * sin(startAngle * PI / 180).toFloat()

                                    drawLine(
                                        color = Color.White,
                                        start = center,
                                        end = Offset(startX, startY),
                                        strokeWidth = 2f
                                    )
                                }
                            }
                        }
                    }

                    // Указатель-стрелка
                    Canvas(modifier = Modifier.size(320.dp)) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val pointerSize = 20f
                        val pointerHeight = 30f

                        val arrowPath = Path().apply {
                            moveTo(center.x, pointerHeight)
                            lineTo(center.x - pointerSize, 0f)
                            lineTo(center.x + pointerSize, 0f)
                            close()
                        }

                        drawPath(
                            path = arrowPath,
                            color = Color.Cyan
                        )

                        drawPath(
                            path = arrowPath,
                            color = Color.Black,
                            style = Stroke(width = 3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (wheelParticipants.isEmpty()) {
                        "Добавьте игроков для начала игры!"
                    } else {
                        currentWinner?.name ?: "Крутите колесо!"
                    },
                    fontSize = 20.sp,
                    color = if (currentWinner != null) currentWinner!!.color else Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки управления игрой
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (wheelParticipants.isNotEmpty() && !isSpinning) {
                                isSpinning = true
                                val spinDuration = (spinTime.toLongOrNull() ?: 3L) * 1000L
                                scope.launch {
                                    // Фиксированная скорость: делаем много оборотов за постоянный интервал времени
                                    val fixedStepTime = 50L // Фиксированное время между кадрами (мс)
                                    val rotationSpeed = 10f // Скорость поворота за кадр (градусы)
                                    val steps = (spinDuration / fixedStepTime).toInt()

                                    // Генерируем случайный финальный угол
                                    val randomFinalAngle = Random.nextFloat() * 360f
                                    val totalRotation = steps * rotationSpeed + randomFinalAngle
                                    val startAngle = angle

                                    for (i in 1..steps) {
                                        // Постоянная скорость в начале, замедление в конце
                                        val progress = i.toFloat() / steps
                                        val easedProgress = 1f - (1f - progress) * (1f - progress) * (1f - progress)

                                        angle = startAngle + totalRotation * easedProgress
                                        delay(fixedStepTime)
                                    }

                                    angle = (startAngle + totalRotation) % 360
                                    currentWinner = getWinnerByAngle(angle, wheelParticipants)
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = wheelParticipants.isNotEmpty()
                    ) { Text("Запустить колесо") }

                    Button(
                        onClick = {
                            currentWinner?.let { winner ->
                                // Обновляем счет игрока в основном списке
                                participants = participants.map { participant ->
                                    if (participant.name == winner.name) {
                                        participant.copy(score = participant.score + 1)
                                    } else {
                                        participant
                                    }
                                }

                                // Обновляем лидерборд
                                leaderboard = leaderboardManager.updatePlayerScore(winner.name, 1)
                                wheelParticipants = wheelParticipants.filter { p -> p != winner }
                                currentWinner = null
                                println("Updated leaderboard: $leaderboard") // Для отладки
                            }
                        },
                        enabled = currentWinner != null
                    ) { Text("Выиграл") }

                    Button(
                        onClick = {
                            currentWinner?.let { winner ->
                                // Обновляем счет игрока в основном списке
                                participants = participants.map { participant ->
                                    if (participant.name == winner.name) {
                                        participant.copy(score = participant.score - 1)
                                    } else {
                                        participant
                                    }
                                }

                                // Обновляем лидерборд (отнимаем очко)
                                leaderboard = leaderboardManager.updatePlayerScore(winner.name, -1)
                                wheelParticipants = wheelParticipants.filter { p -> p != winner }
                                currentWinner = null
                            }
                        },
                        enabled = currentWinner != null
                    ) { Text("Проиграл") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    // Сбрасываем счет всех игроков
                    participants = participants.map { it.copy(score = 0) }
                    wheelParticipants = participants.toList()
                    currentWinner = null
                }) { Text("Сброс раунда") }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = spinTime,
                        onValueChange = { spinTime = it },
                        modifier = Modifier.width(50.dp).background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Время вращения (сек)")
                }
            }

            // Правая часть - лидерборд
            Column(
                modifier = Modifier.weight(0.4f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Доска лидеров", fontSize = 20.sp)

                Spacer(modifier = Modifier.height(16.dp))

                if (leaderboard.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Пока нет результатов.\nСыграйте первую игру!",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    leaderboard.take(10).forEachIndexed { index, entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = entry.name,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    "${entry.totalScore}",
                                    fontSize = 16.sp,
                                    color = Color.Blue
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    leaderboardManager.resetLeaderboard()
                    leaderboard = emptyList()
                }) {
                    Text("Очистить лидерборд")
                }
            }
        }
    }
}
