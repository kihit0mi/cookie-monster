package cz.kihitomi.cookiemonster.logger

/**
 * An in-memory logging system designed to show agent actions and status changes directly to the UI.
 **/
object LogManager {
    //Setting a limit on saved logs prevents OOM crashes.
    private const val MAX_LOG_CAPACITY = 1000
    private val logs = mutableListOf<LogItem>()

    data class LogItem(
        val tag: String,
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    fun addLog(tag: String, message: String) {
        if (logs.size >= MAX_LOG_CAPACITY) {
            logs.removeAt(0)
        }
        logs.add(LogItem(tag, message))
    }
    /**
     * Returns a safe, read-only snapshot of the logs for the UI to render.
     * - .toList(): Prevents ConcurrentModificationException if the UI is drawing while the server adds a new log.
     * - .reversed(): Ensures the user sees the most recent actions at the top of the screen.
     */
    fun getAllLogs(): List<LogItem> {
        return logs.toList().reversed()
    }

    fun clearLogs() {
        logs.clear()
    }
}