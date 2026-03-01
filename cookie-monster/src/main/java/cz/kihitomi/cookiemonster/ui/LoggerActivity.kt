package cz.kihitomi.cookiemonster.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import cz.kihitomi.cookiemonster.logger.LogManager
import cz.kihitomi.cookiemonster.theme.CookieMonsterTheme
import kotlinx.coroutines.delay
import cz.kihitomi.cookiemonster.logger.LogManager.LogItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityLogger : ComponentActivity() {

    private fun formatTimeStamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var refreshTrigger by remember { mutableStateOf(0) }

            // Re-fetch logs only when refreshTrigger changes
            val logs by remember(refreshTrigger) {
                mutableStateOf(LogManager.getAllLogs())
            }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(2000)
                    refreshTrigger++
                }
            }

            CookieMonsterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E3A5F), // Match MainActivity Dark Theme
                                        Color(0xFF2E1A47)
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
                                text = "SCRAPE TERMINAL",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Cyan,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.CenterHorizontally)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(logs.size) { index ->
                                    val log = logs[index]
                                    LogEntryRow(log)
                                }
                            }

                            Button(
                                onClick = { finish() },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("RETURN TO CONSOLE", fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LogEntryRow(log: LogItem) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(8.dp)
        ) {
            Row {
                Text(
                    text = "[${formatTimeStamp(log.timestamp)}]",
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = log.tag,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Text(
                text = log.message,
                color = Color.LightGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}