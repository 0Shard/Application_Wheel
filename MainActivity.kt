package com.example.slotmachine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.example.slotmachine.ui.theme.SlotMachineTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlotMachineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SlotMachineApp()
                }
            }
        }
    }
}

@Composable
fun SlotMachineApp() {
    var showSlotMachine by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDAA520)), // Dark Yellow Background
        contentAlignment = Alignment.Center
    ) {
        SlotMachine()
    }
}

@Composable
fun SlotMachine() {
    val winningNumbers = listOf(8, 5, 2, 4, 1) // Example winning numbers
    val reels = remember { List(5) { mutableStateListOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0) } }
    var spinning by remember { mutableStateOf(false) }

    LaunchedEffect(spinning) {
        if (spinning) {
            reels.forEachIndexed { index, reel ->
                launch {
                    // Stagger the delay for each reel based on its index
                    // Each reel waits a bit longer to stop, creating a sequential stop effect
                    spinReel(reel, winningNumbers[index], index * 200L)
                }
            }
            // Reset spinning to false after the last reel has stopped
            delay(reels.size * 200L)
            spinning = false
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(50.dp))
        ReelsView(reels)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { spinning = true }, enabled = !spinning) {
            Text("Spin")
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun ReelsView(reels: List<SnapshotStateList<Int>>) {
    Row(modifier = Modifier.padding(horizontal = 32.dp)) {
        reels.forEach { reel ->
            Reel(reel)
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun Reel(reel: List<Int>) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray)
            .width(60.dp)
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        reel.forEachIndexed { index, number ->
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (index - 1) * 36.dp)
            )
        }
    }
}

suspend fun spinReel(reel: SnapshotStateList<Int>, winningNumber: Int, delayBeforeStopping: Long) {
    // Simulate spinning
    repeat(20) { // Increase or decrease for more/less spin
        reel.add(0, reel.removeAt(reel.size - 1))
        delay(50) // Speed of spinning
    }
    delay(delayBeforeStopping) // Delay before setting the reel to the winning number for sequential stopping
    // Adjust reel to ensure the winning number is at the 'visible' position
    while (reel[0] != winningNumber) {
        reel.add(0, reel.removeAt(reel.size - 1))
    }
}

@Preview(showBackground = true)
@Composable
fun SlotMachineAppPreview() {
    SlotMachineTheme {
        SlotMachineApp()
    }
}
