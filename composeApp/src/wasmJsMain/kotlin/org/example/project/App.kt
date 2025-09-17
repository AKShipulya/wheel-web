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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
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
    var previousWinner by remember { mutableStateOf<Participant?>(null) } // Добавляем переменную для предыдущего игрока
    var isSpinning by remember { mutableStateOf(false) }
    var spinTime by remember { mutableStateOf("3") }
    var remainingTime by remember { mutableStateOf(0L) }
    var angle by remember { mutableStateOf(0f) }
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }

    var showAddPlayerInput by remember { mutableStateOf(false) }
    var newPlayersText by remember { mutableStateOf("") }

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

    fun addPlayers(playersText: String) {
        if (playersText.isNotBlank()) {
            val playerNames = playersText.split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val newParticipants = playerNames.mapIndexed { index, name ->
                Participant(
                    name = name,
                    score = 0,
                    color = colors[(participants.size + index) % colors.size]
                )
            }

            participants = participants + newParticipants
            wheelParticipants = participants.toList()
            newPlayersText = ""
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

                    // Поле для ввода игроков построчно
                    item {
                        if (showAddPlayerInput) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BasicTextField(
                                        value = newPlayersText,
                                        onValueChange = { newPlayersText = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .background(Color.White)
                                            .padding(8.dp),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.TopStart
                                            ) {
                                                if (newPlayersText.isEmpty()) {
                                                    Text(
                                                        "Введите имена игроков\n(каждое имя с новой строки):\n\nИгрок 1\nИгрок 2\nИгрок 3",
                                                        color = Color.Gray,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { addPlayers(newPlayersText) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Добавить")
                                        }
                                        Button(
                                            onClick = {
                                                showAddPlayerInput = false
                                                newPlayersText = ""
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Отмена")
                                        }
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { showAddPlayerInput = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Добавить игроков")
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
                                previousWinner = null
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

                Box(modifier = Modifier.size(550.dp), contentAlignment = Alignment.Center) {
                    // Колесо
                    val textMeasurer = rememberTextMeasurer()

                    Canvas(modifier = Modifier.size(520.dp)) {
                        val radius = size.minDimension / 2
                        val center = Offset(size.width / 2, size.height / 2)

                        // Используем игроков или радужные цвета для превью
                        val displayParticipants = wheelParticipants.ifEmpty {
                            rainbowColors.mapIndexed { index, color ->
                                Participant("Player${index + 1}", 0, color)
                            }
                        }

                        val sliceAngle = 360f / displayParticipants.size
                        val showNames = wheelParticipants.isNotEmpty() // Показываем имена только если есть реальные участники

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

                                // Добавляем имя игрока в центр сегмента только если есть реальные участники
                                if (showNames) {
                                    val middleAngle = startAngle + sliceAngle / 2
                                    val textRadius = radius * 0.7f // Располагаем текст на 70% от радиуса

                                    val textX = center.x + textRadius * cos(middleAngle * PI / 180).toFloat()
                                    val textY = center.y + textRadius * sin(middleAngle * PI / 180).toFloat()

                                    // Определяем размер шрифта в зависимости от количества игроков
                                    val fontSize = when {
                                        displayParticipants.size <= 4 -> 16.sp
                                        displayParticipants.size <= 8 -> 12.sp
                                        else -> 10.sp
                                    }

                                    // Определяем цвет текста - белый или черный в зависимости от яркости фона
                                    val textColor = if (baseColor.red * 0.299 + baseColor.green * 0.587 + baseColor.blue * 0.114 > 0.5) {
                                        Color.Black
                                    } else {
                                        Color.White
                                    }

                                    val textStyle = TextStyle(
                                        color = textColor,
                                        fontSize = fontSize,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Измеряем текст
                                    val textLayoutResult = textMeasurer.measure(
                                        text = participant.name,
                                        style = textStyle
                                    )

                                    // Рисуем текст с поворотом по направлению от центра
                                    withTransform({
                                        // Поворачиваем на угол сегмента + 90 градусов для чтения "от центра"
                                        rotate(middleAngle + 90f, pivot = Offset(textX, textY))
                                    }) {
                                        drawText(
                                            textLayoutResult = textLayoutResult,
                                            topLeft = Offset(
                                                textX - textLayoutResult.size.width / 2,
                                                textY - textLayoutResult.size.height / 2
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Указатель-стрелка
                    Canvas(modifier = Modifier.size(550.dp)) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val pointerSize = 35f
                        val pointerHeight = 55f

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

                // Обновленное отображение информации о текущем состоянии
                Text(
                    text = if (wheelParticipants.isEmpty()) {
                        "Добавьте игроков для начала игры!"
                    } else if (previousWinner != null) {
                        "Игрок для начисления очков: ${previousWinner!!.name}"
                    } else {
                        currentWinner?.let { "Текущий игрок: ${it.name}" } ?: "Крутите колесо!"
                    },
                    fontSize = 20.sp,
                    color = when {
                        wheelParticipants.isEmpty() -> Color.Gray
                        previousWinner != null -> previousWinner!!.color
                        currentWinner != null -> currentWinner!!.color
                        else -> Color.Gray
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки управления игрой
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (wheelParticipants.isNotEmpty() && !isSpinning) {
                                isSpinning = true
                                val spinDuration = (spinTime.toLongOrNull() ?: 3L) * 1000L
                                remainingTime = spinDuration // Устанавливаем начальное время

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

                                        // Обновляем оставшееся время
                                        remainingTime = spinDuration - (i * fixedStepTime)

                                        delay(fixedStepTime)
                                    }

                                    angle = (startAngle + totalRotation) % 360

                                    // Сохраняем предыдущего победителя
                                    previousWinner = currentWinner
                                    // Устанавливаем нового текущего игрока
                                    currentWinner = getWinnerByAngle(angle, wheelParticipants)

                                    remainingTime = 0L // Сбрасываем таймер
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = wheelParticipants.isNotEmpty()
                    ) { Text("Запустить колесо") }


                    Button(
                        onClick = {
                            previousWinner?.let { winner ->
                                // Обновляем счет предыдущего игрока в основном списке
                                participants = participants.map { participant ->
                                    if (participant.name == winner.name) {
                                        participant.copy(score = participant.score + 1)
                                    } else {
                                        participant
                                    }
                                }

                                // Обновляем лидерборд
                                leaderboard = leaderboardManager.updatePlayerScore(winner.name, 1)

                                // Убираем предыдущего игрока из колеса
                                wheelParticipants = wheelParticipants.filter { p -> p.name != winner.name }

                                // Сбрасываем состояния
                                previousWinner = null
                                // Если предыдущий игрок был также текущим, сбрасываем и его
                                if (currentWinner?.name == winner.name) {
                                    currentWinner = null
                                }

                                println("Updated leaderboard: $leaderboard") // Для отладки
                            }
                        },
                        enabled = previousWinner != null
                    ) { Text("Выиграл") }

                    Button(
                        onClick = {
                            previousWinner?.let { winner ->
                                // Обновляем счет предыдущего игрока в основном списке
                                participants = participants.map { participant ->
                                    if (participant.name == winner.name) {
                                        participant.copy(score = participant.score - 1)
                                    } else {
                                        participant
                                    }
                                }

                                // Обновляем лидерборд (отнимаем очко)
                                leaderboard = leaderboardManager.updatePlayerScore(winner.name, -1)

                                // Убираем предыдущего игрока из колеса
                                wheelParticipants = wheelParticipants.filter { p -> p.name != winner.name }

                                // Сбрасываем состояния
                                previousWinner = null
                                // Если предыдущий игрок был также текущим, сбрасываем и его
                                if (currentWinner?.name == winner.name) {
                                    currentWinner = null
                                }
                            }
                        },
                        enabled = previousWinner != null
                    ) { Text("Проиграл") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    // Сбрасываем счет всех игроков
                    participants = participants.map { it.copy(score = 0) }
                    wheelParticipants = participants.toList()
                    currentWinner = null
                    previousWinner = null
                    remainingTime = 0L // Сбрасываем таймер
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

                // Таймер обратного отсчета
                Spacer(modifier = Modifier.height(8.dp))

                if (isSpinning) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Осталось времени: ${previousWinner?.name ?: ""}",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${(remainingTime / 1000).toInt()} сек",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingTime > 1000) Color.Blue else Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (remainingTime == 0L && !isSpinning && wheelParticipants.isNotEmpty()) {
                    Text(
                        "Колесо остановлено",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
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
                        // Определяем цвет фона для первых трех мест
                        val cardColors = when (index) {
                            0 -> CardDefaults.cardColors(containerColor = Color(0xFFFFD700)) // Золотой
                            1 -> CardDefaults.cardColors(containerColor = Color(0xFFC0C0C0)) // Серебряный
                            2 -> CardDefaults.cardColors(containerColor = Color(0xFFCD7F32)) // Бронзовый
                            else -> CardDefaults.cardColors() // Стандартные цвета Material3
                        }

                        // Определяем цвет текста в зависимости от фона
                        val textColor = when (index) {
                            0, 1, 2 -> Color.Black // Для первых трех мест - черный текст
                            else -> Color.Unspecified // Стандартный цвет текста
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            colors = cardColors
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Добавляем номер места для первых трех
                                    if (index < 3) {
                                        Text(
                                            text = "${index + 1}.",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = entry.name,
                                        fontSize = 16.sp,
                                        color = textColor
                                    )
                                }
                                Text(
                                    "${entry.totalScore}",
                                    fontSize = 16.sp,
                                    color = if (index < 3) textColor else Color.Blue,
                                    fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
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
