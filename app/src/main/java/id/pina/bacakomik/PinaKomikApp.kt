package id.pina.bacakomik

import android.app.Application
import android.util.Log

class PinaKomikApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("PinaKomik", ">>> Application.onCreate OK")
    }
}
