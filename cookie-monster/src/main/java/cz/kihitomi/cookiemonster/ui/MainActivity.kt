package cz.kihitomi.cookiemonster.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cz.kihitomi.cookiemonster.theme.CookieMonsterTheme
import kotlinx.coroutines.delay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import cz.kihitomi.cookiemonster.logger.LogManager
import cz.kihitomi.cookiemonster.accessibility.MyAccessibilityService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private fun captureCurrentWindow() {

        lifecycleScope.launch {
            LogManager.addLog("System", "Scrape scheduled in 5s. Switch to the target app now!")

            delay(5000) // 5 second countdown

            val service = MyAccessibilityService.instance
            if (service != null) {
                service.captureCurrentWindow()
                LogManager.addLog("System", "Scrape executed.")
            } else {
                LogManager.addLog("Error", "Accessibility Service not running!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // createNotificationChannel()

        setContent {
            var promptInput by remember { mutableStateOf("") }
            var showCheckmark by remember { mutableStateOf(false) }
            var showAccessibilityDialog by remember { mutableStateOf(false) }

            val context = LocalContext.current

            // Check service status on launch and return
            LaunchedEffect(Unit) {
                if (!isAccessibilityServiceEnabled(context)) {
                    showAccessibilityDialog = true
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        if (isAccessibilityServiceEnabled(context)) showAccessibilityDialog = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            LaunchedEffect(showCheckmark) {
                if (showCheckmark) {
                    delay(2000)
                    showCheckmark = false
                }
            }

            CookieMonsterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E3A5F), Color(0xFF2E1A47))
                                )
                            )
                            .padding(padding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Cookie Monster",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Agent Terminal & DOM Scraper",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // EXPANDED INPUT FIELD
                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                label = { Text("Agent Prompt", color = Color.White) },
                                placeholder = { Text("Enter commands for the agent...", color = Color.Gray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.6f), // Takes up major screen real estate
                                minLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.Cyan,
                                    unfocusedBorderColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    // Future: Send promptInput to MCP Server
                                    LogManager.addLog("Command", "Sent: $promptInput")
                                    promptInput = ""
                                    showCheckmark = true
                                    captureCurrentWindow()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (showCheckmark) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text("SCRAPE THE SCREEN in 5 seconds")
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.4f))

                            Button(
                                onClick = {
                                    startActivity(Intent(this@MainActivity, ActivityLogger::class.java))
                                },
                                modifier = Modifier.height(56.dp).fillMaxWidth()
                            ) {
                                Text("VIEW SCRAPE LOGS", fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    if (showAccessibilityDialog) {
                        PermissionDialog(
                            title = "Enable Accessibility",
                            message = "The Agent requires Accessibility permissions to analyze the screen and execute commands.",
                            onDismiss = { showAccessibilityDialog = false },
                            onConfirm = {
                                openAccessibilitySettings(context)
                                showAccessibilityDialog = false
                            }
                        )
                    }
                }
            }
        }
    }



    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = "${context.packageName}/${MyAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            if (colonSplitter.next().equals(service, ignoreCase = true)) return true
        }
        return false
    }

    private fun openAccessibilitySettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}

@Composable
fun PermissionDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("OPEN SETTINGS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}