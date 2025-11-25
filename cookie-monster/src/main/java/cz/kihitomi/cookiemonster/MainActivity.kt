package cz.kihitomi.cookiemonster

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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

            var showAccessibilityDialog by remember { mutableStateOf(false) }
            var showNotificationDialog by remember { mutableStateOf(false) }

            val context = LocalContext.current

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted && !areNotificationsEnabled(context)) {
                    showNotificationDialog = true
                }
            }

            LaunchedEffect(Unit) {
                if (!isAccessibilityServiceEnabled(context)) {
                    showAccessibilityDialog = true
                }

                if (!areNotificationsEnabled(context)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        showNotificationDialog = true
                    }
                }
            }

            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        // This runs every time the user returns to the app
                        if (isAccessibilityServiceEnabled(context)) {
                            showAccessibilityDialog = false
                        }
                        if (areNotificationsEnabled(context)) {
                            showNotificationDialog = false
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

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

                            val targetWord = sharedPrefs.getString("target_word", "cookie")

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
                                    placeholder = { Text("$targetWord") },
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
                                        tint = Color(0xFF4CAF50),
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

                            Button(
                                onClick = {
                                    val intent = Intent(this@MainActivity, ActivityLogger::class.java)
                                    startActivity(intent)
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .height(56.dp)
                            ) {
                                Text(
                                    "VIEW LOGS",
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (showAccessibilityDialog) {
                        PermissionDialog(
                            title = "Enable Accessibility Service",
                            message = "Cookie Monster needs accessibility permission to detect words on screen.\n\nPlease enable 'Cookie Monster' in the accessibility settings.",
                            onDismiss = { showAccessibilityDialog = false },
                            onConfirm = {
                                openAccessibilitySettings(context)
                                showAccessibilityDialog = false
                            }
                        )
                    }

                    if (showNotificationDialog) {
                        PermissionDialog(
                            title = "Enable Notifications",
                            message = "Cookie Monster needs notification permission to alert you when your target word is detected.",
                            onDismiss = { showNotificationDialog = false },
                            onConfirm = {
                                openNotificationSettings(context)
                                showNotificationDialog = false
                            }
                        )
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

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val service = "${context.packageName}/${MyAccessibilityService::class.java.name}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        if (enabledServices.isNullOrEmpty()) {
            return false
        }

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(service, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
    }

    private fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
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
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
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
}