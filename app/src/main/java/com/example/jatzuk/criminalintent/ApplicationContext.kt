package com.example.jatzuk.criminalintent

import android.app.Application
import android.content.Context

class ApplicationContext : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        ApplicationContext.context = applicationContext
    }
}