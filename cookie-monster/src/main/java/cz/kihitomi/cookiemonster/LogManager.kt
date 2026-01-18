package cz.kihitomi.cookiemonster

object LogManager {
    private val logs = mutableListOf<LogItem>()

    data class LogItem(
        val tag: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun addLog(tag: String, message: String) {
        logs.add(LogItem(tag, message))
    }

    fun getAllLogs(): List<LogItem> {
        // Return a copy to maintain encapsulation
        return logs.toList().reversed()
    }

    fun clearLogs() {
        logs.clear()
    }
}