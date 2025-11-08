package cz.kihitomi.cookiemonster

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.kihitomi.cookiemonster.theme.CookieMonsterTheme

class ActivityLogger : ComponentActivity() {

    private fun formatTimeStamp(timestamp: Long): String{
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            var refreshTrigger by remember { mutableStateOf(0) }

            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(2000)
                    refreshTrigger++
                }
            }
            CookieMonsterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            //.padding(innerPadding)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFADD8E6),
                                        Color(0xFFB8A4E8)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            Text(
                                text = "LOGS",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            val sharedPrefs = getSharedPreferences("CookieMonsterPrefs", Context.MODE_PRIVATE)
                            val targetWord = sharedPrefs.getString("target_word", "Not set")

                            Text(
                                text = "Hledame slovo: $targetWord",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .padding(horizontal = 16.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                val logs = LogManager.getAllLogs()
                                refreshTrigger

                                items(logs.size) { index ->
                                    val log = logs[index]
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row {
                                            Text(
                                                text = "[${this@ActivityLogger.formatTimeStamp(log.timestamp)}]",
                                                color = Color(0xFF0D47A1),
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )

                                            Text(
                                                text = "[${log.tag}]",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Text(
                                            text = log.message,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val intent =
                                        Intent(this@ActivityLogger, MainActivity::class.java)
                                    startActivity(intent)
                                }, modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp)
                                    .height(56.dp)
                            ) {
                                Text(
                                    "GO BACK",
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}