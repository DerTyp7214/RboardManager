package de.dertyp7214.rboardmanager

import android.app.Application
import com.google.firebase.FirebaseApp

class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
    }
}