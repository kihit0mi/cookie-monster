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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import cz.kihitomi.cookiemonster.accessibility.McpAccessibilityService
import cz.kihitomi.cookiemonster.theme.CookieMonsterTheme

/**
 * Activity serves purely as a status dashboard. It monitors the Android Accessibility Permission, which in turn
 * serves as a proxy to server state (since the server is automatically started when the permission is granted.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            var isServiceRunning by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
            var showAccessibilityDialog by remember { mutableStateOf(!isServiceRunning) }

            // Re-check service status whenever the user comes back to the app from settings
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        val isEnabled = isAccessibilityServiceEnabled(context)
                        isServiceRunning = isEnabled
                        if (isEnabled) showAccessibilityDialog = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                                text = "MCP Server Host",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(64.dp))

                            // SERVER STATUS CARD
                            ServerStatusCard(isServiceRunning)

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            ActivityLogger::class.java
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .height(56.dp)
                                    .fillMaxWidth()
                            ) {
                                Text("VIEW AGENT LOGS", fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    if (showAccessibilityDialog) {
                        PermissionDialog(
                            title = "Enable Accessibility",
                            message = "The Cookie Monster Server requires Accessibility permissions to analyze the screen and execute agent commands.",
                            onDismiss = { showAccessibilityDialog = false },
                            onConfirm = {
                                openAccessibilitySettings(context)
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     *Android stores enabled accessibility settings in Settings.Secure as a single string.
     * This functions parses that string to find if application's unique signature is amongst those enabled.
     */
    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = "${context.packageName}/${McpAccessibilityService::class.java.name}"
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

/**
 * Visual indicator of the server status. Mostly here so the UI isn't empty.
 */
@Composable
fun ServerStatusCard(isRunning: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) Color(0xFF064E3B) else Color(0xFF7F1D1D)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = "Status Icon",
                tint = if (isRunning) Color(0xFF34D399) else Color(0xFFFCA5A5),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isRunning) "SERVER ACTIVE" else "SERVER OFFLINE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = if (isRunning) "Ready for MCP connections" else "Awaiting Accessibility Permission",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }


        }
    }
}

/**
 * Prompts user to enable Accessibility permission.
 */
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
