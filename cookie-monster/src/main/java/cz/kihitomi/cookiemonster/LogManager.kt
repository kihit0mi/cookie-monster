package cz.kihitomi.cookiemonster

object LogManager {
    private val logs = mutableListOf<LogEntry>()

    data class LogEntry(
        val tag: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun addLog(tag: String, message: String) {
        logs.add(LogEntry(tag, message))
    }

    fun getAllLogs(): List<LogEntry> {
        return logs.toList().reversed()
    }

    fun clearLogs() {
        logs.clear()
    }
}