package cz.kihitomi.cookiemonster.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import cz.kihitomi.cookiemonster.logger.LogManager.LogItem
import cz.kihitomi.cookiemonster.theme.CookieMonsterTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A screen for the user to see all the tools the LLM is using and app status changes.
 */
class ActivityLogger : ComponentActivity() {

    /**
     * Formatting it here instead of LogManager ensures separation of concerns, LogManager holds the data,
     * UI displays it - we should decide how to display it here then.
     */
    private fun formatTimeStamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var refreshTrigger by remember { mutableStateOf(0) }

            val logs by remember(refreshTrigger) {
                mutableStateOf(LogManager.getAllLogs())
            }

            // Manually refreshes the UI every two seconds. We are using this instead of StateFlow,
            // because it's frankly not needed - the logs do not need to be updated that often.
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
                                        Color(0xFF1E3A5F),
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
                                text = "LOGS",
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
                                onClick = {
                                    LogManager.clearLogs()
                                    refreshTrigger++
                                          },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text("CLEAR THE LOGS", fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = { finish() }, // closing the activity returns us to the already saved MainActivity, thus saving memory (instead of calling new Intent)
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