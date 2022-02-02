package me.phantomx.aitjseval

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize library first
        AITJsEval.initialize(applicationContext)
    }

}