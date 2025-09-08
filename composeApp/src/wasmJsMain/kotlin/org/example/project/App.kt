package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
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

data class Participant(val name: String, var score: Int = 0)

@Composable
@Preview
fun App() {
    var participantsInput by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf(listOf<Participant>()) }
    var wheelParticipants by remember { mutableStateOf(listOf<Participant>()) }
    var currentWinner by remember { mutableStateOf<Participant?>(null) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinTime by remember { mutableStateOf("3000") }
    var angle by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Участники (через запятую):", fontSize = 16.sp)
            BasicTextField(
                value = participantsInput,
                onValueChange = { participantsInput = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp).background(Color.LightGray)
            )
            Button(onClick = {
                participants = participantsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { Participant(it) }
                wheelParticipants = participants.toList()
            }) {
                Text("Создать участников")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(modifier = Modifier.size(300.dp)) {
                val radius = size.minDimension / 2
                val center = Offset(size.width / 2, size.height / 2)
                val sliceAngle = 360f / (if (wheelParticipants.isEmpty()) 1 else wheelParticipants.size)

                rotate(angle, pivot = center) {
                    wheelParticipants.forEachIndexed { index, participant ->
                        val startAngle = index * sliceAngle
                        drawArc(
                            color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f),
                            startAngle = startAngle,
                            sweepAngle = sliceAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(currentWinner?.name ?: "Победитель", fontSize = 20.sp, color = Color.Red)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (wheelParticipants.isNotEmpty() && !isSpinning) {
                        isSpinning = true
                        val spinDuration = spinTime.toLongOrNull() ?: 3000L
                        scope.launch {
                            val steps = 60
                            val delayTime = spinDuration / steps
                            val randomIndex = Random.nextInt(wheelParticipants.size)
                            val targetAngle = 360f / wheelParticipants.size * randomIndex
                            for (i in 1..steps) {
                                angle += (targetAngle + 360 * Random.nextFloat() - angle) / (steps - i + 1)
                                delay(delayTime)
                            }
                            currentWinner = wheelParticipants[randomIndex]
                            wheelParticipants = wheelParticipants.toMutableList().apply { removeAt(randomIndex) }
                            isSpinning = false
                        }
                    }
                }) { Text("Запустить колесо") }

                Button(onClick = {
                    currentWinner?.let { it.score += 1 }
                }) { Text("Выиграл") }

                Button(onClick = {
                    currentWinner?.let { it.score -= 1 }
                }) { Text("Проиграл") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Счёт участников:")
            participants.forEach {
                Text("${it.name}: ${it.score}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = spinTime,
                onValueChange = { spinTime = it },
                modifier = Modifier.width(100.dp).background(Color.LightGray)
            )
            Text("Время вращения (мс)")
        }
    }
}