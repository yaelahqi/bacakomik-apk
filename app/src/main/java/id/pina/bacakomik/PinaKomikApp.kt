package id.pina.bacakomik

import android.app.Application
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PinaKomikApp : Application() {
    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
    }

    private fun installCrashLogger() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val dir = getExternalFilesDir(null) ?: filesDir
                val file = File(dir, "crash.log")
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                file.appendText(
                    buildString {
                        append("=== CRASH @ ").append(ts).append(" ===\n")
                        append("Thread: ").append(thread.name).append('\n')
                        append("Message: ").append(throwable.message).append('\n')
                        append(sw.toString())
                        append("\n\n")
                    }
                )
                Log.e("PinaKomik", "Crash logged: ${file.absolutePath}", throwable)
            } catch (t: Throwable) {
                Log.e("PinaKomik", "Failed to write crash log", t)
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
