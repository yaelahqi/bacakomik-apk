package id.pina.bacakomik

import android.app.Application
import android.content.Intent
import android.util.Log
import id.pina.bacakomik.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class PinaKomikApp : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
        // Upload any pending crash from previous session
        uploadPendingCrash()
    }

    private fun crashFile(): File {
        val dir = getExternalFilesDir(null) ?: filesDir
        return File(dir, "crash.log")
    }

    private fun installCrashLogger() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = crashFile()
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val payload = buildString {
                    append("=== CRASH @ ").append(ts).append(" ===\n")
                    append("Thread: ").append(thread.name).append('\n')
                    append("Version: ").append(BuildConfig.VERSION_NAME)
                        .append(" (code ").append(BuildConfig.VERSION_CODE).append(")\n")
                    append("Android: ").append(android.os.Build.VERSION.SDK_INT).append('\n')
                    append("Device: ").append(android.os.Build.MANUFACTURER)
                        .append(' ').append(android.os.Build.MODEL).append('\n')
                    append("Message: ").append(throwable.message).append("\n\n")
                    append(sw.toString())
                }
                // Write to file FIRST (synchronous)
                file.writeText(payload)
                Log.e("PinaKomik", "Crash logged to file: ${file.absolutePath}", throwable)
                // Launch CrashActivity to show crash on screen
                try {
                    val intent = Intent(this@PinaKomikApp, CrashActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("PinaKomik", "Failed to launch CrashActivity", e)
                }
                // Also best-effort upload to server
                thread(start = true, isDaemon = true) {
                    runCatching { uploadString(payload) }
                }
            } catch (t: Throwable) {
                Log.e("PinaKomik", "Failed to handle crash", t)
            }
            // Don't call previous handler — we want to keep the process alive
            // so user can see the CrashActivity
            // previous?.uncaughtException(thread, throwable)
        }
    }

    private fun uploadPendingCrash() {
        thread(start = true, isDaemon = true, name = "crash-upload") {
            runCatching {
                val f = crashFile()
                if (!f.exists() || f.length() == 0L) return@runCatching
                val content = f.readText()
                if (uploadString(content)) {
                    f.delete()
                }
            }
        }
    }

    private fun uploadString(payload: String): Boolean {
        return runCatching {
            val url = java.net.URL("${BuildConfig.API_BASE}/api/v1/crashlog")
            val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 5_000
                readTimeout = 5_000
                setRequestProperty("Content-Type", "text/plain; charset=utf-8")
                setRequestProperty(
                    "User-Agent",
                    "PinaKomik/${BuildConfig.VERSION_NAME} (Android ${android.os.Build.VERSION.SDK_INT})"
                )
            }
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        }.getOrDefault(false)
    }
}
