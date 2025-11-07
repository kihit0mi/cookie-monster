package cz.kihitomi.cookiemonster

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cz.kihitomi.cookiemonster.theme.CookieMonsterTheme

class LogsActivity : AppCompatActivity() {

    private lateinit var logsContentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        logsContentTextView = findViewById(R.id.text_view_logs_content)

        loadLogs()
    }
private fun loadLogs{
//placeholder for real log returning function
    logs = "logs" 
    logsContentTextView.text = logs
}