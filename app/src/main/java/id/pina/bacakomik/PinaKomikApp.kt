package id.pina.bacakomik

import android.app.Application
import android.util.Log
import id.pina.bacakomik.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class PinaKomikApp : Application() {

    private val crashEndpoint by lazy { "${BuildConfig.API_BASE}/api/v1/crashlog" }

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
        beacon("app_oncreate")
        // On startup, try to upload any pending crash log from previous session
        uploadPendingCrash()
    }

    private fun beacon(stage: String) {
        thread(start = true, isDaemon = true, name = "beacon-$stage") {
            runCatching {
                val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                val payload = "BEACON: $stage @ $ts\n" +
                    "Version: ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})\n" +
                    "Android: ${android.os.Build.VERSION.SDK_INT}\n" +
                    "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\n"
                uploadString(payload)
            }
        }
    }

    companion object {
        @JvmStatic
        fun beaconStatic(ctx: Application, stage: String) {
            (ctx as? PinaKomikApp)?.beacon(stage)
        }
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
                file.appendText(payload + "\n\n")
                Log.e("PinaKomik", "Crash logged: ${file.absolutePath}", throwable)
                // Best-effort sync upload (we have ~3s before process dies)
                runCatching { uploadString(payload) }
            } catch (t: Throwable) {
                Log.e("PinaKomik", "Failed to write crash log", t)
            }
            previous?.uncaughtException(thread, throwable)
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
            val url = URL(crashEndpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
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
