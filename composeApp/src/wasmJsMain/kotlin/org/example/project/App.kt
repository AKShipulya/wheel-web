package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

data class Participant(
    val name: String,
    var score: Int = 0,
    val color: Color
)

@Composable
@Preview
fun App() {
    var participantsInput by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf(listOf<Participant>()) }
    var wheelParticipants by remember { mutableStateOf(listOf<Participant>()) }
    var currentWinner by remember { mutableStateOf<Participant?>(null) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinTime by remember { mutableStateOf("3") }
    var angle by remember { mutableStateOf(0f) }
    var leaderboard by remember { mutableStateOf(listOf<LeaderboardEntry>()) }

    val scope = rememberCoroutineScope()
    val leaderboardManager = remember { LeaderboardManager() }

    // Загружаем лидерборд при запуске
    LaunchedEffect(Unit) {
        leaderboard = leaderboardManager.getLeaderboard()
        println("Loaded leaderboard on start: $leaderboard") // Для отладки
    }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Левая часть - игра
            Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎯 Колесо Фортуны", fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Участники (через запятую):", fontSize = 16.sp)
                BasicTextField(
                    value = participantsInput,
                    onValueChange = { participantsInput = it },
                    modifier = Modifier.fillMaxWidth().padding(8.dp).background(Color.LightGray)
                )
                Button(onClick = {
                    val colors = listOf(
                        Color.Red, Color.Green, Color.Blue, Color.Yellow,
                        Color.Cyan, Color.Magenta, Color.Gray, Color(0xFFFFA500)
                    )
                    participants = participantsInput.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .mapIndexed { i, name -> Participant(name, 0, colors[i % colors.size]) }
                    wheelParticipants = participants.toList()
                    currentWinner = null
                }) {
                    Text("Создать участников")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
                    // Колесо
                    Canvas(modifier = Modifier.size(300.dp)) {
                        if (wheelParticipants.isNotEmpty()) {
                            val radius = size.minDimension / 2
                            val center = Offset(size.width / 2, size.height / 2)
                            val sliceAngle = 360f / wheelParticipants.size

                            rotate(angle, pivot = center) {
                                wheelParticipants.forEachIndexed { index, participant ->
                                    val startAngle = index * sliceAngle
                                    drawArc(
                                        color = participant.color,
                                        startAngle = startAngle,
                                        sweepAngle = sliceAngle,
                                        useCenter = true,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2)
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

                        drawPath(
                            path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(center.x, 0f)
                                lineTo(center.x - pointerSize, pointerHeight)
                                lineTo(center.x + pointerSize, pointerHeight)
                                close()
                            },
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentWinner?.name ?: "Крутите колесо!",
                    fontSize = 20.sp,
                    color = if (currentWinner != null) Color.Red else Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки управления
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (wheelParticipants.isNotEmpty() && !isSpinning) {
                            isSpinning = true
                            val spinDuration = (spinTime.toLongOrNull() ?: 3L) * 1000L
                            scope.launch {
                                val steps = 100
                                val delayTime = spinDuration / steps
                                val randomIndex = Random.nextInt(wheelParticipants.size)
                                val sliceAngle = 360f / wheelParticipants.size
                                val targetAngle = randomIndex * sliceAngle + sliceAngle / 2
                                val finalAngle = angle + 720 + targetAngle

                                for (i in 1..steps) {
                                    angle += (finalAngle - angle) / (steps - i + 1)
                                    delay(delayTime)
                                }
                                angle = finalAngle % 360
                                currentWinner = wheelParticipants[randomIndex]
                                isSpinning = false
                            }
                        }
                    }) { Text("Запустить колесо") }

                    Button(
                        onClick = {
                            currentWinner?.let {
                                it.score += 1
                                // Обновляем лидерборд
                                leaderboard = leaderboardManager.updatePlayerScore(it.name, 1)
                                wheelParticipants = wheelParticipants.filter { p -> p != it }
                                currentWinner = null
                                println("Updated leaderboard: $leaderboard") // Для отладки
                            }
                        },
                        enabled = currentWinner != null
                    ) { Text("Выиграл") }

                    Button(
                        onClick = {
                            currentWinner?.let {
                                it.score -= 1
                                // Обновляем лидерборд (отнимаем очко)
                                leaderboard = leaderboardManager.updatePlayerScore(it.name, -1)
                                wheelParticipants = wheelParticipants.filter { p -> p != it }
                                currentWinner = null
                            }
                        },
                        enabled = currentWinner != null
                    ) { Text("Проиграл") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    participants.forEach { it.score = 0 }
                    wheelParticipants = participants.toList()
                    currentWinner = null
                }) { Text("Сброс раунда") }

                Spacer(modifier = Modifier.height(16.dp))

                // Список текущих игроков
                if (participants.isNotEmpty()) {
                    Text("Текущие участники:", fontSize = 16.sp)
                    participants.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(16.dp).background(it.color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${it.name}: ${it.score}")
                        }
                    }
                }

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
                modifier = Modifier.weight(0.6f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆 Доска лидеров", fontSize = 20.sp)

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
                                    val medal = when (index) {
                                        0 -> "🥇"
                                        1 -> "🥈"
                                        2 -> "🥉"
                                        else -> "${index + 1}."
                                    }
                                    Text(
                                        text = "$medal ${entry.name}",
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
