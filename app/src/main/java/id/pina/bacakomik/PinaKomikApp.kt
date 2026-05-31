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

    override fun attachBaseContext(base: android.content.Context?) {
        super.attachBaseContext(base)
        // Earliest possible — install crash logger before anything else can crash
        try {
            installCrashLogger()
            recordLifecycle("attachBaseContext")
        } catch (t: Throwable) {
            Log.e("PinaKomik", "attachBaseContext failed", t)
        }
    }

    override fun onCreate() {
        super.onCreate()
        recordLifecycle("Application.onCreate")
        // Upload any pending crash from previous session
        uploadPendingCrash()
    }

    private fun crashFile(): File {
        val dir = getExternalFilesDir(null) ?: filesDir
        return File(dir, "crash.log")
    }

    private fun recordLifecycle(stage: String) {
        try {
            val prefs = getSharedPreferences("pina_lifecycle", MODE_PRIVATE)
            val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
            val existing = prefs.getString("trace", "") ?: ""
            prefs.edit().putString("trace", "$existing\n[$ts] $stage").apply()
        } catch (_: Throwable) {}
    }

    private fun installCrashLogger() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
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
                // Save to SharedPreferences SYNCHRONOUSLY (commit, not apply)
                try {
                    val prefs = getSharedPreferences("pina_crash", MODE_PRIVATE)
                    prefs.edit().putString("last_crash", payload).commit()
                } catch (_: Throwable) {}

                // Save to file (sync)
                try {
                    crashFile().writeText(payload)
                } catch (_: Throwable) {}

                Log.e("PinaKomik", "Crash logged", throwable)

                // Try to launch CrashActivity (might fail if process is dying)
                try {
                    val intent = Intent(this@PinaKomikApp, CrashActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                } catch (_: Throwable) {}
            } catch (t: Throwable) {
                Log.e("PinaKomik", "Crash handler itself failed", t)
            }
            // Kill the process — Android will respawn into new task
            android.os.Process.killProcess(android.os.Process.myPid())
            kotlin.system.exitProcess(10)
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
