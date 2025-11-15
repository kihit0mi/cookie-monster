package cz.kihitomi.cookiemonster

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Check
import cz.kihitomi.cookiemonster.theme.CookieMonsterTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        val sharedPrefs = getSharedPreferences("CookieMonsterPrefs", Context.MODE_PRIVATE)

        setContent {
            var userInput by remember { mutableStateOf("") }
            var searchWord by remember { mutableStateOf("") }
            var showCheckmark by remember { mutableStateOf(false) }

            LaunchedEffect(showCheckmark) {
                if (showCheckmark) {
                    kotlinx.coroutines.delay(2000)
                    showCheckmark = false
                }
            }

            CookieMonsterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            //.padding(innerPadding)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1E3A5F),
                                        Color(0xFF2E1A47)
                                    )
                                )
                            )
                    ) { Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "Cookie Monster",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "This app logs all instances of websites showing your selected word.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .padding(horizontal = 16.dp)
                                .align(Alignment.CenterHorizontally)

                        )

                        Spacer(modifier = Modifier.height(64.dp))


                        Text(
                            text = "Enter target word for testing:",
                            fontSize = 16.sp,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                        ) {
                            OutlinedTextField(
                                value = userInput,
                                onValueChange = { userInput = it },
                                placeholder = { Text("Type here...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            if (showCheckmark) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Submitted",
                                    tint = Color(0xFF4CAF50),  // Green checkmark
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                                LogManager.clearLogs()
                                searchWord = userInput
                                sharedPrefs.edit().putString("target_word", searchWord.lowercase()).apply()
                                userInput = ""
                                showCheckmark = true
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Submit"
                                )
                            }


                        Spacer(modifier = Modifier.weight(1f))


                        Button(onClick = {
                            val intent = Intent(this@MainActivity, ActivityLogger::class.java)
                            startActivity(intent)
                        }, modifier = Modifier
                            .padding(16.dp)
                            .height(56.dp)
                        ) {
                            Text("VIEW LOGS",
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("cookie_alert", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Text(
        text = "This is COOKIE MONSTER!",
        modifier = modifier
    )
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CookieMonsterTheme {
        Greeting()
    }
}}