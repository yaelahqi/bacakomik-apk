package id.pina.bacakomik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import id.pina.bacakomik.ui.AppRoot
import id.pina.bacakomik.ui.theme.BacaKomikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BacaKomikTheme {
                AppRoot()
            }
        }
    }
}
