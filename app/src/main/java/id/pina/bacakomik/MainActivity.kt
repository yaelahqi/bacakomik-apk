package id.pina.bacakomik

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import id.pina.bacakomik.ui.AppRoot
import id.pina.bacakomik.ui.theme.PinaKomikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            installSplashScreen()
        } catch (t: Throwable) {
            Log.e("PinaKomik", "installSplashScreen failed", t)
        }
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (t: Throwable) {
            Log.e("PinaKomik", "enableEdgeToEdge failed", t)
        }
        try {
            setContent {
                PinaKomikTheme {
                    AppRoot()
                }
            }
        } catch (t: Throwable) {
            Log.e("PinaKomik", "setContent failed", t)
            // Write crash to file and redirect to CrashActivity
            try {
                val crashFile = getExternalFilesDir(null)?.let {
                    java.io.File(it, "crash.log")
                } ?: java.io.File(filesDir, "crash.log")
                val sw = java.io.StringWriter()
                t.printStackTrace(java.io.PrintWriter(sw))
                crashFile.writeText(
                    "=== COMPOSE CRASH ===\n" +
                    "Message: ${t.message}\n\n" +
                    sw.toString()
                )
                startActivity(
                    android.content.Intent(this, CrashActivity::class.java).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                )
                finish()
            } catch (e: Throwable) {
                Log.e("PinaKomik", "Failed to redirect to CrashActivity", e)
                throw t
            }
        }
    }
}
