package id.pina.bacakomik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import id.pina.bacakomik.ui.AppRoot
import id.pina.bacakomik.ui.theme.PinaKomikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        beacon("activity_oncreate_start")
        try {
            installSplashScreen()
            beacon("after_splashscreen")
        } catch (t: Throwable) {
            beacon("splashscreen_failed: ${t.javaClass.simpleName}: ${t.message}")
        }
        super.onCreate(savedInstanceState)
        beacon("after_super_oncreate")
        try {
            enableEdgeToEdge()
            beacon("after_edge_to_edge")
        } catch (t: Throwable) {
            beacon("edge_to_edge_failed: ${t.javaClass.simpleName}: ${t.message}")
        }
        try {
            setContent {
                beacon("inside_setcontent_lambda")
                PinaKomikTheme {
                    beacon("inside_theme")
                    AppRoot()
                }
            }
            beacon("after_setcontent")
        } catch (t: Throwable) {
            beacon("setcontent_failed: ${t.javaClass.simpleName}: ${t.message}")
            throw t
        }
    }

    private fun beacon(stage: String) {
        (application as? PinaKomikApp)?.let {
            PinaKomikApp.beaconStatic(it, "MainActivity:$stage")
        }
    }
}

